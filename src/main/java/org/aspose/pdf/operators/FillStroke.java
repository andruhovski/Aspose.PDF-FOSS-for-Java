package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Fill and stroke path operator (B).
 * <p>
 * Fills and then strokes the current path using the nonzero winding number rule
 * for filling. This produces the same result as constructing two identical path
 * objects, painting the first with f and the second with S.
 * See ISO 32000-1:2008, §8.5.3.1, Table 60.
 * </p>
 */
public class FillStroke extends Operator {

    /** Creates a FillStroke operator. */
    public FillStroke() {
        super("B");
    }

    /** Creates a FillStroke operator from parsed operands. */
    public FillStroke(List<COSBase> operands) {
        super("B", operands);
    }
}
