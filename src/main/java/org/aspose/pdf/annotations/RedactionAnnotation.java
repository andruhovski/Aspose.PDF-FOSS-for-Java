package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Redaction annotation (ISO 32000-1:2008, Section 12.5.6.23, /Subtype /Redact).
 * <p>
 * A redaction annotation identifies content that is intended to be removed from
 * the document. The intent is to mark regions of a page for redaction prior to
 * actually applying the redaction (removing the content permanently).
 * </p>
 */
public class RedactionAnnotation extends MarkupAnnotation {

    private static final Logger LOG = Logger.getLogger(RedactionAnnotation.class.getName());

    /**
     * Constructs a redaction annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public RedactionAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new redaction annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public RedactionAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Redact"));
    }

    /**
     * Returns the overlay text to be displayed over the redacted region after redaction is applied.
     *
     * @return the overlay text string, or null if not set
     */
    public String getOverlayText() {
        PdfBase ot = dict.get("OverlayText");
        return (ot instanceof PdfString) ? ((PdfString) ot).getString() : null;
    }

    /**
     * Sets the overlay text to be displayed over the redacted region after redaction is applied.
     *
     * @param text the overlay text string, or null to remove
     */
    public void setOverlayText(String text) {
        if (text != null) {
            dict.set(PdfName.of("OverlayText"), new PdfString(text));
        } else {
            dict.remove(PdfName.of("OverlayText"));
        }
    }

    /**
     * Returns the fill color used to paint the redacted area after applying redaction.
     * This is stored as the /IC (interior color) entry per ISO 32000.
     *
     * @return the fill color, or null if not set
     */
    public Color getFillColor() {
        PdfBase ic = dict.get("IC");
        if (ic instanceof PdfArray) {
            PdfArray arr = (PdfArray) ic;
            if (arr.size() == 3) {
                return Color.fromRgb(arr.getFloat(0, 0), arr.getFloat(1, 0), arr.getFloat(2, 0));
            }
            if (arr.size() == 1) {
                return Color.fromGray(arr.getFloat(0, 0));
            }
            if (arr.size() == 4) {
                return Color.fromCmyk(arr.getFloat(0, 0), arr.getFloat(1, 0),
                        arr.getFloat(2, 0), arr.getFloat(3, 0));
            }
        }
        return null;
    }

    /**
     * Sets the fill color used to paint the redacted area after applying redaction.
     * This is stored as the /IC (interior color) entry per ISO 32000.
     *
     * @param color the fill color, or null to remove
     */
    public void setFillColor(Color color) {
        if (color == null) {
            dict.remove(PdfName.of("IC"));
            return;
        }
        PdfArray arr = new PdfArray();
        arr.add(new PdfFloat(color.getR()));
        arr.add(new PdfFloat(color.getG()));
        arr.add(new PdfFloat(color.getB()));
        dict.set(PdfName.of("IC"), arr);
    }

    /**
     * Returns the border color of the redaction annotation.
     * This is the annotation color (/C entry) which defines the border appearance.
     *
     * @return the border color, or null if not set
     */
    public Color getBorderColor() {
        return getColor();
    }

    /**
     * Sets the border color of the redaction annotation.
     * This is the annotation color (/C entry) which defines the border appearance.
     *
     * @param color the border color, or null to remove
     */
    public void setBorderColor(Color color) {
        setColor(color);
    }

    /**
     * Returns the quad points defining the redaction region as an array of {@link Point} objects.
     *
     * @return the quad points, or null if not set
     */
    public Point[] getQuadPoint() {
        PdfBase qp = dict.get("QuadPoints");
        if (qp instanceof PdfArray) {
            PdfArray arr = (PdfArray) qp;
            int count = arr.size() / 2;
            if (count == 0) return null;
            Point[] points = new Point[count];
            for (int i = 0; i < count; i++) {
                points[i] = new Point(arr.getFloat(i * 2, 0), arr.getFloat(i * 2 + 1, 0));
            }
            return points;
        }
        return null;
    }

