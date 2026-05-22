package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Begin inline image operator (BI).
 * <p>
 * Marks the beginning of an inline image object in a content stream. The BI operator
 * is followed by key-value pairs defining the image dictionary, then the {@link ID}
 * operator, the raw image data bytes, and finally the {@link EI} operator.
 * See ISO 32000-1:2008, §8.9.7, Table 92.
 * </p>
 */
public class BI extends Operator {

    /**
     * Creates a BI operator with no operands.
     */
    public BI() {
        super("BI");
    }

    /**
     * Creates a BI operator from parsed operands.
     * <p>
     * When produced by the parser, operands may contain the inline image dictionary
     * (operands[0]) and the image data as a COS string (operands[1]).
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public BI(List<COSBase> operands) {
        super("BI", operands);
    }
}
