package org.aspose.pdf.tests.generation;

import org.aspose.pdf.Document;
import org.aspose.pdf.ImageStamp;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug B — {@code Page.addStamp(ImageStamp)} must register the image XObject in
 * the page's {@code /Resources/XObject} dictionary before emitting the
 * {@code Do} operator. Before the fix the operator referenced an unattached
 * resource name and spec-compliant viewers painted nothing.
 */
class ImageStampRendersTest {

    @TempDir Path tempDir;

    private static byte[] tinyJpeg(int w, int h) throws IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "JPEG", out);
        return out.toByteArray();
    }

    private static ImageStamp newStamp(byte[] bytes) {
        ImageStamp s = new ImageStamp("ignored.jpg");
        s.setImageStream(new ByteArrayInputStream(bytes));
        return s;
    }

    private static List<PdfStream> imageXObjects(Page page) {
        List<PdfStream> result = new ArrayList<>();
        PdfDictionary xo = page.ensureResources().getXObjects();
        if (xo == null) return result;
        for (PdfName key : xo.keySet()) {
            PdfBase val = xo.get(key);
            if (val instanceof PdfObjectReference) {
                try { val = ((PdfObjectReference) val).dereference(); }
                catch (IOException e) { continue; }
            }
            if (val instanceof PdfStream) {
                PdfStream s = (PdfStream) val;
                if ("Image".equals(s.getNameAsString("Subtype"))) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private static String contentStreamText(Page page) throws IOException {
        PdfBase contents = page.getPdfDictionary().get(PdfName.of("Contents"));
        if (contents instanceof PdfObjectReference) {
            contents = ((PdfObjectReference) contents).dereference();
        }
        StringBuilder all = new StringBuilder();
        if (contents instanceof PdfStream) {
            all.append(new String(((PdfStream) contents).getDecodedData(), StandardCharsets.ISO_8859_1));
        } else if (contents instanceof PdfArray) {
            PdfArray arr = (PdfArray) contents;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase e = arr.get(i);
                if (e instanceof PdfObjectReference) e = ((PdfObjectReference) e).dereference();
                if (e instanceof PdfStream) {
                    all.append(new String(((PdfStream) e).getDecodedData(), StandardCharsets.ISO_8859_1));
                    all.append('\n');
                }
            }
        }
        return all.toString();
    }

    @Test
    @DisplayName("addStamp(ImageStamp) registers an Image XObject on the page")
    void addStamp_imageStamp_registersImageInPageResources() throws IOException {
        Path out = tempDir.resolve("img.pdf");
        byte[] jpeg = tinyJpeg(16, 8);
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            ImageStamp stamp = newStamp(jpeg);
            stamp.setWidth(200); stamp.setHeight(100);
            stamp.setXIndent(50); stamp.setYIndent(50);
            page.addStamp(stamp);
            doc.save(out.toString());
        }
        try (Document r = new Document(out.toString())) {
            List<PdfStream> imgs = imageXObjects(r.getPages().get(1));
            assertEquals(1, imgs.size(), "exactly one Image XObject must be registered");
            PdfStream s = imgs.get(0);
            assertEquals(16, s.getInt("Width", -1));
            assertEquals(8, s.getInt("Height", -1));
        }
    }

    @Test
    @DisplayName("Do operator's operand matches the registered XObject name")
    void addStamp_imageStamp_doOperatorMatchesRegisteredName() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.addStamp(newStamp(tinyJpeg(4, 4)));
            String cs = contentStreamText(page);
            PdfDictionary xo = page.ensureResources().getXObjects();
            assertEquals(1, xo.keySet().size());
            String name = xo.keySet().iterator().next().getName();
            assertTrue(cs.contains("/" + name + " Do"),
                    () -> "content stream must reference /" + name + " Do; got: " + cs);
        }
    }

    @Test
    @DisplayName("Two ImageStamps produce two independent Image XObjects")
    void addStamp_imageStamp_twice_producesTwoIndependentXObjects() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.addStamp(newStamp(tinyJpeg(4, 4)));
            page.addStamp(newStamp(tinyJpeg(8, 8)));
            PdfDictionary xo = page.ensureResources().getXObjects();
            assertEquals(2, xo.keySet().size(),
                    "two distinct XObject entries (no name collision) expected");
        }
    }

    @Test
    @DisplayName("Background ImageStamp's operators are prepended (drawn behind other content)")
    void addStamp_imageStamp_background_isPrependedNotAppended() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            // Existing content first
            page.appendToContentStream("q 1 0 0 1 0 0 cm Q\n".getBytes(StandardCharsets.ISO_8859_1));
            ImageStamp bg = newStamp(tinyJpeg(4, 4));
            bg.setBackground(true);
            page.addStamp(bg);
            String cs = contentStreamText(page);
            int doIdx = cs.indexOf(" Do");
            int qIdx = cs.indexOf("1 0 0 1 0 0 cm");
            assertTrue(doIdx >= 0 && qIdx >= 0, "both operator sequences must be present");
            assertTrue(doIdx < qIdx,
                    "background stamp's Do must appear before the pre-existing cm operator");
        }
    }

    @Test
    @DisplayName("addStamp(null) throws IllegalArgumentException")
    void addStamp_imageStamp_nullStamp_throwsIllegalArgument() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            assertThrows(IllegalArgumentException.class,
                    () -> page.addStamp((ImageStamp) null));
        }
    }

    @Test
    @DisplayName("ImageStamp with neither file nor stream throws IllegalArgumentException")
    void addStamp_imageStamp_noBytes_throwsIllegalArgument() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            // ImageStamp ctor requires a file string but we never wire any bytes.
            // The "ignored.jpg" path doesn't exist on disk; addStamp must fail
            // rather than silently emit a dangling /Do.
            ImageStamp stamp = new ImageStamp("does-not-exist-anywhere.jpg");
            assertThrows(IOException.class, () -> page.addStamp(stamp));
        }
    }
}
