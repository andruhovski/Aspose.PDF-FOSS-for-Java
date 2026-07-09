package org.aspose.pdf.engine.barcode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Validates {@link QrEncoder} against the ISO/IEC 18004 Annex&nbsp;I worked example (numeric
 * "01234567", version&nbsp;1, level&nbsp;M) — the published data and error-correction codewords — plus
 * the structural invariants of the produced symbol (size, finder and timing patterns) and the capacity
 * tables. The spec example pins down the hardest, mode-independent parts: Reed&ndash;Solomon over
 * GF(2<sup>8</sup>), padding, and block layout.
 */
class QrEncoderTest {

    // ISO/IEC 18004 Annex I: "01234567" encoded at version 1, level M.
    private static final int[] SPEC_DATA = {
        0x10, 0x20, 0x0C, 0x56, 0x61, 0x80, 0xEC, 0x11,
        0xEC, 0x11, 0xEC, 0x11, 0xEC, 0x11, 0xEC, 0x11};
    private static final int[] SPEC_ECC = {
        0xA5, 0x24, 0xD4, 0xC1, 0xED, 0x36, 0xC7, 0x87, 0x2C, 0x55};

    @Test
    void reedSolomonMatchesSpecExample() {
        byte[] data = toBytes(SPEC_DATA);
        byte[] ecc = QrEncoder.rsRemainder(data, QrEncoder.rsGenerator(10));
        assertArrayEquals(toBytes(SPEC_ECC), ecc, "RS ECC codewords must match ISO 18004 Annex I");
    }

    @Test
    void codewordStreamMatchesSpecExample() {
        QrEncoder.Encoded e = QrEncoder.buildCodewords("01234567".getBytes(), "01234567",
                QrEncoder.Ecc.MEDIUM);
        assertEquals(1, e.version, "numeric 8-digit payload fits version 1");
        int[] expected = new int[SPEC_DATA.length + SPEC_ECC.length];
        System.arraycopy(SPEC_DATA, 0, expected, 0, SPEC_DATA.length);
        System.arraycopy(SPEC_ECC, 0, expected, SPEC_DATA.length, SPEC_ECC.length);
        assertArrayEquals(toBytes(expected), e.codewords,
                "v1-M single block: data codewords followed by ECC codewords");
    }

    @Test
    void symbolSizeIsVersionDependent() {
        assertEquals(21, QrEncoder.encode("01234567", QrEncoder.Ecc.MEDIUM).length); // v1 = 21x21
        // A long byte payload forces a higher version (larger grid), still square.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('A');
        }
        boolean[][] big = QrEncoder.encodeBytes(sb.toString().getBytes(), QrEncoder.Ecc.LOW);
        assertTrue(big.length > 21 && big.length == big[0].length, "higher version, square grid");
        assertEquals(0, (big.length - 17) % 4, "size = 4*version + 17");
    }

    @Test
    void finderPatternsAtThreeCorners() {
        boolean[][] m = QrEncoder.encode("HELLO WORLD", QrEncoder.Ecc.QUARTILE);
        int n = m.length;
        assertFinder(m, 0, 0);
        assertFinder(m, 0, n - 7);
        assertFinder(m, n - 7, 0);
        // The fourth corner carries NO finder (only data / an alignment pattern nearby).
        assertFalse(isFinderRow(m, n - 7, n - 7), "no finder in the bottom-right corner");
    }

    @Test
    void timingPatternsAlternate() {
        boolean[][] m = QrEncoder.encode("01234567", QrEncoder.Ecc.LOW);
        // Row 6 and column 6 between the finders alternate dark/light, starting dark at index 8.
        for (int i = 8; i <= m.length - 9; i++) {
            assertEquals(i % 2 == 0, m[6][i], "horizontal timing module " + i);
            assertEquals(i % 2 == 0, m[i][6], "vertical timing module " + i);
        }
    }

    @Test
    void capacityTableMatchesSpec() {
        assertEquals(19, QrEncoder.numDataCodewords(1, QrEncoder.Ecc.LOW));
        assertEquals(16, QrEncoder.numDataCodewords(1, QrEncoder.Ecc.MEDIUM));
        assertEquals(13, QrEncoder.numDataCodewords(1, QrEncoder.Ecc.QUARTILE));
        assertEquals(9, QrEncoder.numDataCodewords(1, QrEncoder.Ecc.HIGH));
        assertEquals(2956, QrEncoder.numDataCodewords(40, QrEncoder.Ecc.LOW));
        assertEquals(3706, QrEncoder.numRawDataModules(40) / 8);
    }

    @Test
    void alignmentPositionsMatchSpec() {
        // ISO/IEC 18004 Annex E centre coordinates for a sampling of versions.
        assertArrayEquals(new int[0], QrEncoder.alignmentPositions(1));
        assertArrayEquals(new int[]{6, 18}, QrEncoder.alignmentPositions(2));
        assertArrayEquals(new int[]{6, 22, 38}, QrEncoder.alignmentPositions(7));
        assertArrayEquals(new int[]{6, 26, 46}, QrEncoder.alignmentPositions(9));
        assertArrayEquals(new int[]{6, 30, 54}, QrEncoder.alignmentPositions(11));
        assertArrayEquals(new int[]{6, 26, 46, 66}, QrEncoder.alignmentPositions(14));
        assertArrayEquals(new int[]{6, 26, 50, 74}, QrEncoder.alignmentPositions(16));
        assertArrayEquals(new int[]{6, 34, 62, 90, 118}, QrEncoder.alignmentPositions(27));
        assertArrayEquals(new int[]{6, 34, 60, 86, 112, 138}, QrEncoder.alignmentPositions(32));
        assertArrayEquals(new int[]{6, 30, 58, 86, 114, 142, 170}, QrEncoder.alignmentPositions(40));
    }

    @Test
    void rejectsOversizePayload() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append('X');
        }
        assertThrows(IllegalArgumentException.class,
                () -> QrEncoder.encodeBytes(sb.toString().getBytes(), QrEncoder.Ecc.HIGH));
    }

    /* ----------------------------------------------------------------- helpers */

    private static void assertFinder(boolean[][] m, int row, int col) {
        // 7x7: solid dark border ring, light inner ring, 3x3 dark centre.
        for (int dy = 0; dy < 7; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                int ring = Math.min(Math.min(dx, dy), Math.min(6 - dx, 6 - dy));
                boolean expectDark = ring != 1; // ring 1 is the light separator inside the border
                assertEquals(expectDark, m[row + dy][col + dx],
                        "finder@(" + row + "," + col + ") module (" + dy + "," + dx + ")");
            }
        }
    }

    private static boolean isFinderRow(boolean[][] m, int row, int col) {
        for (int dx = 0; dx < 7; dx++) {
            if (!m[row][col + dx]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] toBytes(int[] vals) {
        byte[] out = new byte[vals.length];
        for (int i = 0; i < vals.length; i++) {
            out[i] = (byte) vals[i];
        }
        return out;
    }
}
