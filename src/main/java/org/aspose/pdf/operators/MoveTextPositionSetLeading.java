package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Move text position and set leading operator (TD).
///
/// Moves to the start of the next line, offset from the start of the current line
/// by (`tx`, `ty`). As a side effect, this operator also sets the text
/// leading to `-ty`. This operator is equivalent to:
///
/// <pre>
///   -ty TL
///   tx ty Td
/// </pre>
///
/// See ISO 32000-1:2008, §9.4.2, Table 108.
///
public class MoveTextPositionSetLeading extends TextPlaceOperator {

    private final double x;
    private final double y;

    /// Creates a MoveTextPositionSetLeading (TD) operator with the specified offsets.
    ///
    /// @param x the horizontal offset
    /// @param y the vertical offset (also sets leading to -y)
    public MoveTextPositionSetLeading(double x, double y) {
        super("TD", coords(x, y));
        this.x = x;
        this.y = y;
    }

    /// Creates a MoveTextPositionSetLeading (TD) operator from parsed operands.
    ///
    /// @param operands the operands from the content stream parser
    public MoveTextPositionSetLeading(List<PdfBase> operands) {
        super("TD", operands);
        this.x = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.y = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
    }

    /// Returns the horizontal offset.
    ///
    /// @return the x offset
    public double getX() {
        return x;
    }

    /// Returns the vertical offset.
    ///
    /// @return the y offset
    public double getY() {
        return y;
    }
}
