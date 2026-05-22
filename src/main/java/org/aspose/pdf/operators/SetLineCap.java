package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set line cap style operator (J).
 * <p>
 * Sets the line cap style in the graphics state. Values: 0 = butt cap,
 * 1 = round cap, 2 = projecting square cap. See ISO 32000-1:2008, §8.4.3.3, Table 54.
 * </p>
 */
public class SetLineCap extends Operator {

    private final int lineCap;

    /**
     * Creates a SetLineCap (J) operator with the specified line cap style.
     *
     * @param lineCap the line cap style (0, 1, or 2)
     */
    public SetLineCap(int lineCap) {
        super("J", Collections.singletonList(num(lineCap)));
        this.lineCap = lineCap;
    }

    /**
     * Creates a SetLineCap (J) operator from parsed operands.
     * <p>
     * Expects one numeric operand: the line cap style.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetLineCap(List<COSBase> operands) {
        super("J", operands);
        this.lineCap = (operands != null && operands.size() > 0) ? (int) getNumber(operands.get(0)) : 0;
    }

    /**
     * Returns the line cap style.
     *
     * @return the line cap style (0 = butt, 1 = round, 2 = projecting square)
     */
    public int getLineCap() {
        return lineCap;
    }
}
