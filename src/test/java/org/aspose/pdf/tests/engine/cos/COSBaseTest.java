package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSBase}.
 */
public class COSBaseTest {

    @Test
    public void directObjectIsNotIndirect() {
        COSBase obj = COSInteger.valueOf(42);
        assertFalse(obj.isIndirect());
        assertNull(obj.getObjectKey());
    }

    @Test
    public void settingKeyMakesIndirect() {
        // Use non-cached value (>=256) since cached COSIntegers are flyweights
        COSBase obj = COSInteger.valueOf(1000);
        obj.setObjectKey(new COSObjectKey(1, 0));
        assertTrue(obj.isIndirect());
        assertEquals(new COSObjectKey(1, 0), obj.getObjectKey());
    }

    @Test
    public void settingKeyOnCachedIntegerIsIgnored() {
        // Cached COSIntegers (0-255) are flyweight — setObjectKey is no-op
        COSBase obj = COSInteger.valueOf(42);
        obj.setObjectKey(new COSObjectKey(1, 0));
        assertFalse(obj.isIndirect());
    }

    @Test
    public void settingKeyOnSingletonsIsIgnored() {
        // COSBoolean and COSNull are flyweight singletons — setObjectKey is no-op
        COSBoolean.TRUE.setObjectKey(new COSObjectKey(1, 0));
        assertFalse(COSBoolean.TRUE.isIndirect());
        COSNull.INSTANCE.setObjectKey(new COSObjectKey(2, 0));
        assertFalse(COSNull.INSTANCE.isIndirect());
    }

    @Test
    public void clearingKeyMakesDirect() {
        COSBase obj = COSInteger.valueOf(1000);
        obj.setObjectKey(new COSObjectKey(1, 0));
        obj.setObjectKey(null);
        assertFalse(obj.isIndirect());
    }

    @Test
    public void visitorDispatchesCorrectly() {
        // Track which visit method was called
        ICOSVisitor<String> visitor = new ICOSVisitor<String>() {
            @Override public String visitBoolean(COSBoolean obj) { return "boolean"; }
            @Override public String visitInteger(COSInteger obj) { return "integer"; }
            @Override public String visitFloat(COSFloat obj) { return "float"; }
            @Override public String visitName(COSName obj) { return "name"; }
            @Override public String visitString(COSString obj) { return "string"; }
            @Override public String visitNull(COSNull obj) { return "null"; }
            @Override public String visitArray(COSArray obj) { return "array"; }
            @Override public String visitDictionary(COSDictionary obj) { return "dictionary"; }
            @Override public String visitStream(COSStream obj) { return "stream"; }
        };

        assertEquals("boolean", COSBoolean.TRUE.accept(visitor));
        assertEquals("integer", COSInteger.valueOf(1).accept(visitor));
        assertEquals("float", new COSFloat(1.0).accept(visitor));
        assertEquals("name", COSName.of("Test").accept(visitor));
        assertEquals("string", new COSString("Test").accept(visitor));
        assertEquals("null", COSNull.INSTANCE.accept(visitor));
        assertEquals("array", new COSArray().accept(visitor));
        assertEquals("dictionary", new COSDictionary().accept(visitor));
        assertEquals("stream", new COSStream().accept(visitor));
    }
}
