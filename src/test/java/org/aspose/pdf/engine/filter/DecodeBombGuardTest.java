package org.aspose.pdf.engine.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Guards against decompression bombs in the stream decode filters.
///
/// A corrupt (or hostile) stream can expand thousands of times its encoded
/// size; without a cap the decode loop grows a `ByteArrayOutputStream`
/// until [OutOfMemoryError], which poisons every other thread sharing
/// the heap (observed in mass corpus runs: hundreds of cascading OOMs).
/// [DecodeLimits] now bounds a single decoded stream; these tests lower
/// the cap via the `aspose.pdf.maxDecodedStreamBytes` system property so
/// the bombs stay test-sized.
///
public class DecodeBombGuardTest {

    private static final long TEST_CAP = 1L << 20; // 1 MB

    @AfterEach
    void restoreLimit() {
        System.clearProperty(DecodeLimits.PROPERTY);
    }

    private static void setCap(long bytes) {
        System.setProperty(DecodeLimits.PROPERTY, Long.toString(bytes));
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater();
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            out.write(buf, 0, deflater.deflate(buf));
        }
        deflater.end();
        return out.toByteArray();
    }

    /// A 4 MB-of-zeros flate bomb (compresses to \~4 KB) must hit the cap, not OOM.
    @Test
    public void flateBombIsRejectedAtCap() {
        setCap(TEST_CAP);
        byte[] bomb = deflate(new byte[4 << 20]);
        assertTrue(bomb.length < 64 * 1024, "bomb must be small encoded");
        IOException e = assertThrows(IOException.class,
                () -> new FlateFilter().decode(bomb, null));
        assertTrue(e.getMessage().contains("exceeds"),
                "cap diagnostics expected, got: " + e.getMessage());
    }

    /// Repeat-runs expanding 64:1 past the cap must throw, not OOM.
    @Test
    public void runLengthBombIsRejectedAtCap() {
        setCap(TEST_CAP);
        // Each pair (0x81, X) expands to 128 bytes; 32k pairs = 4 MB decoded.
        byte[] bomb = new byte[32 * 1024 * 2];
        for (int i = 0; i < bomb.length; i += 2) {
            bomb[i] = (byte) 0x81; // 257-129 = 128 repeats
            bomb[i + 1] = 'A';
        }
        IOException e = assertThrows(IOException.class,
                () -> new RunLengthFilter().decode(bomb, null));
        assertTrue(e.getMessage().contains("exceeds"),
                "cap diagnostics expected, got: " + e.getMessage());
    }

    /// Legitimate data below the cap must round-trip unchanged.
    @Test
    public void smallStreamsDecodeNormallyUnderCap() throws Exception {
        setCap(TEST_CAP);
        byte[] raw = "Hello, ISO 32000! ".repeat(100).getBytes("US-ASCII");
        assertArrayEquals(raw, new FlateFilter().decode(deflate(raw), null),
                "flate round-trip under cap");
        RunLengthFilter rl = new RunLengthFilter();
        assertArrayEquals(raw, rl.decode(rl.encode(raw, null), null),
                "run-length round-trip under cap");
        // LZWFilter is decode-only (encode throws "use FlateDecode") — its
        // cap shares DecodeLimits.check with the two filters tested above.
    }

    /// The default cap (no property) is 256 MB; the disabled value lifts it.
    @Test
    public void capDefaultsAndDisable() {
        System.clearProperty(DecodeLimits.PROPERTY);
        assertEquals(DecodeLimits.DEFAULT_MAX_DECODED_BYTES, DecodeLimits.maxDecodedBytes());
        setCap(0); // <= 0 disables the guard
        assertEquals(Long.MAX_VALUE, DecodeLimits.maxDecodedBytes());
        setCap(12345);
        assertEquals(12345, DecodeLimits.maxDecodedBytes());
    }
}
