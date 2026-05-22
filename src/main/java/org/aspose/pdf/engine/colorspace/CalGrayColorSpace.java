package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * CalGray color space (ISO 32000-1:2008, §8.6.5.2).
 * A single-component CIE-based space with configurable gamma and white point.
 *
 * <p>Conversion to XYZ:</p>
 * <pre>
 *   X = Xw × A^Gamma
 *   Y = Yw × A^Gamma
 *   Z = Zw × A^Gamma
 * </pre>
 * where (Xw,Yw,Zw) = WhitePoint, A = input gray value (0..1).
 */
public final class CalGrayColorSpace extends ColorSpaceBase {

    private final double gamma;
    private final double[] whitePoint;
    private final double[] blackPoint;

    /**
     * Creates a CalGray color space from its parameter dictionary.
     *
     * @param params the CalGray parameter dictionary
     */
    public CalGrayColorSpace(COSDictionary params) {
        this.whitePoint = getTriple(params, "WhitePoint", new double[]{0.9505, 1.0, 1.089});
        this.blackPoint = getTriple(params, "BlackPoint", new double[]{0, 0, 0});
        this.gamma = params.getFloat("Gamma", 1.0f);
    }

    /**
     * Converts a CalGray value (0..1) to sRGB components.
     *
     * @param gray the gray value (0..1)
     * @return array of [r, g, b] each in 0..1
     */
    public double[] toRGB(double gray) {
        double ag = Math.pow(clamp01(gray), gamma);
        double x = whitePoint[0] * ag;
        double y = whitePoint[1] * ag;
        double z = whitePoint[2] * ag;
        return xyzToSRGB(x, y, z);
    }

    @Override
    public String getName() { return "CalGray"; }

    @Override
    public int getNumberOfComponents() { return 1; }

    /** Returns the gamma exponent. */
    public double getGamma() { return gamma; }

    /** Returns the white point [Xw, Yw, Zw]. */
    public double[] getWhitePoint() { return whitePoint.clone(); }
}
