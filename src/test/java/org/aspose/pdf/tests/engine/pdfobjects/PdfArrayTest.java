package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfArray].
public class PdfArrayTest {

    @Test
    public void emptyArray() throws IOException {
        PdfArray arr = new PdfArray();
        assertEquals(0, arr.size());
        assertTrue(arr.isEmpty());
        assertWritesTo("[]", arr);
    }

    @Test
    public void singleElement() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(42));
        assertWritesTo("[42]", arr);
    }

    @Test
    public void mixedTypes() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(42));
        arr.add(new PdfFloat(3.14));
        arr.add(PdfName.of("Name"));
        arr.add(new PdfString("String"));
        arr.add(PdfBoolean.TRUE);
        arr.add(PdfNull.INSTANCE);
        String result = writeTo(arr);
        assertTrue(result.startsWith("[42 "));
        assertTrue(result.contains("/Name"));
        assertTrue(result.contains("(String)"));
        assertTrue(result.endsWith("null]"));
    }

    @Test
    public void nestedArray() throws IOException {
        PdfArray inner1 = new PdfArray();
        inner1.add(PdfInteger.valueOf(1));
        inner1.add(PdfInteger.valueOf(2));
        PdfArray inner2 = new PdfArray();
        inner2.add(PdfInteger.valueOf(3));
        inner2.add(PdfInteger.valueOf(4));
        PdfArray outer = new PdfArray();
        outer.add(inner1);
        outer.add(inner2);
        assertWritesTo("[[1 2] [3 4]]", outer);
    }

    @Test
    public void getIntFromInteger() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(42));
        assertEquals(42, arr.getInt(0, -1));
    }

    @Test
    public void getIntFromFloat() {
        PdfArray arr = new PdfArray();
        arr.add(new PdfFloat(3.14));
        assertEquals(3, arr.getInt(0, -1));
    }

    @Test
    public void getIntFromNonNumeric() {
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("Name"));
        assertEquals(-1, arr.getInt(0, -1));
    }

    @Test
    public void toFloatArray() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(0));
        arr.add(PdfInteger.valueOf(0));
        arr.add(PdfInteger.valueOf(612));
        arr.add(PdfInteger.valueOf(792));
        float[] result = arr.toFloatArray();
        assertArrayEquals(new float[]{0f, 0f, 612f, 792f}, result);
    }

    @Test
    public void addRemoveSet() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(1));
        arr.add(PdfInteger.valueOf(2));
        arr.add(PdfInteger.valueOf(3));
        assertEquals(3, arr.size());

        arr.set(1, PdfInteger.valueOf(20));
        assertEquals(20, ((PdfInteger) arr.get(1)).longValue());

        arr.remove(0);
        assertEquals(2, arr.size());
        assertEquals(20, ((PdfInteger) arr.get(0)).longValue());
    }

    @Test
    public void iterator() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(1));
        arr.add(PdfInteger.valueOf(2));
        arr.add(PdfInteger.valueOf(3));
        int count = 0;
        for (PdfBase item : arr) {
            count++;
            assertNotNull(item);
        }
        assertEquals(3, count);
    }

    @Test
    public void largeArray() throws IOException {
        PdfArray arr = new PdfArray(10000);
        for (int i = 0; i < 10000; i++) {
            arr.add(PdfInteger.valueOf(i));
        }
        assertEquals(10000, arr.size());
        String result = writeTo(arr);
        assertTrue(result.startsWith("[0 1 2 "));
        assertTrue(result.endsWith("9999]"));
    }

    @Test
    public void getName() {
        PdfArray arr = new PdfArray();
        arr.add(PdfName.of("Test"));
        assertEquals("Test", arr.getName(0));
        assertNull(arr.getName(1)); // out of bounds
    }

    @Test
    public void getString() {
        PdfArray arr = new PdfArray();
        arr.add(new PdfString("Hello"));
        assertEquals("Hello", arr.getString(0));
    }

    @Test
    public void getDictionary() {
        PdfArray arr = new PdfArray();
        PdfDictionary dict = new PdfDictionary();
        arr.add(dict);
        assertSame(dict, arr.getDictionary(0));
        assertNull(arr.getDictionary(1)); // out of bounds
    }

    @Test
    public void getArray() {
        PdfArray arr = new PdfArray();
        PdfArray inner = new PdfArray();
        arr.add(inner);
        assertSame(inner, arr.getArray(0));
    }

    private void assertWritesTo(String expected, PdfArray arr) throws IOException {
        assertEquals(expected, writeTo(arr));
    }

    private String writeTo(PdfArray arr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        arr.writeTo(baos);
        return baos.toString("US-ASCII");
    }
}
