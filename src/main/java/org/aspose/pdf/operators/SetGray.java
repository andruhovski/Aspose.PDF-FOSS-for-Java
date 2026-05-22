package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set gray level for non-stroking operations operator (g).
 * <p>
 * Sets the non-stroking color to the specified gray level. The value
 * must be in the range 0.0 (black) to 1.0 (white).
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetGray extends SetColorOperator {

    private final double gray;

    /**
     * Creates a SetGray (g) operator with the specified gray level.
     *
     * @param gray the gray level (0.0 to 1.0)
     */
    public SetGray(double gray) {
        super("g", Collections.singletonList(num(gray)));
        this.gray = gray;
    }

    /**
     * Creates a SetGray (g) operator from parsed operands.
     * <p>
     * Expects one numeric operand: the gray level.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetGray(List<COSBase> operands) {
        super("g", operands);
        this.gray = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
    }

    /**
     * Returns the gray level.
     *
     * @return the gray value (0.0 to 1.0)
     */
    public double getGray() {
        return gray;
    }
}
