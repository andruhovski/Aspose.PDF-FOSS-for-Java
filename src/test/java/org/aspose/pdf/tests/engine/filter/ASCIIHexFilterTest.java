package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [ASCIIHexFilter].
public class ASCIIHexFilterTest {

    private final ASCIIHexFilter filter = new ASCIIHexFilter();

    @Test
    public void decode_hello() throws IOException {
        byte[] encoded = "48656C6C6F>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertEquals("Hello", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    public void decode_withWhitespace() throws IOException {
        byte[] encoded = "48 65 6C\n6C 6F>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertEquals("Hello", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    public void decode_oddDigitPadsZero() throws IOException {
        // "4865 6C6C6F0>" — odd trailing digit gets padded
        byte[] encoded = "4865 6C6C6F0>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        // "Hello" + 0x00
        assertEquals(6, result.length);
        assertEquals("Hello", new String(result, 0, 5, StandardCharsets.US_ASCII));
        assertEquals(0x00, result[5] & 0xFF);
    }

    @Test
    public void encode_hello() throws IOException {
        byte[] decoded = "Hello".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.encode(decoded, null);
        assertEquals("48656C6C6F>", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    public void roundTrip() throws IOException {
        byte[] original = {0x00, 0x7F, (byte) 0xFF, 0x42, 0x13};
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
        assertEquals(">", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    public void decode_lowercaseHex() throws IOException {
        byte[] encoded = "48656c6c6f>".getBytes(StandardCharsets.US_ASCII);
        byte[] result = filter.decode(encoded, null);
        assertEquals("Hello", new String(result, StandardCharsets.US_ASCII));
    }
}
