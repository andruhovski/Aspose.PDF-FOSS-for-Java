package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSStream}.
 */
public class COSStreamTest {

    @Test
    public void emptyStream() throws IOException {
        COSStream stream = new COSStream();
        byte[] decoded = stream.getDecodedData();
        assertEquals(0, decoded.length);
        assertEquals(0, stream.getLength());
    }

    @Test
    public void streamWithoutFilter() throws IOException {
        byte[] data = "Hello PDF".getBytes(StandardCharsets.US_ASCII);
        COSStream stream = new COSStream();
        stream.setDecodedData(data);
        assertArrayEquals(data, stream.getDecodedData());
    }

    @Test
    public void writeToFormat() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.US_ASCII);
        COSStream stream = new COSStream();
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
        COSStream stream = new COSStream();
        stream.setDecodedData(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.writeTo(baos);

        assertEquals(5, stream.getInt("Length", 0));
    }

    @Test
    public void getFiltersEmpty() {
        COSStream stream = new COSStream();
        assertTrue(stream.getFilters().isEmpty());
    }

    @Test
    public void setFilterSingle() {
        COSStream stream = new COSStream();
        stream.setFilter(COSName.FLATE_DECODE);
        assertEquals(1, stream.getFilters().size());
        assertEquals(COSName.FLATE_DECODE, stream.getFilters().get(0));
    }

    @Test
    public void setEncodedData() {
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        COSStream stream = new COSStream();
        stream.setEncodedData(data);
        assertArrayEquals(data, stream.getEncodedData());
    }

    @Test
    public void constructorFromDictAndData() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.setName("Type", "XObject");
        byte[] data = "content".getBytes(StandardCharsets.US_ASCII);
        COSStream stream = new COSStream(dict, data);

        assertEquals("XObject", stream.getType());
        assertArrayEquals(data, stream.getEncodedData());
    }

    @Test
    public void getDictionaryReturnsSelf() {
        COSStream stream = new COSStream();
        assertSame(stream, stream.getDictionary());
    }

    @Test
    public void binaryData() throws IOException {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        COSStream stream = new COSStream();
        stream.setDecodedData(data);
        assertArrayEquals(data, stream.getDecodedData());
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
        assertEquals("stream", new COSStream().accept(visitor));
    }
}
