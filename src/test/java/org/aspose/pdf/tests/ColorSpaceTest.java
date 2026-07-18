package org.aspose.pdf.tests;

import org.aspose.pdf.engine.colorspace.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for color space classes.
public class ColorSpaceTest {

    @Test
    public void testDeviceRGBSingleton() {
        assertSame(DeviceRGB.INSTANCE, DeviceRGB.INSTANCE);
        assertEquals("DeviceRGB", DeviceRGB.INSTANCE.getName());
        assertEquals(3, DeviceRGB.INSTANCE.getNumberOfComponents());
    }

    @Test
    public void testDeviceCMYKSingleton() {
        assertEquals("DeviceCMYK", DeviceCMYK.INSTANCE.getName());
        assertEquals(4, DeviceCMYK.INSTANCE.getNumberOfComponents());
    }

    @Test
    public void testDeviceGraySingleton() {
        assertEquals("DeviceGray", DeviceGray.INSTANCE.getName());
        assertEquals(1, DeviceGray.INSTANCE.getNumberOfComponents());
    }

    @Test
    public void testDeviceRGBToInt() {
        int white = DeviceRGB.INSTANCE.toRGBInt(1.0, 1.0, 1.0);
        assertEquals(0xFFFFFFFF, white);
        int black = DeviceRGB.INSTANCE.toRGBInt(0.0, 0.0, 0.0);
        assertEquals(0xFF000000, black);
        int red = DeviceRGB.INSTANCE.toRGBInt(1.0, 0.0, 0.0);
        assertEquals(0xFFFF0000, red);
    }

    @Test
    public void testDeviceCMYKToInt() {
        // DeviceCMYK uses the naive subtractive formula (no ICC profile),
        // so primaries map to algebraically pure RGB.
        assertEquals(0xFF000000, DeviceCMYK.INSTANCE.toRGBInt(0, 0, 0, 1), "pure K → black");
        assertEquals(0xFFFFFFFF, DeviceCMYK.INSTANCE.toRGBInt(0, 0, 0, 0), "no ink → white");
    }

    @Test
    public void cmykPrimariesMapCleanlyToRgb() {
        DeviceCMYK cmyk = DeviceCMYK.INSTANCE;

        assertEquals(0xFF000000, cmyk.toRGBInt(0, 0, 0, 1), "pure K → black");
        assertEquals(0xFFFFFFFF, cmyk.toRGBInt(0, 0, 0, 0), "no ink → white");

        assertEquals(0xFF00FFFF, cmyk.toRGBInt(1, 0, 0, 0), "pure C → cyan");
        assertEquals(0xFFFF00FF, cmyk.toRGBInt(0, 1, 0, 0), "pure M → magenta");
        assertEquals(0xFFFFFF00, cmyk.toRGBInt(0, 0, 1, 0), "pure Y → yellow");

        assertEquals(0xFF000000, cmyk.toRGBInt(1, 1, 1, 1), "registration black");

        // Mid-tones — sanity, not exact
        int gray = cmyk.toRGBInt(0, 0, 0, 0.5);
        int r = (gray >> 16) & 0xFF;
        assertTrue(r >= 120 && r <= 140, "50% K should give mid-gray, got R=" + r);
    }

    @Test
    public void testDeviceGrayToInt() {
        int black = DeviceGray.INSTANCE.toRGBInt(0.0);
        assertEquals(0xFF000000, black);
        int white = DeviceGray.INSTANCE.toRGBInt(1.0);
        assertEquals(0xFFFFFFFF, white);
    }

    @Test
    public void testResolveDeviceRGBName() throws IOException {
        ColorSpaceBase cs = ColorSpaceBase.resolve(PdfName.of("DeviceRGB"), null, null);
        assertSame(DeviceRGB.INSTANCE, cs);
    }

    @Test
    public void testResolveDeviceCMYKName() throws IOException {
        ColorSpaceBase cs = ColorSpaceBase.resolve(PdfName.of("DeviceCMYK"), null, null);
        assertSame(DeviceCMYK.INSTANCE, cs);
    }

    @Test
    public void testResolveDeviceGrayName() throws IOException {
        ColorSpaceBase cs = ColorSpaceBase.resolve(PdfName.of("DeviceGray"), null, null);
        assertSame(DeviceGray.INSTANCE, cs);
    }

    @Test
    public void testResolveShortNames() throws IOException {
        assertSame(DeviceRGB.INSTANCE, ColorSpaceBase.resolve(PdfName.of("RGB"), null, null));
        assertSame(DeviceCMYK.INSTANCE, ColorSpaceBase.resolve(PdfName.of("CMYK"), null, null));
        assertSame(DeviceGray.INSTANCE, ColorSpaceBase.resolve(PdfName.of("G"), null, null));
    }

    @Test
    public void testResolveNull() throws IOException {
        ColorSpaceBase cs = ColorSpaceBase.resolve(null, null, null);
        assertSame(DeviceRGB.INSTANCE, cs);
    }

