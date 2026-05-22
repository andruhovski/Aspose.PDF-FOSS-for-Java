package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FlateFilter}.
 */
public class FlateFilterTest {

    private final FlateFilter filter = new FlateFilter();

    @Test
    public void roundTrip_helloWorld() throws IOException {
        byte[] original = "Hello World".getBytes(StandardCharsets.UTF_8);
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void roundTrip_largeRandomData() throws IOException {
        Random rnd = new Random(42);
        byte[] original = new byte[1024 * 1024]; // 1MB
        rnd.nextBytes(original);
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void decode_corruptedData_throwsIOException() {
        byte[] corrupted = {0x00, 0x01, 0x02, 0x03, 0x04};
        assertThrows(IOException.class, () -> filter.decode(corrupted, null));
    }

    @Test
    public void encode_emptyData() throws IOException {
        byte[] result = filter.encode(new byte[0], null);
        assertEquals(0, result.length);
    }

    @Test
    public void decode_emptyData() throws IOException {
        byte[] result = filter.decode(new byte[0], null);
        assertEquals(0, result.length);
    }

    @Test
    public void decode_nowrapFallback() throws IOException {
        // Create raw deflate data (no zlib header) using Deflater with nowrap=true
        byte[] original = "Test raw deflate data".getBytes(StandardCharsets.UTF_8);
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true); // nowrap=true
        deflater.setInput(original);
        deflater.finish();
        byte[] buf = new byte[1024];
        int len = deflater.deflate(buf);
        deflater.end();

        byte[] rawDeflate = new byte[len];
        System.arraycopy(buf, 0, rawDeflate, 0, len);

        // FlateFilter should handle this via nowrap fallback
        byte[] decoded = filter.decode(rawDeflate, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void roundTrip_allByteValues() throws IOException {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }
}
