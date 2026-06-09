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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renderer regression test: a Tiling Pattern fill must stay clipped to the
 * filled path even when the pattern cell's content contains q/Q pairs.
 *
 * <p>The cell runs with a fresh GraphicsState whose clip used to be null —
 * the first Q inside the cell then called applyClip(null) and erased the
 * path clip, splattering the cell content across the whole page (corpus
 * 16222.pdf: a 123pt photo tile painted 20× over the article text).</p>
 */
public class TilingPatternClipRenderTest {

    private static byte[] pdfWithPattern() {
        // Pattern cell: q/Q pair first (the clip killer), then a big black
        // square — far larger than the filled path.
        String cell = "q Q 0 0 0 rg 0 0 200 200 re f";
        String content = "/Pattern cs /P1 scn 50 50 40 40 re f";
        StringBuilder body = new StringBuilder("%PDF-1.4\n");
        String[] objs = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 300] "
                        + "/Resources << /Pattern << /P1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n",
                "4 0 obj\n<< /Length " + content.length() + " >>\nstream\n"
                        + content + "\nendstream\nendobj\n",
                "5 0 obj\n<< /Type /Pattern /PatternType 1 /PaintType 1 /TilingType 1 "
                        + "/BBox [0 0 200 200] /XStep 200 /YStep 200 /Resources << >> /Length "
                        + cell.length() + " >>\nstream\n" + cell + "\nendstream\nendobj\n"
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

    private static int inkIn(BufferedImage img, int x0, int y0pdf, int w, int h) {
        int ink = 0;
        int y0 = img.getHeight() - y0pdf - h;
        for (int y = Math.max(0, y0); y < Math.min(img.getHeight(), y0 + h); y++) {
            for (int x = Math.max(0, x0); x < Math.min(img.getWidth(), x0 + w); x++) {
                int rgb = img.getRGB(x, y);
                int lum = ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3;
                if (lum < 128) ink++;
            }
        }
        return ink;
    }

    @Test
    public void patternCellWithQQStaysInsideFilledPath() throws Exception {
        try (Document doc = new Document(new ByteArrayInputStream(pdfWithPattern()))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            assertTrue(inkIn(img, 52, 52, 36, 36) > 800,
                    "pattern must paint inside the filled rect");
            assertEquals(0, inkIn(img, 120, 120, 150, 150),
                    "pattern cell (with q/Q) must NOT leak outside the filled rect");
            assertEquals(0, inkIn(img, 0, 120, 40, 150),
                    "no leakage left of the rect either");
        }
    }
}
