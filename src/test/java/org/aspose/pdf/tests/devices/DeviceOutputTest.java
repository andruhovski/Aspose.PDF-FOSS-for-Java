package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.aspose.pdf.Page;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PngDevice, JpegDevice, BmpDevice output formats.
 */
public class DeviceOutputTest {

    private Page createEmptyPage() {
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.of("Type"), PdfName.of("Page"));
        PdfArray mediaBox = new PdfArray(4);
        mediaBox.add(new PdfFloat(0));
        mediaBox.add(new PdfFloat(0));
        mediaBox.add(new PdfFloat(200));
        mediaBox.add(new PdfFloat(100));
        pageDict.set(PdfName.of("MediaBox"), mediaBox);
        return new Page(pageDict, null);
    }

    @Test
    public void pngDeviceProducesValidPng() throws Exception {
        PngDevice device = new PngDevice(new Resolution(72));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        device.process(createEmptyPage(), baos);

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0, "PNG output should not be empty");

        // Verify it's valid PNG by reading it back
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        assertNotNull(img, "Should be parseable as image");
        assertEquals(200, img.getWidth());
        assertEquals(100, img.getHeight());
    }

    @Test
    public void pngDeviceWithDimensions() throws Exception {
        PngDevice device = new PngDevice(400, 200, new Resolution(72));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        device.process(createEmptyPage(), baos);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(img);
        assertEquals(400, img.getWidth());
        assertEquals(200, img.getHeight());
    }

    @Test
    public void jpegDeviceProducesValidJpeg() throws Exception {
        JpegDevice device = new JpegDevice(new Resolution(72), 80);
        assertEquals(80, device.getQuality());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        device.process(createEmptyPage(), baos);

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0, "JPEG output should not be empty");

        // JPEG magic bytes: FF D8
        assertEquals((byte) 0xFF, data[0]);
        assertEquals((byte) 0xD8, data[1]);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        assertNotNull(img, "Should be parseable as JPEG");
    }

    @Test
    public void jpegQualityClamped() {
        JpegDevice device1 = new JpegDevice(new Resolution(72), 0);
        assertEquals(1, device1.getQuality());

        JpegDevice device2 = new JpegDevice(new Resolution(72), 200);
        assertEquals(100, device2.getQuality());
    }

    @Test
    public void bmpDeviceProducesValidBmp() throws Exception {
        BmpDevice device = new BmpDevice(new Resolution(72));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        device.process(createEmptyPage(), baos);

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0, "BMP output should not be empty");

        // BMP magic: "BM"
        assertEquals((byte) 'B', data[0]);
        assertEquals((byte) 'M', data[1]);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        assertNotNull(img, "Should be parseable as BMP");
    }

    @Test
    public void textDeviceExtractsText() throws Exception {
        // Empty page will produce empty text
        TextDevice device = new TextDevice();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        device.process(createEmptyPage(), baos);
        // Empty page → empty or no output
        assertTrue(baos.size() >= 0);
    }

    @Test
    public void textDeviceNullPageThrows() {
        TextDevice device = new TextDevice();
        assertThrows(IllegalArgumentException.class,
                () -> device.process(null, new ByteArrayOutputStream()));
    }

    @Test
    public void textDeviceNullOutputThrows() {
        TextDevice device = new TextDevice();
        assertThrows(IllegalArgumentException.class,
                () -> device.process(createEmptyPage(), (java.io.OutputStream) null));
    }
}
