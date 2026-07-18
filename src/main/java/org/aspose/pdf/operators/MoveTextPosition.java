package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Move text position operator (Td).
///
/// Moves to the start of the next line, offset from the start of the current line
/// by (`tx`, `ty`). More precisely, this operator performs:
///
/// <pre>
///   Tm = [ 1  0  0 ]   * Tlm
///        [ 0  1  0 ]
///        [ tx ty 1 ]
///   Tlm = Tm
/// </pre>
///
/// See ISO 32000-1:2008, §9.4.2, Table 108.
///
public class MoveTextPosition extends TextPlaceOperator {

    private final double x;
    private final double y;

    /// Creates a MoveTextPosition (Td) operator with the specified offsets.
    ///
    /// @param x the horizontal offset
    /// @param y the vertical offset
    public MoveTextPosition(double x, double y) {
        super("Td", coords(x, y));
        this.x = x;
        this.y = y;
    }

    /// Creates a MoveTextPosition (Td) operator from parsed operands.
    ///
    /// @param operands the operands from the content stream parser
    public MoveTextPosition(List<PdfBase> operands) {
        super("Td", operands);
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
