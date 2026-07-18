package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [ASCII85Filter].
public class ASCII85FilterTest {

    private final ASCII85Filter filter = new ASCII85Filter();

    @Test
    public void decode_standardTestVector() throws IOException {
        // "Man " in ASCII85 is "9jqo^"
        // Full test: "Man sure." → known encoding
        // Let's use a simpler known vector:
        // ASCII85 of "Hello" = "87cURD]i"  (but "Hello" is only 5 bytes, partial group)
        // Simpler: 4 zero bytes → "z"
        byte[] encoded = "z~>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    @Test
    public void decode_zeroGroup() throws IOException {
        byte[] encoded = "z~>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    @Test
    public void decode_withWhitespace() throws IOException {
        byte[] encoded = "z ~>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    @Test
    public void roundTrip() throws IOException {
        byte[] original = "Hello, World! This is a test of ASCII85.".getBytes(StandardCharsets.US_ASCII);
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void roundTrip_partialGroups() throws IOException {
        // 1 byte remaining
        byte[] original1 = {0x42};
        assertArrayEquals(original1, filter.decode(filter.encode(original1, null), null));

        // 2 bytes remaining
        byte[] original2 = {0x42, 0x43};
        assertArrayEquals(original2, filter.decode(filter.encode(original2, null), null));

        // 3 bytes remaining
        byte[] original3 = {0x42, 0x43, 0x44};
        assertArrayEquals(original3, filter.decode(filter.encode(original3, null), null));
    }

    @Test
    public void decode_empty() throws IOException {
        byte[] result = filter.decode(new byte[0], null);
        assertEquals(0, result.length);
    }

    @Test
    public void encode_empty() throws IOException {
        byte[] result = filter.encode(new byte[0], null);
        assertEquals("~>", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    public void roundTrip_binaryData() throws IOException {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }
        byte[] encoded = filter.encode(original, null);
        byte[] decoded = filter.decode(encoded, null);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void decode_multipleZGroups() throws IOException {
        byte[] encoded = "zzz~>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertEquals(12, result.length);
        for (byte b : result) {
            assertEquals(0, b);
        }
    }
}
