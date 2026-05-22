package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;
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

/**
 * Tests for Document.save() methods.
 */
public class DocumentSaveTest {

    /**
     * Creates a minimal valid PDF in memory and returns its bytes.
     */
    private byte[] createMinimalPdf() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Build objects: catalog (1 0), pages (2 0), page (3 0)
        COSDictionary catalog = new COSDictionary();
        catalog.set(COSName.TYPE, COSName.CATALOG);
        catalog.set(COSName.PAGES, new COSObjectReference(2, 0));

        COSDictionary pages = new COSDictionary();
        pages.set(COSName.TYPE, COSName.PAGES);
        COSArray kids = new COSArray();
        kids.add(new COSObjectReference(3, 0));
        pages.set(COSName.KIDS, kids);
        pages.set(COSName.COUNT, COSInteger.valueOf(1));

        COSDictionary page = new COSDictionary();
        page.set(COSName.TYPE, COSName.PAGE);
        page.set(COSName.PARENT, new COSObjectReference(2, 0));
        COSArray mediaBox = new COSArray(4);
        mediaBox.add(COSInteger.valueOf(0));
        mediaBox.add(COSInteger.valueOf(0));
        mediaBox.add(new COSFloat(595.0));
        mediaBox.add(new COSFloat(842.0));
        page.set(COSName.MEDIABOX, mediaBox);

        COSDictionary trailer = new COSDictionary();
        trailer.set(COSName.ROOT, new COSObjectReference(1, 0));
        trailer.set(COSName.SIZE, COSInteger.valueOf(4));

        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        objects.put(new COSObjectKey(1, 0), catalog);
        objects.put(new COSObjectKey(2, 0), pages);
        objects.put(new COSObjectKey(3, 0), page);

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
        COSBase root = doc2.getCatalog().get(COSName.TYPE);
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
        COSDictionary catalog = doc2.getCatalog();
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
