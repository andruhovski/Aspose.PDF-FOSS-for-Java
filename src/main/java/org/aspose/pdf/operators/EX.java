package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// End compatibility section operator (EX).
///
/// Ends a compatibility section begun by a BX operator.
/// See ISO 32000-1:2008, §7.8.2, Table 32.
///
public class EX extends Operator {

    /// Creates an EX operator.
    public EX() {
        super("EX");
    }

    /// Creates an EX operator from parsed operands.
    public EX(List<PdfBase> operands) {
        super("EX", operands);
    }
}
