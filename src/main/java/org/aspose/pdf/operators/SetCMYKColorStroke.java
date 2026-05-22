package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Set CMYK color for stroking operations operator (K).
 * <p>
 * Sets the stroking color to the specified CMYK values. Each component
 * must be in the range 0.0 to 1.0.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetCMYKColorStroke extends SetColorOperator {

    private final double c;
    private final double m;
    private final double y;
    private final double k;

    /**
     * Creates a SetCMYKColorStroke (K) operator with the specified CMYK components.
     *
     * @param c the cyan component (0.0 to 1.0)
     * @param m the magenta component (0.0 to 1.0)
     * @param y the yellow component (0.0 to 1.0)
     * @param k the black component (0.0 to 1.0)
     */
    public SetCMYKColorStroke(double c, double m, double y, double k) {
        super("K", Arrays.asList(num(c), num(m), num(y), num(k)));
        this.c = c;
        this.m = m;
        this.y = y;
        this.k = k;
    }

    /**
     * Creates a SetCMYKColorStroke (K) operator from parsed operands.
     * <p>
     * Expects four numeric operands: cyan, magenta, yellow, black.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetCMYKColorStroke(List<COSBase> operands) {
        super("K", operands);
        this.c = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.m = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.y = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.k = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
    }

    /**
     * Returns the cyan component.
     *
     * @return the cyan value (0.0 to 1.0)
     */
    public double getC() {
        return c;
    }

    /**
     * Returns the magenta component.
     *
     * @return the magenta value (0.0 to 1.0)
     */
    public double getM() {
        return m;
    }

    /**
     * Returns the yellow component.
     *
     * @return the yellow value (0.0 to 1.0)
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the black component.
     *
     * @return the black value (0.0 to 1.0)
     */
    public double getK() {
        return k;
    }
}
