package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// End marked content operator (EMC).
///
/// Ends a marked-content sequence begun by a BMC or BDC operator.
/// See ISO 32000-1:2008, §14.6, Table 320.
///
public class EMC extends Operator {

    /// Creates an EMC operator.
    public EMC() {
        super("EMC");
    }

    /// Creates an EMC operator from parsed operands.
    public EMC(List<PdfBase> operands) {
        super("EMC", operands);
    }
}
