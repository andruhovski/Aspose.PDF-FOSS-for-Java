package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Restore graphics state operator (Q).
 * <p>
 * Restores the graphics state by removing the most recently saved state from the stack
 * and making it the current state.
 * See ISO 32000-1:2008, §8.4.2, Table 57.
 * </p>
 */
public class GRestore extends Operator {

    /** Creates a GRestore operator. */
    public GRestore() {
        super("Q");
    }

    /** Creates a GRestore operator from parsed operands. */
    public GRestore(List<PdfBase> operands) {
        super("Q", operands);
    }
}
