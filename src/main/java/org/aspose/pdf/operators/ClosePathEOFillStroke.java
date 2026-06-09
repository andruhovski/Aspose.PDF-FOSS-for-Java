package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Close, fill (even-odd), and stroke path operator (b*).
 * <p>
 * Closes the current subpath, fills using the even-odd rule, and then strokes
 * the path. Equivalent to h followed by B*.
 * See ISO 32000-1:2008, §8.5.3.1, Table 60.
 * </p>
 */
public class ClosePathEOFillStroke extends Operator {

    /** Creates a ClosePathEOFillStroke operator. */
    public ClosePathEOFillStroke() {
        super("b*");
    }

    /** Creates a ClosePathEOFillStroke operator from parsed operands. */
    public ClosePathEOFillStroke(List<PdfBase> operands) {
        super("b*", operands);
    }
}
