package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TiffDevice}.
 */
public class TiffDeviceTest {

    @Test
    public void nullResolutionThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TiffDevice((Resolution) null));
    }

    @Test
    public void resolutionAccessor() {
        Resolution r = new Resolution(300);
        TiffDevice device = new TiffDevice(r);
        assertSame(r, device.getResolution());
    }

    @Test
    public void nullDocumentThrows() {
        TiffDevice device = new TiffDevice(new Resolution(72));
        assertThrows(IllegalArgumentException.class,
                () -> device.process((org.aspose.pdf.Document) null,
                        new java.io.ByteArrayOutputStream()));
    }

    @Test
    public void nullPageThrows() {
        TiffDevice device = new TiffDevice(new Resolution(72));
        assertThrows(IllegalArgumentException.class,
                () -> device.process((org.aspose.pdf.Page) null,
                        new java.io.ByteArrayOutputStream()));
    }

    @Test
    public void nullOutputThrowsForPage() {
        TiffDevice device = new TiffDevice(new Resolution(72));
        org.aspose.pdf.engine.pdfobjects.PdfDictionary pageDict =
                new org.aspose.pdf.engine.pdfobjects.PdfDictionary();
        pageDict.set(org.aspose.pdf.engine.pdfobjects.PdfName.of("Type"),
                org.aspose.pdf.engine.pdfobjects.PdfName.of("Page"));
        org.aspose.pdf.engine.pdfobjects.PdfArray mediaBox =
                new org.aspose.pdf.engine.pdfobjects.PdfArray(4);
        mediaBox.add(new org.aspose.pdf.engine.pdfobjects.PdfFloat(0));
        mediaBox.add(new org.aspose.pdf.engine.pdfobjects.PdfFloat(0));
        mediaBox.add(new org.aspose.pdf.engine.pdfobjects.PdfFloat(100));
        mediaBox.add(new org.aspose.pdf.engine.pdfobjects.PdfFloat(100));
        pageDict.set(org.aspose.pdf.engine.pdfobjects.PdfName.of("MediaBox"), mediaBox);

        org.aspose.pdf.Page page = new org.aspose.pdf.Page(pageDict, null);
        assertThrows(IllegalArgumentException.class,
                () -> device.process(page, null));
    }
}
