package org.aspose.pdf.tests;

import org.aspose.pdf.XImage;
import org.aspose.pdf.engine.colorspace.*;
import org.aspose.pdf.engine.cos.*;
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
        COSStream stream = new COSStream();
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(10));
        stream.set(COSName.of("Height"), COSInteger.valueOf(20));
        stream.set(COSName.of("BitsPerComponent"), COSInteger.valueOf(8));

        XImage img = new XImage(stream, "Im1", null);
        assertEquals(10, img.getWidth());
        assertEquals(20, img.getHeight());
        assertEquals(8, img.getBitsPerComponent());
        assertEquals("Im1", img.getName());
    }

    @Test
    public void testDefaultColorSpace() throws IOException {
        COSStream stream = new COSStream();
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(1));
        stream.set(COSName.of("Height"), COSInteger.valueOf(1));

        XImage img = new XImage(stream, "Im1", null);
        ColorSpaceBase cs = img.getColorSpace();
        assertSame(DeviceRGB.INSTANCE, cs);
    }

    @Test
    public void testExplicitColorSpace() throws IOException {
        COSStream stream = new COSStream();
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(1));
        stream.set(COSName.of("Height"), COSInteger.valueOf(1));
        stream.set(COSName.of("ColorSpace"), COSName.of("DeviceGray"));

        XImage img = new XImage(stream, "Im1", null);
        ColorSpaceBase cs = img.getColorSpace();
        assertSame(DeviceGray.INSTANCE, cs);
    }

    @Test
    public void testIsImageMask() {
        COSStream stream = new COSStream();
        stream.set(COSName.of("ImageMask"), COSBoolean.TRUE);
        XImage img = new XImage(stream, "Im1", null);
        assertTrue(img.isImageMask());
    }

    @Test
    public void testIsNotImageMask() {
        COSStream stream = new COSStream();
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
        COSStream stream = new COSStream(pixels);
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(w));
        stream.set(COSName.of("Height"), COSInteger.valueOf(h));
        stream.set(COSName.of("BitsPerComponent"), COSInteger.valueOf(8));
        stream.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

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
        COSStream stream = new COSStream(pixels);
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(w));
        stream.set(COSName.of("Height"), COSInteger.valueOf(h));
        stream.set(COSName.of("BitsPerComponent"), COSInteger.valueOf(8));
        stream.set(COSName.of("ColorSpace"), COSName.of("DeviceGray"));

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
        COSStream stream = new COSStream(fakeJpeg);
        stream.set(COSName.of("Subtype"), COSName.of("Image"));
        stream.set(COSName.of("Width"), COSInteger.valueOf(1));
        stream.set(COSName.of("Height"), COSInteger.valueOf(1));
        stream.set(COSName.of("Filter"), COSName.of("DCTDecode"));

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
    public void testGetCOSStream() {
        COSStream stream = new COSStream();
        XImage img = new XImage(stream, "test", null);
        assertSame(stream, img.getCOSStream());
    }
}
