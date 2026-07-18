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

/// Renderer regression test: graphics-state restore (Q) inside a Form XObject.
///
/// `renderForm` used to ignore the state returned by
/// `processOperator`, so a `Q` inside a form never restored the
/// state. Successive `q <rect> re W n ... Q` blocks (llPDFLib draws every
/// table cell that way — corpus 1493.pdf) intersected their clip rectangles
/// until the clip became empty and everything after the first block vanished:
/// tables rendered as a lone title on a blank page.
public class FormXObjectClipRestoreRenderTest {

    /// Builds a one-page PDF whose page content is `q /F1 Do Q` followed
    /// by `pageTail`; the Form XObject's content stream is `form`.
    private static byte[] pdfWithForm(String form, String pageTail) {
        String pageContent = "q /F1 Do Q\n" + pageTail;
        StringBuilder body = new StringBuilder("%PDF-1.4\n");
        String[] objs = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 300] "
                        + "/Resources << /XObject << /F1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n",
                "4 0 obj\n<< /Length " + pageContent.length() + " >>\nstream\n"
                        + pageContent + "\nendstream\nendobj\n",
                "5 0 obj\n<< /Type /XObject /Subtype /Form /BBox [0 0 300 300] /Length "
                        + form.length() + " >>\nstream\n" + form + "\nendstream\nendobj\n"
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

    /// Renders page 1 at 72 dpi (300x300 px).
    private static BufferedImage render(String form, String pageTail) throws Exception {
        try (Document doc = new Document(new ByteArrayInputStream(pdfWithForm(form, pageTail)))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            return ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
        }
    }

    /// Counts dark pixels inside the device-space rectangle (PDF y-up -> image y-down).
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

    /// Two successive clipped cell blocks inside one form: the second block's
    /// content must survive (its clip must NOT be intersected with the first's).
    @Test
    public void secondClippedBlockInsideFormIsPainted() throws Exception {
        String form =
                "q 10 200 100 50 re W n 0 0 0 rg 10 200 100 50 re f Q\n" +   // cell 1 (top)
                "q 10 100 100 50 re W n 0 0 0 rg 10 100 100 50 re f Q\n";    // cell 2 (disjoint)
        BufferedImage img = render(form, "");
        assertTrue(inkIn(img, 15, 205, 90, 40) > 100,
                "first clipped block must be painted");
        assertTrue(inkIn(img, 15, 105, 90, 40) > 100,
                "second clipped block must be painted - Q inside the form must restore the clip");
    }

    /// A form that leaves a narrow clip behind (q W n without closing Q) must
    /// not clip away page content drawn after the Do.
    @Test
    public void pageContentAfterFormIsNotClippedByLeftoverFormClip() throws Exception {
        String form = "q 0 0 1 1 re W n";                                    // leaves a 1x1 clip
        String tail = "0 0 0 rg 150 150 50 50 re f\n";                       // page-level square
        BufferedImage img = render(form, tail);
        assertTrue(inkIn(img, 155, 155, 40, 40) > 100,
                "page content after Do must not inherit the form's leftover clip");
    }
}
