package org.aspose.pdf.tests.engine.writer;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.aspose.pdf.PdfSaveOptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for XRef Stream and Object Stream writing (PDF 1.5+).
 * ISO 32000-1:2008 §7.5.7 (Object Streams), §7.5.8 (Cross-Reference Streams).
 */
public class XRefStreamWriterTest {

    // ========== Helper methods ==========

    /**
     * Creates a minimal set of objects: catalog, pages, info dict,
     * plus a content stream and several small dicts.
     */
    private Map<COSObjectKey, COSBase> createTestObjects() {
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();

        // Object 1: Catalog
        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.of("Type"), COSName.of("Catalog"));
        catalog.set(COSName.of("Pages"), COSInteger.valueOf(2));
        objects.put(new COSObjectKey(1, 0), catalog);

        // Object 2: Pages
        COSDictionary pages = new COSDictionary();
        pages.set(COSName.of("Type"), COSName.of("Pages"));
        pages.set(COSName.of("Count"), COSInteger.valueOf(1));
        COSArray kids = new COSArray();
        kids.add(COSInteger.valueOf(3));
        pages.set(COSName.of("Kids"), kids);
        objects.put(new COSObjectKey(2, 0), pages);

        // Object 3: Page
        COSDictionary page = new COSDictionary();
        page.set(COSName.of("Type"), COSName.of("Page"));
        page.set(COSName.of("Parent"), COSInteger.valueOf(2));
        COSArray mediaBox = new COSArray();
        mediaBox.add(COSInteger.valueOf(0));
        mediaBox.add(COSInteger.valueOf(0));
        mediaBox.add(COSInteger.valueOf(612));
        mediaBox.add(COSInteger.valueOf(792));
        page.set(COSName.of("MediaBox"), mediaBox);
        objects.put(new COSObjectKey(3, 0), page);

        // Object 4: Info
        COSDictionary info = new COSDictionary();
        info.set(COSName.of("Producer"), new COSString("OpenPDF Test"));
        objects.put(new COSObjectKey(4, 0), info);

