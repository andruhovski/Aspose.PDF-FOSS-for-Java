package org.aspose.pdf.tests.engine.parser;
import org.aspose.pdf.engine.parser.*;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PDFParser} — full PDF file parser.
 */
public class PDFParserTest {

    /**
     * Builds a minimal valid PDF with one page for testing.
     */
    private static byte[] buildMinimalPDF() {
        // Minimal PDF with catalog, pages, and one page
        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n");
        sb.append("%\u00E2\u00E3\u00CF\u00D3\n");

        // Object 1: Catalog
        long obj1Offset = sb.length();
        sb.append("1 0 obj\n");
        sb.append("<< /Type /Catalog /Pages 2 0 R >>\n");
        sb.append("endobj\n");

        // Object 2: Pages
        long obj2Offset = sb.length();
        sb.append("2 0 obj\n");
        sb.append("<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n");
        sb.append("endobj\n");

        // Object 3: Page
        long obj3Offset = sb.length();
        sb.append("3 0 obj\n");
        sb.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\n");
        sb.append("endobj\n");

        // XRef table
        long xrefOffset = sb.length();
        sb.append("xref\n");
        sb.append("0 4\n");
        sb.append(String.format("0000000000 65535 f \r\n"));
        sb.append(String.format("%010d 00000 n \r\n", obj1Offset));
        sb.append(String.format("%010d 00000 n \r\n", obj2Offset));
        sb.append(String.format("%010d 00000 n \r\n", obj3Offset));

        // Trailer
        sb.append("trailer\n");
        sb.append("<< /Size 4 /Root 1 0 R >>\n");
        sb.append("startxref\n");
        sb.append(xrefOffset).append("\n");
        sb.append("%%EOF\n");

        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Test 1: Parse a simple 1-page PDF without errors.
     */
    @Test
    public void testParseMinimalPDF() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        assertNotNull(parser.getTrailer());
        assertTrue(parser.getAllObjectKeys().size() >= 3);
    }

    /**
     * Test 2: getVersion returns correct version.
     */
    @Test
    public void testGetVersion() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        assertEquals(1.4f, parser.getVersion(), 0.01f);
    }

    /**
     * Test 3: getCatalog returns a dictionary with /Type /Catalog.
     */
    @Test
    public void testGetCatalog() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        PdfDictionary catalog = parser.getCatalog();
        assertNotNull(catalog);
        PdfBase type = catalog.get(PdfName.of("Type"));
        assertNotNull(type);
        assertTrue(type instanceof PdfName);
        assertEquals("Catalog", ((PdfName) type).getValue());
    }

    /**
     * Test 4: getObject loads a page dictionary with /Type /Page.
     */
    @Test
    public void testGetObjectPage() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        PdfBase pageObj = parser.getObject(3);
        assertTrue(pageObj instanceof PdfDictionary);
        PdfDictionary page = (PdfDictionary) pageObj;
        PdfBase type = page.get(PdfName.of("Type"));
        assertTrue(type instanceof PdfName);
        assertEquals("Page", ((PdfName) type).getValue());
    }

    /**
     * Test 5: Lazy loading — object cache works.
     */
    @Test
    public void testLazyLoadingCache() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        PdfBase first = parser.getObject(1);
        PdfBase second = parser.getObject(1);
        assertSame(first, second, "Should return cached instance");
    }

    /**
     * Test 8: Non-PDF input gives clear error.
     */
    @Test
    public void testNotAPDF() {
        byte[] notPdf = "This is not a PDF".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReader reader = RandomAccessReader.fromBytes(notPdf);
        PDFParser parser = new PDFParser(reader);

        assertThrows(IOException.class, parser::parse);
    }

    /**
     * Test 9: Missing object returns PdfNull.
     */
    @Test
    public void testMissingObjectReturnsNull() throws IOException {
        byte[] pdf = buildMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        PdfBase missing = parser.getObject(999);
        assertNotNull(missing); // Returns PdfNull, not Java null
    }

    /**
     * Test: Constructor rejects null.
     */
    @Test
    public void testConstructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PDFParser(null));
    }
}
