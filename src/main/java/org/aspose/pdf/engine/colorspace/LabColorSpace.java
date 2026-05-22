package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * Lab color space (ISO 32000-1:2008, §8.6.5.4).
 * CIE L*a*b* color space: L* = lightness (0..100), a* and b* are color opponents.
 *
 * <p>Conversion path: L*a*b* → XYZ → sRGB.</p>
 */
public final class LabColorSpace extends ColorSpaceBase {

    private final double[] whitePoint;
    private final double[] blackPoint;
    private final double[] range; // [amin amax bmin bmax]

    /**
     * Creates a Lab color space from its parameter dictionary.
     *
     * @param params the Lab parameter dictionary
     */
    public LabColorSpace(COSDictionary params) {
        this.whitePoint = getTriple(params, "WhitePoint", new double[]{0.9505, 1.0, 1.089});
        this.blackPoint = getTriple(params, "BlackPoint", new double[]{0, 0, 0});
        double[] r = getNumberArray(params, "Range");
        this.range = (r != null && r.length == 4) ? r : new double[]{-100, 100, -100, 100};
    }

    /**
     * Converts Lab values to sRGB components.
     *
     * @param lStar the L* value (0..100)
     * @param aStar the a* value (range[0]..range[1])
     * @param bStar the b* value (range[2]..range[3])
     * @return array of [r, g, b] each in 0..1
     */
    public double[] toRGB(double lStar, double aStar, double bStar) {
        lStar = Math.max(0, Math.min(100, lStar));
        aStar = Math.max(range[0], Math.min(range[1], aStar));
        bStar = Math.max(range[2], Math.min(range[3], bStar));

        // L*a*b* → XYZ
        double fy = (lStar + 16.0) / 116.0;
        double fx = aStar / 500.0 + fy;
        double fz = fy - bStar / 200.0;

        double x = whitePoint[0] * fInverse(fx);
        double y = whitePoint[1] * fInverse(fy);
        double z = whitePoint[2] * fInverse(fz);

        return xyzToSRGB(x, y, z);
    }

    /**
     * Inverse of the CIE L*a*b* nonlinear mapping function.
     * f^-1(t) = t^3 if t > δ, else 3δ²(t - 4/29), where δ = 6/29.
     */
    private static double fInverse(double t) {
        double delta = 6.0 / 29.0;
        if (t > delta) {
            return t * t * t;
        }
        return 3 * delta * delta * (t - 4.0 / 29.0);
    }

    @Override
    public String getName() { return "Lab"; }

    @Override
    public int getNumberOfComponents() { return 3; }

    /** Returns the a*b* range [amin, amax, bmin, bmax]. */
    public double[] getRange() { return range.clone(); }
}
