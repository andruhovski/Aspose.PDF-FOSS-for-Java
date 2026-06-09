package org.aspose.pdf.tests.engine.parser;
import org.aspose.pdf.engine.parser.*;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XRefParser} — PDF cross-reference table/stream parser.
 */
public class XRefParserTest {

    /**
     * Test 1: Simple text xref table with one subsection.
     */
    @Test
    public void testSimpleTextXref() throws IOException {
        String xref = "xref\n" +
                "0 4\n" +
                "0000000000 65535 f \r\n" +
                "0000000010 00000 n \r\n" +
                "0000000079 00000 n \r\n" +
                "0000000173 00000 n \r\n" +
                "trailer\n" +
                "<< /Size 4 /Root 1 0 R >>\n";

        RandomAccessReader reader = RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
        PDFLexer lexer = new PDFLexer(reader);
        XRefParser parser = new XRefParser(reader, lexer);
        parser.parse(0);

        Map<PdfObjectKey, XRefEntry> entries = parser.getEntries();
        assertEquals(4, entries.size());

        // Object 0 should be free
        XRefEntry entry0 = entries.get(new PdfObjectKey(0, 65535));
        assertNotNull(entry0);
        assertEquals(XRefEntry.Type.FREE, entry0.getType());

        // Object 1 should be in-use at offset 10
        XRefEntry entry1 = entries.get(new PdfObjectKey(1, 0));
        assertNotNull(entry1);
        assertEquals(XRefEntry.Type.IN_USE, entry1.getType());
        assertEquals(10, entry1.getByteOffset());

        // Object 2 at offset 79
        XRefEntry entry2 = entries.get(new PdfObjectKey(2, 0));
        assertNotNull(entry2);
        assertEquals(79, entry2.getByteOffset());

        // Object 3 at offset 173
        XRefEntry entry3 = entries.get(new PdfObjectKey(3, 0));
        assertNotNull(entry3);
        assertEquals(173, entry3.getByteOffset());

        // Trailer should have /Size 4
        PdfDictionary trailer = parser.getTrailerDictionary();
        assertNotNull(trailer);
    }

    /**
     * Test 2: Multiple subsections in xref table.
     */
    @Test
    public void testMultipleSubsections() throws IOException {
        String xref = "xref\n" +
                "0 2\n" +
                "0000000000 65535 f \r\n" +
                "0000000010 00000 n \r\n" +
                "5 1\n" +
                "0000000200 00000 n \r\n" +
                "trailer\n" +
                "<< /Size 6 >>\n";

        RandomAccessReader reader = RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
        PDFLexer lexer = new PDFLexer(reader);
        XRefParser parser = new XRefParser(reader, lexer);
        parser.parse(0);

        Map<PdfObjectKey, XRefEntry> entries = parser.getEntries();
        assertEquals(3, entries.size());

        // Object 5 at offset 200
        XRefEntry entry5 = entries.get(new PdfObjectKey(5, 0));
        assertNotNull(entry5);
        assertEquals(XRefEntry.Type.IN_USE, entry5.getType());
        assertEquals(200, entry5.getByteOffset());
    }

    /**
     * Test 7: findStartxref locates the position.
     */
    @Test
    public void testFindStartxref() throws IOException {
        String content = "%PDF-1.4\nsome content here\n" +
                "startxref\n" +
                "12345\n" +
                "%%EOF\n";

        RandomAccessReader reader = RandomAccessReader.fromBytes(content.getBytes(StandardCharsets.US_ASCII));
        long offset = XRefParser.findStartxref(reader);
        assertEquals(12345, offset);
    }

    /**
     * Test 8: Free entries are correctly recognized.
     */
    @Test
    public void testFreeEntries() throws IOException {
        String xref = "xref\n" +
                "0 3\n" +
                "0000000000 65535 f \r\n" +
                "0000000010 00000 n \r\n" +
                "0000000001 00001 f \r\n" +
                "trailer\n" +
                "<< /Size 3 >>\n";

        RandomAccessReader reader = RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
        PDFLexer lexer = new PDFLexer(reader);
        XRefParser parser = new XRefParser(reader, lexer);
        parser.parse(0);

        // Object 2, gen 1 should be free
        XRefEntry entry2 = parser.getEntries().get(new PdfObjectKey(2, 1));
        assertNotNull(entry2);
        assertEquals(XRefEntry.Type.FREE, entry2.getType());
    }

    /**
     * Test 10: Invalid xref throws IOException.
     */
    @Test
    public void testInvalidXrefThrows() {
        String xref = "not_xref\n";
        RandomAccessReader reader = RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
        PDFLexer lexer = new PDFLexer(reader);
        XRefParser parser = new XRefParser(reader, lexer);

        assertThrows(IOException.class, () -> parser.parse(0));
    }

    /**
     * Test: Constructor rejects null arguments.
     */
    @Test
    public void testConstructorRejectsNull() {
        RandomAccessReader reader = RandomAccessReader.fromBytes(new byte[0]);
        PDFLexer lexer = new PDFLexer(reader);

        assertThrows(IllegalArgumentException.class, () -> new XRefParser(null, lexer));
        assertThrows(IllegalArgumentException.class, () -> new XRefParser(reader, null));
    }

    /**
     * Test: findStartxref fails when keyword is missing.
     */
    @Test
    public void testFindStartxrefMissing() {
        String content = "%PDF-1.4\nno startxref here\n%%EOF\n";
        RandomAccessReader reader = RandomAccessReader.fromBytes(content.getBytes(StandardCharsets.US_ASCII));
        assertThrows(IOException.class, () -> XRefParser.findStartxref(reader));
    }
}
