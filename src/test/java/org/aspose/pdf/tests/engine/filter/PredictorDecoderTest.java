package org.aspose.pdf.tests.engine.filter;
import org.aspose.pdf.engine.filter.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PredictorDecoder}.
 */
public class PredictorDecoderTest {

    // ---- Predictor 1 (None): identity ----

    @Test
    public void predictor1_identity() {
        byte[] data = {1, 2, 3, 4, 5, 6};
        byte[] result = PredictorDecoder.decode(data, 1, 3, 1, 8);
        assertArrayEquals(data, result);
    }

    // ---- Predictor 10 (PNG None): per-row filter byte = 0 ----

    @Test
    public void predictor10_pngNone() {
        // 2 rows, 3 bytes per row, filter byte 0 (None)
        byte[] data = {
                0, 10, 20, 30,   // row 0: filter=0, data=10,20,30
                0, 40, 50, 60    // row 1: filter=0, data=40,50,60
        };
        byte[] result = PredictorDecoder.decode(data, 10, 3, 1, 8);
        assertArrayEquals(new byte[]{10, 20, 30, 40, 50, 60}, result);
    }

    // ---- Predictor 11 (PNG Sub) ----

    @Test
    public void predictor11_pngSub() {
        // Sub: each byte = raw + left
        // bytesPerPixel = 1 (colors=1, bpc=8)
        // Row: filter=1, raw bytes: 10, 5, 3
        // Decoded: 10, 10+5=15, 15+3=18
        byte[] data = {1, 10, 5, 3};
        byte[] result = PredictorDecoder.decode(data, 11, 3, 1, 8);
        assertArrayEquals(new byte[]{10, 15, 18}, result);
    }

    // ---- Predictor 12 (PNG Up): decode+encode round-trip ----

