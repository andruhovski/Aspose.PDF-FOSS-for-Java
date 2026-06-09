package org.aspose.pdf.engine.colorspace;

/**
 * The DeviceCMYK color space (ISO 32000-1:2008, §8.6.4.4).
 * <p>
 * Four components: Cyan, Magenta, Yellow, blacK, each in range [0, 1].
 * </p>
 */
public final class DeviceCMYK extends ColorSpaceBase {

    /** Singleton instance. */
    public static final DeviceCMYK INSTANCE = new DeviceCMYK();

    private DeviceCMYK() {}

    @Override
    public String getName() { return "DeviceCMYK"; }

    @Override
    public int getNumberOfComponents() { return 4; }

    /**
     * Converts CMYK components [0..1] to a packed ARGB int using the standard
     * multiplicative ("no ICC profile") formula
     * <pre>
     *   R = (1 - C) * (1 - K) * 255
     *   G = (1 - M) * (1 - K) * 255
     *   B = (1 - Y) * (1 - K) * 255
     * </pre>
     * This preserves the algebraic primaries — pure C maps to
     * {@code (0,255,255)}, pure K to {@code (0,0,0)}, registration black
     * {@code (1,1,1,1)} to black, white {@code (0,0,0,0)} to white — which is
     * what the public API contract assumes (see
     * {@code ColorSpaceTest.testDeviceCMYKToInt} and
     * {@code AsposeColorActionPortedTest.createDeviceCMYK}), and matches both
     * {@code ColorConverter.cmykToRgb} and PDFBox's non-ICC mapping. The
     * previous additive variant {@code 1 - min(1, C + K)} crushed mid-tone
     * mixes toward black (corpus 10734: banner blue CMYK
     * {@code 0.86,0.42,0,0.43} rendered {@code #000045} instead of
     * {@code #145592}).
     * <p>
     * <b>Visual rendering caveat.</b> Real ICC-aware renderers (Adobe Acrobat,
     * Foxit, Chrome PDF) convert CMYK through a print profile such as
     * USWebCoatedSWOP, which yields more muted output. We intentionally do
     * <i>not</i> bundle an ICC profile (the project has a zero-dependency
     * rule), so visual diffs against ICC-rendered templates can stay slightly
     * brighter/more saturated. That is a property of this color space, not a
     * bug.
     * </p>
     *
     * @param c cyan (0..1)
     * @param m magenta (0..1)
     * @param y yellow (0..1)
     * @param k black (0..1)
     * @return packed ARGB int
     */
    public int toRGBInt(double c, double m, double y, double k) {
        double kk = 1.0 - Math.max(0.0, Math.min(1.0, k));
        int r = clamp((int) Math.round((1.0 - Math.max(0.0, Math.min(1.0, c))) * kk * 255));
        int g = clamp((int) Math.round((1.0 - Math.max(0.0, Math.min(1.0, m))) * kk * 255));
        int b = clamp((int) Math.round((1.0 - Math.max(0.0, Math.min(1.0, y))) * kk * 255));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
