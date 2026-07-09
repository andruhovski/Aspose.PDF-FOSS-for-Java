package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PageCollection mutation methods: add(), add(Page), insert(), delete().
 */
public class PageCollectionMutationTest {

    private PdfDictionary pagesDict;
    private PdfArray kids;
    private PageCollection collection;

    @BeforeEach
    public void setUp() {
        pagesDict = new PdfDictionary();
        pagesDict.set(PdfName.TYPE, PdfName.PAGES);
        kids = new PdfArray();
        pagesDict.set(PdfName.KIDS, kids);
        pagesDict.set(PdfName.COUNT, PdfInteger.valueOf(0));
        collection = new PageCollection(pagesDict, null);
    }

    @Test
    public void addDefaultPage() {
        Page page = collection.add();
        assertNotNull(page);
        assertEquals(1, collection.size());
        assertEquals(1, kids.size());
    }

    @Test
    public void addDefaultPageHasA4MediaBox() {
        Page page = collection.add();
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(0, mb.getLLX(), 1e-10);
        assertEquals(0, mb.getLLY(), 1e-10);
        assertEquals(595, mb.getURX(), 1e-10);
        assertEquals(842, mb.getURY(), 1e-10);
    }

    @Test
    public void addPageMediaBoxIsExactlyA4_LogsCorrectDimensions() throws Exception {
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger("org.aspose.pdf.PageCollection");
        java.util.logging.Level orig = logger.getLevel();
        logger.setLevel(java.util.logging.Level.FINE);
        java.util.List<String> messages = new java.util.ArrayList<>();
        java.util.logging.Handler h = new java.util.logging.Handler() {
            public void publish(java.util.logging.LogRecord r) { messages.add(r.getMessage()); }
            public void flush() {} public void close() {}
        };
        logger.addHandler(h);
        try {
            Page p = collection.add();
            Rectangle mb = p.getMediaBox();
            assertEquals(842.0, mb.getURY(), 1e-10);
            assertEquals(595.0, mb.getURX(), 1e-10);
        } finally {
            logger.removeHandler(h);
            logger.setLevel(orig);
        }
    }

    @Test
    public void addDefaultPageSetsParent() {
        Page page = collection.add();
        PdfDictionary pageDict = page.getPdfDictionary();
        assertSame(pagesDict, pageDict.get(PdfName.PARENT));
    }

    @Test
    public void addDefaultPageSetsTypeToPage() {
        Page page = collection.add();
        PdfDictionary pageDict = page.getPdfDictionary();
        assertEquals(PdfName.PAGE, pageDict.get(PdfName.TYPE));
    }

    @Test
    public void addMultipleDefaultPages() {
        collection.add();
        collection.add();
        collection.add();
        assertEquals(3, collection.size());
        assertEquals(3, kids.size());
    }

    @Test
    public void addExistingPage() {
        PdfDictionary pageDict = createPageDict();
        Page page = new Page(pageDict, null);
        collection.add(page);
        assertEquals(1, collection.size());
        assertSame(pagesDict, pageDict.get(PdfName.PARENT));
    }

    @Test
    public void addNullPageThrows() {
        assertThrows(IllegalArgumentException.class, () -> collection.add((Page) null));
    }

    @Test
    public void insertAtBeginning() {
        collection.add(); // page 1
        collection.add(); // page 2

        PdfDictionary insertDict = createPageDict();
        Page insertPage = new Page(insertDict, null);
        collection.insert(1, insertPage);

        assertEquals(3, collection.size());
        // The inserted page should be at position 1
        assertSame(insertDict, collection.get(1).getPdfDictionary());
    }

    @Test
    public void insertAtEnd() {
        collection.add(); // page 1

        PdfDictionary insertDict = createPageDict();
        Page insertPage = new Page(insertDict, null);
        collection.insert(2, insertPage);

        assertEquals(2, collection.size());
        assertSame(insertDict, collection.get(2).getPdfDictionary());
    }

    @Test
    public void insertInMiddle() {
        Page p1 = collection.add();
        Page p3 = collection.add();

        PdfDictionary insertDict = createPageDict();
        Page p2 = new Page(insertDict, null);
        collection.insert(2, p2);

        assertEquals(3, collection.size());
        assertSame(p1.getPdfDictionary(), collection.get(1).getPdfDictionary());
        assertSame(insertDict, collection.get(2).getPdfDictionary());
        assertSame(p3.getPdfDictionary(), collection.get(3).getPdfDictionary());
    }

    @Test
    public void insertSetsParent() {
        PdfDictionary insertDict = createPageDict();
        Page page = new Page(insertDict, null);
        collection.insert(1, page);
        assertSame(pagesDict, insertDict.get(PdfName.PARENT));
    }

    @Test
    public void insertNullPageThrows() {
        assertThrows(IllegalArgumentException.class, () -> collection.insert(1, null));
    }