    /**
     * Sets the quad points defining the redaction region from an array of {@link Point} objects.
     *
     * @param points the quad points, or null to remove
     */
    public void setQuadPoint(Point[] points) {
        if (points == null) {
            dict.remove(PdfName.of("QuadPoints"));
            return;
        }
        PdfArray arr = new PdfArray();
        for (Point p : points) {
            arr.add(new PdfFloat(p.getX()));
            arr.add(new PdfFloat(p.getY()));
        }
        dict.set(PdfName.of("QuadPoints"), arr);
    }

    /**
     * Applies the redaction: draws a filled rectangle over the annotation area
     * in the page content stream and removes the annotation from the page.
     * <p>
     * After calling this method, the content under the redaction area is visually
     * obscured by a filled rectangle. The annotation itself is removed from the
     * page's annotation list.
     * </p>
     */
    public void redact() {
        if (page == null) {
            LOG.warning("Cannot redact: annotation has no associated page");
            return;
        }

        try {
            Rectangle rect = getRect();
            if (rect == null) {
                LOG.warning("Cannot redact: annotation has no rectangle");
                return;
            }

            // Redaction must REMOVE the underlying text, not merely paint over it —
            // painted-over content stays extractable (and searchable), which defeats
            // the purpose of redaction (ISO 32000-1 §12.5.6.23: the content within
            // the region is to be removed). A fragment fully inside the area is
            // deleted whole; a fragment that merely overlaps it loses only the
            // characters actually covered (PDFNET_50927: redacting the ":" of
            // "Date:  13 November 2019" must keep the rest of the line).
            try {
                org.aspose.pdf.text.TextFragmentAbsorber all =
                        new org.aspose.pdf.text.TextFragmentAbsorber();
                all.visit(page);
                for (org.aspose.pdf.text.TextFragment tf : all.getTextFragments()) {
                    Rectangle fr = tf.getRectangle();
                    if (fr == null || !fr.isIntersect(rect)) {
                        continue;
                    }
                    try {
                        String remaining = textOutsideRedaction(tf, fr, rect);
                        if (remaining != null && !remaining.equals(tf.getText())) {
                            tf.setText(remaining);
                        }
                    } catch (RuntimeException perFragment) {
                        LOG.fine(() -> "Could not remove text fragment under redaction: "
                                + perFragment.getMessage());
                    }
                }
            } catch (RuntimeException textRemoval) {
                LOG.log(Level.FINE, "Text removal under redaction failed", textRemoval);
            }

            // Determine fill color (default to white if not set)
            Color fill = getFillColor();
            double r = 1.0, g = 1.0, b = 1.0;
            if (fill != null) {
                r = fill.getR();
                g = fill.getG();
                b = fill.getB();
            }

            // Build content stream operators to draw a filled rectangle
            StringBuilder sb = new StringBuilder();
            sb.append("\nq\n");
            // Set fill color
            sb.append(formatDouble(r)).append(' ')
              .append(formatDouble(g)).append(' ')
              .append(formatDouble(b)).append(" rg\n");
            // Draw rectangle
            sb.append(formatDouble(rect.getLLX())).append(' ')
              .append(formatDouble(rect.getLLY())).append(' ')
              .append(formatDouble(rect.getWidth())).append(' ')
              .append(formatDouble(rect.getHeight())).append(" re\n");
            sb.append("f\n");

            // Draw overlay text if present
            String overlayText = getOverlayText();
            if (overlayText != null && !overlayText.isEmpty()) {
                // Simple overlay text placement at center of rect
                double cx = rect.getLLX() + rect.getWidth() / 2.0;
                double cy = rect.getLLY() + rect.getHeight() / 2.0;
                sb.append("BT\n");
                sb.append("/Helvetica 10 Tf\n");
                sb.append("0 0 0 rg\n"); // black text
                sb.append(formatDouble(cx)).append(' ').append(formatDouble(cy)).append(" Td\n");
                sb.append('(').append(escapePdfString(overlayText)).append(") Tj\n");
                sb.append("ET\n");
            }

            sb.append("Q\n");

            page.appendToContentStream(sb.toString().getBytes(StandardCharsets.US_ASCII));

            // Remove the redaction annotation from the page
            page.getAnnotations().delete(this);

            LOG.fine("Redaction applied at rect " + rect);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to apply redaction", e);
        }
    }

