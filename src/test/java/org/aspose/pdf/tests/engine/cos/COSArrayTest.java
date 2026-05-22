package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSArray}.
 */
public class COSArrayTest {

    @Test
    public void emptyArray() throws IOException {
        COSArray arr = new COSArray();
        assertEquals(0, arr.size());
        assertTrue(arr.isEmpty());
        assertWritesTo("[]", arr);
    }

    @Test
    public void singleElement() throws IOException {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(42));
        assertWritesTo("[42]", arr);
    }

    @Test
    public void mixedTypes() throws IOException {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(42));
        arr.add(new COSFloat(3.14));
        arr.add(COSName.of("Name"));
        arr.add(new COSString("String"));
        arr.add(COSBoolean.TRUE);
        arr.add(COSNull.INSTANCE);
        String result = writeTo(arr);
        assertTrue(result.startsWith("[42 "));
        assertTrue(result.contains("/Name"));
        assertTrue(result.contains("(String)"));
        assertTrue(result.endsWith("null]"));
    }

    @Test
    public void nestedArray() throws IOException {
        COSArray inner1 = new COSArray();
        inner1.add(COSInteger.valueOf(1));
        inner1.add(COSInteger.valueOf(2));
        COSArray inner2 = new COSArray();
        inner2.add(COSInteger.valueOf(3));
        inner2.add(COSInteger.valueOf(4));
        COSArray outer = new COSArray();
        outer.add(inner1);
        outer.add(inner2);
        assertWritesTo("[[1 2] [3 4]]", outer);
    }

    @Test
    public void getIntFromInteger() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(42));
        assertEquals(42, arr.getInt(0, -1));
    }

    @Test
    public void getIntFromFloat() {
        COSArray arr = new COSArray();
        arr.add(new COSFloat(3.14));
        assertEquals(3, arr.getInt(0, -1));
    }

    @Test
    public void getIntFromNonNumeric() {
        COSArray arr = new COSArray();
        arr.add(COSName.of("Name"));
        assertEquals(-1, arr.getInt(0, -1));
    }

    @Test
    public void toFloatArray() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(0));
        arr.add(COSInteger.valueOf(0));
        arr.add(COSInteger.valueOf(612));
        arr.add(COSInteger.valueOf(792));
        float[] result = arr.toFloatArray();
        assertArrayEquals(new float[]{0f, 0f, 612f, 792f}, result);
    }

    @Test
    public void addRemoveSet() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(1));
        arr.add(COSInteger.valueOf(2));
        arr.add(COSInteger.valueOf(3));
        assertEquals(3, arr.size());

        arr.set(1, COSInteger.valueOf(20));
        assertEquals(20, ((COSInteger) arr.get(1)).longValue());

        arr.remove(0);
        assertEquals(2, arr.size());
        assertEquals(20, ((COSInteger) arr.get(0)).longValue());
    }

    @Test
    public void iterator() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(1));
        arr.add(COSInteger.valueOf(2));
        arr.add(COSInteger.valueOf(3));
        int count = 0;
        for (COSBase item : arr) {
            count++;
            assertNotNull(item);
        }
        assertEquals(3, count);
    }

    @Test
    public void largeArray() throws IOException {
        COSArray arr = new COSArray(10000);
        for (int i = 0; i < 10000; i++) {
            arr.add(COSInteger.valueOf(i));
        }
        assertEquals(10000, arr.size());
        String result = writeTo(arr);
        assertTrue(result.startsWith("[0 1 2 "));
        assertTrue(result.endsWith("9999]"));
    }

    @Test
    public void getName() {
        COSArray arr = new COSArray();
        arr.add(COSName.of("Test"));
        assertEquals("Test", arr.getName(0));
        assertNull(arr.getName(1)); // out of bounds
    }

    @Test
    public void getString() {
        COSArray arr = new COSArray();
        arr.add(new COSString("Hello"));
        assertEquals("Hello", arr.getString(0));
    }

    @Test
    public void getDictionary() {
        COSArray arr = new COSArray();
        COSDictionary dict = new COSDictionary();
        arr.add(dict);
        assertSame(dict, arr.getDictionary(0));
        assertNull(arr.getDictionary(1)); // out of bounds
    }

    @Test
    public void getArray() {
        COSArray arr = new COSArray();
        COSArray inner = new COSArray();
        arr.add(inner);
        assertSame(inner, arr.getArray(0));
    }

    private void assertWritesTo(String expected, COSArray arr) throws IOException {
        assertEquals(expected, writeTo(arr));
    }

    private String writeTo(COSArray arr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        arr.writeTo(baos);
        return baos.toString("US-ASCII");
    }
}
