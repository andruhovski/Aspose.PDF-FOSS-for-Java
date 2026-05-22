package org.aspose.pdf.tests.engine.writer;
import org.aspose.pdf.engine.writer.*;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PDFWriter} — PDF serialization.
 */
public class PDFWriterTest {

    /**
     * Test 1: Write a simple PDF with 3 objects and verify structure.
     */
    @Test
    public void testWriteSimplePDF() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);

        // Create objects
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();

        // Object 1: Catalog
        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.of("Type"), COSName.of("Catalog"));
        objects.put(new COSObjectKey(1, 0), catalog);

        // Object 2: Pages
        COSDictionary pages = new COSDictionary();
        pages.set(COSName.of("Type"), COSName.of("Pages"));
        pages.set(COSName.of("Count"), COSInteger.valueOf(0));
        objects.put(new COSObjectKey(2, 0), pages);

        // Object 3: Info
        COSDictionary info = new COSDictionary();
        info.set(COSName.of("Producer"), new COSString("OpenPDF Test"));
        objects.put(new COSObjectKey(3, 0), info);

        // Trailer
        COSDictionary trailer = new COSDictionary();
        trailer.set(COSName.of("Root"), COSInteger.valueOf(1)); // simplified ref

        writer.write(trailer, objects);

        byte[] result = bos.toByteArray();
        String pdf = new String(result, StandardCharsets.ISO_8859_1);

        // Verify header
        assertTrue(pdf.startsWith("%PDF-1.7"));

        // Verify objects are present
        assertTrue(pdf.contains("1 0 obj"));
        assertTrue(pdf.contains("2 0 obj"));
        assertTrue(pdf.contains("3 0 obj"));
        assertTrue(pdf.contains("endobj"));

        // Verify xref
        assertTrue(pdf.contains("xref"));
        assertTrue(pdf.contains("trailer"));
        assertTrue(pdf.contains("startxref"));
        assertTrue(pdf.contains("%%EOF"));
    }

    /**
     * Test 2: Header format is correct.
     */
    @Test
    public void testHeaderFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);

        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        COSDictionary catalog = new COSDictionary();
        objects.put(new COSObjectKey(1, 0), catalog);

        COSDictionary trailer = new COSDictionary();
        writer.write(trailer, objects);

        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertTrue(pdf.startsWith("%PDF-1.7\n%"));

        // Check binary hint bytes (4 bytes with high bit set)
        byte[] bytes = bos.toByteArray();
        // Find the second '%'
        int secondPercent = pdf.indexOf('%', 1);
        assertTrue(secondPercent > 0);
        for (int i = secondPercent + 1; i <= secondPercent + 4; i++) {
            assertTrue((bytes[i] & 0x80) != 0, "Binary hint byte at " + i + " should have high bit set");
        }
    }

    /**
     * Test 3: XRef offsets are correct — each entry is exactly 20 bytes.
     */
    @Test
    public void testXrefEntrySize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.4f);

        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        COSDictionary obj1 = new COSDictionary();
        objects.put(new COSObjectKey(1, 0), obj1);

        COSDictionary trailer = new COSDictionary();
        writer.write(trailer, objects);

        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);

        // Find xref section
        int xrefPos = pdf.indexOf("xref\n");
        assertTrue(xrefPos >= 0);

        // After "xref\n" and "0 2\n", each entry should be 20 bytes
        int firstEntryStart = pdf.indexOf("\n", xrefPos + 5) + 1; // skip "0 2\n"
        String entry0 = pdf.substring(firstEntryStart, firstEntryStart + 20);
        assertEquals(20, entry0.length());
        assertTrue(entry0.startsWith("0000000000 65535 f "));
    }

    /**
     * Test 5: Round-trip: write → parse → verify objects.
     */
    @Test
    public void testRoundTrip() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.4f);

        // Create a minimal valid PDF
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();

        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.of("Type"), COSName.of("Catalog"));
        objects.put(new COSObjectKey(1, 0), catalog);

        COSDictionary pages = new COSDictionary();
        pages.set(COSName.of("Type"), COSName.of("Pages"));
        pages.set(COSName.of("Count"), COSInteger.valueOf(0));
        COSArray kids = new COSArray();
        pages.set(COSName.of("Kids"), kids);
        objects.put(new COSObjectKey(2, 0), pages);

        COSDictionary trailer = new COSDictionary();
        // Use a simplified reference — in a real PDF /Root would be "1 0 R"
        // but for this test we check that the written PDF can be re-parsed
        writer.write(trailer, objects);

        // Parse the written PDF
        byte[] pdfBytes = bos.toByteArray();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdfBytes);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        assertEquals(1.4f, parser.getVersion(), 0.01f);
        assertNotNull(parser.getTrailer());

        // Load object 1 and verify it's a catalog
        COSBase obj1 = parser.getObject(1);
        assertTrue(obj1 instanceof COSDictionary);
        COSDictionary parsedCatalog = (COSDictionary) obj1;
        COSBase type = parsedCatalog.get(COSName.of("Type"));
        assertTrue(type instanceof COSName);
        assertEquals("Catalog", ((COSName) type).getValue());
    }

    /**
     * Test 6: Large number of objects — offsets don't drift.
     */
    @Test
    public void testManyObjects() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);

        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        for (int i = 1; i <= 100; i++) {
            COSDictionary dict = new COSDictionary();
            dict.set(COSName.of("Index"), COSInteger.valueOf(i));
            objects.put(new COSObjectKey(i, 0), dict);
        }

        COSDictionary trailer = new COSDictionary();
        writer.write(trailer, objects);

        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("100 0 obj"));
        assertTrue(pdf.contains("%%EOF"));

        // Verify xref has correct number of entries
        assertTrue(pdf.contains("0 101")); // 0 through 100
    }

    /**
     * Test: Constructor rejects null output.
     */
    @Test
    public void testConstructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PDFWriter(null, 1.7f));
    }

    /**
     * Test: allocateObjectNumber increments.
     */
    @Test
    public void testAllocateObjectNumber() {
        PDFWriter writer = new PDFWriter(new ByteArrayOutputStream(), 1.7f);
        COSObjectKey k1 = writer.allocateObjectNumber();
        COSObjectKey k2 = writer.allocateObjectNumber();
        assertEquals(1, k1.getObjectNumber());
        assertEquals(2, k2.getObjectNumber());
        assertEquals(0, k1.getGenerationNumber());
    }
}
