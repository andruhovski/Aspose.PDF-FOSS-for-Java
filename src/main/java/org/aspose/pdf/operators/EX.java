package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * End compatibility section operator (EX).
 * <p>
 * Ends a compatibility section begun by a BX operator.
 * See ISO 32000-1:2008, §7.8.2, Table 32.
 * </p>
 */
public class EX extends Operator {

    /** Creates an EX operator. */
    public EX() {
        super("EX");
    }

    /** Creates an EX operator from parsed operands. */
    public EX(List<COSBase> operands) {
        super("EX", operands);
    }
}