    @Test
    public void testResolveICCBased() throws IOException {
        PdfStream iccStream = new PdfStream();
        iccStream.set(PdfName.of("N"), PdfInteger.valueOf(3));
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("ICCBased"));
        arr.add(iccStream);

        ColorSpaceBase cs = ColorSpaceBase.resolve(arr, null, null);
        assertTrue(cs instanceof ICCBasedColorSpace);
        assertEquals(3, cs.getNumberOfComponents());
        assertEquals("ICCBased", cs.getName());
    }

    @Test
    public void testICCBasedAlternate() throws IOException {
        PdfStream iccStream = new PdfStream();
        iccStream.set(PdfName.of("N"), PdfInteger.valueOf(1));
        ICCBasedColorSpace cs = new ICCBasedColorSpace(iccStream, null);
        assertEquals(1, cs.getNumberOfComponents());
        assertSame(DeviceGray.INSTANCE, cs.getAlternate());
    }

    @Test
    public void testICCBasedDefaultAlternate4() throws IOException {
        PdfStream iccStream = new PdfStream();
        iccStream.set(PdfName.of("N"), PdfInteger.valueOf(4));
        ICCBasedColorSpace cs = new ICCBasedColorSpace(iccStream, null);
        assertSame(DeviceCMYK.INSTANCE, cs.getAlternate());
    }

    @Test
    public void testIndexedColorSpace() {
        // 4 entries of RGB (3 bytes each = 12 bytes)
        byte[] palette = new byte[]{
            (byte) 255, 0, 0,       // index 0: red
            0, (byte) 255, 0,       // index 1: green
            0, 0, (byte) 255,       // index 2: blue
            (byte) 255, (byte) 255, 0  // index 3: yellow
        };
        IndexedColorSpace cs = new IndexedColorSpace(DeviceRGB.INSTANCE, 3, palette);
        assertEquals("Indexed", cs.getName());
        assertEquals(1, cs.getNumberOfComponents());
        assertSame(DeviceRGB.INSTANCE, cs.getBase());

        double[] red = cs.lookupColor(0);
        assertEquals(3, red.length);
        assertEquals(1.0, red[0], 0.01); // R
        assertEquals(0.0, red[1], 0.01); // G
        assertEquals(0.0, red[2], 0.01); // B

        double[] green = cs.lookupColor(1);
        assertEquals(0.0, green[0], 0.01);
        assertEquals(1.0, green[1], 0.01);
    }

    @Test
    public void testIndexedFromArray() throws IOException {
        byte[] palette = new byte[]{(byte) 255, 0, 0, 0, (byte) 255, 0};
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("Indexed"));
        arr.add(PdfName.of("DeviceRGB"));
        arr.add(PdfInteger.valueOf(1));
        arr.add(new PdfString(palette));

        ColorSpaceBase cs = ColorSpaceBase.resolve(arr, null, null);
        assertTrue(cs instanceof IndexedColorSpace);
        IndexedColorSpace ics = (IndexedColorSpace) cs;
        assertEquals(1, ics.getHival());
        double[] c0 = ics.lookupColor(0);
        assertEquals(1.0, c0[0], 0.01); // red
    }

    // ═══════════════════════════════════════════════════════════════
    //  CalGray
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testResolveCalGray() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("CalGray"));
        arr.add(new PdfDictionary());
        ColorSpaceBase cs = ColorSpaceBase.resolve(arr, null, null);
        assertTrue(cs instanceof CalGrayColorSpace);
        assertEquals("CalGray", cs.getName());
        assertEquals(1, cs.getNumberOfComponents());
    }

    @Test
    public void testCalGrayGamma1MidGray() {
        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("Gamma"), new PdfFloat(1.0f));
        CalGrayColorSpace cs = new CalGrayColorSpace(params);
        double[] rgb = cs.toRGB(0.5);
        // With gamma=1 and D65 whitepoint, 0.5 should produce a mid-gray
        assertTrue(rgb[0] > 0.3 && rgb[0] < 0.9, "R should be mid-range: " + rgb[0]);
        assertEquals(rgb[0], rgb[1], 0.05); // Should be nearly achromatic
        assertEquals(rgb[1], rgb[2], 0.05);
    }

    @Test
    public void testCalGrayGamma22() {
        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("Gamma"), new PdfFloat(2.2f));
        CalGrayColorSpace cs = new CalGrayColorSpace(params);
        double[] rgb1 = cs.toRGB(0.5);
        // With gamma 2.2, 0.5^2.2 ≈ 0.217 → darker than gamma 1
        PdfDictionary params2 = new PdfDictionary();
        params2.set(PdfName.of("Gamma"), new PdfFloat(1.0f));
        CalGrayColorSpace cs2 = new CalGrayColorSpace(params2);
        double[] rgb2 = cs2.toRGB(0.5);
        assertTrue(rgb1[0] < rgb2[0], "Gamma 2.2 should be darker at 0.5 than gamma 1");
    }

    // ═══════════════════════════════════════════════════════════════
    //  CalRGB
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testResolveCalRGB() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("CalRGB"));
        arr.add(new PdfDictionary());
        ColorSpaceBase cs = ColorSpaceBase.resolve(arr, null, null);
        assertTrue(cs instanceof CalRGBColorSpace);
        assertEquals("CalRGB", cs.getName());
        assertEquals(3, cs.getNumberOfComponents());
    }

    @Test
    public void testCalRGBIdentityMatrix() {
        PdfDictionary params = new PdfDictionary();
        // Identity matrix + gamma 1 → should approximate pass-through
        CalRGBColorSpace cs = new CalRGBColorSpace(params);
        double[] rgb = cs.toRGB(1, 0, 0);
        // With identity matrix, (1,0,0) → XYZ=(1,0,0) → sRGB should be reddish
        assertTrue(rgb[0] > 0.5, "R should be dominant");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Lab
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testResolveLab() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("Lab"));
        arr.add(new PdfDictionary());
        ColorSpaceBase cs = ColorSpaceBase.resolve(arr, null, null);
        assertTrue(cs instanceof LabColorSpace);
        assertEquals("Lab", cs.getName());
        assertEquals(3, cs.getNumberOfComponents());
    }

    @Test
    public void testLabWhite() {
        PdfDictionary params = new PdfDictionary();
        LabColorSpace cs = new LabColorSpace(params);
        double[] rgb = cs.toRGB(100, 0, 0); // L*=100, a*=0, b*=0 → white
        assertTrue(rgb[0] > 0.95, "R should be near 1.0 for white: " + rgb[0]);
        assertTrue(rgb[1] > 0.95, "G should be near 1.0 for white: " + rgb[1]);
        assertTrue(rgb[2] > 0.95, "B should be near 1.0 for white: " + rgb[2]);
    }

    @Test
    public void testLabBlack() {
        PdfDictionary params = new PdfDictionary();
        LabColorSpace cs = new LabColorSpace(params);
        double[] rgb = cs.toRGB(0, 0, 0); // L*=0 → black
        assertTrue(rgb[0] < 0.05, "R should be near 0 for black: " + rgb[0]);
        assertTrue(rgb[1] < 0.05, "G should be near 0 for black: " + rgb[1]);
        assertTrue(rgb[2] < 0.05, "B should be near 0 for black: " + rgb[2]);
    }

    @Test
    public void testLabRedish() {
        PdfDictionary params = new PdfDictionary();
        LabColorSpace cs = new LabColorSpace(params);
        double[] rgb = cs.toRGB(50, 80, 0); // L*=50, a*=80 → reddish
        assertTrue(rgb[0] > rgb[1], "R should be greater than G for positive a*");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Separation
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testSeparationBasic() {
        org.aspose.pdf.engine.function.ExponentialFunction func =
                new org.aspose.pdf.engine.function.ExponentialFunction(
                        new double[]{0, 1}, new double[]{0, 1, 0, 1, 0, 1, 0, 1},
                        new double[]{0, 0, 0, 0},   // tint 0 → no ink
                        new double[]{1, 0, 0, 0},    // tint 1 → full cyan
                        1.0);
        SeparationColorSpace cs = new SeparationColorSpace("Cyan", DeviceCMYK.INSTANCE, func);
        assertEquals("Separation", cs.getName());
        assertEquals(1, cs.getNumberOfComponents());
        assertEquals("Cyan", cs.getColorantName());
        double[] alt = cs.tintToAlternate(1.0);
        assertEquals(4, alt.length);
        assertEquals(1.0, alt[0], 1e-6); // full cyan
    }

    @Test
    public void testSeparationNone() {
        SeparationColorSpace cs = new SeparationColorSpace("None", DeviceCMYK.INSTANCE, null);
        double[] alt = cs.tintToAlternate(1.0);
        assertEquals(4, alt.length);
        assertEquals(0, alt[0], 1e-6); // None → all zeros
    }

    // ═══════════════════════════════════════════════════════════════
    //  DeviceN
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testDeviceNBasic() {
        DeviceNColorSpace cs = new DeviceNColorSpace(
                new String[]{"Cyan", "Magenta"}, DeviceCMYK.INSTANCE, null);
        assertEquals("DeviceN", cs.getName());
        assertEquals(2, cs.getNumberOfComponents());
        assertArrayEquals(new String[]{"Cyan", "Magenta"}, cs.getColorantNames());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Pattern
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void testPatternColorSpace() {
        assertEquals("Pattern", PatternColorSpace.INSTANCE.getName());
        assertEquals(0, PatternColorSpace.INSTANCE.getNumberOfComponents());
    }

    @Test
    public void testResolvePatternByName() throws IOException {
        ColorSpaceBase cs = ColorSpaceBase.resolve(PdfName.of("Pattern"), null, null);
        assertSame(PatternColorSpace.INSTANCE, cs);
    }
}
