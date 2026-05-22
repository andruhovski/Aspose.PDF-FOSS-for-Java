package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Set char width operator for Type 3 fonts (d0).
 * <p>
 * Sets the glyph width in a Type 3 font glyph description. The operands wx and wy
 * specify the horizontal and vertical components of the glyph displacement vector.
 * See ISO 32000-1:2008, §9.6.5, Table 113.
 * </p>
 */
public class SetCharWidth extends Operator {

    private final double wx;
    private final double wy;

    /**
     * Creates a SetCharWidth (d0) operator with the specified displacement vector.
     *
     * @param wx the horizontal displacement
     * @param wy the vertical displacement
     */
    public SetCharWidth(double wx, double wy) {
        super("d0", coords(wx, wy));
        this.wx = wx;
        this.wy = wy;
    }

    /**
     * Creates a SetCharWidth (d0) operator from parsed operands.
     * <p>
     * Expects two numeric operands: wx and wy.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetCharWidth(List<COSBase> operands) {
        super("d0", operands);
        this.wx = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.wy = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
    }

    /**
     * Returns the horizontal displacement component.
     *
     * @return wx
     */
    public double getWx() {
        return wx;
    }

    /**
     * Returns the vertical displacement component.
     *
     * @return wy
     */
    public double getWy() {
        return wy;
    }
}
