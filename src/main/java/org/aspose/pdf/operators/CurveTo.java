package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Cubic Bezier curve operator (c).
 * <p>
 * Appends a cubic Bezier curve to the current path. The curve extends from the current
 * point to the point (x3, y3), using (x1, y1) and (x2, y2) as control points.
 * See ISO 32000-1:2008, §8.5.2.2, Table 59.
 * </p>
 */
public class CurveTo extends Operator {

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;
    private final double x3;
    private final double y3;

    /**
     * Creates a CurveTo (c) operator with the specified control and end points.
     *
     * @param x1 x coordinate of the first control point
     * @param y1 y coordinate of the first control point
     * @param x2 x coordinate of the second control point
     * @param y2 y coordinate of the second control point
     * @param x3 x coordinate of the end point
     * @param y3 y coordinate of the end point
     */
    public CurveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        super("c", Arrays.asList(num(x1), num(y1), num(x2), num(y2), num(x3), num(y3)));
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    /**
     * Creates a CurveTo (c) operator from parsed operands.
     * <p>
     * Expects six numeric operands: x1, y1, x2, y2, x3, y3.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public CurveTo(List<COSBase> operands) {
        super("c", operands);
        this.x1 = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y1 = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.x2 = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.y2 = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
        this.x3 = (operands != null && operands.size() > 4) ? getNumber(operands.get(4)) : 0;
        this.y3 = (operands != null && operands.size() > 5) ? getNumber(operands.get(5)) : 0;
    }

    /**
     * Returns the x coordinate of the first control point.
     *
     * @return x1
     */
    public double getX1() { return x1; }

    /**
     * Returns the y coordinate of the first control point.
     *
     * @return y1
     */
    public double getY1() { return y1; }

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
