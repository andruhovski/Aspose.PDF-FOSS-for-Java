package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Save graphics state operator (q).
 * <p>
 * Pushes a copy of the entire graphics state onto the graphics state stack.
 * See ISO 32000-1:2008, §8.4.2, Table 57.
 * </p>
 */
public class GSave extends Operator {

    /** Creates a GSave operator. */
    public GSave() {
        super("q");
    }

    /** Creates a GSave operator from parsed operands. */
    public GSave(List<PdfBase> operands) {
        super("q", operands);
    }
}
