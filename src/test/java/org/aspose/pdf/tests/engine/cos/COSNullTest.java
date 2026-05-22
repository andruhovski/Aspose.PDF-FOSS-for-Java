package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSNull}.
 */
public class COSNullTest {

    @Test
    public void singletonIdentity() {
        assertSame(COSNull.getInstance(), COSNull.getInstance());
        assertSame(COSNull.INSTANCE, COSNull.getInstance());
    }

    @Test
    public void writeTo() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        COSNull.INSTANCE.writeTo(baos);
        assertEquals("null", baos.toString("US-ASCII"));
    }

    @Test
    public void toStringIsNull() {
        assertEquals("null", COSNull.INSTANCE.toString());
    }

    @Test
    public void acceptVisitor() {
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
        assertEquals("null", COSNull.INSTANCE.accept(visitor));
    }
}
