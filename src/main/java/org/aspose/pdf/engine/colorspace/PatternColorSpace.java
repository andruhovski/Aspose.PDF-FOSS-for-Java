package org.aspose.pdf.engine.colorspace;

/// Pattern color space (ISO 32000-1:2008, §8.6.6.2).
/// Used for tiling and shading patterns. The "color" is a pattern object,
/// not numeric components. This class is primarily a marker.
public final class PatternColorSpace extends ColorSpaceBase {

    /// Singleton instance.
    public static final PatternColorSpace INSTANCE = new PatternColorSpace();

    private PatternColorSpace() {}

    @Override
    public String getName() { return "Pattern"; }

    @Override
    public int getNumberOfComponents() { return 0; }
}
