package org.aspose.pdf.tests;

import org.aspose.pdf.XImage;
import org.aspose.pdf.XImageCollection;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XImageCollection}.
 */
public class XImageCollectionTest {

    private PdfDictionary createXObjectDict() {
        PdfDictionary xobjects = new PdfDictionary();

        // Image XObject
        PdfStream img1 = new PdfStream();
        img1.set(PdfName.of("Subtype"), PdfName.of("Image"));
        img1.set(PdfName.of("Width"), PdfInteger.valueOf(100));
        img1.set(PdfName.of("Height"), PdfInteger.valueOf(50));
        xobjects.set(PdfName.of("Im1"), img1);

        // Another image
        PdfStream img2 = new PdfStream();
        img2.set(PdfName.of("Subtype"), PdfName.of("Image"));
        img2.set(PdfName.of("Width"), PdfInteger.valueOf(200));
        img2.set(PdfName.of("Height"), PdfInteger.valueOf(100));
        xobjects.set(PdfName.of("Im2"), img2);

        // Form XObject (should NOT be included in images)
        PdfStream form = new PdfStream();
        form.set(PdfName.of("Subtype"), PdfName.of("Form"));
        xobjects.set(PdfName.of("Fm1"), form);

        return xobjects;
    }

    @Test
    public void testCountImages() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        assertEquals(2, coll.getCount());
    }

    @Test
    public void testSizeAlias() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        assertEquals(coll.getCount(), coll.size());
    }

    @Test
    public void testGetByIndex() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        XImage img1 = coll.get(1);
        assertNotNull(img1);
        assertTrue(img1.getWidth() > 0);
    }

    @Test
    public void testGetByName() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        XImage img = coll.get("Im1");
        assertNotNull(img);
        assertEquals("Im1", img.getName());
    }

    @Test
    public void testGetByNameNotFound() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        assertNull(coll.get("NonExistent"));
    }

    @Test
    public void testOneBasedIndexing() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(3));
    }

    @Test
    public void testIteration() {
        XImageCollection coll = new XImageCollection(createXObjectDict(), null);
        int count = 0;
        for (XImage img : coll) {
            assertNotNull(img);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testEmptyCollection() {
        XImageCollection coll = new XImageCollection(new PdfDictionary(), null);
        assertEquals(0, coll.getCount());
    }

    @Test
    public void testNullDict() {
        XImageCollection coll = new XImageCollection(null, null);
        assertEquals(0, coll.getCount());
    }

    @Test
    public void testFormXObjectNotIncluded() {
        // Verify that Form XObjects are filtered out
        PdfDictionary xobjects = createXObjectDict();
        XImageCollection coll = new XImageCollection(xobjects, null);
        // Dict has 3 entries (Im1, Im2, Fm1) but only 2 images
        assertEquals(2, coll.getCount());
        for (XImage img : coll) {
            assertNotEquals("Fm1", img.getName());
        }
    }

    @Test
    public void testDeleteByIndexRemovesImage() {
        PdfDictionary xobjects = createXObjectDict();
        XImageCollection coll = new XImageCollection(xobjects, null);

        coll.delete(2);

        assertEquals(1, coll.getCount());
        assertNull(coll.get("Im2"));
        assertEquals(2, xobjects.keySet().size());
    }

    @Test
    public void twoArgCtor_logsCorrectImageCount() throws Exception {
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger("org.aspose.pdf.XImageCollection");
        java.util.logging.Level original = logger.getLevel();
        logger.setLevel(java.util.logging.Level.FINE);
        java.util.List<String> messages = new java.util.ArrayList<>();
        java.util.logging.Handler handler = new java.util.logging.Handler() {
            public void publish(java.util.logging.LogRecord r) { messages.add(r.getMessage()); }
            public void flush() {}
            public void close() {}
        };
        handler.setLevel(java.util.logging.Level.FINE);
        logger.addHandler(handler);
        try {
            XImageCollection coll = new XImageCollection(createXObjectDict(), null);
            coll.getCount();
            boolean sawCount2 = messages.stream().anyMatch(m -> m != null && m.contains("XImageCollection loaded: 2 images"));
            assertTrue(sawCount2, "Expected log message indicating 2 images loaded; got: " + messages);
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(original);
        }
    }

    @Test
    public void testReplaceByIndexUpdatesImageMetadata() throws Exception {
        PdfDictionary xobjects = createXObjectDict();
        XImageCollection coll = new XImageCollection(xobjects, null);

        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", jpeg);

        coll.replace(2, new ByteArrayInputStream(jpeg.toByteArray()));

        XImage replaced = coll.get(2);
        assertEquals(4, replaced.getWidth());
        assertEquals(3, replaced.getHeight());
        assertArrayEquals(jpeg.toByteArray(), replaced.getPdfStream().getEncodedData());
        assertEquals("DCTDecode", ((PdfName) replaced.getPdfStream().get(PdfName.of("Filter"))).getName());
    }
}
