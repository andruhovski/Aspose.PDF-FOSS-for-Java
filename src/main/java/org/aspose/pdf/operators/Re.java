package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.Arrays;
import java.util.List;

/**
 * Rectangle operator (re).
 * <p>
 * Appends a rectangle to the current path as a complete subpath, with lower-left
 * corner at (x, y) and dimensions width by height. See ISO 32000-1:2008, §8.5.2.1, Table 59.
 * </p>
 */
public class Re extends Operator {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    /**
     * Creates a Re (re) operator with the specified rectangle parameters.
     *
     * @param x      the x coordinate of the lower-left corner
     * @param y      the y coordinate of the lower-left corner
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     */
    public Re(double x, double y, double width, double height) {
        super("re", Arrays.asList(num(x), num(y), num(width), num(height)));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a Re (re) operator from parsed operands.
     * <p>
     * Expects four numeric operands: x, y, width, height.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public Re(List<COSBase> operands) {
        super("re", operands);
        this.x = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.width = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.height = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
    }

    /**
     * Returns the x coordinate of the lower-left corner.
     *
     * @return the x coordinate
     */
    public double getX() { return x; }

    /**
     * Returns the y coordinate of the lower-left corner.
     *
     * @return the y coordinate
     */
    public double getY() { return y; }

    /**
     * Returns the width of the rectangle.
     *
     * @return the width
     */
    public double getWidth() { return width; }

    /**
     * Returns the height of the rectangle.
     *
     * @return the height
     */
    public double getHeight() { return height; }
}
