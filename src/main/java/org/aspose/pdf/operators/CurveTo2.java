package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Arrays;
import java.util.List;

/// Cubic Bezier curve operator with final point replicated (y).
///
/// Appends a cubic Bezier curve to the current path. The curve extends from the current
/// point to (x3, y3), using (x1, y1) as the first control point and (x3, y3) as the
/// second control point (replicated). See ISO 32000-1:2008, §8.5.2.2, Table 59.
///
public class CurveTo2 extends Operator {

    private final double x1;
    private final double y1;
    private final double x3;
    private final double y3;

    /// Creates a CurveTo2 (y) operator with the specified control and end points.
    ///
    /// @param x1 x coordinate of the first control point
    /// @param y1 y coordinate of the first control point
    /// @param x3 x coordinate of the end point (also used as second control point)
    /// @param y3 y coordinate of the end point (also used as second control point)
    public CurveTo2(double x1, double y1, double x3, double y3) {
        super("y", Arrays.asList(num(x1), num(y1), num(x3), num(y3)));
        this.x1 = x1;
        this.y1 = y1;
        this.x3 = x3;
        this.y3 = y3;
    }

    /// Creates a CurveTo2 (y) operator from parsed operands.
    ///
    /// Expects four numeric operands: x1, y1, x3, y3.
    ///
    /// @param operands the operands from the content stream parser
    public CurveTo2(List<PdfBase> operands) {
        super("y", operands);
        this.x1 = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y1 = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.x3 = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.y3 = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
    }

    /// Returns the x coordinate of the first control point.
    ///
    /// @return x1
    public double getX1() { return x1; }

    /// Returns the y coordinate of the first control point.
    ///
    /// @return y1
    public double getY1() { return y1; }

    /// Returns the x coordinate of the end point.
    ///
    /// @return x3
    public double getX3() { return x3; }

    /// Returns the y coordinate of the end point.
    ///
    /// @return y3
    public double getY3() { return y3; }
}
