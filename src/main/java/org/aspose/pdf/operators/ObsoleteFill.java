package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Obsolete fill path operator (F).
///
/// Equivalent to the f operator. Included only for compatibility; PDF producers
/// should use f instead. See ISO 32000-1:2008, §8.5.3.1, Table 60.
///
public class ObsoleteFill extends Operator {

    /// Creates an ObsoleteFill operator.
    public ObsoleteFill() {
        super("F");
    }

    /// Creates an ObsoleteFill operator from parsed operands.
    public ObsoleteFill(List<PdfBase> operands) {
        super("F", operands);
    }
}
