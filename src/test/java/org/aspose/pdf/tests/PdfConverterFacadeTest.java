package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.PageCoordinateType;
import org.aspose.pdf.PageSize;
import org.aspose.pdf.devices.CompressionType;
import org.aspose.pdf.devices.Resolution;
import org.aspose.pdf.devices.TiffSettings;
import org.aspose.pdf.facades.ImageFormat;
import org.aspose.pdf.facades.PdfConverter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Facade-level tests for {@link PdfConverter} — Step 1: core iteration +
 * {@link ImageFormat} overloads.
 */
public class PdfConverterFacadeTest {

    @TempDir
    Path tempDir;

    /** Build a 3-page PDF on disk and return its path. */
    private Path buildThreePagePdf(Path dir) throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getPages().add();
        doc.getPages().add();
        Path pdf = dir.resolve("three-pages.pdf");
        doc.save(pdf.toString());
        doc.close();
        return pdf;
    }

    @Test
    public void bindPdfAndIteratePng_producesNonEmptyFilePerPage() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        assertTrue(converter.bindPdf(pdf.toString()));

        int produced = 0;
        while (converter.hasNextImage()) {
            produced++;
            Path out = tempDir.resolve("page-" + produced + ".png");
            assertTrue(converter.getNextImage(out.toString(), ImageFormat.PNG),
                    "getNextImage should return true");
            assertTrue(Files.exists(out), "PNG file must be created");
            assertTrue(Files.size(out) > 0, "PNG file must be non-empty");
            assertPngSignature(out);
        }
        converter.close();

        assertEquals(3, produced, "should produce one image per page");
    }

    @Test
    public void bindDocument_worksWithoutDoConvert() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getPages().add();

        PdfConverter converter = new PdfConverter();
        assertTrue(converter.bindPdf(doc));

        int count = 0;
        while (converter.hasNextImage()) {
            count++;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertTrue(converter.getNextImage(baos, ImageFormat.PNG));
            assertTrue(baos.size() > 0);
        }
        assertEquals(2, count);
        converter.close();
    }

    @Test
    public void constructorWithDocument_bindsDocumentImmediately() throws Exception {
        Document doc = new Document();
        doc.getPages().add();

        PdfConverter converter = new PdfConverter(doc);
        assertSame(doc, converter.getDocument());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(converter.getNextImage(baos, ImageFormat.PNG));
        assertTrue(baos.size() > 0);
        converter.close();
    }

    @Test
    public void coordinateType_roundTrips() {
        PdfConverter converter = new PdfConverter();
        assertEquals(PageCoordinateType.MediaBox, converter.getCoordinateType());
        converter.setCoordinateType(PageCoordinateType.CropBox);
        assertEquals(PageCoordinateType.CropBox, converter.getCoordinateType());
    }

    @Test
    public void getNextImageJpeg_producesValidJpeg() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        assertTrue(converter.bindPdf(pdf.toString()));
        converter.doConvert();

        assertTrue(converter.hasNextImage());
        Path out = tempDir.resolve("page-1.jpg");
        assertTrue(converter.getNextImage(out.toString(), ImageFormat.JPEG));
        assertTrue(Files.size(out) > 0);
        assertJpegSignature(out);
        converter.close();
    }

    @Test
    public void getNextImageJpegWithQuality_lowerQualityProducesSmallerFile() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter c1 = new PdfConverter();
        c1.bindPdf(pdf.toString());
        Path high = tempDir.resolve("high.jpg");
        c1.getNextImage(high.toString(), ImageFormat.JPEG, 95);
        c1.close();

        PdfConverter c2 = new PdfConverter();
        c2.bindPdf(pdf.toString());
        Path low = tempDir.resolve("low.jpg");
        c2.getNextImage(low.toString(), ImageFormat.JPEG, 10);
        c2.close();

        long highSize = Files.size(high);
        long lowSize = Files.size(low);
        assertTrue(highSize > 0 && lowSize > 0);
        assertTrue(lowSize <= highSize,
                "quality=10 file (" + lowSize + ") must not be larger than quality=95 (" + highSize + ")");
    }

    @Test
    public void getNextImageStreamOverload_writesBytes() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        converter.bindPdf(pdf.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(converter.getNextImage(baos, ImageFormat.PNG));
        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 8, "PNG payload expected");
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 0x50, bytes[1]);
        assertEquals((byte) 0x4E, bytes[2]);
        assertEquals((byte) 0x47, bytes[3]);
        converter.close();
    }

    @Test
    public void saveAsTIFF_producesValidTiff() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        assertTrue(converter.bindPdf(pdf.toString()));

        Path out = tempDir.resolve("out.tiff");
        assertTrue(converter.saveAsTIFF(out.toString()));
        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
        assertTiffMagic(out);
        converter.close();
    }

    @Test
    public void saveAsTIFF_lzwSmallerThanUncompressed() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter c1 = new PdfConverter();
        c1.bindPdf(pdf.toString());
        Path uncompressed = tempDir.resolve("uncompressed.tiff");
        TiffSettings none = new TiffSettings(CompressionType.None);
        assertTrue(c1.saveAsTIFF(uncompressed.toString(), none));
        c1.close();

        PdfConverter c2 = new PdfConverter();
        c2.bindPdf(pdf.toString());
        Path lzw = tempDir.resolve("lzw.tiff");
        TiffSettings lzwSettings = new TiffSettings(CompressionType.LZW);
        assertTrue(c2.saveAsTIFF(lzw.toString(), lzwSettings));
        c2.close();

        long noneSize = Files.size(uncompressed);
        long lzwSize = Files.size(lzw);
        assertTrue(noneSize > 0 && lzwSize > 0);
        assertTrue(lzwSize < noneSize,
                "LZW TIFF (" + lzwSize + ") must be smaller than uncompressed (" + noneSize + ")");
    }

    @Test
    public void saveAsTIFF_withExplicitResolution() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        converter.bindPdf(pdf.toString());

        Path out = tempDir.resolve("res72.tiff");
        assertTrue(converter.saveAsTIFF(out.toString(), null, new Resolution(72)));
        assertTrue(Files.size(out) > 0);
        assertTiffMagic(out);
        converter.close();
    }

    @Test
    public void saveAsTIFF_streamOverload_writesTiffBytes() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        converter.bindPdf(pdf.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(converter.saveAsTIFF(baos, new TiffSettings(CompressionType.LZW)));
        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 8);
        boolean littleEndian = bytes[0] == 'I' && bytes[1] == 'I' && bytes[2] == 0x2A && bytes[3] == 0x00;
        boolean bigEndian    = bytes[0] == 'M' && bytes[1] == 'M' && bytes[2] == 0x00 && bytes[3] == 0x2A;
        assertTrue(littleEndian || bigEndian, "TIFF magic expected");
        converter.close();
    }

    @Test
    public void saveAsTIFF_pageSizeOverload_producesTiff() throws Exception {
        Path pdf = buildThreePagePdf(tempDir);

        PdfConverter converter = new PdfConverter();
        converter.bindPdf(pdf.toString());

        Path out = tempDir.resolve("a4.tiff");
        assertTrue(converter.saveAsTIFF(out.toString(), PageSize.A4, new TiffSettings(CompressionType.LZW)));
        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
        assertTiffMagic(out);
        converter.close();
    }

    private static void assertTiffMagic(Path p) throws Exception {
        byte[] head = Files.readAllBytes(p);
        assertTrue(head.length >= 4);
        boolean littleEndian = head[0] == 'I' && head[1] == 'I' && head[2] == 0x2A && head[3] == 0x00;
        boolean bigEndian    = head[0] == 'M' && head[1] == 'M' && head[2] == 0x00 && head[3] == 0x2A;
        assertTrue(littleEndian || bigEndian, "TIFF magic expected, got: "
                + String.format("%02X %02X %02X %02X", head[0], head[1], head[2], head[3]));
    }

    private static void assertPngSignature(Path p) throws Exception {
        byte[] head = Files.readAllBytes(p);
        assertTrue(head.length >= 8, "file too short");
        assertEquals((byte) 0x89, head[0]);
        assertEquals((byte) 0x50, head[1]);
        assertEquals((byte) 0x4E, head[2]);
        assertEquals((byte) 0x47, head[3]);
    }

    private static void assertJpegSignature(Path p) throws Exception {
        byte[] head = Files.readAllBytes(p);
        assertTrue(head.length >= 3);
        assertEquals((byte) 0xFF, head[0]);
        assertEquals((byte) 0xD8, head[1]);
        assertEquals((byte) 0xFF, head[2]);
    }
}
