package org.aspose.pdf.operators;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Set text matrix operator (Tm).
///
/// Sets the text matrix and the text line matrix to the specified values. Unlike other
/// text positioning operators, this operator does not concatenate with the existing matrix
/// but replaces it entirely.
/// See ISO 32000-1:2008, §9.4.2, Table 108.
///
public class SetTextMatrix extends TextPlaceOperator {

    private final Matrix matrix;

    /// Creates a SetTextMatrix (Tm) operator with the specified matrix.
    ///
    /// @param matrix the text matrix to set
    /// @throws IllegalArgumentException if matrix is null
    public SetTextMatrix(Matrix matrix) {
        super("Tm", matrixToOperands(matrix));
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix must not be null");
        }
        this.matrix = matrix;
    }

    /// Creates a SetTextMatrix (Tm) operator from parsed operands.
    ///
    /// Expects six numeric operands: a, b, c, d, e, f.
    ///
    /// @param operands the operands from the content stream parser
    public SetTextMatrix(List<PdfBase> operands) {
        super("Tm", operands);
        if (operands != null && operands.size() >= 6) {
            this.matrix = new Matrix(
                    getNumber(operands.get(0)),
                    getNumber(operands.get(1)),
                    getNumber(operands.get(2)),
                    getNumber(operands.get(3)),
                    getNumber(operands.get(4)),
                    getNumber(operands.get(5))
            );
        } else {
            this.matrix = Matrix.IDENTITY;
        }
    }

    /// Returns the text matrix.
    ///
    /// @return the matrix
    public Matrix getMatrix() {
        return matrix;
    }
}
