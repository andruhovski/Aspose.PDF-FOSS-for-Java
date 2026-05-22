package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Close subpath operator (h).
 * <p>
 * Closes the current subpath by appending a straight line segment from the
 * current point to the starting point of the subpath. If the current subpath
 * is already closed, h does nothing.
 * See ISO 32000-1:2008, §8.5.2.1, Table 59.
 * </p>
 */
public class ClosePath extends Operator {

    /** Creates a ClosePath operator. */
    public ClosePath() {
        super("h");
    }

    /** Creates a ClosePath operator from parsed operands. */
    public ClosePath(List<COSBase> operands) {
        super("h", operands);
    }
}
