package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Set RGB color for non-stroking operations operator (rg).
 * <p>
 * Sets the non-stroking color to the specified RGB values. Each component
 * must be in the range 0.0 to 1.0.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetRGBColor extends SetColorOperator {

    private final double r;
    private final double g;
    private final double b;

    /**
     * Creates a SetRGBColor (rg) operator with the specified RGB components.
     *
     * @param r the red component (0.0 to 1.0)
     * @param g the green component (0.0 to 1.0)
     * @param b the blue component (0.0 to 1.0)
     */
    public SetRGBColor(double r, double g, double b) {
        super("rg", Arrays.asList(num(r), num(g), num(b)));
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Creates a SetRGBColor (rg) operator from parsed operands.
     * <p>
     * Expects three numeric operands: red, green, blue.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetRGBColor(List<COSBase> operands) {
        super("rg", operands);
        this.r = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.g = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.b = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
    }

    /**
     * Returns the red component.
     *
     * @return the red value (0.0 to 1.0)
     */
    public double getR() {
        return r;
    }

    /**
     * Returns the green component.
     *
     * @return the green value (0.0 to 1.0)
     */
    public double getG() {
        return g;
    }

    /**
     * Returns the blue component.
     *
     * @return the blue value (0.0 to 1.0)
     */
    public double getB() {
        return b;
    }
}