    @Test
    public void insertOutOfBoundsThrows() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                collection.insert(0, new Page(createPageDict(), null)));
        assertThrows(IndexOutOfBoundsException.class, () ->
                collection.insert(2, new Page(createPageDict(), null)));
    }

    @Test
    public void deleteFirstPage() {
        collection.add();
        collection.add();
        collection.delete(1);
        assertEquals(1, collection.size());
    }

    @Test
    public void deleteLastPage() {
        Page p1 = collection.add();
        collection.add();
        collection.delete(2);
        assertEquals(1, collection.size());
        assertSame(p1.getPdfDictionary(), collection.get(1).getPdfDictionary());
    }

    @Test
    public void deleteMiddlePage() {
        Page p1 = collection.add();
        collection.add();
        Page p3 = collection.add();
        collection.delete(2);
        assertEquals(2, collection.size());
        assertSame(p1.getPdfDictionary(), collection.get(1).getPdfDictionary());
        assertSame(p3.getPdfDictionary(), collection.get(2).getPdfDictionary());
    }

    @Test
    public void deleteOnlyPage() {
        collection.add();
        collection.delete(1);
        assertEquals(0, collection.size());
    }

    @Test
    public void deleteOutOfBoundsThrows() {
        collection.add();
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(0));
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(2));
    }

    @Test
    public void deleteFromEmptyThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(1));
    }

    @Test
    public void countUpdatedAfterAdd() {
        collection.add();
        collection.add();
        int count = pagesDict.getInt(PdfName.COUNT, -1);
        assertEquals(2, count);
    }

    @Test
    public void countUpdatedAfterDelete() {
        collection.add();
        collection.add();
        collection.delete(1);
        int count = pagesDict.getInt(PdfName.COUNT, -1);
        assertEquals(1, count);
    }

    @Test
    public void countUpdatedAfterInsert() {
        collection.add();
        collection.insert(1, new Page(createPageDict(), null));
        int count = pagesDict.getInt(PdfName.COUNT, -1);
        assertEquals(2, count);
    }

    @Test
    public void pageNumbersReassignedAfterMutation() {
        collection.add();
        collection.add();
        collection.add();
        // Delete middle page
        collection.delete(2);
        // Page numbers should be 1 and 2
        assertEquals(1, collection.get(1).getNumber());
        assertEquals(2, collection.get(2).getNumber());
    }

    /**
     * Creates a minimal page dictionary for testing.
     */
    private PdfDictionary createPageDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.TYPE, PdfName.PAGE);
        dict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 612, 792).toPdfArray());
        return dict;
    }

    /**
     * Regression: some malformed PDFs point the catalog's {@code /Pages} at a
     * leaf page instead of the page-tree root (the leaf's {@code /Parent} is the
     * real {@code /Type /Pages} node). Reading works, but {@code add()} used to
     * mutate the leaf and the new page was silently lost on save/reopen. The
     * document must normalize to the true root so an added page survives.
     * Corpus repro: 33579-2.pdf.
     */
    @Test
    public void catalogPagesPointingAtLeafStillPersistsAddedPage() throws java.io.IOException {
        // Catalog /Pages -> obj 9 (a /Type /Page LEAF) whose /Parent is obj 35,
        // the real /Type /Pages root (Kids=[9], Count 1).
        java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
        java.util.Map<Integer, Integer> offsets = new java.util.LinkedHashMap<>();
        writeAscii(body, "%PDF-1.4\n");
        offsets.put(1, body.size());
        writeAscii(body, "1 0 obj\n<< /Type /Catalog /Pages 9 0 R >>\nendobj\n");
        offsets.put(9, body.size());
        writeAscii(body, "9 0 obj\n<< /Type /Page /Parent 35 0 R /MediaBox [0 0 612 792] >>\nendobj\n");
        offsets.put(35, body.size());
        writeAscii(body, "35 0 obj\n<< /Type /Pages /Kids [9 0 R] /Count 1 >>\nendobj\n");
        int xrefOff = body.size();
        StringBuilder xref = new StringBuilder("xref\n0 1\n0000000000 65535 f \n");
        for (java.util.Map.Entry<Integer, Integer> e : offsets.entrySet()) {
            xref.append(e.getKey()).append(" 1\n")
                .append(String.format("%010d 00000 n \n", e.getValue()));
        }
        xref.append("trailer\n<< /Size 36 /Root 1 0 R >>\nstartxref\n")
            .append(xrefOff).append("\n%%EOF\n");
        writeAscii(body, xref.toString());

        java.nio.file.Path src = java.nio.file.Files.createTempFile("leaf-root-", ".pdf");
        java.nio.file.Path out = java.nio.file.Files.createTempFile("leaf-root-out-", ".pdf");
        try {
            java.nio.file.Files.write(src, body.toByteArray());
            try (org.aspose.pdf.Document d = new org.aspose.pdf.Document(src.toString())) {
                assertEquals(1, d.getPages().getCount(), "leaf-rooted document should read as 1 page");
                d.getPages().add();
                d.save(out.toString());
            }
            try (org.aspose.pdf.Document reopened = new org.aspose.pdf.Document(out.toString())) {
                assertEquals(2, reopened.getPages().getCount(),
                        "page added to a leaf-rooted document must survive save/reopen");
            }
        } finally {
            java.nio.file.Files.deleteIfExists(src);
            java.nio.file.Files.deleteIfExists(out);
        }
    }

    private static void writeAscii(java.io.ByteArrayOutputStream out, String s) {
        byte[] b = s.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        out.write(b, 0, b.length);
    }
}