    @Test
    public void predictor12_pngUp_roundTrip() {
        byte[] original = {10, 20, 30, 40, 50, 60};
        byte[] encoded = PredictorDecoder.encode(original, 12, 3, 1, 8);
        byte[] decoded = PredictorDecoder.decode(encoded, 12, 3, 1, 8);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void predictor12_pngUp_knownVector() {
        // Up: each byte = raw + above
        // Row 0: filter=2, raw: 10, 20, 30  → decoded: 10, 20, 30 (no above)
        // Row 1: filter=2, raw: 5, 5, 5     → decoded: 10+5=15, 20+5=25, 30+5=35
        byte[] data = {2, 10, 20, 30, 2, 5, 5, 5};
        byte[] result = PredictorDecoder.decode(data, 12, 3, 1, 8);
        assertArrayEquals(new byte[]{10, 20, 30, 15, 25, 35}, result);
    }

    // ---- Predictor 13 (PNG Average) ----

    @Test
    public void predictor13_pngAverage() {
        // Average: each byte = raw + floor((left + above) / 2)
        // Row 0: filter=3, raw: 10, 20, 30
        // bytesPerPixel=1, so left for first byte = 0, above = 0
        // Decoded: 10+floor((0+0)/2)=10, 20+floor((10+0)/2)=25, 30+floor((25+0)/2)=42
        byte[] data = {3, 10, 20, 30};
        byte[] result = PredictorDecoder.decode(data, 13, 3, 1, 8);
        assertEquals(10, result[0] & 0xFF);
        assertEquals(25, result[1] & 0xFF);
        assertEquals(42, result[2] & 0xFF);
    }

    // ---- Predictor 14 (PNG Paeth) ----

    @Test
    public void predictor14_pngPaeth() {
        // Paeth: uses PaethPredictor(a, b, c)
        // Single row, bytesPerPixel=1
        // Row 0: filter=4, raw: 10, 5, 3
        // For byte 0: a=0, b=0, c=0 → Paeth=0 → 10+0=10
        // For byte 1: a=10, b=0, c=0 → Paeth(10,0,0): p=10, pa=0, pb=10, pc=10 → a=10 → 5+10=15
        // For byte 2: a=15, b=0, c=0 → Paeth(15,0,0): p=15, pa=0, pb=15, pc=15 → a=15 → 3+15=18
        byte[] data = {4, 10, 5, 3};
        byte[] result = PredictorDecoder.decode(data, 14, 3, 1, 8);
        assertEquals(10, result[0] & 0xFF);
        assertEquals(15, result[1] & 0xFF);
        assertEquals(18, result[2] & 0xFF);
    }

    // ---- TIFF predictor (2) ----

    @Test
    public void predictor2_tiff_horizontalDifferencing() {
        // TIFF: result[i] = raw[i] + result[i - bytesPerPixel]
        // bytesPerPixel = 1, bytesPerRow = 3, 2 rows
        // Row 0: 10, 5, 3 → 10, 10+5=15, 15+3=18
        // Row 1: 20, 7, 2 → 20, 20+7=27, 27+2=29
        byte[] data = {10, 5, 3, 20, 7, 2};
        byte[] result = PredictorDecoder.decode(data, 2, 3, 1, 8);
        assertArrayEquals(new byte[]{10, 15, 18, 20, 27, 29}, result);
    }

    @Test
    public void predictor2_tiff_roundTrip() {
        byte[] original = {10, 15, 18, 20, 27, 29};
        byte[] encoded = PredictorDecoder.encode(original, 2, 3, 1, 8);
        byte[] decoded = PredictorDecoder.decode(encoded, 2, 3, 1, 8);
        assertArrayEquals(original, decoded);
    }

    // ---- RGB image (columns=100, colors=3, bpc=8) ----

    @Test
    public void predictor12_rgbImage() {
        int columns = 100;
        int colors = 3;
        int bytesPerRow = columns * colors; // 300
        int numRows = 2;
        byte[] original = new byte[numRows * bytesPerRow];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) ((i * 7 + 13) & 0xFF);
        }
        byte[] encoded = PredictorDecoder.encode(original, 12, columns, colors, 8);
        byte[] decoded = PredictorDecoder.decode(encoded, 12, columns, colors, 8);
        assertArrayEquals(original, decoded);
    }

    // ---- xref stream (columns=1, colors=1, bpc=8) ----

    @Test
    public void predictor12_xrefStream() {
        byte[] original = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] encoded = PredictorDecoder.encode(original, 12, 1, 1, 8);
        byte[] decoded = PredictorDecoder.decode(encoded, 12, 1, 1, 8);
        assertArrayEquals(original, decoded);
    }

    // ---- Mixed row predictors ----

    @Test
    public void predictor15_mixedRowPredictors() {
        // Predictor 15 (optimum) allows each row to have a different filter type
        // Row 0: filter=1 (Sub), data: 10, 5  → decoded: 10, 15
        // Row 1: filter=2 (Up), data: 3, 3    → decoded: 10+3=13, 15+3=18
        // Row 2: filter=0 (None), data: 99, 88 → decoded: 99, 88
        byte[] data = {
                1, 10, 5,    // Row 0: Sub
                2, 3, 3,     // Row 1: Up
                0, 99, 88    // Row 2: None
        };
        byte[] result = PredictorDecoder.decode(data, 15, 2, 1, 8);
        assertArrayEquals(new byte[]{10, 15, 13, 18, 99, 88}, result);
    }

    // ---- Empty data ----

    @Test
    public void emptyData_returnsEmpty() {
        byte[] result = PredictorDecoder.decode(new byte[0], 12, 3, 1, 8);
        assertEquals(0, result.length);
    }

    // ---- Null data ----

    @Test
    public void nullData_returnsEmpty() {
        byte[] result = PredictorDecoder.decode(null, 12, 3, 1, 8);
        assertEquals(0, result.length);
    }

    // ---- Paeth predictor function ----

    @Test
    public void paethPredictor_algorithm() {
        // p = a + b - c = 10 + 20 - 5 = 25
        // pa = |25-10| = 15, pb = |25-20| = 5, pc = |25-5| = 20
        // pb <= pc and pa > pb → return b = 20
        assertEquals(20, PredictorDecoder.paethPredictor(10, 20, 5));

        // p = 0+0-0 = 0; pa=0, pb=0, pc=0 → pa<=pb && pa<=pc → return a=0
        assertEquals(0, PredictorDecoder.paethPredictor(0, 0, 0));
    }
}
