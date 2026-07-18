package org.aspose.pdf.tests.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.filter.DCTDecodeFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [DCTDecodeFilter].
public class DCTDecodeFilterTest {

    private final DCTDecodeFilter filter = new DCTDecodeFilter();

    @Test
    public void roundTripRgbImage() throws IOException {
        PdfDictionary params = new PdfDictionary();
        params.setInt("Width", 2);
        params.setInt("Height", 2);
        params.setInt("BitsPerComponent", 8);
        params.set(PdfName.of("ColorSpace"), PdfName.of("DeviceRGB"));

        byte[] decoded = new byte[] {
                (byte) 255, 0, 0,
                0, (byte) 255, 0,
                0, 0, (byte) 255,
                (byte) 255, (byte) 255, 0
        };

        byte[] encoded = filter.encode(decoded, params);
        assertTrue(encoded.length > 0);

        byte[] roundTrip = filter.decode(encoded, params);
        assertEquals(decoded.length, roundTrip.length);
    }

    @Test
    public void roundTripGrayImage() throws IOException {
        PdfDictionary params = new PdfDictionary();
        params.setInt("Width", 4);
        params.setInt("Height", 1);
        params.setInt("BitsPerComponent", 8);
        params.set(PdfName.of("ColorSpace"), PdfName.of("DeviceGray"));

        byte[] decoded = new byte[] {0, 64, (byte) 192, (byte) 255};
        byte[] encoded = filter.encode(decoded, params);
        byte[] roundTrip = filter.decode(encoded, params);
        assertEquals(decoded.length, roundTrip.length);
    }
}
