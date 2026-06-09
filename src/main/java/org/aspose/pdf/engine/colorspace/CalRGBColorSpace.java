package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

/**
 * CalRGB color space (ISO 32000-1:2008, §8.6.5.3).
 * Three-component CIE-based space with per-channel gamma, white/black points,
 * and a 3x3 matrix transforming to CIE XYZ.
 *
 * <p>Conversion: apply gamma to each component, multiply by matrix, then XYZ→sRGB.</p>
 */
public final class CalRGBColorSpace extends ColorSpaceBase {

    private final double[] gamma;
    private final double[] whitePoint;
    private final double[] blackPoint;
    private final double[] matrix; // 9 values, column-major per PDF spec

    /**
     * Creates a CalRGB color space from its parameter dictionary.
     *
     * @param params the CalRGB parameter dictionary
     */
    public CalRGBColorSpace(PdfDictionary params) {
        this.whitePoint = getTriple(params, "WhitePoint", new double[]{0.9505, 1.0, 1.089});
        this.blackPoint = getTriple(params, "BlackPoint", new double[]{0, 0, 0});
        this.gamma = getTriple(params, "Gamma", new double[]{1, 1, 1});
        double[] mat = getNumberArray(params, "Matrix");
        this.matrix = (mat != null && mat.length == 9) ? mat
                : new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
    }

    /**
     * Converts CalRGB values (each 0..1) to sRGB components.
     *
     * @param r the red component (0..1)
     * @param g the green component (0..1)
     * @param b the blue component (0..1)
     * @return array of [r, g, b] each in 0..1
     */
    public double[] toRGB(double r, double g, double b) {
        double ar = Math.pow(clamp01(r), gamma[0]);
        double ag = Math.pow(clamp01(g), gamma[1]);
        double ab = Math.pow(clamp01(b), gamma[2]);
        // Matrix is column-major: columns are [XA YA ZA], [XB YB ZB], [XC YC ZC]
        double x = matrix[0] * ar + matrix[3] * ag + matrix[6] * ab;
        double y = matrix[1] * ar + matrix[4] * ag + matrix[7] * ab;
        double z = matrix[2] * ar + matrix[5] * ag + matrix[8] * ab;
        return xyzToSRGB(x, y, z);
    }

    @Override
    public String getName() { return "CalRGB"; }

    @Override
    public int getNumberOfComponents() { return 3; }

    /** CalRGB -> sRGB via gamma + XYZ matrix. */
    @Override
    public int toRGBInt(double[] comps) {
        if (comps == null || comps.length < 3) return 0xFF000000;
        double[] rgb = toRGB(comps[0], comps[1], comps[2]);
        return DeviceRGB.INSTANCE.toRGBInt(rgb[0], rgb[1], rgb[2]);
    }
}
