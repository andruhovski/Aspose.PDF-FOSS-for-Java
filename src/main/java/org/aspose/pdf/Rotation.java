package org.aspose.pdf;

/// Rotation angle enumeration for PDF pages (ISO 32000-1:2008, §7.7.3.3).
public enum Rotation {
    /// No rotation (0 degrees).
    None(0),
    /// 90 degrees clockwise.
    on90(90),
    /// 180 degrees.
    on180(180),
    /// 270 degrees clockwise (90 degrees counter-clockwise).
    on270(270);

    private final int degrees;

    Rotation(int degrees) {
        this.degrees = degrees;
    }

    /// Returns the rotation angle in degrees.
    public int getDegrees() {
        return degrees;
    }

    /// Returns the Rotation for the given degree value, or None if not matched.
    public static Rotation fromDegrees(int degrees) {
        int normalized = ((degrees % 360) + 360) % 360;
        for (Rotation r : values()) {
            if (r.degrees == normalized) return r;
        }
        return None;
    }
}
