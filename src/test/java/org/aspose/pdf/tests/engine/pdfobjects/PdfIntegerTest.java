package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfInteger].
public class PdfIntegerTest {

    @Test
    public void cachedValuesReturnSameInstance() {
        assertSame(PdfInteger.valueOf(0), PdfInteger.valueOf(0));
        assertSame(PdfInteger.valueOf(255), PdfInteger.valueOf(255));
        assertSame(PdfInteger.ZERO, PdfInteger.valueOf(0));
        assertSame(PdfInteger.ONE, PdfInteger.valueOf(1));
    }

    @Test
    public void uncachedValuesAreEqual() {
        assertEquals(PdfInteger.valueOf(256), PdfInteger.valueOf(256));
        assertNotSame(PdfInteger.valueOf(256), PdfInteger.valueOf(256));
    }

    @Test
    public void negativeValuesNotCached() {
        assertEquals(-1L, PdfInteger.valueOf(-1).longValue());
    }

    @Test
    public void longMaxValue() {
        assertEquals(Long.MAX_VALUE, PdfInteger.valueOf(Long.MAX_VALUE).longValue());
    }

    @Test
    public void intValueOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> PdfInteger.valueOf(Long.MAX_VALUE).intValue());
    }

    @Test
    public void floatValue() {
        assertEquals(42.0f, PdfInteger.valueOf(42).floatValue());
    }

    @Test
    public void writeToZero() throws IOException {
        assertWritesTo("0", PdfInteger.ZERO);
    }

    @Test
    public void writeToPositive() throws IOException {
        assertWritesTo("42", PdfInteger.valueOf(42));
    }

    @Test
    public void writeToNegative() throws IOException {
        assertWritesTo("-17", PdfInteger.valueOf(-17));
    }

    @Test
    public void writeToLargeValue() throws IOException {
        assertWritesTo("2147483648", PdfInteger.valueOf(2147483648L));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(PdfInteger.valueOf(42), PdfInteger.valueOf(42));
        assertNotEquals(PdfInteger.valueOf(42), PdfInteger.valueOf(43));
        assertEquals(PdfInteger.valueOf(42).hashCode(), PdfInteger.valueOf(42).hashCode());
    }

    private void assertWritesTo(String expected, PdfInteger value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        value.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
