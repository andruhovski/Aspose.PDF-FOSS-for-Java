package org.aspose.pdf.devices;

import java.util.logging.Logger;

/// Settings for TIFF image output when using [TiffDevice].
///
/// Controls compression type, color depth, shape, blank page skipping,
/// and brightness adjustment.
///
public class TiffSettings {

    private static final Logger LOG = Logger.getLogger(TiffSettings.class.getName());

    private CompressionType compression = CompressionType.LZW;
    private ColorDepth depth = ColorDepth.Default;
    private ShapeType shape = ShapeType.None;
    private boolean skipBlankPages;
    private float brightness = 0.5f;

    /// Creates a TiffSettings with default values.
    public TiffSettings() {
        // defaults applied by field initializers
    }

    /// Creates a TiffSettings with the specified color depth (e.g. 1-bpp for
    /// fax-style output). Compression type defaults to whatever the
    /// field-initializer for [#compression] declares.
    ///
    /// @param depth the color depth
    public TiffSettings(ColorDepth depth) {
        this.depth = depth;
    }

    /// Creates a TiffSettings with the specified compression type.
    ///
    /// @param compression the compression type
    public TiffSettings(CompressionType compression) {
        this.compression = compression;
    }

    /// Creates a TiffSettings with the specified compression type and color depth.
    ///
    /// @param compression the compression type
    /// @param depth       the color depth
    public TiffSettings(CompressionType compression, ColorDepth depth) {
        this.compression = compression;
        this.depth = depth;
    }

    /// Returns the compression type for the TIFF output.
    ///
    /// @return the compression type
    public CompressionType getCompression() {
        return compression;
    }

    /// Sets the compression type for the TIFF output.
    ///
    /// @param compression the compression type
    public void setCompression(CompressionType compression) {
        this.compression = compression;
    }

    /// Returns the color depth for the TIFF output.
    ///
    /// @return the color depth
    public ColorDepth getDepth() {
        return depth;
    }

    /// Sets the color depth for the TIFF output.
    ///
    /// @param depth the color depth
    public void setDepth(ColorDepth depth) {
        this.depth = depth;
    }

    /// Returns the shape type (orientation) for the TIFF output.
    ///
    /// @return the shape type
    public ShapeType getShape() {
        return shape;
    }

    /// Sets the shape type (orientation) for the TIFF output.
    ///
    /// @param shape the shape type
    public void setShape(ShapeType shape) {
        this.shape = shape;
    }

    /// Returns whether blank pages should be skipped in the TIFF output.
    ///
    /// @return `true` if blank pages are skipped
    public boolean isSkipBlankPages() {
        return skipBlankPages;
    }

    /// Sets whether blank pages should be skipped in the TIFF output.
    ///
    /// @param skipBlankPages`true` to skip blank pages
    public void setSkipBlankPages(boolean skipBlankPages) {
        this.skipBlankPages = skipBlankPages;
    }

    /// Returns the brightness adjustment value (0.0 to 1.0).
    ///
    /// @return the brightness value
    public float getBrightness() {
        return brightness;
    }

    /// Sets the brightness adjustment value (0.0 to 1.0).
    ///
    /// @param brightness the brightness value
    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }
}
