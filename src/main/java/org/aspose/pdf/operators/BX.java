package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Begin compatibility section operator (BX).
///
/// Begins a compatibility section. Between BX and EX, unrecognized operators
/// and their operands shall be ignored without error.
/// See ISO 32000-1:2008, §7.8.2, Table 32.
///
public class BX extends Operator {

    /// Creates a BX operator.
    public BX() {
        super("BX");
    }

    /// Creates a BX operator from parsed operands.
    public BX(List<PdfBase> operands) {
        super("BX", operands);
    }
}
