package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSDictionary}.
 */
public class COSDictionaryTest {

    @Test
    public void emptyDictionary() throws IOException {
        COSDictionary dict = new COSDictionary();
        assertTrue(dict.isEmpty());
        assertEquals(0, dict.size());
        String result = writeTo(dict);
        assertTrue(result.contains("<<"));
        assertTrue(result.contains(">>"));
    }

    @Test
    public void singleEntry() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.set("Type", COSName.of("Page"));
        String result = writeTo(dict);
        assertTrue(result.contains("/Type /Page"));
    }

    @Test
    public void typedGettersAndSetters() {
        COSDictionary dict = new COSDictionary();
        dict.setInt("Width", 100);
        dict.setFloat("Height", 72.5f);
        dict.setBoolean("Flag", true);
        dict.setName("Type", "Font");
        dict.setString("Title", "Hello");

        assertEquals(100, dict.getInt("Width", 0));
        assertEquals(72.5f, dict.getFloat("Height", 0f), 0.01f);
        assertTrue(dict.getBoolean("Flag", false));
        assertEquals("Font", dict.getNameAsString("Type"));
        assertEquals("Hello", dict.getString("Title"));
    }

    @Test
    public void setNullRemovesKey() {
        COSDictionary dict = new COSDictionary();
        dict.setInt("Width", 100);
        assertTrue(dict.containsKey("Width"));
        dict.set("Width", null);
        assertFalse(dict.containsKey("Width"));
    }

    @Test
    public void containsKeyStringAndCOSName() {
        COSDictionary dict = new COSDictionary();
        dict.set("Type", COSName.of("Page"));
        assertTrue(dict.containsKey("Type"));
        assertTrue(dict.containsKey(COSName.TYPE));
    }

    @Test
    public void getType() {
        COSDictionary dict = new COSDictionary();
        dict.set("Type", COSName.of("Font"));
        assertEquals("Font", dict.getType());
    }

    @Test
    public void getPath() {
        COSDictionary inner = new COSDictionary();
        inner.set("Encoding", COSName.of("WinAnsiEncoding"));
        COSDictionary outer = new COSDictionary();
        outer.set("Font", inner);

        COSBase result = outer.getPath("Font", "Encoding");
        assertNotNull(result);
        assertTrue(result instanceof COSName);
        assertEquals("WinAnsiEncoding", ((COSName) result).getName());
    }

    @Test
    public void getPathMissing() {
        COSDictionary dict = new COSDictionary();
        assertNull(dict.getPath("NonExistent", "Path"));
    }

    @Test
    public void insertionOrder() {
        COSDictionary dict = new COSDictionary();
        dict.set("C", COSInteger.valueOf(3));
        dict.set("A", COSInteger.valueOf(1));
        dict.set("B", COSInteger.valueOf(2));

        List<String> keys = new ArrayList<>();
        for (Map.Entry<COSName, COSBase> entry : dict) {
            keys.add(entry.getKey().getName());
        }
        assertEquals(List.of("C", "A", "B"), keys);
    }

    @Test
    public void getDictionaryForNonDict() {
        COSDictionary dict = new COSDictionary();
        dict.setInt("Width", 100);
        assertNull(dict.getDictionary("Width"));
    }

    @Test
    public void getArrayForNonArray() {
        COSDictionary dict = new COSDictionary();
        dict.setInt("Width", 100);
        assertNull(dict.getArray("Width"));
    }

    @Test
    public void getSubtypeFallback() {
        COSDictionary dict = new COSDictionary();
        dict.setName("S", "GoTo");
        assertEquals("GoTo", dict.getSubtype());
    }

    @Test
    public void copyConstructor() {
        COSDictionary original = new COSDictionary();
        original.setInt("Width", 100);
        original.setName("Type", "Page");
        COSDictionary copy = new COSDictionary(original);
        assertEquals(100, copy.getInt("Width", 0));
        assertEquals("Page", copy.getType());
    }

    @Test
    public void mixedValueTypes() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.set("Int", COSInteger.valueOf(42));
        dict.set("Float", new COSFloat(3.14));
        dict.set("Name", COSName.of("Test"));
        dict.set("Str", new COSString("Hello"));
        dict.set("Bool", COSBoolean.TRUE);
        dict.set("Arr", new COSArray());
        dict.set("Dict", new COSDictionary());

        assertEquals(7, dict.size());
        String result = writeTo(dict);
        assertTrue(result.contains("/Int 42"));
        assertTrue(result.contains("/Bool true"));
    }

    @Test
    public void getIntWithCOSNameKey() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Predictor"), COSInteger.valueOf(12));
        assertEquals(12, dict.getInt(COSName.of("Predictor"), 1));
    }

    private String writeTo(COSDictionary dict) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dict.writeTo(baos);
        return baos.toString("US-ASCII");
    }
}
