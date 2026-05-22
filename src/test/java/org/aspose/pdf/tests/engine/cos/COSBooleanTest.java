package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSBoolean}.
 */
public class COSBooleanTest {

    @Test
    public void valueOfTrueReturnsSameInstance() {
        assertSame(COSBoolean.valueOf(true), COSBoolean.valueOf(true));
        assertSame(COSBoolean.TRUE, COSBoolean.valueOf(true));
    }

    @Test
    public void valueOfFalseReturnsSameInstance() {
        assertSame(COSBoolean.valueOf(false), COSBoolean.valueOf(false));
        assertSame(COSBoolean.FALSE, COSBoolean.valueOf(false));
    }

    @Test
    public void trueNotEqualsFalse() {
        assertNotSame(COSBoolean.TRUE, COSBoolean.FALSE);
        assertNotEquals(COSBoolean.TRUE, COSBoolean.FALSE);
    }

    @Test
    public void getValueReturnsCorrect() {
        assertTrue(COSBoolean.TRUE.getValue());
        assertFalse(COSBoolean.FALSE.getValue());
    }

    @Test
    public void writeToTrue() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        COSBoolean.TRUE.writeTo(baos);
        assertEquals("true", baos.toString("US-ASCII"));
    }

    @Test
    public void writeToFalse() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        COSBoolean.FALSE.writeTo(baos);
        assertEquals("false", baos.toString("US-ASCII"));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(COSBoolean.TRUE, COSBoolean.TRUE);
        assertNotEquals(COSBoolean.TRUE, COSBoolean.FALSE);
        assertEquals(COSBoolean.TRUE.hashCode(), COSBoolean.valueOf(true).hashCode());
    }

    @Test
    public void toStringFormat() {
        assertEquals("COSBoolean{true}", COSBoolean.TRUE.toString());
        assertEquals("COSBoolean{false}", COSBoolean.FALSE.toString());
    }

    @Test
    public void acceptVisitor() {
        ICOSVisitor<String> visitor = new TestVisitor();
        assertEquals("boolean:true", COSBoolean.TRUE.accept(visitor));
    }

    private static class TestVisitor implements ICOSVisitor<String> {
        @Override public String visitBoolean(COSBoolean obj) { return "boolean:" + obj.getValue(); }
        @Override public String visitInteger(COSInteger obj) { return "integer"; }
        @Override public String visitFloat(COSFloat obj) { return "float"; }
        @Override public String visitName(COSName obj) { return "name"; }
        @Override public String visitString(COSString obj) { return "string"; }
        @Override public String visitNull(COSNull obj) { return "null"; }
        @Override public String visitArray(COSArray obj) { return "array"; }
        @Override public String visitDictionary(COSDictionary obj) { return "dictionary"; }
        @Override public String visitStream(COSStream obj) { return "stream"; }
    }
}
