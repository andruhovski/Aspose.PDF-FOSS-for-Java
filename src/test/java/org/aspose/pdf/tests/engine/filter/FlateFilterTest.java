package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [FlateFilter].
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
    public void decode_corruptionWithinFirstBlock_recoversPrefix() throws IOException {
        // Build a raw-deflate stream: one stored (uncompressed) block of `len`
        // literal bytes, followed by a byte whose block-type field is the
        // reserved value 0b11. The JDK Inflater throws DataFormatException only
        // AFTER it has already produced all `len` stored bytes. With the old
        // 8 KB fast path that output (all produced in a single inflate() call)
        // was discarded and decode() hard-threw. B.1's small-buffer retry must
        // recover (almost) the full prefix that precedes the corruption,
        // surrendering at most one small-buffer chunk (64 bytes) at the boundary.
        int len = 1000;
        byte[] payload = new byte[len];
        for (int i = 0; i < len; i++) {
            payload[i] = (byte) (i & 0xFF);
        }

        java.io.ByteArrayOutputStream raw = new java.io.ByteArrayOutputStream();
        raw.write(0x00);                    // BFINAL=0, BTYPE=00 (stored)
        raw.write(len & 0xFF);              // LEN  (little-endian)
        raw.write((len >> 8) & 0xFF);
        raw.write(~len & 0xFF);             // NLEN = one's complement of LEN
        raw.write((~len >> 8) & 0xFF);
        raw.write(payload, 0, len);         // literal stored data
        raw.write(0x07);                    // next block header: BTYPE=11 -> invalid

        byte[] decoded = filter.decode(raw.toByteArray(), null);
        // Old behaviour: 0 bytes (hard throw). New: nearly the whole prefix.
        assertTrue(decoded.length >= len - 64 && decoded.length <= len,
                "B.1 should recover most of the prefix, got " + decoded.length);
        byte[] expectedPrefix = java.util.Arrays.copyOf(payload, decoded.length);
        assertArrayEquals(expectedPrefix, decoded,
                "recovered bytes must match the original stored prefix");
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
