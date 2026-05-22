package org.aspose.pdf.text;

import org.aspose.pdf.BaseParagraph;
import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.aspose.pdf.engine.text.TextExtractor;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.operators.ShowText;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a fragment of text extracted from a PDF page.
 * <p>
 * A text fragment has a text value, position, bounding rectangle, and one or
 * more {@link TextSegment}s that may have different formatting. The text state
 * of the fragment is delegated to its first segment.
 * </p>
 * <p>
 * When a fragment was extracted from a page via {@link TextFragmentAbsorber},
 * calling {@link #setText(String)} will update the underlying content stream
 * so that the change is reflected when the document is saved.
 * </p>
 */
public class TextFragment extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(TextFragment.class.getName());

    private String text;
    private final List<TextSegment> segments;
    private Position position;
    private Rectangle rectangle;
    private Page page;

    private org.aspose.pdf.Note footNote;
    private org.aspose.pdf.Note endNote;

    // Source location for content stream modification
    private int sourceOperatorIndex = -1;
    private int lastSourceOperatorIndex = -1;
    private String sourceFontName;
    private int sourceTextStart = 0;
    private int sourceTextLength = -1;
    private OperatorCollection sourceOperators;
    private COSStream sourceContentStream;
    private TextReplaceOptions textReplaceOptions;

    /**
     * Creates a TextFragment with the given text.
     *
     * @param text the fragment text
     */
    public TextFragment(String text) {
        this.text = text != null ? text : "";
        this.segments = new ArrayList<>();
        // Create default segment
        this.segments.add(new TextSegment(this.text));
    }

    /**
     * Creates an empty TextFragment.
     */
    public TextFragment() {
        this("");
    }

    /**
     * Returns the text content.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content.
     * <p>
     * If this fragment was extracted from a page (via {@code TextFragmentAbsorber}),
     * this method also updates the underlying content stream operator so the change
     * is reflected when the document is saved.
     * </p>
     *
     * @param text the new text value
     */
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
        List<TextFragment> pageFragments = new TextExtractor(null).extract(page);
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

    /**
     * Updates the content stream operator(s) that produced this fragment.
     * <p>
     * Mutates the page's cached {@link OperatorCollection} in place and then
     * marks the page dirty so {@link Page#flushContentsIfDirty()} serialises
     * the change back into {@code /Contents} during the next save.
     * </p>
     * <p>
     * If the fragment spans a range of adjacent text-showing operators
     * (kerning-split Tj/TJ within a single BT..ET), the range is tracked via
     * {@link #getLastSourceOperatorIndex()}. The first operator is replaced
     * with the new text; intermediate text-showing operators have their text
     * cleared so they no longer re-assemble into the original phrase on
     * reload. Non-text-showing ops (Td, Tm, Tf, ...) within the range are
     * left untouched so positioning is preserved.
     * </p>
     */
    private void updateContentStream(String oldText, String newText) throws IOException {
        OperatorCollection ops = sourceOperators != null
                ? sourceOperators
                : page != null ? page.getContents() : null;
        if (ops == null) {
            return;
        }
        if (sourceOperatorIndex >= ops.size()) return;

        boolean mutated = false;
        int last = lastSourceOperatorIndex >= sourceOperatorIndex
                ? lastSourceOperatorIndex : sourceOperatorIndex;

        if (last == sourceOperatorIndex && sourceTextLength >= 0) {
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

    private static byte[] serializeOperators(OperatorCollection ops) {
        StringBuilder sb = new StringBuilder();
        for (Operator op : ops) {
            sb.append(op.toString()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
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
        List<COSBase> operands = op.getOperands();
        if ("TJ".equals(name)) {
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof COSArray) {
                StringBuilder sb = new StringBuilder();
                COSArray arr = (COSArray) operands.get(0);
                for (int i = 0; i < arr.size(); i++) {
                    COSBase item = arr.get(i);
                    if (item instanceof COSString) {
                        sb.append(((COSString) item).getString());
                    }
                }
                return sb.toString();
            }
        } else if (("'".equals(name) || "\"".equals(name))
                && operands != null && !operands.isEmpty()) {
            COSBase textOperand = operands.get(operands.size() - 1);
            if (textOperand instanceof COSString) {
                return ((COSString) textOperand).getString();
            }
        }
        return null;
    }

    /** Replaces the text payload of the op at {@code idx} with {@code newText}. */
    private static boolean replaceTextOp(OperatorCollection ops, int idx, String newText) {
        Operator op = ops.getAt(idx);
        if (op instanceof ShowText) {
            ops.setAt(idx, new ShowText(newText));
            return true;
        }
        String name = op.getName();
        if ("TJ".equals(name)) {
            // Collapse the whole TJ array to a single string. A partial
            // replacement (first COSString only) would leave kerning-split
            // leftovers that re-assemble to the original phrase on reload.
            List<COSBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof COSArray) {
                COSArray newArr = new COSArray();
                newArr.add(new COSString(newText.getBytes(StandardCharsets.ISO_8859_1)));
                List<COSBase> newOperands = new ArrayList<>(operands);
                newOperands.set(0, newArr);
                ops.setAt(idx, new Operator("TJ", newOperands));
                return true;
            }
        } else if ("'".equals(name) || "\"".equals(name)) {
            // ' and " carry a text string as their final operand.
            List<COSBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()) {
                List<COSBase> newOperands = new ArrayList<>(operands);
                int textPos = newOperands.size() - 1;
                if (newOperands.get(textPos) instanceof COSString) {
                    newOperands.set(textPos,
                            new COSString(newText.getBytes(StandardCharsets.ISO_8859_1)));
                    ops.setAt(idx, new Operator(name, newOperands));
                    return true;
                }
            }
        }
        return false;
    }

    /** Clears the text payload of a text-showing op at {@code idx}. */
    private static boolean clearTextOp(OperatorCollection ops, int idx) {
        Operator op = ops.getAt(idx);
        if (op instanceof ShowText) {
            ops.setAt(idx, new ShowText(""));
            return true;
        }
        String name = op.getName();
        if ("TJ".equals(name)) {
            List<COSBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()
                    && operands.get(0) instanceof COSArray) {
                COSArray newArr = new COSArray();
                newArr.add(new COSString(new byte[0]));
                List<COSBase> newOperands = new ArrayList<>(operands);
                newOperands.set(0, newArr);
                ops.setAt(idx, new Operator("TJ", newOperands));
                return true;
            }
        } else if ("'".equals(name) || "\"".equals(name)) {
            List<COSBase> operands = op.getOperands();
            if (operands != null && !operands.isEmpty()) {
                List<COSBase> newOperands = new ArrayList<>(operands);
                int textPos = newOperands.size() - 1;
                if (newOperands.get(textPos) instanceof COSString) {
                    newOperands.set(textPos, new COSString(new byte[0]));
                    ops.setAt(idx, new Operator(name, newOperands));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the list of text segments.
     *
     * @return the segments (mutable list, matching Aspose.PDF API)
     */
    public List<TextSegment> getSegments() {
        return segments;
    }

    /**
     * Adds a text segment.
     *
     * @param segment the segment to add
     */
    public void addSegment(TextSegment segment) {
        if (segment != null) {
            segments.add(segment);
        }
    }

    /**
     * Returns the position on the page where this fragment begins.
     *
     * @return the position, or null
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the position
     */
    public void setPosition(Position position) {
        this.position = position;
        // Propagate position to segments that don't have their own
        for (TextSegment seg : segments) {
            if (seg.getPosition() == null) {
                seg.setPosition(position);
            }
        }
    }

    /**
     * Returns the bounding rectangle of this fragment on the page.
     *
     * @return the rectangle, or null
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Sets the bounding rectangle.
     *
     * @param rectangle the rectangle
     */
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
        // Propagate rectangle to segments that don't have their own
        for (TextSegment seg : segments) {
            if (seg.getRectangle() == null) {
                seg.setRectangle(rectangle);
            }
        }
    }

    /**
     * Returns the page this fragment was extracted from.
     *
     * @return the page, or null
     */
    public Page getPage() {
        return page;
    }

    /**
     * Sets the source page.
     *
     * @param page the page
     */
    public void setPage(Page page) {
        this.page = page;
    }

    /**
     * Returns the text state of the first segment (convenience accessor).
     *
     * @return the text state
     */
    public TextState getTextState() {
        if (!segments.isEmpty()) {
            return segments.get(0).getTextState();
        }
        return new TextState();
    }

    /**
     * Replaces the text state of the first segment (convenience setter).
     * <p>
     * Mirrors the Aspose.PDF C# API where {@code TextFragment.TextState = new TextState(...)}
     * applies to the first/primary segment of the fragment. If the fragment has
     * no segments yet, a default segment is created so the state can be stored.
     * </p>
     *
     * @param state the text state to apply (null is silently ignored)
     */
    public void setTextState(TextState state) {
        if (state == null) return;
        if (segments.isEmpty()) {
            segments.add(new TextSegment(this.text));
        }
        segments.get(0).setTextState(state);
    }

    /**
     * Returns the index of the content stream operator that produced this fragment.
     * A value of {@code -1} means no source is tracked.
     *
     * @return the operator index, or -1
     */
    public int getSourceOperatorIndex() {
        return sourceOperatorIndex;
    }

    /**
     * Sets the index of the content stream operator that produced this fragment.
     *
     * @param index the operator index
     */
    public void setSourceOperatorIndex(int index) {
        this.sourceOperatorIndex = index;
    }

    /**
     * Returns the last operator index in this fragment's source span.
     * <p>
     * A fragment may span multiple adjacent Tj/TJ operators within a single
     * BT..ET text object (e.g. letters split for kerning). The range
     * {@code [sourceOperatorIndex .. lastSourceOperatorIndex]} covers every
     * text-showing op whose strings concatenate to the fragment's text.
     * </p>
     *
     * @return the last operator index in the fragment's source span, or -1
     */
    public int getLastSourceOperatorIndex() {
        return lastSourceOperatorIndex;
    }

    /**
     * Sets the last operator index in this fragment's source span.
     *
     * @param index the last operator index
     */
    public void setLastSourceOperatorIndex(int index) {
        this.lastSourceOperatorIndex = index;
    }

    /**
     * Returns the name of the font used to render this fragment.
     *
     * @return the font resource name, or null
     */
    public String getSourceFontName() {
        return sourceFontName;
    }

    /**
     * Sets the font resource name used to render this fragment.
     *
     * @param fontName the font name
     */
    public void setSourceFontName(String fontName) {
        this.sourceFontName = fontName;
    }

    /**
     * Returns the start offset of this fragment within the source text-showing operator text.
     *
     * @return the zero-based character offset
     */
    public int getSourceTextStart() {
        return sourceTextStart;
    }

    /**
     * Sets the start offset of this fragment within the source text-showing operator text.
     *
     * @param sourceTextStart the zero-based character offset
     */
    public void setSourceTextStart(int sourceTextStart) {
        this.sourceTextStart = Math.max(0, sourceTextStart);
    }

    /**
     * Returns the original length of this fragment inside the source text-showing operator text.
     *
     * @return the original source text length, or {@code -1} when unknown
     */
    public int getSourceTextLength() {
        return sourceTextLength;
    }

    /**
     * Sets the original length of this fragment inside the source text-showing operator text.
     *
     * @param sourceTextLength the original source text length
     */
    public void setSourceTextLength(int sourceTextLength) {
        this.sourceTextLength = sourceTextLength;
    }

    /**
     * Returns the operator collection that originally produced this fragment.
     *
     * @return the source operators, or null
     */
    public OperatorCollection getSourceOperators() {
        return sourceOperators;
    }

    /**
     * Sets the operator collection that originally produced this fragment.
     *
     * @param sourceOperators the source operators
     */
    public void setSourceOperators(OperatorCollection sourceOperators) {
        this.sourceOperators = sourceOperators;
    }

    /**
     * Returns the form/content stream that owns {@link #getSourceOperators()}.
     *
     * @return the source content stream, or null for page-level cached content
     */
    public COSStream getSourceContentStream() {
        return sourceContentStream;
    }

    /**
     * Sets the form/content stream that owns {@link #getSourceOperators()}.
     *
     * @param sourceContentStream the source content stream
     */
    public void setSourceContentStream(COSStream sourceContentStream) {
        this.sourceContentStream = sourceContentStream;
    }

    /**
     * Returns the text replacement options associated with this fragment.
     *
     * @return the replacement options, or {@code null}
     */
    public TextReplaceOptions getTextReplaceOptions() {
        return textReplaceOptions;
    }

    /**
     * Sets the text replacement options associated with this fragment.
     *
     * @param textReplaceOptions the replacement options
     */
    public void setTextReplaceOptions(TextReplaceOptions textReplaceOptions) {
        this.textReplaceOptions = textReplaceOptions;
    }

    /**
     * Gets the footnote associated with this text fragment.
     *
     * @return the footnote, or {@code null} if none
     */
    public org.aspose.pdf.Note getFootNote() {
        return footNote;
    }

    /**
     * Sets the footnote associated with this text fragment.
     *
     * @param footNote the footnote to associate
     */
    public void setFootNote(org.aspose.pdf.Note footNote) {
        this.footNote = footNote;
    }

    /**
     * Gets the endnote associated with this text fragment.
     *
     * @return the endnote, or {@code null} if none
     */
    public org.aspose.pdf.Note getEndNote() {
        return endNote;
    }

    /**
     * Sets the endnote associated with this text fragment.
     *
     * @param endNote the endnote to associate
     */
    public void setEndNote(org.aspose.pdf.Note endNote) {
        this.endNote = endNote;
    }

    @Override
    public String toString() {
        return text;
    }
}
