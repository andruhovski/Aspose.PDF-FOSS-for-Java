package org.aspose.pdf.engine.render;

import org.aspose.pdf.Document;
import org.aspose.pdf.devices.PngDevice;
import org.aspose.pdf.devices.Resolution;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renderer regression tests for stroked paths that used to vanish:
 *
 * <ul>
 *   <li><b>Axis-aligned lines</b> - a pure horizontal/vertical line has a
 *       zero-height/width bounding box; {@code Rectangle2D.isEmpty()} reports
 *       it empty and an early-out in {@code strokePath} dropped the whole
 *       stroke (corpus 29903.pdf: every table rule missing).</li>
 *   <li><b>Zero-width strokes</b> - ISO 32000 (8.4.3.2) defines width 0 as
 *       the thinnest device-renderable line, but Java2D draws nothing for a
 *       0-width stroke under the anti-aliased pipeline, and small positive
 *       widths under a down-scaling CTM anti-alias to invisibility. The
 *       renderer now clamps the effective device width to 0.25 px (the
 *       Adobe Reader / reference-renderer convention).</li>
 * </ul>
 */
public class HairlineStrokeRenderTest {

    /** Builds a minimal one-page PDF whose content stream is {@code content}. */
    private static byte[] minimalPdf(String content) {
        StringBuilder body = new StringBuilder("%PDF-1.4\n");
        String[] objs = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 300] /Contents 4 0 R >>\nendobj\n",
                "4 0 obj\n<< /Length " + content.length() + " >>\nstream\n" + content + "\nendstream\nendobj\n"
        };
        int[] offsets = new int[objs.length];
        for (int i = 0; i < objs.length; i++) {
            offsets[i] = body.length();
            body.append(objs[i]);
        }
        int xrefPos = body.length();
        body.append("xref\n0 ").append(objs.length + 1).append("\n0000000000 65535 f \n");
        for (int off : offsets) {
            body.append(String.format("%010d 00000 n \n", off));
        }
        body.append("trailer\n<< /Size ").append(objs.length + 1)
            .append(" /Root 1 0 R >>\nstartxref\n").append(xrefPos).append("\n%%EOF");
        return body.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /** Renders page 1 at 72 dpi and returns the max count of non-white pixels in any row. */
    private static int widestInkRow(String content) throws Exception {
        try (Document doc = new Document(new ByteArrayInputStream(minimalPdf(content)))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            int widest = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                int ink = 0;
                for (int x = 0; x < img.getWidth(); x++) {
                    int rgb = img.getRGB(x, y);
                    int lum = ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3;
                    if (lum < 250) ink++;
                }
                widest = Math.max(widest, ink);
            }
            return widest;
        }
    }

    /** A horizontal 1-width line must not be dropped by the empty-bounds guard. */
    @Test
    public void horizontalLineWithNormalWidthIsStroked() throws Exception {
        assertTrue(widestInkRow("0 0 0 RG 1 w 50 150 m 250 150 l S") >= 190,
                "horizontal line (zero-height bounds) must be stroked");
    }

    /** A vertical 1-width line must not be dropped by the empty-bounds guard. */
    @Test
    public void verticalLineWithNormalWidthIsStroked() throws Exception {
        // Widest row sees only ~1-2 px of a vertical line; just require ink.
        assertTrue(widestInkRow("0 0 0 RG 1 w 150 50 m 150 250 l S") >= 1,
                "vertical line (zero-width bounds) must be stroked");
    }

    /** Zero-width (`0 w`) = thinnest device line, NOT invisible (ISO 32000 8.4.3.2). */
    @Test
    public void zeroWidthHairlineIsVisible() throws Exception {
        assertTrue(widestInkRow("0 0 0 RG 0 w 50 150 m 250 150 l S") >= 190,
                "0-width hairline must render as the thinnest visible line");
    }

    /**
     * The corpus-29903 shape: a down-scaling CTM (0.05) with a 0-width rule
     * inside a clip. The stroke must survive both the clip and the scale.
     */
    @Test
    public void hairlineUnderDownScalingCtmAndClipIsVisible() throws Exception {
        String cs = "q 0.05 0 0 0.05 0 0 cm"
                + " 400 400 5200 5200 re W n"
                + " 0 0 0 RG 0 w 1000 3000 m 5000 3000 l S Q";
        assertTrue(widestInkRow(cs) >= 190,
                "hairline under a 0.05 down-scaling CTM must stay visible");
    }
}
