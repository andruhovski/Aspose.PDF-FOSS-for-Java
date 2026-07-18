package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Abstract base class for text block operators (ISO 32000-1:2008, §9.4).
///
/// Text block operators delimit the beginning and end of a text object within a
/// content stream. The two concrete operators in this category are BT (begin text)
/// and ET (end text).
///
public abstract class BlockTextOperator extends TextOperator {

    /// Creates a block text operator with the given name and no operands.
    ///
    /// @param name the operator keyword (e.g., "BT", "ET")
    protected BlockTextOperator(String name) {
        super(name);
    }

    /// Creates a block text operator with the given name and operands.
    ///
    /// @param name     the operator keyword
    /// @param operands the operands preceding this operator in the content stream
    protected BlockTextOperator(String name, List<PdfBase> operands) {
        super(name, operands);
    }
}
