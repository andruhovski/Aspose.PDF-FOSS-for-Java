package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSInteger}.
 */
public class COSIntegerTest {

    @Test
    public void cachedValuesReturnSameInstance() {
        assertSame(COSInteger.valueOf(0), COSInteger.valueOf(0));
        assertSame(COSInteger.valueOf(255), COSInteger.valueOf(255));
        assertSame(COSInteger.ZERO, COSInteger.valueOf(0));
        assertSame(COSInteger.ONE, COSInteger.valueOf(1));
    }

    @Test
    public void uncachedValuesAreEqual() {
        assertEquals(COSInteger.valueOf(256), COSInteger.valueOf(256));
        assertNotSame(COSInteger.valueOf(256), COSInteger.valueOf(256));
    }

    @Test
    public void negativeValuesNotCached() {
        assertEquals(-1L, COSInteger.valueOf(-1).longValue());
    }

    @Test
    public void longMaxValue() {
        assertEquals(Long.MAX_VALUE, COSInteger.valueOf(Long.MAX_VALUE).longValue());
    }

    @Test
    public void intValueOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> COSInteger.valueOf(Long.MAX_VALUE).intValue());
    }

    @Test
    public void floatValue() {
        assertEquals(42.0f, COSInteger.valueOf(42).floatValue());
    }

    @Test
    public void writeToZero() throws IOException {
        assertWritesTo("0", COSInteger.ZERO);
    }

    @Test
    public void writeToPositive() throws IOException {
        assertWritesTo("42", COSInteger.valueOf(42));
    }

    @Test
    public void writeToNegative() throws IOException {
        assertWritesTo("-17", COSInteger.valueOf(-17));
    }

    @Test
    public void writeToLargeValue() throws IOException {
        assertWritesTo("2147483648", COSInteger.valueOf(2147483648L));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(COSInteger.valueOf(42), COSInteger.valueOf(42));
        assertNotEquals(COSInteger.valueOf(42), COSInteger.valueOf(43));
        assertEquals(COSInteger.valueOf(42).hashCode(), COSInteger.valueOf(42).hashCode());
    }

    private void assertWritesTo(String expected, COSInteger value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        value.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
