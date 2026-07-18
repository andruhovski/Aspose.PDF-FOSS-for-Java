package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Fill path operator (f\*) using the even-odd rule.
///
/// Fills the current path using the even-odd rule to determine the region to fill.
/// Any open subpaths are implicitly closed before being filled.
/// See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class EOFill extends Operator {

    /// Creates an EOFill operator.
    public EOFill() {
        super("f*");
    }

    /// Creates an EOFill operator from parsed operands.
    public EOFill(List<PdfBase> operands) {
        super("f*", operands);
    }
}
