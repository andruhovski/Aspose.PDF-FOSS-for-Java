package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Begin text object operator (BT).
///
/// Begins a text object, initializing the text matrix and the text line matrix
/// to the identity matrix.
/// See ISO 32000-1:2008, §9.4.1, Table 107.
///
public class BT extends BlockTextOperator {

    /// Creates a BT operator.
    public BT() {
        super("BT");
    }

    /// Creates a BT operator from parsed operands.
    public BT(List<PdfBase> operands) {
        super("BT", operands);
    }
}
