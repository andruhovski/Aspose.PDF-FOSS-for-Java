package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Move-to operator (m).
 * <p>
 * Begins a new subpath by moving the current point to coordinates (x, y),
 * omitting any connecting line segment. See ISO 32000-1:2008, §8.5.2.1, Table 59.
 * </p>
 */
public class MoveTo extends Operator {

    private final double x;
    private final double y;

    /**
     * Creates a MoveTo (m) operator with the specified coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public MoveTo(double x, double y) {
        super("m", coords(x, y));
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a MoveTo (m) operator from parsed operands.
     * <p>
     * Expects two numeric operands: x and y.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public MoveTo(List<PdfBase> operands) {
        super("m", operands);
        this.x = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
    }

    /**
     * Returns the x coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }
}
