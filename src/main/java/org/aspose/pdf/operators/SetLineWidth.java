package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/// Set line width operator (w).
///
/// Sets the line width in the graphics state. See ISO 32000-1:2008, §8.4.3.2, Table 57.
///
public class SetLineWidth extends Operator {

    private final double width;

    /// Creates a SetLineWidth (w) operator with the specified width.
    ///
    /// @param width the line width
    public SetLineWidth(double width) {
        super("w", Collections.singletonList(num(width)));
        this.width = width;
    }

    /// Creates a SetLineWidth (w) operator from parsed operands.
    ///
    /// Expects one numeric operand: the line width.
    ///
    /// @param operands the operands from the content stream parser
    public SetLineWidth(List<PdfBase> operands) {
        super("w", operands);
        this.width = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
    }

    /// Returns the line width.
    ///
    /// @return the line width
    public double getWidth() {
        return width;
    }
}
