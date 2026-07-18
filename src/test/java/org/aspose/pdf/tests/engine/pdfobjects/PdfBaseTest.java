package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfBase].
public class PdfBaseTest {

    @Test
    public void directObjectIsNotIndirect() {
        PdfBase obj = PdfInteger.valueOf(42);
        assertFalse(obj.isIndirect());
        assertNull(obj.getObjectKey());
    }

    @Test
    public void settingKeyMakesIndirect() {
        // Use non-cached value (>=256) since cached PdfIntegers are flyweights
        PdfBase obj = PdfInteger.valueOf(1000);
        obj.setObjectKey(new PdfObjectKey(1, 0));
        assertTrue(obj.isIndirect());
        assertEquals(new PdfObjectKey(1, 0), obj.getObjectKey());
    }

    @Test
    public void settingKeyOnCachedIntegerIsIgnored() {
        // Cached PdfIntegers (0-255) are flyweight — setObjectKey is no-op
        PdfBase obj = PdfInteger.valueOf(42);
        obj.setObjectKey(new PdfObjectKey(1, 0));
        assertFalse(obj.isIndirect());
    }

    @Test
    public void settingKeyOnSingletonsIsIgnored() {
        // PdfBoolean and PdfNull are flyweight singletons — setObjectKey is no-op
        PdfBoolean.TRUE.setObjectKey(new PdfObjectKey(1, 0));
        assertFalse(PdfBoolean.TRUE.isIndirect());
        PdfNull.INSTANCE.setObjectKey(new PdfObjectKey(2, 0));
        assertFalse(PdfNull.INSTANCE.isIndirect());
    }

    @Test
    public void clearingKeyMakesDirect() {
        PdfBase obj = PdfInteger.valueOf(1000);
        obj.setObjectKey(new PdfObjectKey(1, 0));
        obj.setObjectKey(null);
        assertFalse(obj.isIndirect());
    }

    @Test
    public void visitorDispatchesCorrectly() {
        // Track which visit method was called
        IPdfVisitor<String> visitor = new IPdfVisitor<String>() {
            @Override public String visitBoolean(PdfBoolean obj) { return "boolean"; }
            @Override public String visitInteger(PdfInteger obj) { return "integer"; }
            @Override public String visitFloat(PdfFloat obj) { return "float"; }
            @Override public String visitName(PdfName obj) { return "name"; }
            @Override public String visitString(PdfString obj) { return "string"; }
            @Override public String visitNull(PdfNull obj) { return "null"; }
            @Override public String visitArray(PdfArray obj) { return "array"; }
            @Override public String visitDictionary(PdfDictionary obj) { return "dictionary"; }
            @Override public String visitStream(PdfStream obj) { return "stream"; }
        };

        assertEquals("boolean", PdfBoolean.TRUE.accept(visitor));
        assertEquals("integer", PdfInteger.valueOf(1).accept(visitor));
        assertEquals("float", new PdfFloat(1.0).accept(visitor));
        assertEquals("name", PdfName.of("Test").accept(visitor));
        assertEquals("string", new PdfString("Test").accept(visitor));
        assertEquals("null", PdfNull.INSTANCE.accept(visitor));
        assertEquals("array", new PdfArray().accept(visitor));
        assertEquals("dictionary", new PdfDictionary().accept(visitor));
        assertEquals("stream", new PdfStream().accept(visitor));
    }
}
