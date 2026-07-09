package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.pdfobjects.PdfNull;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
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
 * dirty tracking on PDF objects, incremental save, same-file save,
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
        PdfDictionary dict = new PdfDictionary();
        dict.setDirty(false);
        assertFalse(dict.isDirty());
        dict.set(PdfName.of("Key"), PdfInteger.valueOf(42));
        assertTrue(dict.isDirty());
    }

    @Test
    public void cosDictionaryRemoveMarksDirty() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Key"), PdfInteger.valueOf(1));
        dict.setDirty(false);
        dict.remove(PdfName.of("Key"));
        assertTrue(dict.isDirty());
    }

    @Test
    public void cosArrayAddMarksDirty() {
        PdfArray arr = new PdfArray();
        arr.setDirty(false);
        arr.add(PdfInteger.valueOf(1));
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArraySetMarksDirty() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(1));
        arr.setDirty(false);
        arr.set(0, PdfInteger.valueOf(2));
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArrayRemoveMarksDirty() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(1));
        arr.setDirty(false);
        arr.remove(0);
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosArrayClearMarksDirty() {
        PdfArray arr = new PdfArray();
        arr.add(PdfInteger.valueOf(1));
        arr.setDirty(false);
        arr.clear();
        assertTrue(arr.isDirty());
    }

    @Test
    public void cosStreamSetDecodedDataMarksDirty() {
        PdfStream stream = new PdfStream();
        stream.setDirty(false);
        stream.setDecodedData(new byte[]{1, 2, 3});
        assertTrue(stream.isDirty());
    }

    @Test
    public void cosStreamSetEncodedDataMarksDirty() {
        PdfStream stream = new PdfStream();
        stream.setDirty(false);
        stream.setEncodedData(new byte[]{1, 2, 3});
        assertTrue(stream.isDirty());
    }

    @Test
    public void cosBaseDefaultNotDirty() {
        PdfDictionary dict = new PdfDictionary();
        // Default dirty state is false (field default for boolean)
        assertFalse(dict.isDirty());
    }

    @Test
    public void setDirtyFalseResets() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("A"), PdfInteger.valueOf(1));
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
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = parser.getObject(key);
            if (obj != null && !(obj instanceof PdfNull)) {
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
        PdfBase catalog = parser.getObject(1);
        assertNotNull(catalog);
        assertFalse(catalog.isDirty());

        // Modify it
        if (catalog instanceof PdfDictionary) {
            ((PdfDictionary) catalog).set(PdfName.of("NewKey"), PdfInteger.valueOf(99));
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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("TestKey"), new PdfString("TestValue"));

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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("Modified"), PdfInteger.valueOf(1));

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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("Extra"), new PdfString("Data"));

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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("Test"), PdfInteger.valueOf(1));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] result = bos.toByteArray();

        // Reload and check /Size in new trailer
        RandomAccessReader reader = RandomAccessReader.fromBytes(result);
        PDFParser parser = new PDFParser(reader);
        parser.parse();
        PdfDictionary trailer = parser.getTrailer();

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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("Modified"), PdfInteger.valueOf(1));

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
        PdfDictionary catalog = doc.getCatalog();
        catalog.set(PdfName.of("SameFileSave"), PdfInteger.valueOf(1));

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
        PdfDictionary trailer = parseParser.getTrailer();

        // Now do an incremental write
        ByteArrayOutputStream incrBos = new ByteArrayOutputStream();
        PDFWriter incrWriter = new PDFWriter(incrBos, 1.7f);
        RandomAccessReader origReader = RandomAccessReader.fromBytes(originalBytes);

        Map<PdfObjectKey, PdfBase> modObjects = createModifiedCatalog();
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

        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        PdfObjectKey catalogKey = new PdfObjectKey(1, 0);
        PdfObjectKey pagesKey = new PdfObjectKey(2, 0);
        PdfObjectKey infoKey = new PdfObjectKey(3, 0);

        // Object 1: Catalog
        PdfDictionary catalog = new PdfDictionary();
        catalog.set(PdfName.of("Type"), PdfName.of("Catalog"));
        catalog.set(PdfName.PAGES, new PdfObjectReference(pagesKey, k -> objects.get(k)));
        objects.put(catalogKey, catalog);

        // Object 2: Pages
        PdfDictionary pages = new PdfDictionary();
        pages.set(PdfName.of("Type"), PdfName.PAGES);
        pages.set(PdfName.of("Count"), PdfInteger.valueOf(0));
        pages.set(PdfName.of("Kids"), new PdfArray());
        objects.put(pagesKey, pages);

        // Object 3: Info
        PdfDictionary info = new PdfDictionary();
        info.set(PdfName.of("Producer"), new PdfString("OpenPDF Test"));
        objects.put(infoKey, info);

        // Trailer
        PdfDictionary trailer = new PdfDictionary();
        trailer.set(PdfName.ROOT, new PdfObjectReference(catalogKey, k -> objects.get(k)));
        trailer.set(PdfName.of("Size"), PdfInteger.valueOf(4));

        writer.write(trailer, objects);
        return bos.toByteArray();
    }

    /**
     * Creates a minimal set of modified objects for incremental write tests.
     */
    private Map<PdfObjectKey, PdfBase> createModifiedCatalog() {
        Map<PdfObjectKey, PdfBase> modObjects = new LinkedHashMap<>();
        PdfDictionary modCatalog = new PdfDictionary();
        modCatalog.set(PdfName.of("Type"), PdfName.of("Catalog"));
        modCatalog.set(PdfName.of("NewEntry"), PdfInteger.valueOf(42));
        modObjects.put(new PdfObjectKey(1, 0), modCatalog);
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

    // ═══════════════════════════════════════════════════════════════
    //  Object-number collision on incremental update
    // ═══════════════════════════════════════════════════════════════

    /**
     * Regression: in an incremental update the original objects are preserved
     * verbatim in the copied prefix but are absent from the modified-objects
     * map. A freshly-created orphan stream must therefore be numbered above the
     * original {@code /Size}, never reusing a still-live original number.
     *
     * <p>The real-world failure: a redaction appends a content stream that the
     * writer numbered using only the small modified set, colliding with an
     * {@code /ObjStm} that backed compressed objects. On reopen, loading those
     * compressed objects threw "Invalid index 0 in object stream N" because the
     * reused number now pointed at a non-{@code /ObjStm} dictionary (no
     * {@code /N}). Corpus repro: 45502.pdf (RegressionTests_v24_09).</p>
     */
    @Test
    public void incrementalSaveDoesNotReuseOriginalObjectNumbers() throws IOException {
        // Minimal original whose trailer declares /Size 50 — i.e. objects up to
        // number 49 are considered to exist (preserved in the copied prefix).
        byte[] original = ("%PDF-1.7\n"
                + "1 0 obj\n<< /Type /Catalog >>\nendobj\n"
                + "xref\n0 2\n0000000000 65535 f \n0000000009 00000 n \n"
                + "trailer\n<< /Size 50 /Root 1 0 R >>\nstartxref\n0\n%%EOF\n")
                .getBytes(StandardCharsets.US_ASCII);
        RandomAccessReader reader = RandomAccessReader.fromBytes(original);

        PdfDictionary trailer = new PdfDictionary();
        trailer.set(PdfName.of("Size"), PdfInteger.valueOf(50));
        trailer.set(PdfName.of("Root"),
                new PdfObjectReference(new PdfObjectKey(1, 0), k -> PdfNull.INSTANCE));

        // A modified object with a LOW number (5) that owns an orphan stream
        // (no object key). getMaxObjectNumber over the modified set alone is 5,
        // so the buggy allocator would hand the orphan number 6 — a preserved
        // original number.
        PdfStream orphan = new PdfStream();
        orphan.setDecodedData("appended".getBytes(StandardCharsets.US_ASCII));
        PdfDictionary modDict = new PdfDictionary();
        modDict.setObjectKey(new PdfObjectKey(5, 0));
        modDict.set(PdfName.of("Contents"), orphan);

        Map<PdfObjectKey, PdfBase> modified = new LinkedHashMap<>();
        modified.put(new PdfObjectKey(5, 0), modDict);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(out, 1.7f);
        writer.writeIncremental(reader, trailer, modified);

        assertNotNull(orphan.getObjectKey(), "orphan stream was not promoted to indirect");
        assertTrue(orphan.getObjectKey().getObjectNumber() >= 50,
                "orphan stream reused a preserved original number: "
                        + orphan.getObjectKey().getObjectNumber() + " (< /Size 50)");
    }
}
