package org.aspose.pdf.engine.colorspace;

/// The DeviceGray color space (ISO 32000-1:2008, §8.6.4.2).
///
/// One component: gray level in range [0, 1] (0=black, 1=white).
///
public final class DeviceGray extends ColorSpaceBase {

    /// Singleton instance.
    public static final DeviceGray INSTANCE = new DeviceGray();

    private DeviceGray() {}

    @Override
    public String getName() { return "DeviceGray"; }

    @Override
    public int getNumberOfComponents() { return 1; }

    /// Converts a gray level [0..1] to a packed ARGB int.
    ///
    /// @param gray the gray level (0=black, 1=white)
    /// @return packed ARGB int
    public int toRGBInt(double gray) {
        return DeviceRGB.INSTANCE.toRGBInt(gray, gray, gray);
    }
}
