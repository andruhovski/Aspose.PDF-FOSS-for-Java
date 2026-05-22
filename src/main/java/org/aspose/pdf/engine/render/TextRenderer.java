package org.aspose.pdf.engine.render;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.font.FontRepository;
import org.aspose.pdf.engine.font.PdfFont;
import org.aspose.pdf.engine.parser.PDFParser;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Renders text glyphs onto a {@link Graphics2D} context (ISO 32000-1:2008, §9.4).
 */
public class TextRenderer {

    private static final Logger LOG = Logger.getLogger(TextRenderer.class.getName());

    private final FontRepository fontRepo = new FontRepository();

    /** FRC for measuring JDK glyph widths (outline metrics, no hinting bias). */
    private static final FontRenderContext MEASURE_FRC =
            new FontRenderContext(null, true, true);

    /** Cache: scaled JDK fonts keyed by PDF base font name. */
    private final Map<String, Font> scaledFontCache = new HashMap<>();

    /** Cache for JDK font substitutions keyed by PDF base font name. */
    private static final Map<String, String> FONT_SUBSTITUTION = new HashMap<>();
    static {
        FONT_SUBSTITUTION.put("Helvetica", "SansSerif");
        FONT_SUBSTITUTION.put("Helvetica-Bold", "SansSerif");
        FONT_SUBSTITUTION.put("Helvetica-Oblique", "SansSerif");
        FONT_SUBSTITUTION.put("Helvetica-BoldOblique", "SansSerif");
        FONT_SUBSTITUTION.put("Arial", "Arial");
        FONT_SUBSTITUTION.put("ArialMT", "Arial");
        FONT_SUBSTITUTION.put("Arial-BoldMT", "Arial");
        FONT_SUBSTITUTION.put("Times-Roman", "Serif");
        FONT_SUBSTITUTION.put("Times-Bold", "Serif");
        FONT_SUBSTITUTION.put("Times-Italic", "Serif");
        FONT_SUBSTITUTION.put("Times-BoldItalic", "Serif");
        FONT_SUBSTITUTION.put("TimesNewRomanPSMT", "Serif");
        FONT_SUBSTITUTION.put("Courier", "Monospaced");
        FONT_SUBSTITUTION.put("Courier-Bold", "Monospaced");
        FONT_SUBSTITUTION.put("Courier-Oblique", "Monospaced");
        FONT_SUBSTITUTION.put("Courier-BoldOblique", "Monospaced");
        FONT_SUBSTITUTION.put("Symbol", "SansSerif");
        FONT_SUBSTITUTION.put("ZapfDingbats", "SansSerif");
    }

    /**
     * Renders a text string (Tj operator).
     */
    public void renderText(Graphics2D g2d, GraphicsState state,
                           byte[] rawBytes, Resources resources, PDFParser parser) {
        if (rawBytes == null || rawBytes.length == 0) return;

        PdfFont pdfFont = resolveFont(state.getFontName(), resources, parser);
        String text = decodeText(pdfFont, rawBytes);
        if (text.isEmpty()) return;

        Font jdkFont = mapToJdkFont(pdfFont, state.getFontName());
        drawTextGlyphs(g2d, state, text, rawBytes, pdfFont, jdkFont);
    }

    /**
     * Renders a TJ array (interleaved strings and positioning adjustments).
     */
    public void renderTJArray(Graphics2D g2d, GraphicsState state,
                              COSArray tjArray, Resources resources, PDFParser parser) {
        if (tjArray == null) return;

        PdfFont pdfFont = resolveFont(state.getFontName(), resources, parser);
        Font jdkFont = mapToJdkFont(pdfFont, state.getFontName());

        for (int i = 0; i < tjArray.size(); i++) {
            COSBase element = tjArray.get(i);
            if (element instanceof COSString) {
                byte[] rawBytes = ((COSString) element).getBytes();
                String text = decodeText(pdfFont, rawBytes);
                if (!text.isEmpty()) {
                    drawTextGlyphs(g2d, state, text, rawBytes, pdfFont, jdkFont);
                }
            } else if (element instanceof COSInteger || element instanceof COSFloat) {
                double adj = getNumber(element);
                adjustTextPosition(state, -adj / 1000.0);
            }
        }
    }

