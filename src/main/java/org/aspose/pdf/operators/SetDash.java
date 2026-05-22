package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Set dash pattern operator (d).
 * <p>
 * Sets the line dash pattern in the graphics state. The dash pattern is defined by
 * a dash array and a dash phase. See ISO 32000-1:2008, §8.4.3.6, Table 57.
 * </p>
 */
public class SetDash extends Operator {

    private final double[] dashArray;
    private final double dashPhase;

    /**
     * Creates a SetDash (d) operator with the specified dash array and phase.
     *
     * @param dashArray the dash array defining the pattern of dashes and gaps
     * @param dashPhase the dash phase (offset into the dash pattern)
     */
    public SetDash(double[] dashArray, double dashPhase) {
        super("d", buildOperands(dashArray, dashPhase));
        this.dashArray = dashArray != null ? dashArray.clone() : new double[0];
        this.dashPhase = dashPhase;
    }

    /**
     * Creates a SetDash (d) operator from parsed operands.
     * <p>
     * Expects two operands: a {@link COSArray} for the dash array and a number
     * for the dash phase.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetDash(List<COSBase> operands) {
        super("d", operands);
        if (operands != null && operands.size() > 0 && operands.get(0) instanceof COSArray) {
            COSArray arr = (COSArray) operands.get(0);
            this.dashArray = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                this.dashArray[i] = getNumber(arr.get(i));
            }
        } else {
            this.dashArray = new double[0];
        }
        this.dashPhase = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
    }

    /**
     * Returns a copy of the dash array.
     *
     * @return the dash array
     */
    public double[] getDashArray() {
        return dashArray.clone();
    }

    /**
     * Returns the dash phase.
     *
     * @return the dash phase
     */
    public double getDashPhase() {
        return dashPhase;
    }

    private static List<COSBase> buildOperands(double[] dashArray, double dashPhase) {
        COSArray arr = new COSArray();
        if (dashArray != null) {
            for (double v : dashArray) {
                arr.add(num(v));
            }
        }
        return Arrays.asList(arr, num(dashPhase));
    }
}
