package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PageDevice} base class (via PngDevice).
 */
public class PageDeviceTest {

    @Test
    public void nullResolutionThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PngDevice(null));
    }

    @Test
    public void resolutionAccessor() {
        Resolution r = new Resolution(150);
        PngDevice device = new PngDevice(r);
        assertSame(r, device.getResolution());
    }

    @Test
    public void nullPageThrows() {
        PngDevice device = new PngDevice(new Resolution(72));
        assertThrows(IllegalArgumentException.class, () -> device.process(null, System.out));
    }

    @Test
    public void nullOutputThrows() {
        PngDevice device = new PngDevice(new Resolution(72));
        assertThrows(IllegalArgumentException.class, () -> device.process(
                new org.aspose.pdf.Page(
                        new org.aspose.pdf.engine.pdfobjects.PdfDictionary(), null),
                null));
    }

    @Test
    public void dimensionConstructor() {
        PngDevice device = new PngDevice(800, 600, new Resolution(96));
        assertEquals(96, device.getResolution().getX());
    }
}
