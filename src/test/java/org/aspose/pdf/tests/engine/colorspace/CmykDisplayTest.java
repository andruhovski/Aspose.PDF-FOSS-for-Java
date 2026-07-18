package org.aspose.pdf.tests.engine.colorspace;

import org.aspose.pdf.engine.colorspace.CmykDisplay;
import org.aspose.pdf.engine.colorspace.DeviceCMYK;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for the analytical DeviceCMYK display conversion (process-ink mixing
/// formula, see [CmykDisplay]). Anchors are the standard process-color
/// primaries; tolerances cover the mixing-model approximation.
public class CmykDisplayTest {

    private static int[] rgb(int packed) {
        return new int[]{(packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF};
    }

    @Test
    public void paperWhiteAndRegistrationBlack() {
        int[] w = rgb(CmykDisplay.toRGBInt(0, 0, 0, 0));
        assertTrue(w[0] >= 250 && w[1] >= 250 && w[2] >= 250, "no ink → white");
        int[] b = rgb(CmykDisplay.toRGBInt(1, 1, 1, 1));
        assertTrue(b[0] <= 8 && b[1] <= 8 && b[2] <= 8, "full ink → black");
    }

    @Test
    public void printPrimariesAreMuted() {
        // CGATS solid-ink anchors (±10 for LUT interpolation)
        int[] c = rgb(CmykDisplay.toRGBInt(1, 0, 0, 0));
        assertTrue(c[0] <= 16 && Math.abs(c[1] - 174) <= 10 && Math.abs(c[2] - 239) <= 10,
                "print cyan ~ (0,174,239), got " + java.util.Arrays.toString(c));
        int[] k = rgb(CmykDisplay.toRGBInt(0, 0, 0, 1));
        assertTrue(Math.abs(k[0] - 35) <= 10 && Math.abs(k[1] - 31) <= 10 && Math.abs(k[2] - 32) <= 10,
                "print black ~ (35,31,32) not pure black, got " + java.util.Arrays.toString(k));
    }

    /// The corpus-10734 background: print-neutral gray must not be greenish.
    @Test
    public void printNeutralGrayIsNeutral() {
        int[] g = rgb(CmykDisplay.toRGBInt(0.20, 0.14, 0.14, 0.04));
        assertTrue(Math.abs(g[0] - g[1]) <= 8 && Math.abs(g[1] - g[2]) <= 8,
                "C>M=Y print gray must be neutral, got " + java.util.Arrays.toString(g));
    }

    /// API contract unchanged: DeviceCMYK keeps algebraic primaries.
    @Test
    public void apiConversionKeepsAlgebraicAnchors() {
        assertEquals(0xFF00FFFF, DeviceCMYK.INSTANCE.toRGBInt(1, 0, 0, 0));
        assertEquals(0xFF000000, DeviceCMYK.INSTANCE.toRGBInt(0, 0, 0, 1));
        assertEquals(0xFFFFFFFF, DeviceCMYK.INSTANCE.toRGBInt(0, 0, 0, 0));
    }
}
