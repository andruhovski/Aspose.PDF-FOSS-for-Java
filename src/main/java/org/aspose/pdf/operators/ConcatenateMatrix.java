package org.aspose.pdf.operators;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Concatenate matrix operator (cm).
///
/// Modifies the current transformation matrix (CTM) by concatenating it with the
/// specified matrix. See ISO 32000-1:2008, §8.4.4, Table 57.
///
public class ConcatenateMatrix extends Operator {

    private final Matrix matrix;

    /// Creates a ConcatenateMatrix (cm) operator from a [Matrix].
    ///
    /// @param matrix the transformation matrix to concatenate
    /// @throws IllegalArgumentException if matrix is null
    public ConcatenateMatrix(Matrix matrix) {
        super("cm", matrixToOperands(matrix));
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix must not be null");
        }
        this.matrix = matrix;
    }

    /// Creates a ConcatenateMatrix (cm) operator from six individual values.
    ///
    /// @param a scale X / rotate
    /// @param b rotate / skew
    /// @param c rotate / skew
    /// @param d scale Y / rotate
    /// @param e translate X
    /// @param f translate Y
    public ConcatenateMatrix(double a, double b, double c, double d, double e, double f) {
        this(new Matrix(a, b, c, d, e, f));
    }

    /// Creates a ConcatenateMatrix (cm) operator from parsed operands.
    ///
    /// Expects six numeric operands representing the matrix values [a, b, c, d, e, f].
    ///
    /// @param operands the operands from the content stream parser
    public ConcatenateMatrix(List<PdfBase> operands) {
        super("cm", operands);
        double a = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 1;
        double b = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        double c = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        double d = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 1;
        double e = (operands != null && operands.size() > 4) ? getNumber(operands.get(4)) : 0;
        double f = (operands != null && operands.size() > 5) ? getNumber(operands.get(5)) : 0;
        this.matrix = new Matrix(a, b, c, d, e, f);
    }

    /// Returns the transformation matrix.
    ///
    /// @return the matrix
    public Matrix getMatrix() {
        return matrix;
    }
}
