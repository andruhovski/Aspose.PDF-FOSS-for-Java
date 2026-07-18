package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Close, fill, and stroke path operator (b).
///
/// Closes the current subpath, fills, and then strokes the path using the
/// nonzero winding number rule. Equivalent to h followed by B.
/// See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class ClosePathFillStroke extends Operator {

    /// Creates a ClosePathFillStroke operator.
    public ClosePathFillStroke() {
        super("b");
    }

    /// Creates a ClosePathFillStroke operator from parsed operands.
    public ClosePathFillStroke(List<PdfBase> operands) {
        super("b", operands);
    }
}
