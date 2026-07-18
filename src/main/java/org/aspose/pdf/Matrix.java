package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;

import java.util.Arrays;
import java.util.logging.Logger;

/// Represents a 3x3 affine transformation matrix used in PDF graphics state.
///
/// The matrix is stored as six values `[a, b, c, d, e, f]` corresponding to:
///
/// <pre>
///   | a  b  0 |
///   | c  d  0 |
///   | e  f  1 |
/// </pre>
///
/// This follows the PDF specification (ISO 32000-1:2008, §8.3.3).
/// Matrix instances are immutable.
///
public class Matrix {

    private static final Logger LOG = Logger.getLogger(Matrix.class.getName());

    /// The identity matrix (no transformation).
    public static final Matrix IDENTITY = new Matrix(1, 0, 0, 1, 0, 0);

    private final double a, b, c, d, e, f;

    /// Creates a matrix with the specified six values.
    ///
    /// @param a scale X / rotate
    /// @param b rotate / skew
    /// @param c rotate / skew
    /// @param d scale Y / rotate
    /// @param e translate X
    /// @param f translate Y
    public Matrix(double a, double b, double c, double d, double e, double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        LOG.fine(() -> "Matrix created: [" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + "]");
    }

    /// Creates a matrix from a six-element array.
    ///
    /// @param elements the array `[a, b, c, d, e, f]`
    /// @throws IllegalArgumentException if elements is null or not length 6
    public Matrix(double[] elements) {
        this(validateElements(elements)[0], elements[1], elements[2],
             elements[3], elements[4], elements[5]);
    }

    /// Creates the identity matrix.
    public Matrix() {
        this(1, 0, 0, 1, 0, 0);
    }

    /// Creates a rotation matrix for the given angle in radians.
    ///
    /// @param angleRadians the rotation angle in radians (counter-clockwise)
    /// @return the rotation matrix
    public static Matrix rotation(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        return new Matrix(cos, sin, -sin, cos, 0, 0);
    }

    private static double[] validateElements(double[] elements) {
        if (elements == null || elements.length != 6) {
            throw new IllegalArgumentException("Matrix requires exactly 6 elements");
        }
        return elements;
    }

    /// Returns a copy of the six matrix values.
    ///
    /// @return a double array `[a, b, c, d, e, f]`
    public double[] getValues() {
        return new double[]{a, b, c, d, e, f};
    }

    /// Returns the a (scale X / rotate) component.
    ///
    /// @return the a value
    public double getA() {
        return a;
    }

    /// Returns the b (rotate / skew) component.
    ///
    /// @return the b value
    public double getB() {
        return b;
    }

    /// Returns the c (rotate / skew) component.
    ///
    /// @return the c value
    public double getC() {
        return c;
    }

    /// Returns the d (scale Y / rotate) component.
    ///
    /// @return the d value
    public double getD() {
        return d;
    }

    /// Returns the e (translate X) component.
    ///
    /// @return the e value
    public double getE() {
        return e;
    }

    /// Returns the f (translate Y) component.
    ///
    /// @return the f value
    public double getF() {
        return f;
    }

    /// Multiplies this matrix by another, returning a new matrix.
    ///
    /// The result represents the combined transformation: first apply `this`,
    /// then apply `other`.
    ///
    /// @param other the matrix to multiply with
    /// @return the resulting matrix
    /// @throws IllegalArgumentException if other is null
    public Matrix multiply(Matrix other) {
        if (other == null) {
            throw new IllegalArgumentException("Matrix must not be null");
        }
        double a2 = other.a, b2 = other.b, c2 = other.c, d2 = other.d, e2 = other.e, f2 = other.f;
        return new Matrix(
                a * a2 + b * c2,
                a * b2 + b * d2,
                c * a2 + d * c2,
                c * b2 + d * d2,
                e * a2 + f * c2 + e2,
                e * b2 + f * d2 + f2
        );
    }

