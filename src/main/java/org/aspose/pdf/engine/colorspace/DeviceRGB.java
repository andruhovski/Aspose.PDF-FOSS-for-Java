package org.aspose.pdf.engine.colorspace;

/**
 * The DeviceRGB color space (ISO 32000-1:2008, §8.6.4.3).
 * <p>
 * Three components: Red, Green, Blue, each in range [0, 1].
 * </p>
 */
public final class DeviceRGB extends ColorSpaceBase {

    /** Singleton instance. */
    public static final DeviceRGB INSTANCE = new DeviceRGB();

    private DeviceRGB() {}

    @Override
    public String getName() { return "DeviceRGB"; }

    @Override
    public int getNumberOfComponents() { return 3; }

    /**
     * Converts RGB components [0..1] to a packed ARGB int.
     *
     * @param r red component (0..1)
     * @param g green component (0..1)
     * @param b blue component (0..1)
     * @return packed ARGB int (alpha=0xFF)
     */
    public int toRGBInt(double r, double g, double b) {
        return (0xFF << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(double v) {
        return Math.max(0, Math.min(255, (int) (v * 255 + 0.5)));
    }
}
