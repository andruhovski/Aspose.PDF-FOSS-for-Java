package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FilterFactory}.
 */
public class FilterFactoryTest {

    @Test
    public void getFilter_flateDecode() throws IOException {
        COSFilter filter = FilterFactory.getFilter(COSName.FLATE_DECODE);
        assertNotNull(filter);
        assertTrue(filter instanceof FlateFilter);
    }

    @Test
    public void getFilter_abbreviated_Fl() throws IOException {
        COSFilter filter = FilterFactory.getFilter(COSName.of("Fl"));
        assertNotNull(filter);
        assertTrue(filter instanceof FlateFilter);
    }

    @Test
    public void getFilter_unknown_throwsIOException() {
        assertThrows(IOException.class,
                () -> FilterFactory.getFilter(COSName.of("UnknownFilter")));
    }

    @Test
    public void getFilter_allStandardFilters() throws IOException {
        assertNotNull(FilterFactory.getFilter(COSName.FLATE_DECODE));
        assertNotNull(FilterFactory.getFilter(COSName.LZW_DECODE));
        assertNotNull(FilterFactory.getFilter(COSName.ASCII_HEX_DECODE));
        assertNotNull(FilterFactory.getFilter(COSName.ASCII85_DECODE));
        assertNotNull(FilterFactory.getFilter(COSName.RUN_LENGTH_DECODE));
    }

    @Test
    public void getFilter_allAbbreviations() throws IOException {
        assertNotNull(FilterFactory.getFilter(COSName.of("Fl")));
        assertNotNull(FilterFactory.getFilter(COSName.of("LZW")));
        assertNotNull(FilterFactory.getFilter(COSName.of("AHx")));
        assertNotNull(FilterFactory.getFilter(COSName.of("A85")));
        assertNotNull(FilterFactory.getFilter(COSName.of("RL")));
    }

    @Test
    public void decodeChain_singleFilter() throws IOException {
        byte[] original = "Hello".getBytes(StandardCharsets.UTF_8);
        FlateFilter flate = new FlateFilter();
        byte[] encoded = flate.encode(original, null);

        List<COSName> filters = Arrays.asList(COSName.FLATE_DECODE);
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
        List<COSName> filters = Arrays.asList(COSName.ASCII_HEX_DECODE, COSName.FLATE_DECODE);
        byte[] decoded = FilterFactory.decodeChain(step2, filters, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void encodeChain_decodeChain_roundTrip() throws IOException {
        byte[] original = "Round-trip test data".getBytes(StandardCharsets.UTF_8);
        List<COSName> filters = Arrays.asList(COSName.ASCII_HEX_DECODE, COSName.FLATE_DECODE);

        byte[] encoded = FilterFactory.encodeChain(original, filters, null);
        byte[] decoded = FilterFactory.decodeChain(encoded, filters, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void register_customFilter() throws IOException {
        COSName customName = COSName.of("TestCustomFilter");
        COSFilter custom = new COSFilter() {
            @Override
            public byte[] decode(byte[] encoded, COSDictionary params) {
                return encoded;
            }

            @Override
            public byte[] encode(byte[] decoded, COSDictionary params) {
                return decoded;
            }

            @Override
            public COSName getName() {
                return customName;
            }
        };

        FilterFactory.register(custom);
        COSFilter retrieved = FilterFactory.getFilter(customName);
        assertSame(custom, retrieved);
    }
}