    /**
     * Draws decoded text character by character, positioning each glyph via the
     * full Text Rendering Matrix (Trm) per ISO 32000 §9.4.4.
     * <p>
     * A horizontally-scaled unit JDK font is used so that glyphs fit within the
     * PDF advance width with natural sidebearing gaps. The advance stays strictly
     * based on PDF font metrics — no accumulation error.
     * </p>
     */
    private void drawTextGlyphs(Graphics2D g2d, GraphicsState state, String text,
                                byte[] rawBytes, PdfFont pdfFont, Font jdkFont) {
        if (canDrawAsSingleRun(state, text, rawBytes)) {
            drawSingleRun(g2d, state, text, rawBytes, pdfFont, jdkFont);
            return;
        }

        double fontSize = state.getFontSize();
        double hScale = state.getHorizontalScaling() / 100.0;
        double charSpacing = state.getCharSpacing();
        double wordSpacing = state.getWordSpacing();
        double rise = state.getTextRise();
        int renderMode = state.getTextRenderingMode();
        boolean invisible = (renderMode == 3);

        Matrix ctm = state.getCTM();

        // Composite fonts (Type0 / Identity-H) encode each glyph as 2 bytes;
        // simple Type1/TrueType fonts use 1 byte. Walk rawBytes in that
        // stride so widths and advances line up with the decoded text.
        int cidLen = (pdfFont != null && pdfFont.isComposite()) ? 2 : 1;

        int textIdx = 0;
        for (int i = 0; i + cidLen <= rawBytes.length; i += cidLen) {
            int charCode = (cidLen == 2)
                    ? (((rawBytes[i] & 0xFF) << 8) | (rawBytes[i + 1] & 0xFF))
                    : (rawBytes[i] & 0xFF);

            String ch = null;
            if (textIdx < text.length()) {
                int cp = text.codePointAt(textIdx);
                ch = new String(Character.toChars(cp));
                textIdx += Character.charCount(cp);
            }

            if (!invisible && ch != null && !ch.isEmpty()) {
                Matrix textState = new Matrix(fontSize * hScale, 0, 0, fontSize, 0, rise);
                Matrix trm = textState.multiply(state.getTextMatrix()).multiply(ctm);

                AffineTransform savedTransform = g2d.getTransform();
                try {
                    AffineTransform trmTransform = new AffineTransform(
                            trm.getA(), trm.getB(),
                            trm.getC(), trm.getD(),
                            trm.getE(), trm.getF());
                    g2d.transform(trmTransform);
                    g2d.scale(1, -1);

                    if (state.getNonStrokingAlpha() < 1.0f) {
                        g2d.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, state.getNonStrokingAlpha()));
                    }
                    g2d.setColor(state.getFillColor());
                    g2d.setFont(jdkFont);
                    g2d.drawString(ch, 0, 0);
                } finally {
                    g2d.setTransform(savedTransform);
                }
            }

