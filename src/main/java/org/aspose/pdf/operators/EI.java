package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * End inline image operator (EI).
 * <p>
 * Marks the end of an inline image object begun by the {@link BI} operator.
 * See ISO 32000-1:2008, §8.9.7, Table 92.
 * </p>
 */
public class EI extends Operator {

    /**
     * Creates an EI operator with no operands.
     */
    public EI() {
        super("EI");
    }

    /**
     * Creates an EI operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public EI(List<COSBase> operands) {
        super("EI", operands);
    }
}
