package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Fill and stroke path operator (B\*) using the even-odd rule.
///
/// Fills and then strokes the current path using the even-odd rule for filling.
/// See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class EOFillStroke extends Operator {

    /// Creates an EOFillStroke operator.
    public EOFillStroke() {
        super("B*");
    }

    /// Creates an EOFillStroke operator from parsed operands.
    public EOFillStroke(List<PdfBase> operands) {
        super("B*", operands);
    }
}
