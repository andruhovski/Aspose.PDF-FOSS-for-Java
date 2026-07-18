package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [RunLengthFilter].
public class RunLengthFilterTest {

    private final RunLengthFilter filter = new RunLengthFilter();

    @Test
    public void decode_literalRun() throws IOException {
        // Length byte 3 = copy next 4 bytes literally
        byte[] encoded = {3, 'a', 'b', 'c', 'd', (byte) 128};
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{'a', 'b', 'c', 'd'}, result);
    }

    @Test
    public void decode_repeatRun() throws IOException {
        // Length byte 254 = repeat next byte 257-254=3 times
        byte[] encoded = {(byte) 254, 'x', (byte) 128};
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{'x', 'x', 'x'}, result);
    }

    @Test
    public void decode_mixed() throws IOException {
        // Literal: 1 = copy 2 bytes, then repeat: 253 = repeat 4 times, then literal: 0 = copy 1 byte
        byte[] encoded = {
                1, 'a', 'b',        // literal: "ab"
                (byte) 253, 'x',    // repeat: "xxxx"
                0, 'z',             // literal: "z"
                (byte) 128          // EOD
        };
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{'a', 'b', 'x', 'x', 'x', 'x', 'z'}, result);
    }

    @Test
    public void decode_eodStopsDecoding() throws IOException {
        // EOD in the middle; data after should be ignored
        byte[] encoded = {0, 'a', (byte) 128, 0, 'b'};
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{'a'}, result);
    }

    @Test
    public void roundTrip_repeatingData() throws IOException {
        byte[] original = new byte[100];
        for (int i = 0; i < 100; i++) {
            original[i] = (byte) (i < 50 ? 'A' : 'B');
        }
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void roundTrip_mixedData() throws IOException {
        byte[] original = {1, 2, 3, 3, 3, 3, 3, 4, 5, 6};
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void decode_empty() throws IOException {
        byte[] result = filter.decode(new byte[0], null);
        assertEquals(0, result.length);
    }

    @Test
    public void encode_empty() throws IOException {
        byte[] result = filter.encode(new byte[0], null);
        // Should just be EOD marker
        assertEquals(1, result.length);
        assertEquals((byte) 128, result[0]);
    }
}
