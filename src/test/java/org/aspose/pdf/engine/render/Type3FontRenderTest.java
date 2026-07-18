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

/// Renderer regression tests for Type 3 fonts (ISO 32000-1:2008, §9.6.5).
///
/// Type 3 glyphs are content streams in /CharProcs; before this support the
/// renderer substituted a JDK system font (corpus 30506.pdf: embedded monospace
/// bitmap font replaced by proportional Helvetica → layout broken). Two glyph
/// flavours are covered: a vector glyph (filled rectangle) and a 1-bit
/// inline-image stencil mask — the dominant style of scan-line PDF generators
/// (each EM12B glyph in 30506.pdf is `d1 ... cm BI .. ID <bits> EI`).
public class Type3FontRenderTest {

    /// Builds a one-page PDF using a Type 3 font /F3 with a single glyph "A"
    /// (code 65) whose CharProc is `charProc`. FontMatrix is 1/10 — a
    /// 10x10 glyph space box maps to one em. Page content shows "AA" at size
    /// 50 at (100, 100).
    private static byte[] pdfWithType3(String charProc, String widths) {
        String pageContent = "BT /F3 50 Tf 100 100 Td (AA) Tj ET";
        StringBuilder body = new StringBuilder("%PDF-1.4\n");
        String[] objs = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 300] "
                        + "/Resources << /Font << /F3 5 0 R >> >> /Contents 4 0 R >>\nendobj\n",
                "4 0 obj\n<< /Length " + pageContent.length() + " >>\nstream\n"
                        + pageContent + "\nendstream\nendobj\n",
                "5 0 obj\n<< /Type /Font /Subtype /Type3 /FontMatrix [0.1 0 0 0.1 0 0] "
                        + "/FontBBox [0 0 10 10] /FirstChar 65 /LastChar 65 /Widths [" + widths + "] "
                        + "/Encoding << /Type /Encoding /Differences [65 /glyphA] >> "
                        + "/CharProcs << /glyphA 6 0 R >> >>\nendobj\n",
                "6 0 obj\n<< /Length " + charProc.length() + " >>\nstream\n"
                        + charProc + "\nendstream\nendobj\n"
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
    private static BufferedImage render(String charProc, String widths) throws Exception {
        try (Document doc = new Document(new ByteArrayInputStream(pdfWithType3(charProc, widths)))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            return ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
        }
    }

    /// Counts dark pixels inside the device-space rect (PDF y-up → image y-down).
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

    /// A vector CharProc (filled square covering the full 10x10 glyph box,
    /// i.e. one 50x50 em at the page) must paint at the text position, and the
    /// second glyph must land one advance (/Widths 10 × FontMatrix 0.1 × 50pt
    /// = 50pt) to the right.
    @Test
    public void vectorGlyphIsPaintedAtTextPositionWithAdvance() throws Exception {
        String proc = "10 0 0 0 10 10 d1 0 0 10 10 re f";
        BufferedImage img = render(proc, "10");
        // First glyph: 50x50 box at (100,100)
        assertTrue(inkIn(img, 110, 110, 30, 30) > 500,
                "first Type3 vector glyph must be painted");
        // Second glyph: advanced by 50pt → (150,100)
        assertTrue(inkIn(img, 160, 110, 30, 30) > 500,
                "second Type3 glyph must land one advance to the right");
        // Beyond the second glyph: blank
        assertTrue(inkIn(img, 210, 110, 30, 30) == 0,
                "no ink expected past the last glyph");
    }

    /// The 30506.pdf style: the glyph is a 1-bit inline-image stencil mask
    /// (`d1 ... cm BI /W /H /BPC 1 /IM true ID <all-ones> EI`). With
    /// /D [1 0] every 1-bit paints in the current fill colour.
    @Test
    public void inlineImageMaskGlyphIsPainted() throws Exception {
        // 8x8 mask, every bit set; D[1 0] → bit 1 = paint
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < 8; i++) bits.append((char) 0xFF);
        String proc = "10 0 0 0 10 10 d1 10 0 0 10 0 0 cm "
                + "BI /W 8 /H 8 /BPC 1 /D [1 0] /IM true ID " + bits + " EI";
        BufferedImage img = render(proc, "10");
        assertTrue(inkIn(img, 110, 110, 30, 30) > 500,
                "Type3 inline-image-mask glyph must be painted");
        assertTrue(inkIn(img, 160, 110, 30, 30) > 500,
                "second mask glyph must follow the advance");
    }

    /// Text rendering mode 3 (invisible) must skip the glyph painting but keep
    /// advancing — regression guard for the Tr handling in the Type3 path.
    @Test
    public void invisibleRenderingModeSkipsGlyphs() throws Exception {
        String proc = "10 0 0 0 10 10 d1 0 0 10 10 re f";
        // Same PDF but with Tr 3 — patch the content stream
        byte[] pdf = pdfWithType3(proc, "10");
        String s = new String(pdf, StandardCharsets.ISO_8859_1)
                .replace("BT /F3 50 Tf", "BT 3 Tr /F3 50 Tf");
        // /Length of the content object is now stale by 5 bytes, but the
        // parser tolerates it (endstream re-sync) — acceptable for a test.
        try (Document doc = new Document(new ByteArrayInputStream(
                s.getBytes(StandardCharsets.ISO_8859_1)))) {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            new PngDevice(new Resolution(72)).process(doc.getPages().get(1), png);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            assertTrue(inkIn(img, 100, 100, 120, 60) == 0,
                    "Tr 3 must render no Type3 glyph ink");
        }
    }
}
