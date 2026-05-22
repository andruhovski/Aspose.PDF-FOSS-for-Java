package org.aspose.pdf.text;

import org.aspose.pdf.Color;
import org.aspose.pdf.Page;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

/**
 * Builds and appends text content to a PDF page by generating content stream operators.
 * <p>
 * Registers fonts in the page resources and produces proper PDF content stream syntax
 * (BT/ET blocks with Tf, Td, Tj operators) as specified in ISO 32000-1:2008, §9.
 * </p>
 */
public class TextBuilder {

    private static final Logger LOG = Logger.getLogger(TextBuilder.class.getName());

    private final Page page;

    /**
     * Creates a TextBuilder that appends text to the given page.
     *
     * @param page the target page
     * @throws IllegalArgumentException if page is null
     */
    public TextBuilder(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        this.page = page;
    }

    /**
     * Appends a single text fragment to the page.
     * <p>
     * Registers the font in the page's /Resources/Font dictionary if not already present,
     * then builds and appends content stream bytes: {@code q BT /Fn size Tf x y Td (text) Tj ET Q}.
     * </p>
     *
     * @param fragment the text fragment to append
     * @throws IllegalArgumentException if fragment is null
     */
    public void appendText(TextFragment fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("TextFragment must not be null");
        }
        fragment.setPage(this.page);

        TextState fragState = fragment.getTextState();
        Position fragPos = fragment.getPosition();
        double fragX = fragPos != null ? fragPos.getXIndent() : 0;
        double fragY = fragPos != null ? fragPos.getYIndent() : 0;

        // ── Build a working list of segments to render ───────────────────────
        // The TextFragment ctor seeds segments[0] with the primary text. If a
        // user adds more segments via getSegments().add(...), render each one
        // as its own Tj so absorbers reading the document back can decompose
        // the fragment into per-segment text fragments (matching the
        // Aspose.PDF semantics that each Tj-per-state becomes a fragment).
        java.util.List<TextSegment> segments = fragment.getSegments();
        if (segments == null || segments.isEmpty()) {
            // Defensive: render only the primary text via the historical path.
            String resourceName = registerFont(resolveFontName(fragState));
            page.appendToContentStream(buildTextContent(resourceName,
                    resolveFontSize(fragState), fragX, fragY, fragment.getText()));
            LOG.fine(() -> "Appended text fragment (no segments): \"" + fragment.getText() + "\"");
            return;
        }

        // Compute background rectangles up-front (one for the whole fragment if
        // any of its segments inherits its BG, plus one per segment with its own).
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        try {
            write(baos, "q\n");

            // Fragment-level background rectangle (covers all segments roughly):
            // position at fragment Y baseline, width estimated from primary text.
            if (fragState.getBackgroundColor() != null) {
                double fragSize = resolveFontSize(fragState);
                double fragWidth = estimateTextWidth(fragment.getText(), fragSize);
                writeBackgroundRect(baos, fragState.getBackgroundColor(),
                        fragX, fragY, fragWidth, fragSize * 1.32);
            }

            // Per-segment background rectangles (skip primary if it inherits
            // the fragment-level one — the primary segment shares state with
            // the fragment and doesn't get a separate rectangle).
            for (int i = 0; i < segments.size(); i++) {
                TextSegment seg = segments.get(i);
                TextState segState = seg.getTextState();
                if (segState == null) continue;
                Color bg = segState.getBackgroundColor();
                if (bg == null) continue;
                if (i == 0 && bg == fragState.getBackgroundColor()) continue;
                Position sp = seg.getPosition();
                double sx = sp != null ? sp.getXIndent() : fragX;
                double sy = sp != null ? sp.getYIndent() : fragY;
                double size = resolveFontSize(segState);
                double w = estimateTextWidth(seg.getText(), size);
                writeBackgroundRect(baos, bg, sx, sy, w, size * 1.32);
            }

            // Now emit one BT…ET for the segments.
            write(baos, "BT\n");
            String currentFont = null;
            double currentSize = -1;
            Color currentFg = null;
            for (int i = 0; i < segments.size(); i++) {
                TextSegment seg = segments.get(i);
                TextState segState = seg.getTextState();
                if (segState == null) {
                    segState = fragState;
                }
                String fontName = resolveFontName(segState);
                double fontSize = resolveFontSize(segState);

                // Position: if segment has explicit position use Tm, else for the
                // first segment use fragment position via Tm, else inherit cursor.
                Position segPos = seg.getPosition();
                if (segPos != null) {
                    write(baos, "1 0 0 1 " + formatNumber(segPos.getXIndent())
                            + " " + formatNumber(segPos.getYIndent()) + " Tm\n");
                } else if (i == 0) {
                    write(baos, "1 0 0 1 " + formatNumber(fragX)
                            + " " + formatNumber(fragY) + " Tm\n");
                }

                if (!fontName.equals(currentFont) || fontSize != currentSize) {
                    String resourceName = registerFont(fontName);
                    write(baos, "/" + resourceName + " " + formatNumber(fontSize) + " Tf\n");
                    currentFont = fontName;
                    currentSize = fontSize;
                }

                Color fg = segState.getForegroundColor();
                if (fg == null) fg = fragState.getForegroundColor();
                if (fg != null && fg != currentFg) {
                    writeFillColorRG(baos, fg);
                    currentFg = fg;
                }

                String txt = seg.getText() != null ? seg.getText() : "";
                write(baos, "(" + escapePdfString(txt) + ") Tj\n");
            }
            write(baos, "ET\n");
            write(baos, "Q\n");
        } catch (IOException e) {
            throw new RuntimeException("Unexpected I/O error building content stream", e);
        }

