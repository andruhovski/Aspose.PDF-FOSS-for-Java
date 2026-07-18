package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [FilterFactory].
public class FilterFactoryTest {

    @Test
    public void getFilter_flateDecode() throws IOException {
        PdfFilter filter = FilterFactory.getFilter(PdfName.FLATE_DECODE);
        assertNotNull(filter);
        assertTrue(filter instanceof FlateFilter);
    }

    @Test
    public void getFilter_abbreviated_Fl() throws IOException {
        PdfFilter filter = FilterFactory.getFilter(PdfName.of("Fl"));
        assertNotNull(filter);
        assertTrue(filter instanceof FlateFilter);
    }

    @Test
    public void getFilter_unknown_throwsIOException() {
        assertThrows(IOException.class,
                () -> FilterFactory.getFilter(PdfName.of("UnknownFilter")));
    }

    @Test
    public void getFilter_allStandardFilters() throws IOException {
        assertNotNull(FilterFactory.getFilter(PdfName.FLATE_DECODE));
        assertNotNull(FilterFactory.getFilter(PdfName.LZW_DECODE));
        assertNotNull(FilterFactory.getFilter(PdfName.ASCII_HEX_DECODE));
        assertNotNull(FilterFactory.getFilter(PdfName.ASCII85_DECODE));
        assertNotNull(FilterFactory.getFilter(PdfName.RUN_LENGTH_DECODE));
    }

    @Test
    public void getFilter_allAbbreviations() throws IOException {
        assertNotNull(FilterFactory.getFilter(PdfName.of("Fl")));
        assertNotNull(FilterFactory.getFilter(PdfName.of("LZW")));
        assertNotNull(FilterFactory.getFilter(PdfName.of("AHx")));
        assertNotNull(FilterFactory.getFilter(PdfName.of("A85")));
        assertNotNull(FilterFactory.getFilter(PdfName.of("RL")));
    }

    @Test
    public void decodeChain_singleFilter() throws IOException {
        byte[] original = "Hello".getBytes(StandardCharsets.UTF_8);
        FlateFilter flate = new FlateFilter();
        byte[] encoded = flate.encode(original, null);

        List<PdfName> filters = Arrays.asList(PdfName.FLATE_DECODE);
        byte[] decoded = FilterFactory.decodeChain(encoded, filters, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void decodeChain_twoFilters() throws IOException {
        byte[] original = "Hello, PDF world!".getBytes(StandardCharsets.UTF_8);

        // Encode: Flate first, then ASCIIHex (reverse order for encoding)
        FlateFilter flate = new FlateFilter();
        ASCIIHexFilter asciiHex = new ASCIIHexFilter();
        byte[] step1 = flate.encode(original, null);
        byte[] step2 = asciiHex.encode(step1, null);

        // Decode chain: [ASCIIHexDecode, FlateDecode] — applied left to right
        List<PdfName> filters = Arrays.asList(PdfName.ASCII_HEX_DECODE, PdfName.FLATE_DECODE);
        byte[] decoded = FilterFactory.decodeChain(step2, filters, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void encodeChain_decodeChain_roundTrip() throws IOException {
        byte[] original = "Round-trip test data".getBytes(StandardCharsets.UTF_8);
        List<PdfName> filters = Arrays.asList(PdfName.ASCII_HEX_DECODE, PdfName.FLATE_DECODE);

        byte[] encoded = FilterFactory.encodeChain(original, filters, null);
        byte[] decoded = FilterFactory.decodeChain(encoded, filters, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void register_customFilter() throws IOException {
        PdfName customName = PdfName.of("TestCustomFilter");
        PdfFilter custom = new PdfFilter() {
            @Override
            public byte[] decode(byte[] encoded, PdfDictionary params) {
                return encoded;
            }

            @Override
            public byte[] encode(byte[] decoded, PdfDictionary params) {
                return decoded;
            }

            @Override
            public PdfName getName() {
                return customName;
            }
        };

        FilterFactory.register(custom);
        PdfFilter retrieved = FilterFactory.getFilter(customName);
        assertSame(custom, retrieved);
    }
}
