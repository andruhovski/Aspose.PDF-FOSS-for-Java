package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Set clipping path operator (W) using the non-zero winding number rule.
 * <p>
 * Modifies the current clipping path by intersecting it with the current path,
 * using the nonzero winding number rule to determine which regions lie inside
 * the clipping path.
 * See ISO 32000-1:2008, §8.5.4, Table 61.
 * </p>
 */
public class Clip extends Operator {

    /** Creates a Clip operator. */
    public Clip() {
        super("W");
    }

    /** Creates a Clip operator from parsed operands. */
    public Clip(List<COSBase> operands) {
        super("W", operands);
    }
}
