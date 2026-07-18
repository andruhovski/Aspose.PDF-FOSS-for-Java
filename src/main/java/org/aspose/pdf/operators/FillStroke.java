package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Fill and stroke path operator (B).
///
/// Fills and then strokes the current path using the nonzero winding number rule
/// for filling. This produces the same result as constructing two identical path
/// objects, painting the first with f and the second with S.
/// See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class FillStroke extends Operator {

    /// Creates a FillStroke operator.
    public FillStroke() {
        super("B");
    }

    /// Creates a FillStroke operator from parsed operands.
    public FillStroke(List<PdfBase> operands) {
        super("B", operands);
    }
}
