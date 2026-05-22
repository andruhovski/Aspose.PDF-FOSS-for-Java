package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.cos.COSNull;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for incremental update functionality:
 * dirty tracking on COS objects, incremental save, same-file save,
 * and full write for new documents.
 */
public class IncrementalUpdateTest {

    @TempDir
    Path tempDir;

    // ═══════════════════════════════════════════════════════════════
    //  Dirty tracking tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void cosDictionarySetMarksDirty() {
        COSDictionary dict = new COSDictionary();
        dict.setDirty(false);
        assertFalse(dict.isDirty());
        dict.set(COSName.of("Key"), COSInteger.valueOf(42));
        assertTrue(dict.isDirty());
    }

    @Test
    public void cosDictionaryRemoveMarksDirty() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Key"), COSInteger.valueOf(1));
        dict.setDirty(false);
        dict.remove(COSName.of("Key"));
        assertTrue(dict.isDirty());
    }

    @Test
    public void cosArrayAddMarksDirty() {
        COSArray arr = new COSArray();
        arr.setDirty(false);
        arr.add(COSInteger.valueOf(1));
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArraySetMarksDirty() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(1));
        arr.setDirty(false);
        arr.set(0, COSInteger.valueOf(2));
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArrayRemoveMarksDirty() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(1));
        arr.setDirty(false);
        arr.remove(0);
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArrayClearMarksDirty() {
        COSArray arr = new COSArray();
        arr.add(COSInteger.valueOf(1));
        arr.setDirty(false);
        arr.clear();
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosStreamSetDecodedDataMarksDirty() {
        COSStream stream = new COSStream();
        stream.setDirty(false);
        stream.setDecodedData(new byte[]{1, 2, 3});
        assertTrue(stream.isDirty());
    }

    @Test
    public void cosStreamSetEncodedDataMarksDirty() {
        COSStream stream = new COSStream();
        stream.setDirty(false);
        stream.setEncodedData(new byte[]{1, 2, 3});
        assertTrue(stream.isDirty());
    }

    @Test
    public void cosBaseDefaultNotDirty() {
        COSDictionary dict = new COSDictionary();
        // Default dirty state is false (field default for boolean)
        assertFalse(dict.isDirty());
    }

    @Test
    public void setDirtyFalseResets() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("A"), COSInteger.valueOf(1));
        assertTrue(dict.isDirty());
        dict.setDirty(false);
        assertFalse(dict.isDirty());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Parser clears dirty flags after loading
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void afterParseObjectsNotDirty() throws IOException {
        byte[] pdfBytes = createMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdfBytes);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        // Trailer should not be dirty after parse
        assertFalse(parser.getTrailer().isDirty());

        // Load an object — it should not be dirty
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj = parser.getObject(key);
            if (obj != null && !(obj instanceof COSNull)) {
                assertFalse(obj.isDirty(), "Object " + key + " should not be dirty after loading");
            }
        }

        parser.close();
    }

    @Test
    public void modifyAfterParseMarksDirty() throws IOException {
        byte[] pdfBytes = createMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdfBytes);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        // Find the catalog (object 1)
        COSBase catalog = parser.getObject(1);
        assertNotNull(catalog);
        assertFalse(catalog.isDirty());

        // Modify it
        if (catalog instanceof COSDictionary) {
            ((COSDictionary) catalog).set(COSName.of("NewKey"), COSInteger.valueOf(99));
            assertTrue(catalog.isDirty());
        }

        parser.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Document source path tracking
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void documentFromFileHasSourcePath() throws IOException {
        File pdfFile = writePdfToTempFile("source-test.pdf");
        Document doc = new Document(pdfFile.getAbsolutePath());
        assertEquals(pdfFile.getAbsolutePath(), doc.getSourcePath());
        doc.close();
    }

    @Test
    public void newDocumentHasNoSourcePath() {
        Document doc = new Document();
        assertNull(doc.getSourcePath());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Save: full rewrite for new documents
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void newDocumentSavesFullWrite() throws IOException {
        Document doc = new Document();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] result = bos.toByteArray();
        String pdf = new String(result, StandardCharsets.ISO_8859_1);
        assertTrue(pdf.startsWith("%PDF-"));
        assertTrue(pdf.contains("xref"));
        assertTrue(pdf.contains("%%EOF"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Incremental save: modified document
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void incrementalSaveStartsWithOriginalBytes() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "incr-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        // Modify the catalog
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("TestKey"), new COSString("TestValue"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] result = bos.toByteArray();

        // Result should start with original bytes
        assertTrue(result.length >= originalPdf.length,
                "Incremental save should be >= original size");
        for (int i = 0; i < originalPdf.length; i++) {
            assertEquals(originalPdf[i], result[i],
                    "Byte " + i + " should match original");
        }

        doc.close();
    }

    @Test
    public void incrementalSaveHasPrevInTrailer() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "prev-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        // Modify something
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("Modified"), COSInteger.valueOf(1));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);

        // Should have /Prev in the new trailer
        assertTrue(pdf.contains("/Prev"), "Incremental trailer should contain /Prev");

        doc.close();
    }

    @Test
    public void incrementalSavePreservesOriginalStructure() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "preserve-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("Extra"), new COSString("Data"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] result = bos.toByteArray();

        // Reload and verify the document is valid
        RandomAccessReader reader = RandomAccessReader.fromBytes(result);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        assertNotNull(parser.getTrailer());
        assertNotNull(parser.getCatalog());
        parser.close();

        doc.close();
    }

    @Test
    public void noModificationDoesFullRewrite() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "nomod-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        // Don't modify anything

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);

        // No modifications → full rewrite → no /Prev
        assertFalse(pdf.contains("/Prev"), "Full rewrite should not contain /Prev");

        doc.close();
    }

    @Test
    public void incrementalSaveTrailerSizeIsCorrect() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "size-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("Test"), COSInteger.valueOf(1));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] result = bos.toByteArray();

        // Reload and check /Size in new trailer
        RandomAccessReader reader = RandomAccessReader.fromBytes(result);
        PDFParser parser = new PDFParser(reader);
        parser.parse();
        COSDictionary trailer = parser.getTrailer();

        int size = trailer.getInt("Size", -1);
        assertTrue(size >= 3, "Trailer /Size should be >= 3 (catalog + pages + info)");

        parser.close();
        doc.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Incremental xref subsections
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void incrementalXrefHasSubsections() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "subsection-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("Modified"), COSInteger.valueOf(1));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        String pdf = new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);

        // Count "xref" occurrences — original has one, incremental adds another
        int xrefCount = countOccurrences(pdf, "xref\n");
        assertTrue(xrefCount >= 2, "Should have at least 2 xref sections (original + incremental)");

        doc.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Same-file save
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void saveToSameFileWorks() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "samefile-test.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        COSDictionary catalog = doc.getCatalog();
        catalog.set(COSName.of("SameFileSave"), COSInteger.valueOf(1));

        // Save to the same file
        doc.save(pdfFile.getAbsolutePath());
        doc.close();

        // Verify the file is still a valid PDF
        Document doc2 = new Document(pdfFile.getAbsolutePath());
        assertNotNull(doc2.getCatalog());
        doc2.close();
    }

    @Test
    public void saveToSameFilePreservesContent() throws IOException {
        byte[] originalPdf = createMinimalPDF();
        File pdfFile = writeBytesToTempFile(originalPdf, "samefile-preserve.pdf");

        Document doc = new Document(pdfFile.getAbsolutePath());
        // Don't modify — just save
        doc.save(pdfFile.getAbsolutePath());
        doc.close();

        // Verify still valid
        Document doc2 = new Document(pdfFile.getAbsolutePath());
        assertNotNull(doc2.getCatalog());
        doc2.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Object allocation
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void allocateObjectNumberNewDocument() {
        Document doc = new Document();
        int first = doc.allocateObjectNumber();
        int second = doc.allocateObjectNumber();
        assertEquals(first + 1, second);
        assertTrue(first >= 1);
    }

    @Test
    public void allocateObjectNumberLoadedDocument() throws IOException {
        File pdfFile = writePdfToTempFile("alloc-test.pdf");
        Document doc = new Document(pdfFile.getAbsolutePath());

        int allocated = doc.allocateObjectNumber();
        // Should be > max existing object number
        assertTrue(allocated >= 1);

        int next = doc.allocateObjectNumber();
        assertEquals(allocated + 1, next);

        doc.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PDFWriter incremental xref table
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void writeIncrementalProducesValidPDF() throws IOException {
        // Create original PDF
        byte[] originalBytes = createMinimalPDF();

        // Parse to get trailer
        RandomAccessReader parseReader = RandomAccessReader.fromBytes(originalBytes);
        PDFParser parseParser = new PDFParser(parseReader);
        parseParser.parse();
        COSDictionary trailer = parseParser.getTrailer();

        // Now do an incremental write
        ByteArrayOutputStream incrBos = new ByteArrayOutputStream();
        PDFWriter incrWriter = new PDFWriter(incrBos, 1.7f);
        RandomAccessReader origReader = RandomAccessReader.fromBytes(originalBytes);

        Map<COSObjectKey, COSBase> modObjects = createModifiedCatalog();
        incrWriter.writeIncremental(origReader, trailer, modObjects);
        origReader.close();
        parseParser.close();

        byte[] result = incrBos.toByteArray();
        String pdf = new String(result, StandardCharsets.ISO_8859_1);

        // Verify incremental structure
        assertTrue(pdf.contains("/Prev"));
        assertTrue(result.length > originalBytes.length);

        // Verify the result is parseable
        RandomAccessReader resultReader = RandomAccessReader.fromBytes(result);
        PDFParser parser = new PDFParser(resultReader);
        parser.parse();
        assertNotNull(parser.getTrailer());
        parser.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates a minimal valid PDF byte array using PDFWriter.
     */
    private byte[] createMinimalPDF() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);

        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        COSObjectKey catalogKey = new COSObjectKey(1, 0);
        COSObjectKey pagesKey = new COSObjectKey(2, 0);
        COSObjectKey infoKey = new COSObjectKey(3, 0);

        // Object 1: Catalog
        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.of("Type"), COSName.of("Catalog"));
        catalog.set(COSName.PAGES, new COSObjectReference(pagesKey, k -> objects.get(k)));
        objects.put(catalogKey, catalog);

        // Object 2: Pages
        COSDictionary pages = new COSDictionary();
        pages.set(COSName.of("Type"), COSName.PAGES);
        pages.set(COSName.of("Count"), COSInteger.valueOf(0));
        pages.set(COSName.of("Kids"), new COSArray());
        objects.put(pagesKey, pages);

        // Object 3: Info
        COSDictionary info = new COSDictionary();
        info.set(COSName.of("Producer"), new COSString("OpenPDF Test"));
        objects.put(infoKey, info);

        // Trailer
        COSDictionary trailer = new COSDictionary();
        trailer.set(COSName.ROOT, new COSObjectReference(catalogKey, k -> objects.get(k)));
        trailer.set(COSName.of("Size"), COSInteger.valueOf(4));

        writer.write(trailer, objects);
        return bos.toByteArray();
    }

    /**
     * Creates a minimal set of modified objects for incremental write tests.
     */
    private Map<COSObjectKey, COSBase> createModifiedCatalog() {
        Map<COSObjectKey, COSBase> modObjects = new LinkedHashMap<>();
        COSDictionary modCatalog = new COSDictionary();
        modCatalog.set(COSName.of("Type"), COSName.of("Catalog"));
        modCatalog.set(COSName.of("NewEntry"), COSInteger.valueOf(42));
        modObjects.put(new COSObjectKey(1, 0), modCatalog);
        return modObjects;
    }

    /**
     * Writes a minimal PDF to a temp file and returns the File.
     */
    private File writePdfToTempFile(String name) throws IOException {
        byte[] pdfBytes = createMinimalPDF();
        return writeBytesToTempFile(pdfBytes, name);
    }

    /**
     * Writes bytes to a temp file and returns the File.
     */
    private File writeBytesToTempFile(byte[] data, String name) throws IOException {
        File file = tempDir.resolve(name).toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file;
    }

    /**
     * Counts occurrences of a substring in a string.
     */
    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
