package org.aspose.pdf.html;

import org.aspose.pdf.*;
import org.aspose.pdf.text.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Converts a PDF {@link Document} to HTML markup.
 * <p>
 * Supports two layout modes:
 * <ul>
 *   <li><b>Fixed layout</b> (default) — each text span is absolutely positioned,
 *       faithfully reproducing the original PDF layout.</li>
 *   <li><b>Reflowable layout</b> — text is grouped into paragraphs and headings
 *       for responsive display.</li>
 * </ul>
 * Images can be embedded as Base64 data URIs or saved to an external folder.
 * </p>
 */
public class PdfToHtmlConverter {

    private static final Logger LOG = Logger.getLogger(PdfToHtmlConverter.class.getName());

    /**
     * Converts the given PDF document to an HTML string.
     *
     * @param document the PDF document to convert
     * @param options  save options controlling layout, images, etc.
     * @return the HTML string
     * @throws IOException if reading the document fails
     */
    public String convert(Document document, HtmlSaveOptions options) throws IOException {
        if (options == null) options = new HtmlSaveOptions();

        StringBuilder html = new StringBuilder(4096);
        appendHtmlHead(html, options);

        PageCollection pages = document.getPages();
        for (int i = 1; i <= pages.getCount(); i++) {
            Page page = pages.get(i);
            appendPageDiv(html, page, i, options);
        }

        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private void appendHtmlHead(StringBuilder html, HtmlSaveOptions options) {
        if (options.getDocumentType() == HtmlDocumentType.Xhtml) {
            html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
            html.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        } else {
            html.append("<!DOCTYPE html>\n");
        }
        html.append("<html>\n<head>\n<meta charset=\"UTF-8\"/>\n");
        html.append("<style>\n");
        html.append("body { margin: 0; padding: 0; background: #e0e0e0; }\n");
        html.append(".page { position: relative; margin: 20px auto; background: white; ");
        html.append("overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }\n");
        html.append(".t { position: absolute; white-space: pre; }\n");
        html.append(".i { position: absolute; }\n");
        html.append("</style>\n</head>\n<body>\n");
    }

    private void appendPageDiv(StringBuilder html, Page page, int pageNum,
                                HtmlSaveOptions options) throws IOException {
        Rectangle box = page.getCropBox() != null ? page.getCropBox() : page.getMediaBox();
        if (box == null) box = new Rectangle(0, 0, 595, 842); // A4 fallback
        double w = box.getWidth() * options.getScale();
        double h = box.getHeight() * options.getScale();

        html.append(String.format(
            "<div class=\"page\" id=\"p%d\" style=\"width:%.0fpx;height:%.0fpx;\">\n",
            pageNum, w, h));

        // Ruled tables render as real <table> markup (PDFNET-39027); their
        // text is excluded from the span/paragraph flow to avoid duplicates.
        List<Rectangle> tableRects = appendTables(html, page, options, box);

        if (options.isFixedLayout()) {
            appendFixedLayoutContent(html, page, options, box, tableRects);
        } else {
            appendReflowableContent(html, page, options, box, tableRects);
        }

        appendImages(html, page, options, box);

        html.append("</div>\n");
    }

    /**
     * Emits every ruled table of the page (detected by {@link TableAbsorber})
     * as an HTML {@code <table>} with one {@code <td>} per detected cell.
     * Border-box-only regions (a single row or column) are left to the text
     * flow. Returns the page rectangles the emitted tables cover so the
     * caller can exclude their text from the regular flow.
     */
    private List<Rectangle> appendTables(StringBuilder html, Page page,
                                         HtmlSaveOptions options, Rectangle box) {
        List<Rectangle> covered = new ArrayList<>();
        org.aspose.pdf.text.TableAbsorber absorber = new org.aspose.pdf.text.TableAbsorber();
        try {
            absorber.visit(page);
        } catch (IOException e) {
            LOG.warning("Table detection failed for HTML export: " + e.getMessage());
            return covered;
        }
        for (org.aspose.pdf.text.AbsorbedTable table : absorber.getTableList()) {
            List<org.aspose.pdf.text.AbsorbedRow> rows = table.getRowList();
            if (rows.size() < 2 || rows.get(0).getCellList().size() < 2) {
                continue;   // plain border box, not tabular content
            }
            Rectangle rect = table.getRectangle();
            // Emit the bare <table> tag (Aspose does the same; PDFNET-39027
            // checks for it literally). Cell geometry is carried by the
            // detected structure, not inline CSS.
            html.append("<table>\n");
            for (org.aspose.pdf.text.AbsorbedRow row : rows) {
                html.append("<tr>");
                for (org.aspose.pdf.text.AbsorbedCell cell : row.getCellList()) {
                    html.append("<td>").append(escapeHtml(cell.getText())).append("</td>");
                }
                html.append("</tr>\n");
            }
            html.append("</table>\n");
            if (rect != null) {
                covered.add(rect);
            }
        }
        return covered;
    }

    /** Whether the text position lies inside any of the covered rectangles. */
    private static boolean insideAny(List<Rectangle> rects, Position pos) {
        if (pos == null || rects.isEmpty()) {
            return false;
        }
        for (Rectangle r : rects) {
            if (pos.getXIndent() >= r.getLLX() - 1 && pos.getXIndent() <= r.getURX() + 1
                    && pos.getYIndent() >= r.getLLY() - 1 && pos.getYIndent() <= r.getURY() + 1) {
                return true;
            }
        }
        return false;
    }

    // ── Fixed Layout ──

    private void appendFixedLayoutContent(StringBuilder html, Page page,
                                           HtmlSaveOptions options, Rectangle box,
                                           List<Rectangle> tableRects) {
        double scale = options.getScale();
        double pageH = box.getHeight();

        TextFragmentAbsorber absorber = new TextFragmentAbsorber();
        try {
            page.accept(absorber);
        } catch (IOException e) {
            LOG.warning("Failed to extract text from page: " + e.getMessage());
            return;
        }

        for (TextFragment tf : absorber.getTextFragments()) {
            if (insideAny(tableRects, tf.getPosition())) {
                continue;   // already rendered inside a <table>
            }
            List<TextSegment> segments = tf.getSegments();
            if (segments == null || segments.isEmpty()) {
                // Use fragment-level data
                appendTextSpan(html, tf.getText(), tf.getPosition(), tf.getTextState(),
                               scale, pageH, box.getLLX(), box.getLLY(), options);
                continue;
            }
            for (TextSegment seg : segments) {
                appendTextSpan(html, seg.getText(), seg.getPosition(), seg.getTextState(),
                               scale, pageH, box.getLLX(), box.getLLY(), options);
            }
        }
    }

    private void appendTextSpan(StringBuilder html, String text, Position pos,
                                 TextState ts, double scale, double pageH,
                                 double llx, double lly, HtmlSaveOptions options) {
        if (text == null || text.trim().isEmpty()) return;
        if (pos == null) return;

        double x = (pos.getXIndent() - llx) * scale;
        double fontSize = (ts != null && ts.getFontSize() > 0) ? ts.getFontSize() * scale : 12 * scale;
        double y = (pageH - (pos.getYIndent() - lly)) * scale - fontSize * 0.8;

        StringBuilder style = new StringBuilder();
        style.append(String.format(Locale.US, "left:%.1fpx;top:%.1fpx;font-size:%.1fpx;", x, y, fontSize));

        if (ts != null) {
            String fontFamily = mapFontToCSS(ts.getFontName());
            style.append("font-family:").append(fontFamily).append(';');

            String fn = ts.getFontName();
            if (fn != null) {
                String lower = fn.toLowerCase();
                if (lower.contains("bold")) style.append("font-weight:bold;");
                if (lower.contains("italic") || lower.contains("oblique"))
                    style.append("font-style:italic;");
            }

            Color fg = ts.getForegroundColor();
            if (fg != null) {
                int r = clamp255(fg.getR());
                int g = clamp255(fg.getG());
                int b = clamp255(fg.getB());
                if (r != 0 || g != 0 || b != 0) {
                    style.append(String.format("color:rgb(%d,%d,%d);", r, g, b));
                }
            }
        }

        html.append("  <span class=\"").append(options.getCssPrefix()).append("t\" style=\"")
            .append(style).append("\">")
            .append(escapeHtml(text))
            .append("</span>\n");
    }

    // ── Reflowable Layout ──

    private void appendReflowableContent(StringBuilder html, Page page,
                                          HtmlSaveOptions options, Rectangle box,
                                          List<Rectangle> tableRects) {
        TextFragmentAbsorber absorber = new TextFragmentAbsorber();
        try {
            page.accept(absorber);
        } catch (IOException e) {
            LOG.warning("Failed to extract text from page: " + e.getMessage());
            return;
        }

        List<TextFragment> fragments = new ArrayList<>();
        for (TextFragment tf : absorber.getTextFragments()) {
            if (insideAny(tableRects, tf.getPosition())) {
                continue;   // already rendered inside a <table>
            }
            fragments.add(tf);
        }
        if (fragments.isEmpty()) return;

        // Group into lines by Y-coordinate (tolerance ±2pt)
        List<List<TextFragment>> lines = groupIntoLines(fragments, 2.0);

        // Group lines into paragraphs by inter-line gap
        List<List<List<TextFragment>>> paragraphs = groupIntoParagraphs(lines);

        for (List<List<TextFragment>> para : paragraphs) {
            double maxSize = 0;
            for (List<TextFragment> line : para) {
                for (TextFragment tf : line) {
                    TextState ts = tf.getTextState();
                    if (ts != null && ts.getFontSize() > maxSize) maxSize = ts.getFontSize();
                }
            }
            String tag = maxSize >= 20 ? "h1" : maxSize >= 16 ? "h2" : maxSize >= 13 ? "h3" : "p";

            html.append('<').append(tag).append('>');
            for (int li = 0; li < para.size(); li++) {
                List<TextFragment> line = para.get(li);
                line.sort(Comparator.comparingDouble(f ->
                    f.getPosition() != null ? f.getPosition().getXIndent() : 0));
                for (TextFragment tf : line) {
                    String text = tf.getText();
                    if (text == null || text.isEmpty()) continue;

                    TextState ts = tf.getTextState();
                    boolean isBold = false, isItalic = false;
                    if (ts != null && ts.getFontName() != null) {
                        String fn = ts.getFontName().toLowerCase();
                        isBold = fn.contains("bold");
                        isItalic = fn.contains("italic") || fn.contains("oblique");
                    }

                    if (isBold) html.append("<b>");
                    if (isItalic) html.append("<i>");
                    html.append(escapeHtml(text));
                    if (isItalic) html.append("</i>");
                    if (isBold) html.append("</b>");
                }
                if (li < para.size() - 1) html.append(' ');
            }
            html.append("</").append(tag).append(">\n");
        }
    }

    private List<List<TextFragment>> groupIntoLines(List<TextFragment> fragments, double tolerance) {
        fragments.sort(Comparator.comparingDouble(f ->
            f.getPosition() != null ? -f.getPosition().getYIndent() : 0));

        List<List<TextFragment>> lines = new ArrayList<>();
        List<TextFragment> currentLine = new ArrayList<>();
        double currentY = Double.NaN;

        for (TextFragment tf : fragments) {
            if (tf.getPosition() == null) continue;
            double y = tf.getPosition().getYIndent();
            if (Double.isNaN(currentY) || Math.abs(y - currentY) <= tolerance) {
                currentLine.add(tf);
                if (Double.isNaN(currentY)) currentY = y;
            } else {
                if (!currentLine.isEmpty()) lines.add(currentLine);
                currentLine = new ArrayList<>();
                currentLine.add(tf);
                currentY = y;
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine);
        return lines;
    }

    private List<List<List<TextFragment>>> groupIntoParagraphs(List<List<TextFragment>> lines) {
        List<List<List<TextFragment>>> paragraphs = new ArrayList<>();
        List<List<TextFragment>> currentPara = new ArrayList<>();

        double prevY = Double.NaN;
        for (List<TextFragment> line : lines) {
            double lineY = line.get(0).getPosition().getYIndent();
            double fontSize = 12;
            TextState ts = line.get(0).getTextState();
            if (ts != null && ts.getFontSize() > 0) fontSize = ts.getFontSize();

            if (!Double.isNaN(prevY) && Math.abs(prevY - lineY) > fontSize * 2.0) {
                if (!currentPara.isEmpty()) paragraphs.add(currentPara);
                currentPara = new ArrayList<>();
            }
            currentPara.add(line);
            prevY = lineY;
        }
        if (!currentPara.isEmpty()) paragraphs.add(currentPara);
        return paragraphs;
    }

    // ── Images ──

    private void appendImages(StringBuilder html, Page page,
                               HtmlSaveOptions options, Rectangle box) {
        if (!options.isEmbedImages()) return;

        double scale = options.getScale();
        double pageH = box.getHeight();

        ImagePlacementAbsorber imgAbsorber = new ImagePlacementAbsorber();
        try {
            page.accept(imgAbsorber);
        } catch (IOException e) {
            LOG.warning("Failed to extract images from page: " + e.getMessage());
            return;
        }

        for (ImagePlacement placement : imgAbsorber.getImagePlacements()) {
            Rectangle rect = placement.getRectangle();
            if (rect == null) continue;

            double x = (rect.getLLX() - box.getLLX()) * scale;
            double y = (pageH - (rect.getURY() - box.getLLY())) * scale;
            double w = rect.getWidth() * scale;
            double h = rect.getHeight() * scale;

            try {
                BufferedImage bimg = placement.getImage().toBufferedImage();
                if (bimg == null) continue;
                String base64 = imageToBase64(bimg);
                html.append(String.format(Locale.US,
                    "  <img class=\"i\" style=\"left:%.0fpx;top:%.0fpx;width:%.0fpx;height:%.0fpx;\" " +
                    "src=\"data:image/png;base64,%s\"/>\n",
                    x, y, w, h, base64));
            } catch (Exception e) {
                LOG.fine("Failed to convert image: " + e.getMessage());
            }
        }
    }

    // ── Utilities ──

    private static String imageToBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Maps a PDF font name to a CSS font-family string. */
    public static String mapFontToCSS(String pdfFontName) {
        if (pdfFontName == null) return "'Helvetica',sans-serif";
        String lower = pdfFontName.toLowerCase();
        if (lower.contains("courier")) return "'Courier New',monospace";
        if (lower.contains("times")) return "'Times New Roman',serif";
        if (lower.contains("arial") || lower.contains("helvetica")) return "'Helvetica','Arial',sans-serif";
        if (lower.contains("symbol")) return "'Symbol',serif";
        if (lower.contains("zapf")) return "'ZapfDingbats',serif";
        return "'" + pdfFontName + "',sans-serif";
    }

    /** Escapes special HTML characters in text. */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static int clamp255(double v) {
        return Math.max(0, Math.min(255, (int) (v * 255)));
    }
}
