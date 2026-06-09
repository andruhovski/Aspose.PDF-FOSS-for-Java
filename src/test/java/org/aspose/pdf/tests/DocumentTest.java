package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.DocumentInfo;
import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Document}.
 */
public class DocumentTest {

    /**
     * Creates a minimal valid PDF as a byte array using PDFWriter.
     * Structure: Catalog -> Pages -> Page (with MediaBox)
     */
    private static byte[] createMinimalPdf() throws IOException {
        return createMinimalPdf(null);
    }

    /**
     * Creates a minimal valid PDF with optional /Info dictionary.
     */
    private static byte[] createMinimalPdf(PdfDictionary infoDict) throws IOException {
        // Build PDF objects
        // Object 1: Catalog
        PdfDictionary catalog = new PdfDictionary();
        catalog.set("Type", PdfName.of("Catalog"));
        catalog.set("Pages", new PdfObjectReference(new PdfObjectKey(2, 0)));

        // Object 2: Pages
        PdfDictionary pages = new PdfDictionary();
        pages.set("Type", PdfName.PAGES);
        PdfArray kids = new PdfArray(1);
        kids.add(new PdfObjectReference(new PdfObjectKey(3, 0)));
        pages.set("Kids", kids);
        pages.set("Count", PdfInteger.valueOf(1));

        // Object 3: Page
        PdfDictionary page = new PdfDictionary();
        page.set("Type", PdfName.PAGE);
        page.set("Parent", new PdfObjectReference(new PdfObjectKey(2, 0)));
        PdfArray mediaBox = new PdfArray(4);
        mediaBox.add(new PdfFloat(0));
        mediaBox.add(new PdfFloat(0));
        mediaBox.add(new PdfFloat(612));
        mediaBox.add(new PdfFloat(792));
        page.set("MediaBox", mediaBox);

        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        objects.put(new PdfObjectKey(1, 0), catalog);
        objects.put(new PdfObjectKey(2, 0), pages);
        objects.put(new PdfObjectKey(3, 0), page);

        // Trailer
        PdfDictionary trailer = new PdfDictionary();
        trailer.set("Root", new PdfObjectReference(new PdfObjectKey(1, 0)));
        trailer.set("Size", PdfInteger.valueOf(4));

        if (infoDict != null) {
            objects.put(new PdfObjectKey(4, 0), infoDict);
            trailer.set("Info", new PdfObjectReference(new PdfObjectKey(4, 0)));
            trailer.set("Size", PdfInteger.valueOf(5));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDFWriter writer = new PDFWriter(baos, 1.4f);
        writer.write(trailer, objects);
        return baos.toByteArray();
    }

    @Test
    public void constructorFromStreamParsesMinimalPdf() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            assertNotNull(doc);
            assertNotNull(doc.getTrailer());
            assertNotNull(doc.getCatalog());
        }
    }

    @Test
    public void getVersionReturnsCorrectVersion() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            String version = doc.getVersion();
            assertTrue(version.startsWith("1.4"), "Expected version starting with 1.4, got: " + version);
        }
    }

    @Test
    public void getPagesReturnsOnePage() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            PageCollection pages = doc.getPages();
            assertNotNull(pages);
            assertEquals(1, pages.size());

            Page page = pages.get(1);
            assertNotNull(page);
            assertEquals(1, page.getNumber());

            Rectangle mb = page.getMediaBox();
            assertNotNull(mb);
            assertEquals(612, mb.getURX(), 1e-10);
            assertEquals(792, mb.getURY(), 1e-10);
        }
    }

    @Test
    public void getInfoReturnsDocumentInfo() throws IOException {
        PdfDictionary infoDict = new PdfDictionary();
        infoDict.set("Title", new PdfString("Test Document"));
        infoDict.set("Author", new PdfString("Test Author"));

        byte[] pdfBytes = createMinimalPdf(infoDict);
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            DocumentInfo info = doc.getInfo();
            assertNotNull(info);
            assertEquals("Test Document", info.getTitle());
            assertEquals("Test Author", info.getAuthor());
        }
    }

    @Test
    public void getInfoAutoCreatesWhenAbsent() throws IOException {
        // As of Stage 12 / Bug K, getInfo() auto-creates an empty writable
        // DocumentInfo on documents whose trailer has no /Info entry, so
        // callers don't have to differentiate between "fresh doc" and
        // "loaded but no metadata".
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            DocumentInfo info = doc.getInfo();
            assertNotNull(info, "getInfo() must auto-create when trailer lacks /Info");
            assertNull(info.getTitle(), "the auto-created info is empty");
        }
    }

    @Test
    public void getCatalogReturnsValidCatalog() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            PdfDictionary catalog = doc.getCatalog();
            assertNotNull(catalog);
            assertEquals("Catalog", catalog.getType());
        }
    }

    @Test
    public void getTrailerContainsRoot() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            PdfDictionary trailer = doc.getTrailer();
            assertNotNull(trailer);
            assertNotNull(trailer.get("Root"));
        }
    }

    @Test
    public void constructorFromNullStreamThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Document((InputStream) null));
    }

    @Test
    public void constructorFromNullFilePathThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Document((String) null));
    }

    @Test
    public void closeIsIdempotent() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        Document doc = new Document(new ByteArrayInputStream(pdfBytes));
        doc.close();
        // Second close should not throw
        doc.close();
    }

    @Test
    public void iteratePagesViaForEach() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        try (Document doc = new Document(new ByteArrayInputStream(pdfBytes))) {
            int count = 0;
            for (Page page : doc.getPages()) {
                count++;
                assertNotNull(page.getMediaBox());
            }
            assertEquals(1, count);
        }
    }
}
