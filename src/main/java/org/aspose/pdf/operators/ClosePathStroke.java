package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Close and stroke path operator (s).
///
/// Closes the current subpath by appending a straight line segment from the current
/// point to the starting point of the subpath, then strokes the path.
/// Equivalent to h followed by S.
/// See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class ClosePathStroke extends Operator {

    /// Creates a ClosePathStroke operator.
    public ClosePathStroke() {
        super("s");
    }

    /// Creates a ClosePathStroke operator from parsed operands.
    public ClosePathStroke(List<PdfBase> operands) {
        super("s", operands);
    }
}
