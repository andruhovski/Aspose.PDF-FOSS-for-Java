package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/**
 * Set miter limit operator (M).
 * <p>
 * Sets the miter limit in the graphics state. See ISO 32000-1:2008, §8.4.3.5, Table 57.
 * </p>
 */
public class SetMiterLimit extends Operator {

    private final double miterLimit;

    /**
     * Creates a SetMiterLimit (M) operator with the specified miter limit.
     *
     * @param miterLimit the miter limit value
     */
    public SetMiterLimit(double miterLimit) {
        super("M", Collections.singletonList(num(miterLimit)));
        this.miterLimit = miterLimit;
    }

    /**
     * Creates a SetMiterLimit (M) operator from parsed operands.
     * <p>
     * Expects one numeric operand: the miter limit.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetMiterLimit(List<PdfBase> operands) {
        super("M", operands);
        this.miterLimit = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
    }

    /**
     * Returns the miter limit.
     *
     * @return the miter limit value
     */
    public double getMiterLimit() {
        return miterLimit;
    }
}
