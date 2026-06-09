package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * End text object operator (ET).
 * <p>
 * Ends a text object, discarding the text matrix.
 * See ISO 32000-1:2008, §9.4.1, Table 107.
 * </p>
 */
public class ET extends BlockTextOperator {

    /** Creates an ET operator. */
    public ET() {
        super("ET");
    }

    /** Creates an ET operator from parsed operands. */
    public ET(List<PdfBase> operands) {
        super("ET", operands);
    }
}
