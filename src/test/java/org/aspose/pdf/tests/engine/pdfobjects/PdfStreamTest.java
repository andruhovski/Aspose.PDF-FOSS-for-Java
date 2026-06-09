package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PdfStream}.
 */
public class PdfStreamTest {

    @Test
    public void emptyStream() throws IOException {
        PdfStream stream = new PdfStream();
        byte[] decoded = stream.getDecodedData();
        assertEquals(0, decoded.length);
        assertEquals(0, stream.getLength());
    }

    @Test
    public void streamWithoutFilter() throws IOException {
        byte[] data = "Hello PDF".getBytes(StandardCharsets.US_ASCII);
        PdfStream stream = new PdfStream();
        stream.setDecodedData(data);
        assertArrayEquals(data, stream.getDecodedData());
    }

    @Test
    public void writeToFormat() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.US_ASCII);
        PdfStream stream = new PdfStream();
        stream.setDecodedData(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.writeTo(baos);
        String result = baos.toString("US-ASCII");

        assertTrue(result.contains("stream\r\n"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\r\nendstream"));
    }

    @Test
    public void lengthUpdatedOnWrite() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.US_ASCII);
        PdfStream stream = new PdfStream();
        stream.setDecodedData(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.writeTo(baos);

        assertEquals(5, stream.getInt("Length", 0));
    }

    @Test
    public void getFiltersEmpty() {
        PdfStream stream = new PdfStream();
        assertTrue(stream.getFilters().isEmpty());
    }

    @Test
    public void setFilterSingle() {
        PdfStream stream = new PdfStream();
        stream.setFilter(PdfName.FLATE_DECODE);
        assertEquals(1, stream.getFilters().size());
        assertEquals(PdfName.FLATE_DECODE, stream.getFilters().get(0));
    }

    @Test
    public void setEncodedData() {
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        PdfStream stream = new PdfStream();
        stream.setEncodedData(data);
        assertArrayEquals(data, stream.getEncodedData());
    }

    @Test
    public void constructorFromDictAndData() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.setName("Type", "XObject");
        byte[] data = "content".getBytes(StandardCharsets.US_ASCII);
        PdfStream stream = new PdfStream(dict, data);

        assertEquals("XObject", stream.getType());
        assertArrayEquals(data, stream.getEncodedData());
    }

    @Test
    public void getDictionaryReturnsSelf() {
        PdfStream stream = new PdfStream();
        assertSame(stream, stream.getDictionary());
    }

    @Test
    public void binaryData() throws IOException {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        PdfStream stream = new PdfStream();
        stream.setDecodedData(data);
        assertArrayEquals(data, stream.getDecodedData());
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
        assertEquals("stream", new PdfStream().accept(visitor));
    }
}
