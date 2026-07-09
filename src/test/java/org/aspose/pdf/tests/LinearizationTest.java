package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.PdfSaveOptions;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.linearization.HintTableGenerator;
import org.aspose.pdf.engine.linearization.LinearizationDetector;
import org.aspose.pdf.engine.linearization.LinearizationParams;
import org.aspose.pdf.engine.linearization.LinearizationPlan;
import org.aspose.pdf.engine.linearization.LinearizedPDFWriter;
import org.aspose.pdf.engine.linearization.PageObjectCollector;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for linearized PDF support:
 * detection, writing, hint tables, page classification, and round-trips.
 */
public class LinearizationTest {

    @TempDir
    Path tempDir;

    // ═══════════════════════════════════════════════════════════════
    //  Detection tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void detectNonLinearizedReturnsNull() throws IOException {
        byte[] pdf = createMinimalPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        LinearizationParams params = LinearizationDetector.detect(reader);
        assertNull(params, "Non-linearized PDF should not be detected as linearized");
        reader.close();
    }

    @Test
    public void detectLinearizedPDF() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(linPdf);
        LinearizationParams params = LinearizationDetector.detect(reader);
        assertNotNull(params, "Linearized PDF should be detected");
        assertTrue(params.getNumPages() > 0, "Should report at least 1 page");
        reader.close();
    }

    @Test
    public void linearizedDictWithinFirst1024Bytes() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        String prefix = new String(linPdf, 0, Math.min(1024, linPdf.length), StandardCharsets.US_ASCII);
        assertTrue(prefix.contains("/Linearized"),
                "/Linearized key must appear within first 1024 bytes");
    }

    @Test
    public void fileLengthMatchesActual() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(linPdf);
        LinearizationParams params = LinearizationDetector.detect(reader);
        assertNotNull(params);
        // /L should match actual length (set during fixup)
        assertEquals(linPdf.length, params.getFileLength(),
                "/L should match actual file length");
        reader.close();
    }

    @Test
    public void modifiedLinearizedPDFInvalid() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(linPdf);
        LinearizationParams params = LinearizationDetector.detect(reader);
        assertNotNull(params);
        // Simulate modification by reporting different length
        assertFalse(params.isValid(linPdf.length + 100),
                "Modified file should invalidate linearization");
        reader.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Linearization structure tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void linearizedPDFHasXrefAndEof() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        String pdf = new String(linPdf, StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("xref"), "Should contain xref table");
        assertTrue(pdf.contains("%%EOF"), "Should contain %%EOF marker");
        assertTrue(pdf.contains("startxref"), "Should contain startxref");
    }

    @Test
    public void linearizedPDFIsParseableByOurParser() throws IOException {
        byte[] linPdf = createLinearizedPDF();
        RandomAccessReader reader = RandomAccessReader.fromBytes(linPdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();
        assertNotNull(parser.getTrailer());
        assertNotNull(parser.getCatalog());
        parser.close();
    }

    @Test
    public void linearizedPreservesPageCount() throws IOException {
        // Create a 1-page PDF, linearize, verify still 1 page
        File pdfFile = createMinimalPDFFile("pagecount.pdf");
        Document doc = new Document(pdfFile.getAbsolutePath());

        File linFile = tempDir.resolve("pagecount-lin.pdf").toFile();
        doc.save(linFile.getAbsolutePath(), new PdfSaveOptions().setLinearize(true));
        doc.close();

        Document linDoc = new Document(linFile.getAbsolutePath());
        assertEquals(1, linDoc.getPages().getCount(), "Page count should be preserved");
        linDoc.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PageObjectCollector tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void singlePageDocAllObjectsInFirstPage() throws IOException {
        byte[] pdf = createMinimalPDFWithPage();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        LinearizationPlan plan = PageObjectCollector.collect(parser);
        assertEquals(1, plan.getNumPages());
        assertTrue(plan.getFirstPageObjects().size() > 0,
                "First page should have at least one object");
        assertTrue(plan.getSharedObjects().isEmpty(),
                "Single page doc should have no shared objects");

        parser.close();
    }

    @Test
    public void pageCollectorClassifiesDocumentLevel() throws IOException {
        byte[] pdf = createMinimalPDFWithPage();
        RandomAccessReader reader = RandomAccessReader.fromBytes(pdf);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        LinearizationPlan plan = PageObjectCollector.collect(parser);
        assertTrue(plan.getDocumentLevel().size() > 0,
                "Should have document-level objects (catalog, pages dict, etc.)");

        parser.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  HintTableGenerator tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void hintTableProducesNonEmptyData() {
        LinearizationPlan plan = createTestPlan(2);
        long[] pageOffsets = {100, 500};
        int[] pageLengths = {400, 300};
        int[] pageObjCounts = {5, 3};
        long[] sharedOffsets = {800};
        int[] sharedLengths = {100};
        List<List<Integer>> sharedRefs = Arrays.asList(
                Arrays.asList(0), new ArrayList<>());

        byte[] hint = HintTableGenerator.generate(plan, pageOffsets, pageLengths,
                pageObjCounts, sharedOffsets, sharedLengths, sharedRefs);

        assertNotNull(hint);
        assertTrue(hint.length > 0, "Hint table should produce non-empty data");
    }

    @Test
    public void bitsNeededCalculation() {
        assertEquals(0, HintTableGenerator.bitsNeeded(0));
        assertEquals(1, HintTableGenerator.bitsNeeded(1));
        assertEquals(2, HintTableGenerator.bitsNeeded(2));
        assertEquals(2, HintTableGenerator.bitsNeeded(3));
        assertEquals(3, HintTableGenerator.bitsNeeded(4));
        assertEquals(8, HintTableGenerator.bitsNeeded(255));
        assertEquals(9, HintTableGenerator.bitsNeeded(256));
    }

    // ═══════════════════════════════════════════════════════════════
    //  BitOutputStream tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void bitOutputStreamWriteFullByte() {
        HintTableGenerator.BitOutputStream bits = new HintTableGenerator.BitOutputStream();
        bits.writeBits(0xFF, 8);
        byte[] result = bits.toByteArray();
        assertEquals(1, result.length);
        assertEquals((byte) 0xFF, result[0]);
    }

    @Test
    public void bitOutputStreamWriteAcrossBoundary() {
        HintTableGenerator.BitOutputStream bits = new HintTableGenerator.BitOutputStream();
        bits.writeBits(0xF, 4);  // 1111 in high nibble
        bits.writeBits(0xA, 4);  // 1010 in low nibble
        byte[] result = bits.toByteArray();
        assertEquals(1, result.length);
        assertEquals((byte) 0xFA, result[0]);
    }

    @Test
    public void bitOutputStreamMultipleBytes() {
        HintTableGenerator.BitOutputStream bits = new HintTableGenerator.BitOutputStream();
        bits.writeBits(0xAB, 8);
        bits.writeBits(0xCD, 8);
        byte[] result = bits.toByteArray();
        assertEquals(2, result.length);
        assertEquals((byte) 0xAB, result[0]);
        assertEquals((byte) 0xCD, result[1]);
    }

    @Test
    public void bitOutputStreamAlignToByte() {
        HintTableGenerator.BitOutputStream bits = new HintTableGenerator.BitOutputStream();
        bits.writeBits(1, 3);    // 001 -> 0010_0000
        bits.alignToByte();
        bits.writeBits(0xFF, 8);
        byte[] result = bits.toByteArray();
        assertEquals(2, result.length);
        assertEquals((byte) 0x20, result[0]); // 001 + 00000
        assertEquals((byte) 0xFF, result[1]);
    }

    @Test
    public void bitOutputStream32BitValue() {
        HintTableGenerator.BitOutputStream bits = new HintTableGenerator.BitOutputStream();
        bits.writeBits(0x12345678, 32);
        byte[] result = bits.toByteArray();
        assertEquals(4, result.length);
        assertEquals((byte) 0x12, result[0]);
        assertEquals((byte) 0x34, result[1]);
        assertEquals((byte) 0x56, result[2]);
        assertEquals((byte) 0x78, result[3]);
    }

    // ═══════════════════════════════════════════════════════════════
    //  LinearizationParams tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void linearizationParamsRoundTrip() {
        LinearizationParams params = new LinearizationParams();
        params.setFileLength(12345);
        params.setNumPages(3);
        params.setFirstPageObjNum(5);
        params.setEndOfFirstPage(1000);
        params.setMainXRefOffset(10000);
        params.setHintStreamOffset(500);
        params.setHintStreamLength(200);

        PdfDictionary dict = params.toDictionary();
        LinearizationParams parsed = LinearizationParams.parse(dict);

        assertNotNull(parsed);
        assertEquals(12345, parsed.getFileLength());
        assertEquals(3, parsed.getNumPages());
        assertEquals(5, parsed.getFirstPageObjNum());
        assertEquals(1000, parsed.getEndOfFirstPage());
        assertEquals(10000, parsed.getMainXRefOffset());
        assertEquals(500, parsed.getHintStreamOffset());
        assertEquals(200, parsed.getHintStreamLength());
    }

    @Test
    public void linearizationParamsParseNonLinDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Type"), PdfName.of("Catalog"));
        assertNull(LinearizationParams.parse(dict));
    }

    // ═══════════════════════════════════════════════════════════════
    //  PdfSaveOptions tests
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void saveOptionsLinearizeDefault() {
        PdfSaveOptions opts = new PdfSaveOptions();
        assertFalse(opts.isLinearize());
    }

    @Test
    public void saveOptionsLinearizeChaining() {
        PdfSaveOptions opts = new PdfSaveOptions().setLinearize(true);
        assertTrue(opts.isLinearize());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Document integration
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void documentSaveLinearized() throws IOException {
        File pdfFile = createMinimalPDFFile("doc-lin.pdf");
        Document doc = new Document(pdfFile.getAbsolutePath());

        File linFile = tempDir.resolve("doc-lin-out.pdf").toFile();
        doc.save(linFile.getAbsolutePath(), new PdfSaveOptions().setLinearize(true));
        doc.close();

        assertTrue(linFile.length() > 0, "Linearized file should not be empty");

        // Verify it's parseable
        Document linDoc = new Document(linFile.getAbsolutePath());
        assertNotNull(linDoc.getCatalog());
        linDoc.close();
    }

    @Test
    public void documentSaveNonLinearizedWithOptions() throws IOException {
        File pdfFile = createMinimalPDFFile("doc-nonlin.pdf");
        Document doc = new Document(pdfFile.getAbsolutePath());

        File outFile = tempDir.resolve("doc-nonlin-out.pdf").toFile();
        doc.save(outFile.getAbsolutePath(), new PdfSaveOptions()); // linearize=false
        doc.close();

        assertTrue(outFile.length() > 0);
        // Should NOT have /Linearized
        byte[] bytes = java.nio.file.Files.readAllBytes(outFile.toPath());
        String prefix = new String(bytes, 0, Math.min(1024, bytes.length), StandardCharsets.US_ASCII);
        assertFalse(prefix.contains("/Linearized"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    private byte[] createMinimalPDF() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);
        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        PdfObjectKey catKey = new PdfObjectKey(1, 0);
        PdfObjectKey pagesKey = new PdfObjectKey(2, 0);

        PdfDictionary catalog = new PdfDictionary();
        catalog.set(PdfName.of("Type"), PdfName.of("Catalog"));
        catalog.set(PdfName.PAGES, new PdfObjectReference(pagesKey, k -> objects.get(k)));
        objects.put(catKey, catalog);

        PdfDictionary pages = new PdfDictionary();
        pages.set(PdfName.of("Type"), PdfName.PAGES);
        pages.set(PdfName.of("Count"), PdfInteger.valueOf(0));
        pages.set(PdfName.KIDS, new PdfArray());
        objects.put(pagesKey, pages);

        PdfDictionary trailer = new PdfDictionary();
        trailer.set(PdfName.ROOT, new PdfObjectReference(catKey, k -> objects.get(k)));
        trailer.set(PdfName.of("Size"), PdfInteger.valueOf(3));
        writer.write(trailer, objects);
        return bos.toByteArray();
    }

    /**
     * Creates a minimal PDF with one actual page (needed for linearization).
     */
    private byte[] createMinimalPDFWithPage() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(bos, 1.7f);
        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        PdfObjectKey catKey = new PdfObjectKey(1, 0);
        PdfObjectKey pagesKey = new PdfObjectKey(2, 0);
        PdfObjectKey pageKey = new PdfObjectKey(3, 0);

        // Page
        PdfDictionary page = new PdfDictionary();
        page.set(PdfName.of("Type"), PdfName.of("Page"));
        page.set(PdfName.PARENT, new PdfObjectReference(pagesKey, k -> objects.get(k)));
        PdfArray mediaBox = new PdfArray();
        mediaBox.add(PdfInteger.valueOf(0)); mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(PdfInteger.valueOf(612)); mediaBox.add(PdfInteger.valueOf(792));
        page.set(PdfName.of("MediaBox"), mediaBox);
        objects.put(pageKey, page);

        // Pages
        PdfArray kids = new PdfArray();
        kids.add(new PdfObjectReference(pageKey, k -> objects.get(k)));
        PdfDictionary pages = new PdfDictionary();
        pages.set(PdfName.of("Type"), PdfName.PAGES);
        pages.set(PdfName.of("Count"), PdfInteger.valueOf(1));
        pages.set(PdfName.KIDS, kids);
        objects.put(pagesKey, pages);

        // Catalog
        PdfDictionary catalog = new PdfDictionary();
        catalog.set(PdfName.of("Type"), PdfName.of("Catalog"));
        catalog.set(PdfName.PAGES, new PdfObjectReference(pagesKey, k -> objects.get(k)));
        objects.put(catKey, catalog);

        PdfDictionary trailer = new PdfDictionary();
        trailer.set(PdfName.ROOT, new PdfObjectReference(catKey, k -> objects.get(k)));
        trailer.set(PdfName.of("Size"), PdfInteger.valueOf(4));
        writer.write(trailer, objects);
        return bos.toByteArray();
    }

    /**
     * Creates a linearized PDF from a minimal 1-page document.
     */
    private byte[] createLinearizedPDF() throws IOException {
        byte[] source = createMinimalPDFWithPage();
        RandomAccessReader reader = RandomAccessReader.fromBytes(source);
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LinearizedPDFWriter linWriter = new LinearizedPDFWriter();
        linWriter.write(bos, parser, parser.getTrailer());
        parser.close();

        return bos.toByteArray();
    }

    private File createMinimalPDFFile(String name) throws IOException {
        byte[] pdf = createMinimalPDFWithPage();
        File file = tempDir.resolve(name).toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pdf);
        }
        return file;
    }

    /**
     * Re-linearizing an already-linearized document must not accumulate a second
     * {@code /Linearized} dictionary. The writer emits a fresh one as the first
     * object; the stale one carried over from the source is dropped by
     * {@link PageObjectCollector}. The result must remain a valid, linearized,
     * same-page-count document. Mirrors the corpus repro (29360-1.pdf).
     */
    @Test
    public void reLinearizingKeepsExactlyOneLinearizationDict() throws IOException {
        File src = createMinimalPDFFile("relin-src.pdf");

        File lin1 = tempDir.resolve("relin-1.pdf").toFile();
        try (Document d = new Document(src.getAbsolutePath())) {
            d.save(lin1.getAbsolutePath(), new PdfSaveOptions().setLinearize(true));
        }
        // Reopen the (now already-linearized) output and linearize it AGAIN.
        File lin2 = tempDir.resolve("relin-2.pdf").toFile();
        try (Document d = new Document(lin1.getAbsolutePath())) {
            d.save(lin2.getAbsolutePath(), new PdfSaveOptions().setLinearize(true));
        }

        byte[] bytes = java.nio.file.Files.readAllBytes(lin2.toPath());
        String text = new String(bytes, StandardCharsets.ISO_8859_1);
        int dicts = 0;
        for (int i = text.indexOf("/Linearized"); i >= 0; i = text.indexOf("/Linearized", i + 1)) {
            dicts++;
        }
        assertEquals(1, dicts,
                "re-linearized file must carry exactly one /Linearized dictionary, found " + dicts);

        try (Document d = new Document(lin2.getAbsolutePath())) {
            assertEquals(1, d.getPages().getCount(), "page count must survive re-linearization");
            assertTrue(d.isLinearized(), "re-linearized document must be detected as linearized");
        }
    }

    /**
     * Creates a test LinearizationPlan for hint table tests.
     */
    private LinearizationPlan createTestPlan(int numPages) {
        List<PdfObjectKey> pageKeys = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            pageKeys.add(new PdfObjectKey(i + 10, 0));
        }
        return new LinearizationPlan(
                pageKeys, 0,
                List.of(new PdfObjectKey(10, 0)),  // first page private
                List.of(new PdfObjectKey(20, 0)),  // first page shared
                new LinkedHashMap<>(),              // other page private
                List.of(new PdfObjectKey(20, 0)),  // shared
                List.of(new PdfObjectKey(1, 0)),    // document level
                numPages);
    }
}
