package org.aspose.pdf.tests;

import org.aspose.pdf.XImage;
import org.aspose.pdf.engine.colorspace.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XImage}.
 */
public class XImageTest {

    @Test
    public void testBasicProperties() {
        PdfStream stream = new PdfStream();
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(10));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(20));
        stream.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(8));

        XImage img = new XImage(stream, "Im1", null);
        assertEquals(10, img.getWidth());
        assertEquals(20, img.getHeight());
        assertEquals(8, img.getBitsPerComponent());
        assertEquals("Im1", img.getName());
    }

    @Test
    public void testDefaultColorSpace() throws IOException {
        PdfStream stream = new PdfStream();
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(1));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(1));

        XImage img = new XImage(stream, "Im1", null);
        ColorSpaceBase cs = img.getColorSpace();
        assertSame(DeviceRGB.INSTANCE, cs);
    }

    @Test
    public void testExplicitColorSpace() throws IOException {
        PdfStream stream = new PdfStream();
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(1));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(1));
        stream.set(PdfName.of("ColorSpace"), PdfName.of("DeviceGray"));

        XImage img = new XImage(stream, "Im1", null);
        ColorSpaceBase cs = img.getColorSpace();
        assertSame(DeviceGray.INSTANCE, cs);
    }

    @Test
    public void testIsImageMask() {
        PdfStream stream = new PdfStream();
        stream.set(PdfName.of("ImageMask"), PdfBoolean.TRUE);
        XImage img = new XImage(stream, "Im1", null);
        assertTrue(img.isImageMask());
    }

    @Test
    public void testIsNotImageMask() {
        PdfStream stream = new PdfStream();
        XImage img = new XImage(stream, "Im1", null);
        assertFalse(img.isImageMask());
    }

    @Test
    public void testToBufferedImageRGB() throws IOException {
        int w = 2, h = 2;
        // 2x2 RGB image: red, green, blue, white
        byte[] pixels = new byte[]{
            (byte) 255, 0, 0,           // red
            0, (byte) 255, 0,           // green
            0, 0, (byte) 255,           // blue
            (byte) 255, (byte) 255, (byte) 255  // white
        };
        PdfStream stream = new PdfStream(pixels);
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(w));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(h));
        stream.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(8));
        stream.set(PdfName.of("ColorSpace"), PdfName.of("DeviceRGB"));

        XImage img = new XImage(stream, "Im1", null);
        BufferedImage bi = img.toBufferedImage();
        assertNotNull(bi);
        assertEquals(w, bi.getWidth());
        assertEquals(h, bi.getHeight());
        // Check top-left pixel is red
        int rgb = bi.getRGB(0, 0);
        assertEquals(0xFFFF0000, rgb);
    }

    @Test
    public void testToBufferedImageGray() throws IOException {
        int w = 2, h = 2;
        byte[] pixels = new byte[]{0, (byte) 128, (byte) 255, 64};
        PdfStream stream = new PdfStream(pixels);
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(w));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(h));
        stream.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(8));
        stream.set(PdfName.of("ColorSpace"), PdfName.of("DeviceGray"));

        XImage img = new XImage(stream, "Im1", null);
        BufferedImage bi = img.toBufferedImage();
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, bi.getType());
        assertEquals(w, bi.getWidth());
        assertEquals(h, bi.getHeight());
    }

    @Test
    public void testSaveAsJPEG() throws IOException {
        // Create a stream with DCTDecode filter
        byte[] fakeJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        PdfStream stream = new PdfStream(fakeJpeg);
        stream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        stream.set(PdfName.of("Width"), PdfInteger.valueOf(1));
        stream.set(PdfName.of("Height"), PdfInteger.valueOf(1));
        stream.set(PdfName.of("Filter"), PdfName.of("DCTDecode"));

        XImage img = new XImage(stream, "Im1", null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.save(baos);
        byte[] saved = baos.toByteArray();
        // Should be the raw JPEG bytes
        assertEquals(fakeJpeg.length, saved.length);
        assertEquals((byte) 0xFF, saved[0]);
        assertEquals((byte) 0xD8, saved[1]);
    }

    @Test
    public void testGetPdfStream() {
        PdfStream stream = new PdfStream();
        XImage img = new XImage(stream, "test", null);
        assertSame(stream, img.getPdfStream());
    }
}
