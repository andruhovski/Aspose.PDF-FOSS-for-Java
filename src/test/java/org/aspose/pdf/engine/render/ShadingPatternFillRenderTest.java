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

/// Renderer regression test: a fill with a shading Pattern (PatternType 2,
/// §8.7.4.3) must paint the gradient inside the path, not fall back to a solid
/// colour. The renderer previously handled only tiling patterns (PatternType 1)
/// and dropped shading-pattern fills to the current fill colour — black for the
/// gradient-built reaction emoji of corpus 59149.
public class ShadingPatternFillRenderTest {

    /// A page that fills the rect [50,50 100x60] with a red→blue axial shading pattern.
    private static byte[] pdfWithShadingPattern() {
        String content = "/Pattern cs /P1 scn 50 50 100 60 re f";
        String pattern =
                "<< /Type /Pattern /PatternType 2 /Matrix [1 0 0 1 0 0] "
              + "/Shading << /ShadingType 2 /ColorSpace /DeviceRGB /Coords [50 0 150 0] "
              + "/Extend [true true] "
              + "/Function << /FunctionType 2 /Domain [0 1] /C0 [1 0 0] /C1 [0 0 1] /N 1 >> >> >>";
        StringBuilder body = new StringBuilder("%PDF-1.4\n");
        String[] objs = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 160] "
                        + "/Resources << /Pattern << /P1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n",
                "4 0 obj\n<< /Length " + content.length() + " >>\nstream\n"
                        + content + "\nendstream\nendobj\n",
                "5 0 obj\n" + pattern + "\nendobj\n"
        };
        int[] offsets = new int[objs.length];
        for (int i = 0; i < objs.length; i++) {
            offsets[i] = body.length();
            body.append(objs[i]);
        }
        int xrefPos = body.length();
        body.append("xref\n0 ").append(objs.length + 1).append("\n0000000000 65535 f \n");
        for (int off : offsets) body.append(String.format("%010d 00000 n \n", off));
        body.append("trailer\n<< /Size ").append(objs.length + 1)
            .append(" /Root 1 0 R >>\nstartxref\n").append(xrefPos).append("\n%%EOF");
        return body.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /// Average [r,g,b] over a PDF-space box (origin bottom-left).
    private static int[] avg(BufferedImage img, int x0, int y0pdf, int w, int h) {
        long r = 0, g = 0, b = 0, n = 0;
        int y0 = img.getHeight() - y0pdf - h;
        for (int y = Math.max(0, y0); y < Math.min(img.getHeight(), y0 + h); y++) {
            for (int x = Math.max(0, x0); x < Math.min(img.getWidth(), x0 + w); x++) {
                int rgb = img.getRGB(x, y);
                r += rgb >> 16 & 0xFF; g += rgb >> 8 & 0xFF; b += rgb & 0xFF; n++;
            }
        }
        if (n == 0) return new int[]{0, 0, 0};
        return new int[]{(int) (r / n), (int) (g / n), (int) (b / n)};
    }

    @Test
    public void shadingPatternFillPaintsGradientNotSolid() throws Exception {
        try (Document doc = new Document(new ByteArrayInputStream(pdfWithShadingPattern()))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));

            int[] left = avg(img, 55, 70, 10, 20);   // near x=55 → red end
            int[] right = avg(img, 135, 70, 10, 20);  // near x=145 → blue end

            // Left is red-dominant, right is blue-dominant — i.e. an actual
            // gradient, not a single flat colour (and definitely not black).
            assertTrue(left[0] > 150 && left[0] > left[2] + 60,
                    "left of shading-pattern fill must be red, was " + java.util.Arrays.toString(left));
            assertTrue(right[2] > 150 && right[2] > right[0] + 60,
                    "right of shading-pattern fill must be blue, was " + java.util.Arrays.toString(right));
        }
    }
}
