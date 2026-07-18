package org.aspose.pdf.text;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.font.PdfFont;
import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.engine.text.TextExtractor;
import org.aspose.pdf.operators.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/// Represents a fragment of text extracted from a PDF page.
///
/// A text fragment has a text value, position, bounding rectangle, and one or
/// more [TextSegment]s that may have different formatting. The text state
/// of the fragment is delegated to its first segment.
///
/// When a fragment was extracted from a page via [TextFragmentAbsorber],
/// calling [#setText(String)] will update the underlying content stream
/// so that the change is reflected when the document is saved.
///
public class TextFragment extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(TextFragment.class.getName());

    private String text;
    private final List<TextSegment> segments;
    private Position position;
    private Rectangle rectangle;
    private Page page;

    // Exact device/user-space X position of each character boundary, in the
    // same coordinate space as {@link #rectangle} (page-box origin normalised).
    // Length is text.length()+1 when present: charXPositions[i] is the left edge
    // of character i, charXPositions[i+n] the right edge of an n-char run. Set by
    // the extractor for upright, non-kerning-split fragments so the absorber can
    // pinpoint a sub-phrase's X without re-measuring (which loses per-space word
    // spacing). May be null — callers must fall back to approximation.
    private double[] charXPositions;

    // Engine font that rendered this fragment's source operator. Used by
    // TextReplaceOptions.AdjustSpaceWidth to measure the exact glyph advance of
    // replacement text in the same metric the renderer uses, so a compensating
    // TJ adjustment keeps following text in place. May be null.
    private PdfFont sourceFont;

    // Raw /Tf operand size of the source show operator. TextState.getFontSize()
    // reports the EFFECTIVE size (Tf × Tm scale, matching Aspose),
    // but content-stream math — the TJ compensation in replaceTextOp — works in unscaled
    // text space where only the raw Tf size is correct (character spacing does
    // not cancel out of n = Δadv·1000/Tfs). ≤0 when unknown.
    private double sourceTfSize = -1;

    // Text baseline rotation in device space, quantized to {0,90,180,270}.
    // Computed by the extractor from the combined text-matrix×CTM. 0 means the
    // ordinary horizontal left-to-right writing direction. Used by
    // TextAbsorber to group extracted glyphs into lines along the correct axis
    // (rotated text advances along Y and stacks lines along X). See BUG-EXT-WSPC.
    private int rotation = 0;

    private org.aspose.pdf.Note footNote;
    private org.aspose.pdf.Note endNote;

    // Source location for content stream modification
    private int sourceOperatorIndex = -1;
    private int lastSourceOperatorIndex = -1;
    // Sprint 36: track the source operator by identity so we can re-derive its
    // current index after a sibling fragment's update inserted/removed ops in
    // the same collection. Stale `sourceOperatorIndex` would otherwise point
    // at the wrong operator and silently corrupt subsequent replacements.
    private Operator sourceOperator;
    private Operator lastSourceOperator;
    private String sourceFontName;
    private int sourceTextStart = 0;
    private int sourceTextLength = -1;
    private OperatorCollection sourceOperators;
    private PdfStream sourceContentStream;
    private TextReplaceOptions textReplaceOptions;
    // Underline path operators detected in the source content (each group = one
    // underline subpath: re/m/l constructing ops + the f/S paint op). Removed from
    // the content stream when the fragment's underline is turned off (see
    // TextEditOptions.ToAttemptGetUnderlineFromSource).
    private java.util.List<java.util.List<Operator>> sourceUnderlineOpGroups;
    private OperatorCollection sourceUnderlineCollection;

    /// Creates a TextFragment with the given text.
    ///
    /// @param text the fragment text
    public TextFragment(String text) {
        this.text = text != null ? text : "";
        this.segments = new ArrayList<>();
        // Create default segment
        this.segments.add(new TextSegment(this.text));
    }

    /// Creates an empty TextFragment.
    public TextFragment() {
        this("");
    }

    /// Returns the text content.
    ///
    /// @return the text
    public String getText() {
        return text;
    }

    /// Sets the text content.
    ///
    /// If this fragment was extracted from a page (via `TextFragmentAbsorber`),
    /// this method also updates the underlying content stream operator so the change
    /// is reflected when the document is saved.
    ///
    /// @param text the new text value
    public void setText(String text) {
        String oldText = this.text != null ? this.text : "";
        this.text = text != null ? text : "";
        syncPrimarySegmentText(this.text);
        if (page != null && sourceOperatorIndex >= 0) {
            try {
                if (textReplaceOptions != null
                        && textReplaceOptions.getReplaceAdjustmentAction()
                        == TextReplaceOptions.ReplaceAdjustment.WholeWordsHyphenation) {
                    replaceWithWholeWordsHyphenation(oldText, this.text);
                    return;
                }
                updateContentStream(oldText, this.text);
            } catch (IOException e) {
                LOG.warning("Failed to update content stream: " + e.getMessage());
            }
        }
    }

    private void syncPrimarySegmentText(String value) {
        if (segments.isEmpty()) {
            segments.add(new TextSegment(value));
            return;
        }
        segments.get(0).setText(value);
    }

    private void replaceWithWholeWordsHyphenation(String oldText, String newText) throws IOException {
        updateContentStream(oldText, "");
        appendWrappedParagraph(newText);
    }

    private void appendWrappedParagraph(String newText) throws IOException {
        if (page == null || newText == null || newText.isEmpty()) {
            return;
        }
        Position anchor = position;
        Rectangle sourceRect = rectangle;
        if (anchor == null && sourceRect != null) {
            anchor = new Position(sourceRect.getLLX(), sourceRect.getLLY());
        }
        if (anchor == null) {
            return;
        }

        double startX = anchor.getXIndent();
        double startY = anchor.getYIndent();
        double availableWidth = computeAvailableReplacementWidth();
        if (availableWidth <= 0 && page.getRect() != null) {
            availableWidth = Math.max(40.0, page.getRect().getURX() - startX);
        }
        if (availableWidth <= 0) {
            availableWidth = 180.0;
        }

        TextState sourceState = getTextState();
        String layoutFont = normalizeLayoutFont(sourceState != null ? sourceState.getFontName() : null);
        double fontSize = sourceState != null && sourceState.getFontSize() > 0
                ? sourceState.getFontSize() : 12.0;
        double lineSpacing = 1.21325;

        List<String> lines = TextLayoutHelper.wrapText(newText, layoutFont, fontSize, availableWidth);
        double currentY = startY;
        double lineHeight = fontSize * lineSpacing;
        List<TextFragment> syntheticLines = new ArrayList<>();
        double widthScale = sourceRect != null && availableWidth > sourceRect.getWidth() * 2.0 ? 0.81 : 0.95;
        for (String line : lines) {
            TextFragment lineFragment = new TextFragment(line);
            lineFragment.getTextState().setFontName(layoutFont);
            lineFragment.getTextState().setFontSize(fontSize);
            lineFragment.setPosition(new Position(startX, currentY));
            double lineWidth = TextLayoutHelper.measureTextWidth(line, layoutFont, fontSize);
            Rectangle lineRect = new Rectangle(startX, currentY,
                    startX + lineWidth * widthScale, currentY + fontSize * 1.095);
            lineFragment.setRectangle(lineRect);
            lineFragment.setPage(page);
            syntheticLines.add(lineFragment);
            currentY -= lineHeight;
        }
        page.addSyntheticTextFragments(syntheticLines);
    }

    private double computeAvailableReplacementWidth() throws IOException {
        Rectangle sourceRect = rectangle;
        Position anchor = position;
        if (page == null || sourceRect == null || anchor == null) {
            return 0;
        }
        double startX = anchor.getXIndent();
        double pageWidth = page.getRect() != null ? page.getRect().getURX() - startX : 0;
        double bestWidth = pageWidth;
        boolean foundRightNeighbor = false;
        List<TextFragment> pageFragments = new TextExtractor(
                page.getOwningDocument() != null ? page.getOwningDocument().getParser() : null)
                .extract(page);
        for (TextFragment candidate : pageFragments) {
            Rectangle candidateRect = candidate.getRectangle();
            if (candidateRect == null) {
                continue;
            }
            if (candidateRect.getLLX() <= sourceRect.getURX() + 1.0) {
                continue;
            }
            double overlap = Math.min(sourceRect.getURY(), candidateRect.getURY())
                    - Math.max(sourceRect.getLLY(), candidateRect.getLLY());
            double minHeight = Math.min(sourceRect.getHeight(), candidateRect.getHeight());
            if (overlap < Math.max(1.0, minHeight * 0.4)) {
                continue;
            }
            foundRightNeighbor = true;
            bestWidth = Math.min(bestWidth, candidateRect.getLLX() - startX);
        }
        if (!foundRightNeighbor) {
            double fallbackWidth = sourceRect.getWidth() * 2.15;
            if (pageWidth > 0) {
                bestWidth = Math.min(pageWidth, fallbackWidth);
            } else {
                bestWidth = fallbackWidth;
            }
        }
        return bestWidth;
    }

    private String normalizeLayoutFont(String fontName) {
        if (fontName == null || fontName.isEmpty()) {
            return "Helvetica";
        }
        if (fontName.matches("[A-Z]\\d+_\\d+")) {
            return "Helvetica";
        }
        return fontName;
    }

    /// Updates the content stream operator(s) that produced this fragment.
    ///
    /// Mutates the page's cached [OperatorCollection] in place and then
    /// marks the page dirty so [Page#flushContentsIfDirty()] serialises
    /// the change back into `/Contents` during the next save.
    ///
    /// If the fragment spans a range of adjacent text-showing operators
    /// (kerning-split Tj/TJ within a single BT..ET), the range is tracked via
    /// [#getLastSourceOperatorIndex()]. The first operator is replaced
    /// with the new text; intermediate text-showing operators have their text
    /// cleared so they no longer re-assemble into the original phrase on
    /// reload. Non-text-showing ops (Td, Tm, Tf, ...) within the range are
    /// left untouched so positioning is preserved.
    ///
    private void updateContentStream(String oldText, String newText) throws IOException {
        OperatorCollection ops = sourceOperators != null
                ? sourceOperators
                : page != null ? page.getContents() : null;
        if (ops == null) {
            return;
        }
        // Sprint 36: re-derive the index from operator identity so a sibling
        // fragment's insert (e.g. font-restore via insertFontRestoreAfter)
        // doesn't leave us pointing at the wrong op.
        if (sourceOperator != null) {
            int refreshed = indexOfByIdentity(ops, sourceOperator);
            if (refreshed >= 0) {
                sourceOperatorIndex = refreshed;
            }
        }
        if (lastSourceOperator != null && lastSourceOperator != sourceOperator) {
            int refreshed = indexOfByIdentity(ops, lastSourceOperator);
            if (refreshed >= 0) {
                lastSourceOperatorIndex = refreshed;
            }
        } else if (lastSourceOperator == sourceOperator && sourceOperator != null) {
            lastSourceOperatorIndex = sourceOperatorIndex;
        }
        if (sourceOperatorIndex < 0 || sourceOperatorIndex >= ops.size()) return;

        boolean mutated = false;
        int last = lastSourceOperatorIndex >= sourceOperatorIndex
                ? lastSourceOperatorIndex : sourceOperatorIndex;

        // TextReplaceOptions.AdjustSpaceWidth: keep text following the replaced
        // run in place by emitting a compensating TJ adjustment, so a longer or
        // shorter replacement does not shift the rest of the line (PDFNET_59697).
        if (last == sourceOperatorIndex
                && textReplaceOptions != null
                && textReplaceOptions.getReplaceAdjustmentAction()
                        == TextReplaceOptions.ReplaceAdjustment.AdjustSpaceWidth) {
            mutated = replaceTextInSingleOpAdjustSpace(ops, sourceOperatorIndex, oldText, newText);
        }
        if (!mutated && last == sourceOperatorIndex && sourceTextLength >= 0) {
            mutated = replaceTextInSingleOp(ops, sourceOperatorIndex, oldText, newText);
        }
        if (!mutated) {
            mutated = replaceTextOp(ops, sourceOperatorIndex, newText);
        }

        if (last > sourceOperatorIndex && last < ops.size()) {
            for (int i = sourceOperatorIndex + 1; i <= last; i++) {
                if (clearTextOp(ops, i)) {
                    mutated = true;
                }
            }
        }

        // Sprint 36: emit a font-restore operator (`Tf /FontName Size`)
        // immediately after the last modified text-show op. Mirrors Aspose
        // redaction-pipeline behaviour where the original font state is
        // re-asserted after every replacement so subsequent content keeps
        // rendering in the right font and assertions like PDFNET_43250's
        // "original font really restored after text replacement" succeed.
        if (mutated) {
            insertFontRestoreAfter(ops, last);
        }

        if (mutated) {
            if (sourceContentStream != null) {
                // setDecodedData clears the encoded cache; the writer's
                // prepareEncodedData() will re-encode through the existing
                // /Filter chain on save. Stripping /Filter here would emit
                // the modified content stream uncompressed, inflating the
                // saved PDF by ≈25% on large text-heavy fixtures (BUG-046).
                sourceContentStream.setDecodedData(serializeOperators(ops));
            } else if (page != null) {
                page.markContentsDirty();
            }
        }
    }

    /// Returns the 0-based index of `op` in `ops` by reference, or -1.
    private static int indexOfByIdentity(OperatorCollection ops, Operator op) {
        if (op == null) return -1;
        for (int i = 0; i < ops.size(); i++) {
            if (ops.getAt(i) == op) return i;
        }
        return -1;
    }

    /// Inserts a copy of the currently active `Tf` (font selection)
    /// operator immediately after `textShowIdx`. Walks backwards within
    /// the enclosing `BT..ET` block to find the most recent SelectFont
    /// and clones its font name + size. No-op if none is found (e.g. the
    /// modified op sits outside a text object, which would be a malformed
    /// content stream).
    private static int insertFontRestoreAfter(OperatorCollection ops, int textShowIdx) {
        if (textShowIdx < 0 || textShowIdx + 1 > ops.size()) {
            return -1;
        }
        for (int i = textShowIdx - 1; i >= 0; i--) {
            Operator op = ops.getAt(i);
            if (op instanceof SelectFont) {
                SelectFont src = (SelectFont) op;
                String name = src.getFontName();
                if (name == null || name.isEmpty()) {
                    return -1;
                }
                ops.addAt(textShowIdx + 1, new SelectFont(name, src.getSize()));
                return textShowIdx + 1;
            }
            // Stop searching once we leave the current text object — there is
            // no in-scope SelectFont before a BT, and any SelectFont before
            // a prior ET applies to a different text object.
            if (op instanceof BT || op instanceof ET) {
                return -1;
            }
        }
        return -1;
    }

    private static byte[] serializeOperators(OperatorCollection ops) {
        // Byte-level serialization (Sprint 30): op.toString()→US-ASCII would corrupt
        // PdfString operands with bytes >= 0x80 (CID/Identity-H, non-Latin literals).
        // ByteArrayOutputStream never actually throws IOException.
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            for (Operator op : ops) {
                op.writeTo(baos);
                baos.write('\n');
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unexpected IO error serializing operators", e);
        }
        return baos.toByteArray();
    }

    private boolean replaceTextInSingleOp(OperatorCollection ops, int idx, String oldText, String newText) {
        String currentText = getOpText(ops.getAt(idx));
        if (currentText == null) {
            return false;
        }
        int start = findBestReplacementStart(currentText, oldText);
        if (start < 0) {
            return false;
        }
        String replaced = currentText.substring(0, start)
                + newText
                + currentText.substring(start + oldText.length());
        return replaceTextOp(ops, idx, replaced);
    }

    /// Replaces `oldText` with `newText` inside a single text-showing
    /// operator while keeping any text that follows the replaced run on the same
    /// line in its original position (TextReplaceOptions.AdjustSpaceWidth).
    ///
    /// The operator is rewritten as a `TJ` array
    /// `[(prefix+newText) adj (suffix)]` where `adj` is a numeric
    /// displacement, in thousandths of a text-space unit, equal to the exact
    /// glyph-advance difference between the old and new text measured in the
    /// embedded font. Because the renderer applies the same glyph metrics, the
    /// total advance of the operator is left unchanged, so the suffix — and every
    /// operator positioned relative to the end of this one — stays put.
    ///
    /// @return `true` if the operator was rewritten
    private boolean replaceTextInSingleOpAdjustSpace(OperatorCollection ops, int idx,
                                                     String oldText, String newText) {
        if (sourceFont == null) {
            return false;
        }
        // Composite (Type0/CID) fonts carry 2-byte codes; the byte-splicing
        // below would corrupt them. Fall through to replaceTextOp, which
        // re-encodes the whole payload through the font (RTL2/RTL3_changeText).
        if (sourceFont.isComposite()) {
            return false;
        }
        Operator op = ops.getAt(idx);
        // Only the plain text-showing operators carry a single replaceable
        // string payload; ', " and others mix in positioning we must not lose.
        if (!(op instanceof ShowText) && !"TJ".equals(op.getName())) {
            return false;
        }
        String currentText = getOpText(op);
        if (currentText == null) {
            return false;
        }
        int start = findBestReplacementStart(currentText, oldText);
        if (start < 0) {
            return false;
        }
        String prefix = currentText.substring(0, start);
        String suffix = currentText.substring(start + oldText.length());

        TextState st = getTextState();
        double tfs = sourceTfSize > 0 ? sourceTfSize
                : (st != null && st.getFontSize() > 0 ? st.getFontSize() : 1.0);
        double tc = st != null ? st.getCharacterSpacing() : 0;
        double tw = st != null ? st.getWordSpacing() : 0;
        // Exact text-space advance (per the Tf size) of the removed vs inserted
        // glyphs. Horizontal scaling cancels between advance and the TJ number,
        // so it is intentionally omitted here.
        double advOld = glyphAdvanceTextSpace(oldText, tfs, tc, tw);
        double advNew = glyphAdvanceTextSpace(newText, tfs, tc, tw);
        // TJ number n: rendered displacement = -n/1000 * Tfs. Choosing
        // n = (advNew - advOld) * 1000 / Tfs cancels the width change so the
        // suffix keeps its place.
        double n = (advNew - advOld) * 1000.0 / tfs;

        PdfArray arr = new PdfArray();
        arr.add(new PdfString((prefix + newText).getBytes(StandardCharsets.ISO_8859_1)));
        if (Math.abs(n) > 0.01) {
            arr.add(new PdfFloat(n));
        }
        if (!suffix.isEmpty()) {
            arr.add(new PdfString(suffix.getBytes(StandardCharsets.ISO_8859_1)));
        }
        List<PdfBase> newOperands = new ArrayList<>();
        newOperands.add(arr);
        ops.setAt(idx, new SetGlyphsPositionShowText(newOperands));
        return true;
    }

    /// Sums the text-space glyph advances (in Tf-size units) of `text` in
    /// [#sourceFont], including character spacing and word spacing, using
    /// the ISO-8859-1 byte codes that [#replaceTextOp] writes to the
    /// content stream so the measurement matches what the renderer will consume.
    private double glyphAdvanceTextSpace(String text, double tfs, double tc, double tw) {
        double total = 0;
        double unitScale = sourceFont.getWidthUnitScale();
        for (byte b : text.getBytes(StandardCharsets.ISO_8859_1)) {
            int code = b & 0xFF;
            total += sourceFont.getWidth(code) * unitScale * tfs + tc;
            if (code == 32) {
                total += tw;
            }
        }
        return total;
    }

    private int findBestReplacementStart(String currentText, String oldText) {
        if (oldText == null || oldText.isEmpty()) {
            return clampSourceTextStart(currentText.length());
        }
        int expected = clampSourceTextStart(currentText.length());
        int best = -1;
        int bestDistance = Integer.MAX_VALUE;
        int from = 0;
        while (from <= currentText.length() - oldText.length()) {
            int found = currentText.indexOf(oldText, from);
            if (found < 0) {
                break;
            }
            int distance = Math.abs(found - expected);
            if (distance < bestDistance) {
                best = found;
                bestDistance = distance;
                if (distance == 0) {
                    break;
                }
            }
            from = found + 1;
        }
        return best;
    }

    private int clampSourceTextStart(int currentLength) {
        if (sourceTextStart < 0) {
            return 0;
        }
        return Math.min(sourceTextStart, Math.max(0, currentLength));
    }

    private static String getOpText(Operator op) {
        if (op instanceof ShowText) {
            return ((ShowText) op).getText();
        }
        String name = op.getName();
        List<PdfBase> operands = op.getOperands();
        if ("TJ".equals(name)) {
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof PdfArray) {
                StringBuilder sb = new StringBuilder();
                PdfArray arr = (PdfArray) operands.get(0);
                for (int i = 0; i < arr.size(); i++) {
                    PdfBase item = arr.get(i);
                    if (item instanceof PdfString) {
                        sb.append(((PdfString) item).getString());
                    }
                }
                return sb.toString();
            }
        } else if (("'".equals(name) || "\"".equals(name))
                && operands != null && !operands.isEmpty()) {
            PdfBase textOperand = operands.get(operands.size() - 1);
            if (textOperand instanceof PdfString) {
                return ((PdfString) textOperand).getString();
            }
        }
        return null;
    }

    /// Replaces the text payload of the op at `idx` with `newText`.
    private boolean replaceTextOp(OperatorCollection ops, int idx, String newText) {
        Operator op = ops.getAt(idx);
        if (op instanceof ShowText) {
            List<PdfBase> operand = new ArrayList<>(1);
            operand.add(encodeReplacementText(newText));
            ops.setAt(idx, new ShowText(operand));
            return true;
        }
        String name = op.getName();
        if ("TJ".equals(name)) {
            // Collapse the whole TJ array to a single string. A partial
            // replacement (first PdfString only) would leave kerning-split
            // leftovers that re-assemble to the original phrase on reload.
            List<PdfBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof PdfArray) {
                PdfArray newArr = new PdfArray();
                newArr.add(encodeReplacementText(newText));
                List<PdfBase> newOperands = new ArrayList<>(operands);
                newOperands.set(0, newArr);
                // Sprint 35: preserve TextShowOperator subclass so downstream
                // `instanceof TextShowOperator` checks (used by extractor and
                // regression tests verifying operator sequences) keep working.
                ops.setAt(idx, new SetGlyphsPositionShowText(newOperands));
                return true;
            }
        } else if ("'".equals(name) || "\"".equals(name)) {
            // ' and " carry a text string as their final operand.
            List<PdfBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()) {
                List<PdfBase> newOperands = new ArrayList<>(operands);
                int textPos = newOperands.size() - 1;
                if (newOperands.get(textPos) instanceof PdfString) {
                    newOperands.set(textPos, encodeReplacementText(newText));
                    Operator replacement = "'".equals(name)
                            ? new MoveToNextLineShowText(newOperands)
                            : new SetSpacingMoveToNextLineShowText(newOperands);
                    ops.setAt(idx, replacement);
                    return true;
                }
            }
        }
        return false;
    }

    /// Encodes replacement text as the show-operator payload for this
    /// fragment's source font.
    ///
    /// Simple fonts keep the historical ISO-8859-1 byte mapping. Composite
    /// (Type0/CID) fonts get the full RTL write pipeline
    /// (RTL2/RTL3\_changeText):
    ///
    ///   1. contextually shape plain Arabic letters into presentation forms
    ///     ([ArabicShaper]) — the form the surrounding document text
    ///     is stored in;
    ///   2. reverse strong-RTL runs into visual order, matching how RTL
    ///     producers store glyphs in the content stream (the extractor
    ///     applies the inverse via `TextAbsorber.reverseRtlRuns`);
    ///   3. map each character to a 2-byte code through the font's reverse
    ///     ToUnicode CMap, falling back to the character's own codepoint as
    ///     the CID — the decode pipeline mirrors that fallback, so the text
    ///     round-trips even for glyphs absent from the subset.
    private PdfString encodeReplacementText(String newText) {
        if (sourceFont == null || !sourceFont.isComposite()) {
            return new PdfString(newText.getBytes(StandardCharsets.ISO_8859_1));
        }
        String visual = TextAbsorber.reverseRtlRuns(ArabicShaper.shape(newText));
        java.util.Map<Character, Integer> reverse = new java.util.HashMap<>();
        if (sourceFont.getToUnicode() != null) {
            for (java.util.Map.Entry<Integer, String> e
                    : sourceFont.getToUnicode().getMappings().entrySet()) {
                String value = e.getValue();
                if (value != null && value.length() == 1) {
                    // Keep the lowest code when several map to the same char.
                    reverse.merge(value.charAt(0), e.getKey(), Math::min);
                }
            }
        }
        byte[] bytes = new byte[visual.length() * 2];
        for (int i = 0; i < visual.length(); i++) {
            char c = visual.charAt(i);
            int code = reverse.getOrDefault(c, (int) c);
            bytes[2 * i] = (byte) (code >>> 8);
            bytes[2 * i + 1] = (byte) code;
        }
        return new PdfString(bytes);
    }

    /// Clears the text payload of a text-showing op at `idx`.
    private static boolean clearTextOp(OperatorCollection ops, int idx) {
        Operator op = ops.getAt(idx);
        if (op instanceof ShowText) {
            ops.setAt(idx, new ShowText(""));
            return true;
        }
        String name = op.getName();
        if ("TJ".equals(name)) {
            List<PdfBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof PdfArray) {
                PdfArray newArr = new PdfArray();
                newArr.add(new PdfString(new byte[0]));
                List<PdfBase> newOperands = new ArrayList<>(operands);
                newOperands.set(0, newArr);
                // Sprint 35: preserve TextShowOperator subclass (see replaceTextOp).
                ops.setAt(idx, new SetGlyphsPositionShowText(newOperands));
                return true;
            }
        } else if ("'".equals(name) || "\"".equals(name)) {
            List<PdfBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()) {
                List<PdfBase> newOperands = new ArrayList<>(operands);
                int textPos = newOperands.size() - 1;
                if (newOperands.get(textPos) instanceof PdfString) {
                    newOperands.set(textPos, new PdfString(new byte[0]));
                    Operator replacement = "'".equals(name)
                            ? new MoveToNextLineShowText(newOperands)
                            : new SetSpacingMoveToNextLineShowText(newOperands);
                    ops.setAt(idx, replacement);
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the list of text segments.
    ///
    /// @return the segments (mutable list, matching Aspose.PDF API)
    public List<TextSegment> getSegments() {
        return segments;
    }

    /// Adds a text segment.
    ///
    /// @param segment the segment to add
    public void addSegment(TextSegment segment) {
        if (segment != null) {
            segments.add(segment);
        }
    }

    /// Returns the position on the page where this fragment begins.
    ///
    /// @return the position, or null
    public Position getPosition() {
        return position;
    }

    /// Sets the position.
    ///
    /// @param position the position
    public void setPosition(Position position) {
        this.position = position;
        // Propagate position to segments that don't have their own
        for (TextSegment seg : segments) {
            if (seg.getPosition() == null) {
                seg.setPosition(position);
            }
        }
    }

    /// Returns the bounding rectangle of this fragment on the page.
    ///
    /// @return the rectangle, or null
    public Rectangle getRectangle() {
        if (rectangle != null) {
            return rectangle;
        }
        // Unplaced fragment (constructed via `new TextFragment(...)` and not yet
        // absorbed or laid out on a page): synthesize a measured bounding box from
        // the text and the TextState font metrics so geometric callers (e.g.
        // width-based stamp positioning) work instead of hitting an NPE on a null
        // rectangle. Aspose likewise returns a measured rectangle here. Origin is
        // (0,0) since the fragment has no page position yet.
        TextState ts = getTextState();
        double fontSize = (ts != null) ? ts.getFontSize() : 0;
        if (fontSize <= 0) {
            fontSize = 12;
        }
        String fontName = (ts != null && ts.getFont() != null) ? ts.getFont().getName() : null;
        String text = getText();
        double width = TextLayoutHelper.measureTextWidth(text == null ? "" : text, fontName, fontSize);
        return new Rectangle(0, 0, width, fontSize);
    }

    /// Returns the text baseline rotation in device space, quantized to one of
    /// `0, 90, 180, 270`. `0` is ordinary horizontal text.
    ///
    /// @return the rotation in degrees
    public int getRotation() {
        return rotation;
    }

    /// Sets the text baseline rotation (quantized to `0/90/180/270`).
    ///
    /// @param rotation the rotation in degrees
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    /// Returns the exact per-character X boundaries, or `null` if the
    /// extractor could not provide them for this fragment.
    ///
    /// @return the character X positions, or `null`
    public double[] getCharXPositions() {
        return charXPositions;
    }

    /// Sets the exact per-character X boundaries (extractor use).
    ///
    /// @param charXPositions array of length `text.length()+1`, or `null`
    public void setCharXPositions(double[] charXPositions) {
        this.charXPositions = charXPositions;
    }

    /// Returns the engine font that rendered this fragment, or `null`.
    ///
    /// @return the source font, or `null`
    public PdfFont getSourceFont() {
        return sourceFont;
    }

    /// Records the raw `/Tf` operand size of the source show operator
    /// (internal, set by the extractor). See [#sourceTfSize].
    ///
    /// @param tfSize the raw Tf size, or ≤0 when unknown
    public void setSourceTfSize(double tfSize) {
        this.sourceTfSize = tfSize;
    }

    /// Returns the raw `/Tf` operand size of the source show operator,
    /// or ≤0 when unknown. See [#sourceTfSize].
    ///
    /// @return the raw Tf size
    public double getSourceTfSize() {
        return sourceTfSize;
    }

    /// Sets the engine font that rendered this fragment (extractor use).
    ///
    /// @param sourceFont the source font
    public void setSourceFont(PdfFont sourceFont) {
        this.sourceFont = sourceFont;
    }

    /// Sets the bounding rectangle.
    ///
    /// @param rectangle the rectangle
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
        // Propagate rectangle to segments that don't have their own
        for (TextSegment seg : segments) {
            if (seg.getRectangle() == null) {
                seg.setRectangle(rectangle);
            }
        }
    }

    /// Returns the page this fragment was extracted from.
    ///
    /// @return the page, or null
    public Page getPage() {
        return page;
    }

    /// Sets the source page.
    ///
    /// @param page the page
    public void setPage(Page page) {
        this.page = page;
    }

    /// Returns the text state of the first segment (convenience accessor).
    ///
    /// @return the text state
    public TextState getTextState() {
        if (!segments.isEmpty()) {
            return segments.get(0).getTextState();
        }
        return new TextState();
    }

    /// Replaces the text state of the first segment (convenience setter).
    ///
    /// Mirrors the Aspose.PDF C# API where `TextFragment.TextState = new TextState(...)`
    /// applies to the first/primary segment of the fragment. If the fragment has
    /// no segments yet, a default segment is created so the state can be stored.
    ///
    /// @param state the text state to apply (null is silently ignored)
    public void setTextState(TextState state) {
        if (state == null) return;
        if (segments.isEmpty()) {
            segments.add(new TextSegment(this.text));
        }
        segments.get(0).setTextState(state);
    }

    /// Returns the index of the content stream operator that produced this fragment.
    /// A value of `-1` means no source is tracked.
    ///
    /// @return the operator index, or -1
    public int getSourceOperatorIndex() {
        return sourceOperatorIndex;
    }

    /// Sets the index of the content stream operator that produced this fragment.
    ///
    /// @param index the operator index
    public void setSourceOperatorIndex(int index) {
        this.sourceOperatorIndex = index;
    }

    /// Returns the last operator index in this fragment's source span.
    ///
    /// A fragment may span multiple adjacent Tj/TJ operators within a single
    /// BT..ET text object (e.g. letters split for kerning). The range
    /// `[sourceOperatorIndex .. lastSourceOperatorIndex]` covers every
    /// text-showing op whose strings concatenate to the fragment's text.
    ///
    /// @return the last operator index in the fragment's source span, or -1
    public int getLastSourceOperatorIndex() {
        return lastSourceOperatorIndex;
    }

    /// Sets the last operator index in this fragment's source span.
    ///
    /// @param index the last operator index
    public void setLastSourceOperatorIndex(int index) {
        this.lastSourceOperatorIndex = index;
    }

    /// Sprint 36: store source operator by identity. After a sibling fragment
    /// mutates the shared [OperatorCollection] (e.g. inserts a
    /// font-restore op), our cached `sourceOperatorIndex` would be stale;
    /// the reference lets us re-derive the current index before each mutation.
    public void setSourceOperator(Operator op) {
        this.sourceOperator = op;
    }

    public void setLastSourceOperator(Operator op) {
        this.lastSourceOperator = op;
    }

    public Operator getSourceOperator() {
        return sourceOperator;
    }

    public Operator getLastSourceOperator() {
        return lastSourceOperator;
    }

    /// Returns the name of the font used to render this fragment.
    ///
    /// @return the font resource name, or null
    public String getSourceFontName() {
        return sourceFontName;
    }

    /// Sets the font resource name used to render this fragment.
    ///
    /// @param fontName the font name
    public void setSourceFontName(String fontName) {
        this.sourceFontName = fontName;
    }

    /// Returns the start offset of this fragment within the source text-showing operator text.
    ///
    /// @return the zero-based character offset
    public int getSourceTextStart() {
        return sourceTextStart;
    }

    /// Sets the start offset of this fragment within the source text-showing operator text.
    ///
    /// @param sourceTextStart the zero-based character offset
    public void setSourceTextStart(int sourceTextStart) {
        this.sourceTextStart = Math.max(0, sourceTextStart);
    }

    /// Returns the original length of this fragment inside the source text-showing operator text.
    ///
    /// @return the original source text length, or `-1` when unknown
    public int getSourceTextLength() {
        return sourceTextLength;
    }

    /// Sets the original length of this fragment inside the source text-showing operator text.
    ///
    /// @param sourceTextLength the original source text length
    public void setSourceTextLength(int sourceTextLength) {
        this.sourceTextLength = sourceTextLength;
    }

    /// Returns the operator collection that originally produced this fragment.
    ///
    /// @return the source operators, or null
    public OperatorCollection getSourceOperators() {
        return sourceOperators;
    }

    /// Sets the operator collection that originally produced this fragment.
    ///
    /// @param sourceOperators the source operators
    public void setSourceOperators(OperatorCollection sourceOperators) {
        this.sourceOperators = sourceOperators;
    }


    /// Associates a group of source content-stream operators that draw an underline
    /// beneath this fragment, and arms the fragment so that turning the underline off
    /// (`getTextState().setUnderline(false)`) removes those operators on save.
    ///
    /// Called by the text-extraction engine when underline detection is enabled
    /// via `TextEditOptions.ToAttemptGetUnderlineFromSource`.
    ///
    /// @param ops  the operators that draw the underline (re/m/l + paint operator)
    /// @param coll the operator collection that owns `ops`
    public void addSourceUnderline(java.util.List<Operator> ops, OperatorCollection coll) {
        if (ops == null || ops.isEmpty() || coll == null) {
            return;
        }
        if (sourceUnderlineOpGroups == null) {
            sourceUnderlineOpGroups = new java.util.ArrayList<>(1);
        }
        sourceUnderlineOpGroups.add(new java.util.ArrayList<>(ops));
        this.sourceUnderlineCollection = coll;
        // Arm the (possibly shared) TextState so a later setUnderline(false) edit
        // strips these operators. Captured `this` carries page/sourceContentStream.
        getTextState().addUnderlineRemovalHook(this::removeSourceUnderlineFromContent);
    }

    /// Returns the underline operator groups associated with this fragment, or
    /// `null` if none. Used to propagate underline linkage to match fragments.
    ///
    /// @return the underline operator groups, or `null`
    java.util.List<java.util.List<Operator>> getSourceUnderlineOpGroups() {
        return sourceUnderlineOpGroups;
    }

    /// Returns the operator collection that owns this fragment's underline operators.
    ///
    /// @return the collection, or `null`
    OperatorCollection getSourceUnderlineCollection() {
        return sourceUnderlineCollection;
    }

    /// Removes the previously [associated][#addSourceUnderline] underline
    /// operators from the content stream and re-serialises so the change persists on
    /// save. Idempotent: subsequent calls are no-ops.
    void removeSourceUnderlineFromContent() {
        if (sourceUnderlineOpGroups == null || sourceUnderlineCollection == null) {
            return;
        }
        boolean mutated = false;
        for (java.util.List<Operator> group : sourceUnderlineOpGroups) {
            int before = sourceUnderlineCollection.size();
            sourceUnderlineCollection.delete(group);
            if (sourceUnderlineCollection.size() != before) {
                mutated = true;
            }
        }
        sourceUnderlineOpGroups = null; // avoid double-removal
        if (!mutated) {
            return;
        }
        if (sourceContentStream != null) {
            sourceContentStream.setDecodedData(serializeOperators(sourceUnderlineCollection));
        } else if (page != null) {
            page.markContentsDirty();
        }
    }

    /// Returns the form/content stream that owns [#getSourceOperators()].
    ///
    /// @return the source content stream, or null for page-level cached content
    public PdfStream getSourceContentStream() {
        return sourceContentStream;
    }

    /// Sets the form/content stream that owns [#getSourceOperators()].
    ///
    /// @param sourceContentStream the source content stream
    public void setSourceContentStream(PdfStream sourceContentStream) {
        this.sourceContentStream = sourceContentStream;
    }

    /// Returns the text replacement options associated with this fragment.
    ///
    /// @return the replacement options, or `null`
    public TextReplaceOptions getTextReplaceOptions() {
        return textReplaceOptions;
    }

    /// Sets the text replacement options associated with this fragment.
    ///
    /// @param textReplaceOptions the replacement options
    public void setTextReplaceOptions(TextReplaceOptions textReplaceOptions) {
        this.textReplaceOptions = textReplaceOptions;
    }

    /// Gets the footnote associated with this text fragment.
    ///
    /// @return the footnote, or `null` if none
    public org.aspose.pdf.Note getFootNote() {
        return footNote;
    }

    /// Sets the footnote associated with this text fragment.
    ///
    /// @param footNote the footnote to associate
    public void setFootNote(org.aspose.pdf.Note footNote) {
        this.footNote = footNote;
    }

    /// Gets the endnote associated with this text fragment.
    ///
    /// @return the endnote, or `null` if none
    public org.aspose.pdf.Note getEndNote() {
        return endNote;
    }

    /// Sets the endnote associated with this text fragment.
    ///
    /// @param endNote the endnote to associate
    public void setEndNote(org.aspose.pdf.Note endNote) {
        this.endNote = endNote;
    }

    @Override
    public String toString() {
        return text;
    }
}
