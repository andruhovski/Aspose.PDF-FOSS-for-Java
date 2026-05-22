package org.aspose.pdf.engine.function;

import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * Type 2 (Exponential Interpolation) function (ISO 32000-1:2008, §7.10.3).
 *
 * <p>Computes: {@code f(x) = C0 + x^N × (C1 - C0)}</p>
 * <ul>
 *   <li>When N=1: linear interpolation from C0 to C1</li>
 *   <li>C0 defaults to [0.0], C1 defaults to [1.0]</li>
 *   <li>Input must be one value; output is same dimensionality as C0/C1</li>
 * </ul>
 *
 * <p>This is the most common function type for Separation tint transforms.</p>
 */
public final class ExponentialFunction extends PdfFunction {

    private final double[] c0;
    private final double[] c1;
    private final double exponent;

    /**
     * Creates an exponential function from a COS dictionary.
     *
     * @param dict   the function dictionary
     * @param domain the input domain
     * @param range  the output range (may be null)
     */
    public ExponentialFunction(COSDictionary dict, double[] domain, double[] range) {
        super(domain, range);
        this.exponent = dict.getFloat("N", 1.0f);
        double[] c0Raw = getNumberArray(dict, "C0");
        double[] c1Raw = getNumberArray(dict, "C1");

        int numOutputs = (range != null) ? range.length / 2 : (c0Raw != null ? c0Raw.length : 1);
        this.c0 = (c0Raw != null) ? c0Raw : new double[numOutputs];
        this.c1 = (c1Raw != null) ? c1Raw : fillArray(numOutputs, 1.0);
    }

    /**
     * Creates an exponential function directly (for testing).
     *
     * @param domain   the input domain
     * @param range    the output range
     * @param c0       output at input 0
     * @param c1       output at input 1
     * @param exponent the interpolation exponent N
     */
    public ExponentialFunction(double[] domain, double[] range,
                                double[] c0, double[] c1, double exponent) {
        super(domain, range);
        this.c0 = c0;
        this.c1 = c1;
        this.exponent = exponent;
    }

    @Override
    public double[] evaluate(double[] input) {
        double x = clamp(input[0], domain[0], domain[1]);
        double xn = Math.pow(x, exponent);
        double[] result = new double[c0.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = c0[i] + xn * (c1[i] - c0[i]);
            if (range != null && i * 2 + 1 < range.length) {
                result[i] = clamp(result[i], range[i * 2], range[i * 2 + 1]);
            }
        }
        return result;
    }

    /** Returns the exponent N. */
    public double getExponent() { return exponent; }
}