    /**
     * Computes the text of a fragment with the characters covered by the
     * redaction area removed.
     * <p>
     * A fragment whose rectangle lies (almost) entirely inside the area loses
     * all its text. For a partial horizontal overlap the covered character
     * range is derived from the extractor's per-character X boundaries when
     * available, else by linear interpolation across the fragment width — a
     * character is removed when the redaction covers most (&gt;60%) of its
     * cell. Returns {@code null} when nothing is covered.
     * </p>
     *
     * @param tf   the fragment overlapping the redaction area
     * @param fr   the fragment rectangle (non-null)
     * @param rect the redaction rectangle (non-null)
     * @return the remaining text, {@code ""} to delete all, or {@code null} to keep as is
     */
    private static String textOutsideRedaction(org.aspose.pdf.text.TextFragment tf,
                                               Rectangle fr, Rectangle rect) {
        String text = tf.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        final double tol = 0.5;
        boolean fullyInside = fr.getLLX() >= rect.getLLX() - tol
                && fr.getURX() <= rect.getURX() + tol
                && fr.getLLY() >= rect.getLLY() - tol
                && fr.getURY() <= rect.getURY() + tol;
        if (fullyInside) {
            return "";
        }
        // Require a real vertical overlap before removing anything: a redaction
        // band above/below the baseline must not eat the neighbouring line.
        double vOverlap = Math.min(fr.getURY(), rect.getURY()) - Math.max(fr.getLLY(), rect.getLLY());
        double frHeight = fr.getURY() - fr.getLLY();
        if (frHeight <= 0 || vOverlap < frHeight * 0.5) {
            return null;
        }
        double[] xs = tf.getCharXPositions();
        int n = text.length();
        StringBuilder remaining = new StringBuilder(n);
        boolean removedAny = false;
        for (int i = 0; i < n; i++) {
            double left;
            double right;
            if (xs != null && xs.length == n + 1) {
                left = xs[i];
                right = xs[i + 1];
            } else {
                double w = (fr.getURX() - fr.getLLX()) / n;
                left = fr.getLLX() + i * w;
                right = left + w;
            }
            double cover = Math.min(right, rect.getURX()) - Math.max(left, rect.getLLX());
            double cellWidth = right - left;
            // Midpoint-in-area also counts as covered: with interpolated cells
            // a narrow glyph (":") occupies less than 60% of its average-width
            // cell even when the redaction covers the whole glyph.
            double mid = (left + right) / 2;
            boolean covered = cellWidth > 0
                    && (cover >= cellWidth * 0.6
                        || (mid >= rect.getLLX() && mid <= rect.getURX() && cover > 0));
            if (covered) {
                removedAny = true;
                // Replace with a space instead of deleting so the surviving
                // glyphs keep their original advance positions: rectangles of
                // other pending redaction matches on the same line were
                // computed against the ORIGINAL layout and must stay valid
                // (PDFNET_40853 redacts three keywords sequentially).
                remaining.append(' ');
            } else {
                remaining.append(text.charAt(i));
            }
        }
        return removedAny ? remaining.toString() : null;
    }

    /**
     * Formats a double value for PDF content stream output.
     *
     * @param val the value to format
     * @return the formatted string
     */
    private static String formatDouble(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }

    /**
     * Escapes a string for use in a PDF literal string.
     *
     * @param s the string to escape
     * @return the escaped string
     */
    private static String escapePdfString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
