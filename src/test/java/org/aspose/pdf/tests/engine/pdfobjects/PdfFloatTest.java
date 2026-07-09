package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PdfFloat}.
 */
public class PdfFloatTest {

    @Test
    public void writeToZero() throws IOException {
        assertWritesTo("0", new PdfFloat(0.0));
    }

    @Test
    public void writeToOnePointFive() throws IOException {
        assertWritesTo("1.5", new PdfFloat(1.5));
    }

    @Test
    public void writeToNegativePi() throws IOException {
        assertWritesTo("-3.14159", new PdfFloat(-3.14159));
    }

    @Test
    public void writeToWholeNumber() throws IOException {
        assertWritesTo("100", new PdfFloat(100.0));
    }

    @Test
    public void writeToSmallValue() throws IOException {
        assertWritesTo("0.00001", new PdfFloat(0.00001));
    }

    @Test
    public void writeToVerySmallValue() throws IOException {
        assertWritesTo("0.000001", new PdfFloat(0.000001));
    }

    @Test
    public void writeToNegativeZero() throws IOException {
        assertWritesTo("0", new PdfFloat(-0.0));
    }

    @Test
    public void largeFractionalRoundsToIntegerPerAdobeLimit() throws IOException {
        // ISO 32000-1 Annex C: |value| >= 32767 cannot carry fractional precision,
        // so it is written as an integer (Adobe implementation limit).
        assertWritesTo("-671089", new PdfFloat(-671088.625));
        assertWritesTo("671089", new PdfFloat(671088.625));
        assertWritesTo("32768", new PdfFloat(32767.5));
    }

    @Test
    public void justBelowLimitKeepsFraction() throws IOException {
        assertWritesTo("32766.5", new PdfFloat(32766.5));
    }

    @Test
    public void constructorFromString() {
        assertEquals(3.14, new PdfFloat("3.14").doubleValue(), 0.0001);
    }

    @Test
    public void nanThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PdfFloat(Double.NaN));
    }

    @Test
    public void infinityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PdfFloat(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new PdfFloat(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void emptyStringThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PdfFloat(""));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(new PdfFloat(3.14), new PdfFloat(3.14));
        assertNotEquals(new PdfFloat(3.14), new PdfFloat(3.15));
        assertEquals(new PdfFloat(3.14).hashCode(), new PdfFloat(3.14).hashCode());
    }

    @Test
    public void floatValue() {
        assertEquals(3.14f, new PdfFloat(3.14).floatValue(), 0.001f);
    }

    private void assertWritesTo(String expected, PdfFloat value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        value.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
