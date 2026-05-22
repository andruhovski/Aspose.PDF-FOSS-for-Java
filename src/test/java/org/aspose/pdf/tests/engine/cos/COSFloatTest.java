package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSFloat}.
 */
public class COSFloatTest {

    @Test
    public void writeToZero() throws IOException {
        assertWritesTo("0", new COSFloat(0.0));
    }

    @Test
    public void writeToOnePointFive() throws IOException {
        assertWritesTo("1.5", new COSFloat(1.5));
    }

    @Test
    public void writeToNegativePi() throws IOException {
        assertWritesTo("-3.14159", new COSFloat(-3.14159));
    }

    @Test
    public void writeToWholeNumber() throws IOException {
        assertWritesTo("100", new COSFloat(100.0));
    }

    @Test
    public void writeToSmallValue() throws IOException {
        assertWritesTo("0.00001", new COSFloat(0.00001));
    }

    @Test
    public void writeToVerySmallValue() throws IOException {
        assertWritesTo("0.000001", new COSFloat(0.000001));
    }

    @Test
    public void writeToNegativeZero() throws IOException {
        assertWritesTo("0", new COSFloat(-0.0));
    }

    @Test
    public void constructorFromString() {
        assertEquals(3.14, new COSFloat("3.14").doubleValue(), 0.0001);
    }

    @Test
    public void nanThrows() {
        assertThrows(IllegalArgumentException.class, () -> new COSFloat(Double.NaN));
    }

    @Test
    public void infinityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new COSFloat(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new COSFloat(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void emptyStringThrows() {
        assertThrows(IllegalArgumentException.class, () -> new COSFloat(""));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(new COSFloat(3.14), new COSFloat(3.14));
        assertNotEquals(new COSFloat(3.14), new COSFloat(3.15));
        assertEquals(new COSFloat(3.14).hashCode(), new COSFloat(3.14).hashCode());
    }

    @Test
    public void floatValue() {
        assertEquals(3.14f, new COSFloat(3.14).floatValue(), 0.001f);
    }

    private void assertWritesTo(String expected, COSFloat value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        value.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
