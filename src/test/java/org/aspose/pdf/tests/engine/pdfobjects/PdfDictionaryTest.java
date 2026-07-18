package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfDictionary].
public class PdfDictionaryTest {

    @Test
    public void emptyDictionary() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        assertTrue(dict.isEmpty());
        assertEquals(0, dict.size());
        String result = writeTo(dict);
        assertTrue(result.contains("<<"));
        assertTrue(result.contains(">>"));
    }

    @Test
    public void singleEntry() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Type", PdfName.of("Page"));
        String result = writeTo(dict);
        assertTrue(result.contains("/Type /Page"));
    }

    @Test
    public void typedGettersAndSetters() {
        PdfDictionary dict = new PdfDictionary();
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
        PdfDictionary dict = new PdfDictionary();
        dict.setInt("Width", 100);
        assertTrue(dict.containsKey("Width"));
        dict.set("Width", null);
        assertFalse(dict.containsKey("Width"));
    }

    @Test
    public void containsKeyStringAndPdfName() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Type", PdfName.of("Page"));
        assertTrue(dict.containsKey("Type"));
        assertTrue(dict.containsKey(PdfName.TYPE));
    }

    @Test
    public void getType() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Type", PdfName.of("Font"));
        assertEquals("Font", dict.getType());
    }

    @Test
    public void getPath() {
        PdfDictionary inner = new PdfDictionary();
        inner.set("Encoding", PdfName.of("WinAnsiEncoding"));
        PdfDictionary outer = new PdfDictionary();
        outer.set("Font", inner);

        PdfBase result = outer.getPath("Font", "Encoding");
        assertNotNull(result);
        assertTrue(result instanceof PdfName);
        assertEquals("WinAnsiEncoding", ((PdfName) result).getName());
    }

    @Test
    public void getPathMissing() {
        PdfDictionary dict = new PdfDictionary();
        assertNull(dict.getPath("NonExistent", "Path"));
    }

    @Test
    public void insertionOrder() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("C", PdfInteger.valueOf(3));
        dict.set("A", PdfInteger.valueOf(1));
        dict.set("B", PdfInteger.valueOf(2));

        List<String> keys = new ArrayList<>();
        for (Map.Entry<PdfName, PdfBase> entry : dict) {
            keys.add(entry.getKey().getName());
        }
        assertEquals(List.of("C", "A", "B"), keys);
    }

    @Test
    public void getDictionaryForNonDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.setInt("Width", 100);
        assertNull(dict.getDictionary("Width"));
    }

    @Test
    public void getArrayForNonArray() {
        PdfDictionary dict = new PdfDictionary();
        dict.setInt("Width", 100);
        assertNull(dict.getArray("Width"));
    }

    @Test
    public void getSubtypeFallback() {
        PdfDictionary dict = new PdfDictionary();
        dict.setName("S", "GoTo");
        assertEquals("GoTo", dict.getSubtype());
    }

    @Test
    public void copyConstructor() {
        PdfDictionary original = new PdfDictionary();
        original.setInt("Width", 100);
        original.setName("Type", "Page");
        PdfDictionary copy = new PdfDictionary(original);
        assertEquals(100, copy.getInt("Width", 0));
        assertEquals("Page", copy.getType());
    }

    @Test
    public void mixedValueTypes() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Int", PdfInteger.valueOf(42));
        dict.set("Float", new PdfFloat(3.14));
        dict.set("Name", PdfName.of("Test"));
        dict.set("Str", new PdfString("Hello"));
        dict.set("Bool", PdfBoolean.TRUE);
        dict.set("Arr", new PdfArray());
        dict.set("Dict", new PdfDictionary());

        assertEquals(7, dict.size());
        String result = writeTo(dict);
        assertTrue(result.contains("/Int 42"));
        assertTrue(result.contains("/Bool true"));
    }

    @Test
    public void getIntWithPdfNameKey() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Predictor"), PdfInteger.valueOf(12));
        assertEquals(12, dict.getInt(PdfName.of("Predictor"), 1));
    }

    private String writeTo(PdfDictionary dict) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dict.writeTo(baos);
        return baos.toString("US-ASCII");
    }
}
