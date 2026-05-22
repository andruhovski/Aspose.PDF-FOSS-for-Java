package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Fill path operator (f) using the non-zero winding number rule.
 * <p>
 * Fills the current path using the nonzero winding number rule to determine
 * the region to fill. Any open subpaths are implicitly closed before being filled.
 * See ISO 32000-1:2008, §8.5.3.1, Table 60.
 * </p>
 */
public class Fill extends Operator {

    /** Creates a Fill operator. */
    public Fill() {
        super("f");
    }

    /** Creates a Fill operator from parsed operands. */
    public Fill(List<COSBase> operands) {
        super("f", operands);
    }
}
