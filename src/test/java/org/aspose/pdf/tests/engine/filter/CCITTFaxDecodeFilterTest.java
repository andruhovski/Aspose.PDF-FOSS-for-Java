package org.aspose.pdf.tests.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfBoolean;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.filter.CCITTFaxDecodeFilter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [CCITTFaxDecodeFilter].
public class CCITTFaxDecodeFilterTest {

    private final CCITTFaxDecodeFilter filter = new CCITTFaxDecodeFilter();

    // ─── Empty input ─────────────────────────────────────────────

    @Test
    public void emptyInput() throws IOException {
        byte[] result = filter.decode(new byte[0], null);
        assertEquals(0, result.length);
    }

    @Test
    public void nullInput() throws IOException {
        byte[] result = filter.decode(null, null);
        assertEquals(0, result.length);
    }

    // ─── Group 3 1D: all-white line of 1728 pixels ──────────────

    @Test
    public void group3_1D_allWhiteLine() throws IOException {
        // A white run of 1728 pixels in Group 3 1D Modified Huffman:
        // Makeup 1728 (white): code=0x9B=155, 9 bits MSB-first → 010011011
        // Terminating 0 (white): code=0x35=53, 8 bits MSB-first → 00110101
        // Total: 010011011 00110101 = 17 bits → pad to 3 bytes
        byte[] encoded = bitsToBytes(codeStr(0x9B, 9) + codeStr(0x35, 8));

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(1728));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        params.set(PdfName.of("EndOfBlock"), PdfBoolean.FALSE);
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        // 1728 pixels / 8 = 216 bytes, all zeros (white with BlackIs1=true: white=0)
        assertEquals(216, result.length);
        for (byte b : result) {
            assertEquals(0, b & 0xFF, "All bytes should be 0 (white)");
        }
    }

    // ─── Group 3 1D: short white + black + white line ────────────

    @Test
    public void group3_1D_mixedLine() throws IOException {
        // 16 pixel line: 8 white + 8 black
        // White run 8: code=0x13=19, 5 bits MSB-first → 10011
        // Black run 8: code=0x05=5, 6 bits MSB-first → 000101
        // Total: 10011 000101 = 11 bits
        byte[] encoded = bitsToBytes(codeStr(0x13, 5) + codeStr(0x05, 6));

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(16));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        params.set(PdfName.of("EndOfBlock"), PdfBoolean.FALSE);
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        // 16 pixels = 2 bytes. First 8 = white (00000000), next 8 = black (11111111)
        assertEquals(2, result.length);
        assertEquals(0x00, result[0] & 0xFF); // 8 white pixels
        assertEquals(0xFF, result[1] & 0xFF); // 8 black pixels
    }

    // ─── Group 3 1D: all-black line ──────────────────────────────

    @Test
    public void group3_1D_allBlackLine() throws IOException {
        // 8 pixels, all black.
        // White run 0: code=0x35=53, 8 bits MSB-first → 00110101
        // Black run 8: code=0x05=5, 6 bits MSB-first → 000101
        // Total: 00110101 000101 = 14 bits
        byte[] encoded = bitsToBytes(codeStr(0x35, 8) + codeStr(0x05, 6));

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(8));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        params.set(PdfName.of("EndOfBlock"), PdfBoolean.FALSE);
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        assertEquals(1, result.length);
        assertEquals(0xFF, result[0] & 0xFF); // all black
    }

    // ─── Group 4: all-white line (vertical V(0) from white ref) ──

    @Test
    public void group4_allWhiteLine() throws IOException {
        // Group 4 with all-white reference line and all-white coding line.
        // For an all-white line with all-white reference:
        //   a0=0, curColor=white. b1=columns (no change on ref). V(0)=1 → a1=columns.
        // So a single "1" bit encodes the entire line.
        // Then EOFB: 000000000001 000000000001 (24 bits)
        byte[] encoded = bitsToBytes("1" + "000000000001" + "000000000001");

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(-1));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(8));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        assertEquals(1, result.length);
        assertEquals(0x00, result[0] & 0xFF); // all white
    }

    // ─── BlackIs1=false (default) inverts output ─────────────────

    @Test
    public void blackIs1_false_inverts() throws IOException {
        // Same as allWhiteLine but with BlackIs1=false (default)
        // White pixels should become 0xFF after inversion
        byte[] encoded = bitsToBytes("1" + "000000000001" + "000000000001");

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(-1));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(8));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        // BlackIs1 defaults to false → invert

        byte[] result = filter.decode(encoded, params);
        assertEquals(1, result.length);
        assertEquals(0xFF, result[0] & 0xFF); // inverted: white=1, black=0
    }

    @Test
    public void encodeDecodeRoundTrip_group3_1d() throws IOException {
        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(16));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(1));
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] decoded = new byte[] {(byte) 0x0F, (byte) 0xF0};
        byte[] encoded = filter.encode(decoded, params);
        byte[] roundTrip = filter.decode(encoded, params);
        assertArrayEquals(decoded, roundTrip);
    }

    @Test
    public void encodeDecodeRoundTrip_group4_horizontalMode() throws IOException {
        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(-1));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(8));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(2));
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] decoded = new byte[] {
                (byte) 0x0F,
                (byte) 0xF0
        };
        byte[] encoded = filter.encode(decoded, params);
        byte[] roundTrip = filter.decode(encoded, params);
        assertArrayEquals(decoded, roundTrip);
    }

    // ─── Parameters default correctly ────────────────────────────

    @Test
    public void defaultParameters() throws IOException {
        // With null params, defaults should be K=0, Columns=1728, etc.
        // Just verify it doesn't crash with minimal input
        byte[] encoded = bitsToBytes(codeStr(0x9B, 9) + codeStr(0x35, 8)); // 1728 white
        byte[] result = filter.decode(encoded, null);
        // Should produce at least 216 bytes (1728/8), inverted (BlackIs1=false by default)
        // Rows=0 → decoder reads until EOF, so trailing pad bits may produce extra rows
        assertTrue(result.length >= 216, "Expected at least 216 bytes, got " + result.length);
    }

    // ─── Group 3 1D: EOL-prefixed lines (corpus 29753) ────────────

    @Test
    public void group3_1D_eolPrefixedLines() throws IOException {
        // Two 16px lines (8 white + 8 black), each prefixed by an EOL
        // (000000000001) even though /EndOfLine is false — real T.4 streams
        // routinely include them (corpus 29753). The EOL zeros must not be
        // consumed as run data or the whole stream desyncs.
        String eol = "000000000001";
        String line = codeStr(0x13, 5) + codeStr(0x05, 6); // white 8 + black 8
        byte[] encoded = bitsToBytes(eol + line + eol + line);

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(16));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(2));
        params.set(PdfName.of("EndOfBlock"), PdfBoolean.FALSE);
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        assertEquals(4, result.length);
        assertEquals(0x00, result[0] & 0xFF);
        assertEquals(0xFF, result[1] & 0xFF);
        assertEquals(0x00, result[2] & 0xFF);
        assertEquals(0xFF, result[3] & 0xFF);
    }

    // ─── Group 3 1D: resync at EOL after a corrupt row ────────────

    @Test
    public void group3_1D_resyncAfterCorruptRow() throws IOException {
        // Row 1 starts with a valid white-8 run then hits an invalid code
        // (zeros that match no Huffman entry). The decoder must emit the
        // partial row, scan to the next EOL, and decode row 2 cleanly
        // instead of treating the corrupt tail as row data.
        String line = codeStr(0x13, 5) + codeStr(0x05, 6); // white 8 + black 8
        String corrupt = codeStr(0x13, 5) + "000000000";   // white 8 + invalid
        String eol = "000000000001";
        byte[] encoded = bitsToBytes(corrupt + eol + line);

        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("K"), PdfInteger.valueOf(0));
        params.set(PdfName.of("Columns"), PdfInteger.valueOf(16));
        params.set(PdfName.of("Rows"), PdfInteger.valueOf(2));
        params.set(PdfName.of("EndOfBlock"), PdfBoolean.FALSE);
        params.set(PdfName.of("BlackIs1"), PdfBoolean.TRUE);

        byte[] result = filter.decode(encoded, params);
        assertEquals(4, result.length);
        // Row 1: only the white run decoded → stays all-white
        assertEquals(0x00, result[0] & 0xFF);
        assertEquals(0x00, result[1] & 0xFF);
        // Row 2: clean 8 white + 8 black
        assertEquals(0x00, result[2] & 0xFF);
        assertEquals(0xFF, result[3] & 0xFF);
    }

    // ─── Helpers ──────────────────────────────────────────────────

    /// Converts a Huffman code value to its MSB-first bit string of the given length.
    private static String codeStr(int code, int bits) {
        StringBuilder sb = new StringBuilder(bits);
        for (int i = bits - 1; i >= 0; i--) {
            sb.append((code >> i) & 1);
        }
        return sb.toString();
    }

    private static byte[] bitsToBytes(String bits) {
        int len = (bits.length() + 7) / 8;
        byte[] result = new byte[len];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                result[i >> 3] |= (byte) (0x80 >> (i & 7));
            }
        }
        return result;
    }
}
