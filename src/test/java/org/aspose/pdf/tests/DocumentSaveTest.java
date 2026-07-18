package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
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
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for Document.save() methods.
public class DocumentSaveTest {

    /// Creates a minimal valid PDF in memory and returns its bytes.
    private byte[] createMinimalPdf() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Build objects: catalog (1 0), pages (2 0), page (3 0)
        PdfDictionary catalog = new PdfDictionary();
        catalog.set(PdfName.TYPE, PdfName.CATALOG);
        catalog.set(PdfName.PAGES, new PdfObjectReference(2, 0));

        PdfDictionary pages = new PdfDictionary();
        pages.set(PdfName.TYPE, PdfName.PAGES);
        PdfArray kids = new PdfArray();
        kids.add(new PdfObjectReference(3, 0));
        pages.set(PdfName.KIDS, kids);
        pages.set(PdfName.COUNT, PdfInteger.valueOf(1));

        PdfDictionary page = new PdfDictionary();
        page.set(PdfName.TYPE, PdfName.PAGE);
        page.set(PdfName.PARENT, new PdfObjectReference(2, 0));
        PdfArray mediaBox = new PdfArray(4);
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(new PdfFloat(595.0));
        mediaBox.add(new PdfFloat(842.0));
        page.set(PdfName.MEDIABOX, mediaBox);

        PdfDictionary trailer = new PdfDictionary();
        trailer.set(PdfName.ROOT, new PdfObjectReference(1, 0));
        trailer.set(PdfName.SIZE, PdfInteger.valueOf(4));

        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        objects.put(new PdfObjectKey(1, 0), catalog);
        objects.put(new PdfObjectKey(2, 0), pages);
        objects.put(new PdfObjectKey(3, 0), page);

        PDFWriter writer = new PDFWriter(baos, 1.4f);
        writer.write(trailer, objects);

        return baos.toByteArray();
    }

    @Test
    public void saveToOutputStream() throws IOException {
        byte[] pdfBytes = createMinimalPdf();

        // Parse the PDF
        Document doc = new Document(new ByteArrayInputStream(pdfBytes));

        // Save to a new byte array
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        doc.save(output);
        doc.close();

        byte[] savedBytes = output.toByteArray();
        assertTrue(savedBytes.length > 0, "Saved PDF should not be empty");

        // Re-parse and verify structure
        Document doc2 = new Document(new ByteArrayInputStream(savedBytes));
        assertNotNull(doc2.getTrailer());
        assertNotNull(doc2.getCatalog());
        PdfBase root = doc2.getCatalog().get(PdfName.TYPE);
        assertNotNull(root);
        doc2.close();
    }

    @Test
    public void saveAndReparsePreservesStructure() throws IOException {
        byte[] pdfBytes = createMinimalPdf();

        Document doc = new Document(new ByteArrayInputStream(pdfBytes));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        doc.save(output);
        doc.close();

        // Re-parse
        Document doc2 = new Document(new ByteArrayInputStream(output.toByteArray()));
        PdfDictionary catalog = doc2.getCatalog();
        assertNotNull(catalog);
        assertEquals("Catalog", catalog.getNameAsString("Type"));
        doc2.close();
    }

    @Test
    public void saveNullOutputStreamThrows() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        Document doc = new Document(new ByteArrayInputStream(pdfBytes));
        assertThrows(IllegalArgumentException.class, () -> doc.save((java.io.OutputStream) null));
        doc.close();
    }

    @Test
    public void saveNullFilePathThrows() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        Document doc = new Document(new ByteArrayInputStream(pdfBytes));
        assertThrows(IllegalArgumentException.class, () -> doc.save((String) null));
        doc.close();
    }

    @Test
    public void savedPdfStartsWithHeader() throws IOException {
        byte[] pdfBytes = createMinimalPdf();
        Document doc = new Document(new ByteArrayInputStream(pdfBytes));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        doc.save(output);
        doc.close();

        String header = new String(output.toByteArray(), 0, 9, "US-ASCII");
        assertTrue(header.startsWith("%PDF-"), "Saved PDF should start with %PDF- header");
    }
}