            // Advance by PDF font metrics only (no JDK metrics — avoids accumulation error)
            double glyphWidth;
            if (pdfFont != null) {
                glyphWidth = pdfFont.getWidth(charCode) / 1000.0;
            } else {
                glyphWidth = 0.6;
            }
            double advance = (glyphWidth * fontSize + charSpacing) * hScale;
            if (charCode == 32) {
                advance += wordSpacing * hScale;
            }
            advanceTextMatrix(state, advance);
        }
    }

    private boolean canDrawAsSingleRun(GraphicsState state, String text, byte[] rawBytes) {
        if (text == null || text.isEmpty() || rawBytes == null) return false;
        if (rawBytes.length != text.length()) return false;
        if (Math.abs(state.getCharSpacing()) >= 0.0001) return false;
        if (Math.abs(state.getWordSpacing()) >= 0.0001) return false;
        // The single-run path walks rawBytes one byte at a time and looks up
        // widths per-byte — only correct for simple fonts. A composite (Type0)
        // font that happens to produce rawBytes.length == text.length() (e.g.
        // pure supplementary-plane text where one codepoint = 2-char surrogate
        // pair = 2 CID bytes) would otherwise slip through here and advance
        // by /DW per byte instead of per CID.
        for (int i = 0; i < text.length(); i++) {
            if (Character.isHighSurrogate(text.charAt(i))) return false;
        }
        return true;
    }

    private void drawSingleRun(Graphics2D g2d, GraphicsState state, String text,
                               byte[] rawBytes, PdfFont pdfFont, Font jdkFont) {
        double fontSize = state.getFontSize();
        double hScale = state.getHorizontalScaling() / 100.0;
        double rise = state.getTextRise();
        int renderMode = state.getTextRenderingMode();
        if (renderMode != 3) {
            Matrix ctm = state.getCTM();
            Matrix textState = new Matrix(fontSize * hScale, 0, 0, fontSize, 0, rise);
            Matrix trm = textState.multiply(state.getTextMatrix()).multiply(ctm);

            AffineTransform savedTransform = g2d.getTransform();
            try {
                AffineTransform trmTransform = new AffineTransform(
                        trm.getA(), trm.getB(),
                        trm.getC(), trm.getD(),
                        trm.getE(), trm.getF());
                g2d.transform(trmTransform);
                g2d.scale(1, -1);

                if (state.getNonStrokingAlpha() < 1.0f) {
                    g2d.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, state.getNonStrokingAlpha()));
                }
                g2d.setColor(state.getFillColor());
                g2d.setFont(jdkFont);
                g2d.drawString(text, 0, 0);
            } finally {
                g2d.setTransform(savedTransform);
            }
        }

        double totalAdvance = 0;
        for (byte rawByte : rawBytes) {
            int charCode = rawByte & 0xFF;
            double glyphWidth = pdfFont != null ? pdfFont.getWidth(charCode) / 1000.0 : 0.6;
            totalAdvance += glyphWidth * fontSize * hScale;
        }
        advanceTextMatrix(state, totalAdvance);
    }

    private void adjustTextPosition(GraphicsState state, double displacement) {
        double fontSize = state.getFontSize();
        double hScale = state.getHorizontalScaling() / 100.0;
        double tx = displacement * fontSize * hScale;
        advanceTextMatrix(state, tx);
    }

    private void advanceTextMatrix(GraphicsState state, double tx) {
        Matrix advance = new Matrix(1, 0, 0, 1, tx, 0);
        state.setTextMatrixDirect(advance.multiply(state.getTextMatrix()));
    }

    private PdfFont resolveFont(String fontName, Resources resources, PDFParser parser) {
        if (fontName == null || resources == null) return null;
        COSDictionary fontsDict = resources.getFonts();
        if (fontsDict == null) return null;
        try {
            return fontRepo.getFont(fontsDict, fontName, parser);
        } catch (IOException e) {
            LOG.fine(() -> "Failed to resolve font " + fontName + ": " + e.getMessage());
            return null;
        }
    }

    private String decodeText(PdfFont font, byte[] rawBytes) {
        if (font == null) {
            return new String(rawBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        }
        try {
            return font.decode(rawBytes);
        } catch (IOException e) {
            LOG.fine(() -> "Font decode failed: " + e.getMessage());
            return new String(rawBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Maps a PDF font to a JDK substitute font, horizontally scaled to match
     * the PDF font's glyph widths.
     * <p>
     * Computes the average width ratio (PDF / JDK) across ASCII printable chars
     * and applies it as a horizontal scale via {@link Font#deriveFont(AffineTransform)}.
     * This makes JDK glyphs fit within the PDF advance slots with natural gaps,
     * while the advance stays strictly based on PDF metrics (no accumulation error).
     * </p>
     */
    private Font mapToJdkFont(PdfFont pdfFont) {
        return mapToJdkFont(pdfFont, null);
    }

    /**
     * @param fallbackName the font name from /Tf when {@code pdfFont} is null
     *                     (e.g. an annotation appearance stream referencing a
     *                     font like /Verdana that isn't in the form's empty
     *                     /Resources). Used to look up a system font by name.
     */
    private Font mapToJdkFont(PdfFont pdfFont, String fallbackName) {
        // Cache key: font-dict identity. Two PdfFont instances can share a
        // base-font NAME (e.g. T1_0 Type1C and TT0 TrueType in 31836 both name
        // "XYCBJQ+PerpetuaTitlingMT-Light") but have different /Subtype,
        // /Encoding, and embedded font programs. Keying on name alone hands
        // the wrong loaded Font to the second resource and its glyphs come
        // out as .notdef boxes.
        String cacheKey;
        if (pdfFont == null) {
            cacheKey = fallbackName != null ? "name:" + fallbackName : "__default__";
        } else if (pdfFont.getFontDictionary() != null) {
            cacheKey = "fd@" + System.identityHashCode(pdfFont.getFontDictionary());
        } else {
            cacheKey = pdfFont.getBaseFont() != null ? pdfFont.getBaseFont() : "__default__";
        }
        Font cached = scaledFontCache.get(cacheKey);
        if (cached != null) return cached;

        // Preferred path — embedded CFF (Type1C / CIDFontType0C / OpenType-CFF)
        // wrapped in a synthetic OpenType container. Uses the PDF's own glyph
        // outlines, encoding, and widths; falls back to family substitution
        // below if the font has no FontFile3 or the CFF can't be parsed.
        Font embedded = org.aspose.pdf.engine.font.cff.CFFFontLoader.load(pdfFont);
        if (embedded != null) {
            scaledFontCache.put(cacheKey, embedded);
            return embedded;
        }

        String jdkFamily = "SansSerif";
        int style = Font.PLAIN;

        // Pick the name to resolve. Prefer the embedded PdfFont's PostScript
        // base name (more specific); otherwise use the literal /Tf name from
        // the content stream (annotation appearance streams often reference
        // /Verdana or /Cour without resolvable resources).
        String baseName = null;
        if (pdfFont != null && pdfFont.getBaseFont() != null) {
            baseName = pdfFont.getBaseFont();
        } else if (fallbackName != null && !fallbackName.isEmpty()) {
            baseName = fallbackName;
        }
        if (baseName != null) {
            if (baseName.length() > 7 && baseName.charAt(6) == '+') {
                baseName = baseName.substring(7);
            }

            String mapped = FONT_SUBSTITUTION.get(baseName);
            if (mapped != null) {
                jdkFamily = mapped;
            } else {
                // Try the system font registry by base name first — this picks
                // up Verdana/Cour/Tahoma/etc. that Windows ships natively. PDF
                // appearance streams (FreeText annotations, form fields) often
                // reference these without embedding, expecting the OS to have
                // them. Match the PostScript name OR a "/Cour" → "Courier New"
                // -style abbreviation.
                String resolved = resolveSystemFontName(baseName);
                if (resolved != null) {
                    jdkFamily = resolved;
                } else {
                    String lower = baseName.toLowerCase();
                    if (lower.contains("courier") || lower.contains("mono")) {
                        jdkFamily = "Monospaced";
                    } else if (lower.contains("times") || lower.contains("serif")) {
                        jdkFamily = "Serif";
                    }
                }
            }

            String lower = baseName.toLowerCase();
            if (lower.contains("bold") && (lower.contains("italic") || lower.contains("oblique"))) {
                style = Font.BOLD | Font.ITALIC;
            } else if (lower.contains("bold")) {
                style = Font.BOLD;
            } else if (lower.contains("italic") || lower.contains("oblique")) {
                style = Font.ITALIC;
            }
        }

        Font baseFont = new Font(jdkFamily, style, 1);

        // Compute horizontal scale: ratio of PDF average width to JDK average width.
        // This makes JDK glyphs proportionally match the PDF advance widths.
        double hScaleFactor = computeWidthScale(pdfFont, baseFont);
        Font result;
        if (hScaleFactor > 0.5 && hScaleFactor < 1.5 && Math.abs(hScaleFactor - 1.0) > 0.005) {
            result = baseFont.deriveFont(AffineTransform.getScaleInstance(hScaleFactor, 1.0));
        } else {
            result = baseFont;
        }

        scaledFontCache.put(cacheKey, result);
        return result;
    }

    /**
     * Computes the horizontal scale factor to apply to a JDK font so that its
     * average glyph width matches the PDF font's average glyph width.
     *
     * @return scale factor (PDF_avg / JDK_avg), or 1.0 if not computable
     */
    private double computeWidthScale(PdfFont pdfFont, Font jdkFont) {
        if (pdfFont == null) return 1.0;

        double pdfTotal = 0;
        double jdkTotal = 0;
        int count = 0;

        // Sample ASCII printable characters (space through tilde)
        for (int code = 33; code < 127; code++) {
            double pdfW = pdfFont.getWidth(code) / 1000.0;
            // Skip characters with default/missing width (0 or 1.0 = full em)
            if (pdfW <= 0.01 || pdfW >= 0.999) continue;

            String ch = String.valueOf((char) code);
            double jdkW = jdkFont.getStringBounds(ch, MEASURE_FRC).getWidth();
            if (jdkW <= 0.01) continue;

            pdfTotal += pdfW;
            jdkTotal += jdkW;
            count++;
        }

        if (count >= 10 && jdkTotal > 0) {
            return pdfTotal / jdkTotal;
        }
        return 1.0;
    }

    /** Cache of system font family names (lowercased) → canonical family string. */
    private static volatile java.util.Map<String, String> SYS_FONT_INDEX;

    private static java.util.Map<String, String> systemFontIndex() {
        java.util.Map<String, String> idx = SYS_FONT_INDEX;
        if (idx != null) return idx;
        idx = new java.util.HashMap<>();
        try {
            String[] families = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            for (String f : families) idx.put(f.toLowerCase(java.util.Locale.ROOT), f);
        } catch (Exception ignored) {}
        // Common short-name aliases used in PDF DA strings
        if (idx.containsKey("courier new")) idx.put("cour", idx.get("courier new"));
        if (idx.containsKey("times new roman")) idx.put("times", idx.get("times new roman"));
        if (idx.containsKey("arial")) {
            idx.put("helv", idx.get("arial"));   // common /Helv DA name
            idx.put("helvetica", idx.get("arial"));
        }
        SYS_FONT_INDEX = idx;
        return idx;
    }

    /**
     * Resolves a PDF base font name to a system font family if one matches.
     * Strips trailing weight/style suffixes ("-Bold", ",Italic" etc.) before
     * looking up.
     */
    private static String resolveSystemFontName(String baseName) {
        if (baseName == null || baseName.isEmpty()) return null;
        String stripped = baseName.replaceAll("[-,].*$", "");
        java.util.Map<String, String> idx = systemFontIndex();
        String hit = idx.get(stripped.toLowerCase(java.util.Locale.ROOT));
        if (hit != null) return hit;
        return idx.get(baseName.toLowerCase(java.util.Locale.ROOT));
    }

    private static double getNumber(COSBase val) {
        if (val instanceof COSInteger) return ((COSInteger) val).intValue();
        if (val instanceof COSFloat) return ((COSFloat) val).doubleValue();
        return 0;
    }
}
