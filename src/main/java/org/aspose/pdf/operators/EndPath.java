package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * End path operator (n) without filling or stroking.
 * <p>
 * Ends the path object without filling or stroking it. This operator is a
 * path-painting no-op, primarily used with clipping path operators.
 * See ISO 32000-1:2008, §8.5.3.1, Table 60.
 * </p>
 */
public class EndPath extends Operator {

    /** Creates an EndPath operator. */
    public EndPath() {
        super("n");
    }

    /** Creates an EndPath operator from parsed operands. */
    public EndPath(List<COSBase> operands) {
        super("n", operands);
    }
}
