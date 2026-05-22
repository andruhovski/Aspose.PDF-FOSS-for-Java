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
        org.aspose.pdf.engine.cos.COSDictionary pageDict =
                new org.aspose.pdf.engine.cos.COSDictionary();
        pageDict.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                org.aspose.pdf.engine.cos.COSName.of("Page"));
        org.aspose.pdf.engine.cos.COSArray mediaBox =
                new org.aspose.pdf.engine.cos.COSArray(4);
        mediaBox.add(new org.aspose.pdf.engine.cos.COSFloat(0));
        mediaBox.add(new org.aspose.pdf.engine.cos.COSFloat(0));
        mediaBox.add(new org.aspose.pdf.engine.cos.COSFloat(100));
        mediaBox.add(new org.aspose.pdf.engine.cos.COSFloat(100));
        pageDict.set(org.aspose.pdf.engine.cos.COSName.of("MediaBox"), mediaBox);

        org.aspose.pdf.Page page = new org.aspose.pdf.Page(pageDict, null);
        assertThrows(IllegalArgumentException.class,
                () -> device.process(page, null));
    }
}
