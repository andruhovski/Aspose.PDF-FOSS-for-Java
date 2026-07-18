package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/// Set line join style operator (j).
///
/// Sets the line join style in the graphics state. Values: 0 = miter join,
/// 1 = round join, 2 = bevel join. See ISO 32000-1:2008, §8.4.3.4, Table 55.
///
public class SetLineJoin extends Operator {

    private final int lineJoin;

    /// Creates a SetLineJoin (j) operator with the specified line join style.
    ///
    /// @param lineJoin the line join style (0, 1, or 2)
    public SetLineJoin(int lineJoin) {
        super("j", Collections.singletonList(num(lineJoin)));
        this.lineJoin = lineJoin;
    }

    /// Creates a SetLineJoin (j) operator from parsed operands.
    ///
    /// Expects one numeric operand: the line join style.
    ///
    /// @param operands the operands from the content stream parser
    public SetLineJoin(List<PdfBase> operands) {
        super("j", operands);
        this.lineJoin = (operands != null && operands.size() > 0) ? (int) getNumber(operands.get(0)) : 0;
    }

    /// Returns the line join style.
    ///
    /// @return the line join style (0 = miter, 1 = round, 2 = bevel)
    public int getLineJoin() {
        return lineJoin;
    }
}