        page.appendToContentStream(baos.toByteArray());
        LOG.fine(() -> "Appended text fragment with " + segments.size() + " segment(s)");
    }

    private static String resolveFontName(TextState s) {
        return s != null && s.getFontName() != null ? s.getFontName() : "Helvetica";
    }

    private static double resolveFontSize(TextState s) {
        return s != null && s.getFontSize() > 0 ? s.getFontSize() : 12;
    }

    /**
     * Estimates text width by averaging the typical character advance for the
     * Helvetica/CourierNew sans-serif family. This is intentionally a rough
     * heuristic — it powers background rectangles for which a coarse
     * tolerance suffices; pixel-perfect width would require a real
     * font-metric table (out of scope).
     */
    private static double estimateTextWidth(String text, double fontSize) {
        if (text == null || text.isEmpty()) return 0;
        // Average advance ≈ 0.55 em across mixed-width sans-serif text.
        return text.length() * fontSize * 0.55;
    }

    private static void writeBackgroundRect(ByteArrayOutputStream baos, Color color,
                                            double x, double y, double w, double h) throws IOException {
        write(baos, "q\n");
        writeFillColorRG(baos, color);
        write(baos, formatNumber(x) + " " + formatNumber(y) + " "
                + formatNumber(w) + " " + formatNumber(h) + " re\n");
        write(baos, "f\n");
        write(baos, "Q\n");
    }

    private static void writeFillColorRG(ByteArrayOutputStream baos, Color color) throws IOException {
        double[] c = color.getComponents();
        double r = c.length > 0 ? c[0] : 0;
        double g = c.length > 1 ? c[1] : r;
        double b = c.length > 2 ? c[2] : r;
        write(baos, formatNumber(r) + " " + formatNumber(g) + " " + formatNumber(b) + " rg\n");
    }

    /**
     * Appends multiple text fragments to the page.
     * <p>
     * Each fragment is appended individually by calling {@link #appendText(TextFragment)}.
     * </p>
     *
     * @param fragments the list of text fragments to append
     * @throws IllegalArgumentException if fragments is null
     */
    public void appendText(List<TextFragment> fragments) {
        if (fragments == null) {
            throw new IllegalArgumentException("Fragments list must not be null");
        }
        for (TextFragment fragment : fragments) {
            appendText(fragment);
        }
    }

    /**
     * Appends a paragraph (multiple lines) to the page.
     * <p>
     * Each line is positioned vertically below the previous one using the paragraph's
     * line spacing multiplied by the font size.
     * </p>
     *
     * @param paragraph the text paragraph to append
     * @throws IllegalArgumentException if paragraph is null
     */
    public void appendParagraph(TextParagraph paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("TextParagraph must not be null");
        }

        List<TextFragment> lines = paragraph.getLinesList();
        if (lines.isEmpty()) {
            return;
        }

        Position paraPos = paragraph.getPosition();
        double startX = paraPos != null ? paraPos.getXIndent() : 0;
        double startY = paraPos != null ? paraPos.getYIndent() : 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        try {
            write(baos, "q\n");
            write(baos, "BT\n");

            for (int i = 0; i < lines.size(); i++) {
                TextFragment line = lines.get(i);
                line.setPage(this.page);
                TextState state = line.getTextState();
                String fontName = state.getFontName() != null ? state.getFontName() : "Helvetica";
                double fontSize = state.getFontSize() > 0 ? state.getFontSize() : 12;

                String resourceName = registerFont(fontName);

                write(baos, "/" + resourceName + " " + formatNumber(fontSize) + " Tf\n");

                if (i == 0) {
                    write(baos, formatNumber(startX) + " " + formatNumber(startY) + " Td\n");
                } else {
                    // Move down by line spacing * font size
                    double leading = -(paragraph.getLineSpacing() * fontSize);
                    write(baos, "0 " + formatNumber(leading) + " Td\n");
                }

                write(baos, "(" + escapePdfString(line.getText()) + ") Tj\n");
            }

            write(baos, "ET\n");
            write(baos, "Q\n");
        } catch (IOException e) {
            // ByteArrayOutputStream does not throw IOException
            throw new RuntimeException("Unexpected I/O error building content stream", e);
        }

        page.appendToContentStream(baos.toByteArray());
        LOG.fine(() -> "Appended paragraph with " + lines.size() + " line(s)");
    }

    /**
     * Registers a font in the page's /Resources/Font dictionary if not already present.
     * Creates a simple Type1 font dictionary with /Type /Font, /Subtype /Type1,
     * /BaseFont /&lt;fontName&gt;, /Encoding /WinAnsiEncoding.
     *
     * @param fontName the base font name (e.g., "Helvetica", "Times-Roman")
     * @return the resource name (e.g., "F1", "F2") used to reference this font in the content stream
     */
    private String registerFont(String fontName) {
        Resources resources = page.ensureResources();
        COSDictionary resDict = resources.getCOSDictionary();

        // Get or create the /Font sub-dictionary
        COSDictionary fontsDict = resources.getFonts();
        if (fontsDict == null) {
            fontsDict = new COSDictionary();
            resDict.set(COSName.FONT, fontsDict);
        }

        // Check if this base font is already registered
        for (COSName key : fontsDict.keySet()) {
            org.aspose.pdf.engine.cos.COSBase val = fontsDict.get(key);
            if (val instanceof COSDictionary) {
                COSDictionary fontDict = (COSDictionary) val;
                String existingBase = fontDict.getNameAsString("BaseFont");
                if (fontName.equals(existingBase)) {
                    return key.getName();
                }
            }
        }

        // Generate a new name /F1, /F2, ...
        int index = 1;
        String candidateName;
        do {
            candidateName = "F" + index;
            index++;
        } while (fontsDict.containsKey(candidateName));

        // Create font dictionary: /Type /Font /Subtype /Type1 /BaseFont /<fontName> /Encoding /WinAnsiEncoding
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.TYPE, COSName.FONT);
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.BASE_FONT, COSName.of(fontName));
        fontDict.set(COSName.ENCODING, COSName.of("WinAnsiEncoding"));

        String resourceName = candidateName;
        fontsDict.set(COSName.of(resourceName), fontDict);

        LOG.fine(() -> "Registered font " + fontName + " as /" + resourceName);
        return resourceName;
    }

    /**
     * Builds content stream bytes for a single text fragment.
     */
    private byte[] buildTextContent(String fontResourceName, double fontSize,
                                    double x, double y, String text) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("q\n");
        sb.append("BT\n");
        sb.append("/").append(fontResourceName).append(" ").append(formatNumber(fontSize)).append(" Tf\n");
        sb.append("1 0 0 1 ").append(formatNumber(x)).append(" ").append(formatNumber(y)).append(" Tm\n");
        sb.append("(").append(escapePdfString(text)).append(") Tj\n");
        sb.append("ET\n");
        sb.append("Q\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Escapes a string for use in a PDF literal string: backslash, parentheses, and control chars.
     *
     * @param text the input string
     * @return the escaped string (without enclosing parentheses)
     */
    static String escapePdfString(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '(':
                    sb.append("\\(");
                    break;
                case ')':
                    sb.append("\\)");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Formats a number for PDF content stream, avoiding unnecessary decimals for integers.
     */
    private static String formatNumber(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        // Use up to 4 decimal places, strip trailing zeros
        String s = String.format("%.4f", value);
        // Remove trailing zeros after decimal
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s;
    }

    private static void write(ByteArrayOutputStream baos, String s) throws IOException {
        baos.write(s.getBytes(StandardCharsets.US_ASCII));
    }
}
