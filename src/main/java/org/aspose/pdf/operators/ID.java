package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Begin inline image data operator (ID).
///
/// Marks the beginning of the image data for an inline image. It appears between
/// the [BI] operator (which provides the image dictionary) and the [EI]
/// operator (which terminates the inline image).
/// See ISO 32000-1:2008, §8.9.7, Table 92.
///
public class ID extends Operator {

    /// Creates an ID operator with no operands.
    public ID() {
        super("ID");
    }

    /// Creates an ID operator from parsed operands.
    ///
    /// @param operands the operands from the content stream parser
    public ID(List<PdfBase> operands) {
        super("ID", operands);
    }
}
