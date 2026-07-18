package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfNull].
public class PdfNullTest {

    @Test
    public void singletonIdentity() {
        assertSame(PdfNull.getInstance(), PdfNull.getInstance());
        assertSame(PdfNull.INSTANCE, PdfNull.getInstance());
    }

    @Test
    public void writeTo() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfNull.INSTANCE.writeTo(baos);
        assertEquals("null", baos.toString("US-ASCII"));
    }

    @Test
    public void toStringIsNull() {
        assertEquals("null", PdfNull.INSTANCE.toString());
    }

    @Test
    public void acceptVisitor() {
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
        assertEquals("null", PdfNull.INSTANCE.accept(visitor));
    }
}
