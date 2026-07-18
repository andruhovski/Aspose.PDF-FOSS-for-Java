package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfBoolean].
public class PdfBooleanTest {

    @Test
    public void valueOfTrueReturnsSameInstance() {
        assertSame(PdfBoolean.valueOf(true), PdfBoolean.valueOf(true));
        assertSame(PdfBoolean.TRUE, PdfBoolean.valueOf(true));
    }

    @Test
    public void valueOfFalseReturnsSameInstance() {
        assertSame(PdfBoolean.valueOf(false), PdfBoolean.valueOf(false));
        assertSame(PdfBoolean.FALSE, PdfBoolean.valueOf(false));
    }

    @Test
    public void trueNotEqualsFalse() {
        assertNotSame(PdfBoolean.TRUE, PdfBoolean.FALSE);
        assertNotEquals(PdfBoolean.TRUE, PdfBoolean.FALSE);
    }

    @Test
    public void getValueReturnsCorrect() {
        assertTrue(PdfBoolean.TRUE.getValue());
        assertFalse(PdfBoolean.FALSE.getValue());
    }

    @Test
    public void writeToTrue() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfBoolean.TRUE.writeTo(baos);
        assertEquals("true", baos.toString("US-ASCII"));
    }

    @Test
    public void writeToFalse() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfBoolean.FALSE.writeTo(baos);
        assertEquals("false", baos.toString("US-ASCII"));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(PdfBoolean.TRUE, PdfBoolean.TRUE);
        assertNotEquals(PdfBoolean.TRUE, PdfBoolean.FALSE);
        assertEquals(PdfBoolean.TRUE.hashCode(), PdfBoolean.valueOf(true).hashCode());
    }

    @Test
    public void toStringFormat() {
        assertEquals("PdfBoolean{true}", PdfBoolean.TRUE.toString());
        assertEquals("PdfBoolean{false}", PdfBoolean.FALSE.toString());
    }

    @Test
    public void acceptVisitor() {
        IPdfVisitor<String> visitor = new TestVisitor();
        assertEquals("boolean:true", PdfBoolean.TRUE.accept(visitor));
    }

    private static class TestVisitor implements IPdfVisitor<String> {
        @Override public String visitBoolean(PdfBoolean obj) { return "boolean:" + obj.getValue(); }
        @Override public String visitInteger(PdfInteger obj) { return "integer"; }
        @Override public String visitFloat(PdfFloat obj) { return "float"; }
        @Override public String visitName(PdfName obj) { return "name"; }
        @Override public String visitString(PdfString obj) { return "string"; }
        @Override public String visitNull(PdfNull obj) { return "null"; }
        @Override public String visitArray(PdfArray obj) { return "array"; }
        @Override public String visitDictionary(PdfDictionary obj) { return "dictionary"; }
        @Override public String visitStream(PdfStream obj) { return "stream"; }
    }
}