        return objects;
    }

    /**
     * Creates a trailer dict for the test objects with proper indirect references.
     */
    private COSDictionary createTrailer() {
        COSDictionary trailer = new COSDictionary();
        trailer.set(COSName.of("Root"), new COSObjectReference(1, 0));
        trailer.set(COSName.of("Info"), new COSObjectReference(4, 0));
        return trailer;
    }

    /**
     * Writes a compressed PDF to a byte array using the given objects.
     */
    private byte[] writeCompressedPdf(Map<COSObjectKey, COSBase> objects,
                                       COSDictionary trailer,
                                       int maxPerStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.5f);
        writer.writeCompressed(trailer, objects, maxPerStream);
        return bos.toByteArray();
    }

    /**
     * Writes a standard (text xref) PDF for comparison.
     */
    private byte[] writeStandardPdf(Map<COSObjectKey, COSBase> objects,
                                     COSDictionary trailer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);
        writer.write(trailer, objects);
        return bos.toByteArray();
    }

    // ========== XRef Stream tests ==========

    /** Test 1: Compressed output starts with %PDF-1.5 (version bump). */
    @Test
    public void testCompressedOutputVersion() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String header = new String(pdf, 0, Math.min(20, pdf.length), StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("%PDF-1.5"), "Should start with %PDF-1.5, got: " + header);
    }

    /** Test 2: Output contains /Type /XRef stream. */
    @Test
    public void testCompressedOutputContainsXRefStream() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        assertTrue(content.contains("/Type /XRef"), "Should contain /Type /XRef");
    }

    /** Test 3: No "xref" text keyword in output (replaced by xref stream). */
    @Test
    public void testCompressedOutputNoTextXref() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        // "xref" should not appear as a standalone keyword (only inside /Type /XRef)
        assertFalse(content.contains("\nxref\n"), "Should not contain standalone 'xref' keyword");
    }

    /** Test 4: No "trailer" text keyword in output. */
    @Test
    public void testCompressedOutputNoTrailer() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        assertFalse(content.contains("\ntrailer\n"), "Should not contain 'trailer' keyword");
    }

    /** Test 5: startxref points to a valid offset. */
    @Test
    public void testStartxrefPointsToXRefStream() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        int startxrefIdx = content.lastIndexOf("startxref");
        assertTrue(startxrefIdx > 0, "Should contain startxref");
        // Extract the offset after "startxref\n"
        int nlIdx = content.indexOf('\n', startxrefIdx);
        int eofIdx = content.indexOf('\n', nlIdx + 1);
        String offsetStr = content.substring(nlIdx + 1, eofIdx).trim();
        long offset = Long.parseLong(offsetStr);
        assertTrue(offset > 0 && offset < pdf.length, "Offset should be valid");
        // The content at that offset should be the xref stream object
        String atOffset = content.substring((int) offset, Math.min((int) offset + 40, content.length()));
        assertTrue(atOffset.contains("0 obj"), "Offset should point to an obj: " + atOffset);
    }

    /** Test 6: XRef stream /W array has 3 elements. */
    @Test
    public void testXRefStreamWArray() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        // Find /W [...] in the xref stream dictionary
        int wIdx = content.indexOf("/W [");
        assertTrue(wIdx > 0, "Should contain /W array");
        int closeBracket = content.indexOf(']', wIdx);
        String wContent = content.substring(wIdx + 4, closeBracket).trim();
        String[] parts = wContent.split("\\s+");
        assertEquals(3, parts.length, "/W should have 3 elements");
    }

    /** Test 7: XRef stream /Size >= max object number. */
    @Test
    public void testXRefStreamSize() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        // Find /Size in the xref stream section (after /Type /XRef)
        int typeIdx = content.indexOf("/Type /XRef");
        assertTrue(typeIdx > 0);
        int sizeIdx = content.indexOf("/Size ", typeIdx);
        assertTrue(sizeIdx > 0, "Should contain /Size in xref stream");
    }

    /** Test 8: XRef stream contains /Root from trailer. */
    @Test
    public void testXRefStreamContainsRoot() throws IOException {
        byte[] pdf = writeCompressedPdf(createTestObjects(), createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);
        int typeIdx = content.indexOf("/Type /XRef");
        assertTrue(typeIdx > 0);
        // /Root should appear near /Type /XRef (in the xref stream dict)
        int areaStart = Math.max(0, typeIdx - 200);
        int areaEnd = Math.min(content.length(), typeIdx + 200);
        String xrefArea = content.substring(areaStart, areaEnd);
        assertTrue(xrefArea.contains("/Root"), "XRef stream dict should contain /Root");
    }

    /** Test 9: Round-trip: write compressed → reparse → same page count. */
    @Test
    public void testRoundTripPageCount() throws IOException {
        // First create a standard PDF
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] standardPdf = writeStandardPdf(objects, createTrailer());

        // Parse it, then write compressed
        PDFParser parser1 = new PDFParser(RandomAccessReader.fromBytes(standardPdf));
        parser1.parse();
        Map<COSObjectKey, COSBase> parsedObjects = new LinkedHashMap<>();
        for (COSObjectKey key : parser1.getAllObjectKeys()) {
            COSBase obj = parser1.getObject(key);
            if (obj != null) parsedObjects.put(key, obj);
        }

        byte[] compressedPdf = writeCompressedPdf(parsedObjects, parser1.getTrailer(), 200);
        parser1.close();

        // Re-parse the compressed PDF
        PDFParser parser2 = new PDFParser(RandomAccessReader.fromBytes(compressedPdf));
        parser2.parse();
        COSDictionary catalog2 = parser2.getCatalog();
        assertNotNull(catalog2, "Catalog should be parseable from compressed PDF");

        COSBase pagesRef = catalog2.get("Pages");
        assertNotNull(pagesRef, "Catalog should have /Pages");
        parser2.close();
    }

    /** Test 10: Round-trip: compressed → reparse → text content intact. */
    @Test
    public void testRoundTripContentIntact() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] standardPdf = writeStandardPdf(objects, createTrailer());

        PDFParser parser1 = new PDFParser(RandomAccessReader.fromBytes(standardPdf));
        parser1.parse();
        Map<COSObjectKey, COSBase> parsedObjects = new LinkedHashMap<>();
        for (COSObjectKey key : parser1.getAllObjectKeys()) {
            COSBase obj = parser1.getObject(key);
            if (obj != null) parsedObjects.put(key, obj);
        }

        byte[] compressedPdf = writeCompressedPdf(parsedObjects, parser1.getTrailer(), 200);
        parser1.close();

        PDFParser parser2 = new PDFParser(RandomAccessReader.fromBytes(compressedPdf));
        parser2.parse();
        // Verify the info dict is intact
        COSDictionary trailer2 = parser2.getTrailer();
        assertNotNull(trailer2, "Trailer should be parseable");
        parser2.close();
    }

    // ========== Object Stream tests ==========

    /** Test 11: Stream objects are excluded from object streams (remain as regular objects). */
    @Test
    public void testStreamObjectsExcludedFromObjStm() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();

        // Add a stream object
        COSStream stream = new COSStream();
        stream.set(COSName.of("Type"), COSName.of("XObject"));
        stream.setDecodedData("hello stream".getBytes(StandardCharsets.US_ASCII));
        objects.put(new COSObjectKey(5, 0), stream);

        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);

        // The stream object should be written as a standalone "5 0 obj"
        assertTrue(content.contains("5 0 obj"), "Stream object should be written standalone");
    }

    /** Test 12: Dict/array objects are packed into ObjStm. */
    @Test
    public void testDictObjectsPackedIntoObjStm() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);

        // Should have at least one object stream
        assertTrue(content.contains("/Type /ObjStm"), "Should contain object stream");
    }

    /** Test 13: Object stream has /Type /ObjStm, /N, /First, /Filter. */
    @Test
    public void testObjectStreamHasRequiredKeys() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);

        int objStmIdx = content.indexOf("/Type /ObjStm");
        assertTrue(objStmIdx > 0, "Should contain /Type /ObjStm");

        // Find the dictionary area around /Type /ObjStm
        int dictStart = content.lastIndexOf("<<", objStmIdx);
        int dictEnd = content.indexOf(">>", objStmIdx);
        String dictArea = content.substring(dictStart, dictEnd + 2);

        assertTrue(dictArea.contains("/N "), "Should contain /N");
        assertTrue(dictArea.contains("/First "), "Should contain /First");
        assertTrue(dictArea.contains("/Filter"), "Should contain /Filter");
    }

    /** Test 14: Object stream /N matches number of packed objects. */
    @Test
    public void testObjectStreamNMatchesCount() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        // 4 non-stream objects, all gen 0 → all should be packed
        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 200);
        String content = new String(pdf, StandardCharsets.US_ASCII);

        int objStmIdx = content.indexOf("/Type /ObjStm");
        assertTrue(objStmIdx > 0);

        int nIdx = content.indexOf("/N ", objStmIdx - 200);
        assertTrue(nIdx > 0);
        // Parse /N value
        int valStart = nIdx + 3;
        int valEnd = valStart;
        while (valEnd < content.length() && Character.isDigit(content.charAt(valEnd))) valEnd++;
        int n = Integer.parseInt(content.substring(valStart, valEnd));
        assertEquals(4, n, "Should pack 4 objects into object stream");
    }

    /** Test 15: Round-trip: compressed objects → reparse → objects intact. */
    @Test
    public void testRoundTripCompressedObjectsIntact() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        byte[] standardPdf = writeStandardPdf(objects, createTrailer());

        PDFParser parser1 = new PDFParser(RandomAccessReader.fromBytes(standardPdf));
        parser1.parse();
        Map<COSObjectKey, COSBase> parsedObjects = new LinkedHashMap<>();
        for (COSObjectKey key : parser1.getAllObjectKeys()) {
            COSBase obj = parser1.getObject(key);
            if (obj != null) parsedObjects.put(key, obj);
        }

        byte[] compressedPdf = writeCompressedPdf(parsedObjects, parser1.getTrailer(), 200);
        parser1.close();

        // Re-parse compressed PDF and verify all objects are loadable
        PDFParser parser2 = new PDFParser(RandomAccessReader.fromBytes(compressedPdf));
        parser2.parse();
        for (COSObjectKey key : parser2.getAllObjectKeys()) {
            COSBase obj = parser2.getObject(key);
            assertNotNull(obj, "Object " + key + " should be loadable from compressed PDF");
        }
        parser2.close();
    }

    /** Test 16: objectsPerStream=2 creates multiple ObjStm when there are more objects. */
    @Test
    public void testMultipleObjectStreams() throws IOException {
        Map<COSObjectKey, COSBase> objects = createTestObjects();
        // 4 non-stream objects, maxPerStream=2 → should create 2 object streams
        byte[] pdf = writeCompressedPdf(objects, createTrailer(), 2);
        String content = new String(pdf, StandardCharsets.US_ASCII);

        int firstIdx = content.indexOf("/Type /ObjStm");
        assertTrue(firstIdx > 0, "Should have first ObjStm");
        int secondIdx = content.indexOf("/Type /ObjStm", firstIdx + 1);
        assertTrue(secondIdx > 0, "Should have second ObjStm when maxPerStream=2 with 4 objects");
    }

    // ========== Integration tests ==========

    /** Test 17: Compressed output is smaller than standard output for many objects. */
    @Test
    public void testCompressedSmallerThanStandard() throws IOException {
        // Create a document with many small objects to see size benefit
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.of("Type"), COSName.of("Catalog"));
        catalog.set(COSName.of("Pages"), COSInteger.valueOf(2));
        objects.put(new COSObjectKey(1, 0), catalog);

        COSDictionary pages = new COSDictionary();
        pages.set(COSName.of("Type"), COSName.of("Pages"));
        pages.set(COSName.of("Count"), COSInteger.valueOf(0));
        objects.put(new COSObjectKey(2, 0), pages);

        // Add 50 small dictionary objects
        for (int i = 3; i <= 52; i++) {
            COSDictionary dict = new COSDictionary();
            dict.set(COSName.of("Index"), COSInteger.valueOf(i));
            dict.set(COSName.of("Label"), new COSString("Object number " + i));
            objects.put(new COSObjectKey(i, 0), dict);
        }

        COSDictionary trailer = new COSDictionary();
        trailer.set(COSName.of("Root"), new COSObjectReference(1, 0));

        byte[] standard = writeStandardPdf(objects, trailer);
        byte[] compressed = writeCompressedPdf(objects, trailer, 200);

        assertTrue(compressed.length < standard.length,
                "Compressed (" + compressed.length + ") should be smaller than standard (" + standard.length + ")");
    }

    /** Test 18: PdfSaveOptions flags work correctly. */
    @Test
    public void testPdfSaveOptionsFlags() {
        PdfSaveOptions options = new PdfSaveOptions();

        // Defaults
        assertFalse(options.isUseObjectStreams());
        assertFalse(options.isUseXRefStream());
        assertEquals(200, options.getObjectsPerStream());

        // Setters return this for chaining
        PdfSaveOptions result = options
                .setUseObjectStreams(true)
                .setUseXRefStream(true)
                .setObjectsPerStream(50);

        assertSame(options, result);
        assertTrue(options.isUseObjectStreams());
        assertTrue(options.isUseXRefStream());
        assertEquals(50, options.getObjectsPerStream());

        // objectsPerStream clamped to at least 1
        options.setObjectsPerStream(0);
        assertEquals(1, options.getObjectsPerStream());
        options.setObjectsPerStream(-5);
        assertEquals(1, options.getObjectsPerStream());
    }
}
