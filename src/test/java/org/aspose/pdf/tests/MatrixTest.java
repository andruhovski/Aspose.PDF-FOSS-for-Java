package org.aspose.pdf.tests;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Matrix}.
 */
public class MatrixTest {

    @Test
    public void defaultConstructorIsIdentity() {
        Matrix m = new Matrix();
        assertEquals(1, m.getA());
        assertEquals(0, m.getB());
        assertEquals(0, m.getC());
        assertEquals(1, m.getD());
        assertEquals(0, m.getE());
        assertEquals(0, m.getF());
    }

    @Test
    public void identityConstant() {
        assertEquals(new Matrix(), Matrix.IDENTITY);
    }

    @Test
    public void getValues() {
        Matrix m = new Matrix(1, 2, 3, 4, 5, 6);
        double[] v = m.getValues();
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6}, v);
    }

    @Test
    public void getValuesReturnsClone() {
        Matrix m = new Matrix(1, 0, 0, 1, 0, 0);
        double[] v = m.getValues();
        v[0] = 999;
        assertEquals(1, m.getA());
    }

    @Test
    public void multiplyIdentity() {
        Matrix m = new Matrix(2, 0, 0, 3, 10, 20);
        Matrix result = m.multiply(Matrix.IDENTITY);
        assertEquals(m, result);
    }

    @Test
    public void multiplyTranslation() {
        // Translation by (10, 20) then translation by (5, 7)
        Matrix t1 = new Matrix(1, 0, 0, 1, 10, 20);
        Matrix t2 = new Matrix(1, 0, 0, 1, 5, 7);
        Matrix result = t1.multiply(t2);
        assertEquals(15, result.getE(), 1e-10);
        assertEquals(27, result.getF(), 1e-10);
    }

    @Test
    public void multiplyScaleAndTranslate() {
        // Scale by 2x then translate by (10, 20)
        Matrix scale = new Matrix(2, 0, 0, 2, 0, 0);
        Matrix translate = new Matrix(1, 0, 0, 1, 10, 20);
        Matrix result = scale.multiply(translate);
        assertEquals(2, result.getA(), 1e-10);
        assertEquals(2, result.getD(), 1e-10);
        assertEquals(10, result.getE(), 1e-10);
        assertEquals(20, result.getF(), 1e-10);
    }

    @Test
    public void multiplyNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.IDENTITY.multiply(null));
    }

    @Test
    public void transformPointIdentity() {
        double[] p = Matrix.IDENTITY.transformPoint(5, 10);
        assertEquals(5, p[0], 1e-10);
        assertEquals(10, p[1], 1e-10);
    }

    @Test
    public void transformPointTranslation() {
        Matrix m = new Matrix(1, 0, 0, 1, 100, 200);
        double[] p = m.transformPoint(5, 10);
        assertEquals(105, p[0], 1e-10);
        assertEquals(210, p[1], 1e-10);
    }

    @Test
    public void transformPointScale() {
        Matrix m = new Matrix(2, 0, 0, 3, 0, 0);
        double[] p = m.transformPoint(5, 10);
        assertEquals(10, p[0], 1e-10);
        assertEquals(30, p[1], 1e-10);
    }

    @Test
    public void fromCOSArray() {
        COSArray arr = new COSArray(6);
        arr.add(new COSFloat(1));
        arr.add(new COSFloat(0));
        arr.add(new COSFloat(0));
        arr.add(new COSFloat(1));
        arr.add(new COSFloat(10));
        arr.add(new COSFloat(20));
        Matrix m = Matrix.fromCOSArray(arr);
        assertEquals(1, m.getA(), 1e-10);
        assertEquals(10, m.getE(), 1e-10);
        assertEquals(20, m.getF(), 1e-10);
    }

    @Test
    public void fromCOSArrayWithIntegers() {
        COSArray arr = new COSArray(6);
        for (int i = 0; i < 6; i++) {
            arr.add(COSInteger.valueOf(i));
        }
        Matrix m = Matrix.fromCOSArray(arr);
        assertEquals(0, m.getA(), 1e-10);
        assertEquals(5, m.getF(), 1e-10);
    }

    @Test
    public void fromCOSArrayNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.fromCOSArray(null));
    }

    @Test
    public void fromCOSArrayWrongSizeThrows() {
        COSArray arr = new COSArray();
        arr.add(new COSFloat(1));
        assertThrows(IllegalArgumentException.class, () -> Matrix.fromCOSArray(arr));
    }

    @Test
    public void toCOSArray() {
        Matrix m = new Matrix(1, 2, 3, 4, 5, 6);
        COSArray arr = m.toCOSArray();
        assertEquals(6, arr.size());
        assertEquals(1f, arr.getFloat(0, 0f), 1e-10);
        assertEquals(6f, arr.getFloat(5, 0f), 1e-10);
    }

    @Test
    public void roundTrip() {
        Matrix original = new Matrix(1.5, 2.5, 3.5, 4.5, 5.5, 6.5);
        COSArray arr = original.toCOSArray();
        Matrix restored = Matrix.fromCOSArray(arr);
        assertEquals(original, restored);
    }

    @Test
    public void equalsAndHashCode() {
        Matrix m1 = new Matrix(1, 2, 3, 4, 5, 6);
        Matrix m2 = new Matrix(1, 2, 3, 4, 5, 6);
        Matrix m3 = new Matrix(1, 2, 3, 4, 5, 7);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1, m3);
    }

    @Test
    public void toStringContainsValues() {
        Matrix m = new Matrix(1, 2, 3, 4, 5, 6);
        String s = m.toString();
        assertTrue(s.contains("1"));
        assertTrue(s.contains("6"));
    }
}
