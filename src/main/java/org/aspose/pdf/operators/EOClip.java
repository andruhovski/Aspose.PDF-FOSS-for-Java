package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Set clipping path operator (W*) using the even-odd rule.
 * <p>
 * Modifies the current clipping path by intersecting it with the current path,
 * using the even-odd rule to determine which regions lie inside the clipping path.
 * See ISO 32000-1:2008, §8.5.4, Table 61.
 * </p>
 */
public class EOClip extends Operator {

    /** Creates an EOClip operator. */
    public EOClip() {
        super("W*");
    }

    /** Creates an EOClip operator from parsed operands. */
    public EOClip(List<COSBase> operands) {
        super("W*", operands);
    }
}