    /// Transforms a point (x, y) by this matrix.
    ///
    /// @param x the x coordinate
    /// @param y the y coordinate
    /// @return a double array `[newX, newY]` where
    ///         `newX = a*x + c*y + e` and `newY = b*x + d*y + f`
    public double[] transformPoint(double x, double y) {
        return new double[]{
                a * x + c * y + e,
                b * x + d * y + f
        };
    }

    /// Returns the inverse matrix.
    ///
    /// @return the inverse matrix
    /// @throws IllegalStateException if the matrix is singular
    public Matrix reverse() {
        double det = a * d - b * c;
        if (Math.abs(det) < 1e-12) {
            throw new IllegalStateException("Matrix is singular and cannot be reversed");
        }
        double invA = d / det;
        double invB = -b / det;
        double invC = -c / det;
        double invD = a / det;
        double invE = (c * f - d * e) / det;
        double invF = (b * e - a * f) / det;
        return new Matrix(invA, invB, invC, invD, invE, invF);
    }

    /// Transforms a rectangle and returns the axis-aligned bounding box
    /// of the transformed corners.
    ///
    /// @param rectangle the rectangle to transform
    /// @return the transformed bounding rectangle, or `null` if input is null
    public Rectangle transform(Rectangle rectangle) {
        if (rectangle == null) {
            return null;
        }
        double[] p1 = transformPoint(rectangle.getLLX(), rectangle.getLLY());
        double[] p2 = transformPoint(rectangle.getURX(), rectangle.getLLY());
        double[] p3 = transformPoint(rectangle.getLLX(), rectangle.getURY());
        double[] p4 = transformPoint(rectangle.getURX(), rectangle.getURY());
        double minX = Math.min(Math.min(p1[0], p2[0]), Math.min(p3[0], p4[0]));
        double minY = Math.min(Math.min(p1[1], p2[1]), Math.min(p3[1], p4[1]));
        double maxX = Math.max(Math.max(p1[0], p2[0]), Math.max(p3[0], p4[0]));
        double maxY = Math.max(Math.max(p1[1], p2[1]), Math.max(p3[1], p4[1]));
        return new Rectangle(minX, minY, maxX, maxY);
    }

    /// Creates a Matrix from a PDF array of six numbers.
    ///
    /// @param array the PDF array containing [a, b, c, d, e, f]
    /// @return a new Matrix
    /// @throws IllegalArgumentException if the array is null or does not have exactly 6 elements
    public static Matrix fromPdfArray(PdfArray array) {
        if (array == null) {
            throw new IllegalArgumentException("PdfArray must not be null");
        }
        if (array.size() != 6) {
            throw new IllegalArgumentException("Matrix PdfArray must have exactly 6 elements, got " + array.size());
        }
        return new Matrix(
                array.getFloat(0, 0f),
                array.getFloat(1, 0f),
                array.getFloat(2, 0f),
                array.getFloat(3, 0f),
                array.getFloat(4, 0f),
                array.getFloat(5, 0f)
        );
    }

    /// Converts this matrix to a PDF array of six numbers.
    ///
    /// @return a PdfArray containing [a, b, c, d, e, f]
    public PdfArray toPdfArray() {
        PdfArray array = new PdfArray(6);
        array.add(new PdfFloat(a));
        array.add(new PdfFloat(b));
        array.add(new PdfFloat(c));
        array.add(new PdfFloat(d));
        array.add(new PdfFloat(e));
        array.add(new PdfFloat(f));
        return array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix)) return false;
        Matrix m = (Matrix) o;
        return Double.compare(a, m.a) == 0
                && Double.compare(b, m.b) == 0
                && Double.compare(c, m.c) == 0
                && Double.compare(d, m.d) == 0
                && Double.compare(e, m.e) == 0
                && Double.compare(f, m.f) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new double[]{a, b, c, d, e, f});
    }

    @Override
    public String toString() {
        return "Matrix[" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + "]";
    }
}
