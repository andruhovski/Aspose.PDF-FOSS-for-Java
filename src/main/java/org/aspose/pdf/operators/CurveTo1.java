package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Cubic Bezier curve operator with initial point replicated (v).
 * <p>
 * Appends a cubic Bezier curve to the current path. The curve extends from the current
 * point to (x3, y3), using the current point as the first control point and (x2, y2)
 * as the second control point. See ISO 32000-1:2008, §8.5.2.2, Table 59.
 * </p>
 */
public class CurveTo1 extends Operator {

    private final double x2;
    private final double y2;
    private final double x3;
    private final double y3;

    /**
     * Creates a CurveTo1 (v) operator with the specified control and end points.
     *
     * @param x2 x coordinate of the second control point
     * @param y2 y coordinate of the second control point
     * @param x3 x coordinate of the end point
     * @param y3 y coordinate of the end point
     */
    public CurveTo1(double x2, double y2, double x3, double y3) {
        super("v", Arrays.asList(num(x2), num(y2), num(x3), num(y3)));
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    /**
     * Creates a CurveTo1 (v) operator from parsed operands.
     * <p>
     * Expects four numeric operands: x2, y2, x3, y3.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public CurveTo1(List<COSBase> operands) {
        super("v", operands);
        this.x2 = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y2 = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.x3 = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.y3 = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
    }

    /**
     * Returns the x coordinate of the second control point.
     *
     * @return x2
     */
    public double getX2() { return x2; }

    /**
     * Returns the y coordinate of the second control point.
     *
     * @return y2
     */
    public double getY2() { return y2; }

    /**
     * Returns the x coordinate of the end point.
     *
     * @return x3
     */
    public double getX3() { return x3; }

    /**
     * Returns the y coordinate of the end point.
     *
     * @return y3
     */
    public double getY3() { return y3; }
}
