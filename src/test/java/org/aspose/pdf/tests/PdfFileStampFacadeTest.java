package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.facades.FormattedText;
import org.aspose.pdf.facades.PdfFileStamp;
import org.aspose.pdf.facades.Stamp;
import org.aspose.pdf.text.TextFragment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfFileStampFacadeTest {

    @Test
    public void stampBindImageTracksImageSource() {
        Stamp stamp = new Stamp();

        stamp.bindImage("sample-image.png");
        stamp.setStampId(1234);

        assertEquals("sample-image.png", stamp.getImageFile());
        assertEquals(1234, stamp.getStampId());
    }

    @Test
    public void stampBindPdfTracksDocumentSource() throws Exception {
        Document source = new Document();
        source.getPages().add();
        Stamp stamp = new Stamp();

        stamp.bindPdf(source, 1);

        assertSame(source, stamp.getPdfDocument());
        assertEquals(1, stamp.getPdfPageNumber());
    }

    @Test
    public void pdfFileStampAcceptsHeaderFooterAndPdfPageStamp() throws Exception {
        Document target = new Document();
        Page targetPage = target.getPages().add();
        targetPage.getParagraphs().add(new TextFragment("target"));

        Document source = new Document();
        Page sourcePage = source.getPages().add();
        sourcePage.getParagraphs().add(new TextFragment("source"));

        PdfFileStamp fileStamp = new PdfFileStamp();
        assertTrue(fileStamp.bindPdf(target));

        fileStamp.addHeader(new FormattedText("header"), 10);
        fileStamp.addFooter(new FormattedText("footer"), 10);

        Stamp pdfStamp = new Stamp();
        pdfStamp.bindPdf(source, 1);
        pdfStamp.setStampId(77);
        fileStamp.addStamp(pdfStamp);

        assertEquals(3, fileStamp.getStampCount());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(fileStamp.save(out));
        assertTrue(out.size() > 0);

        Document reopened = new Document(new java.io.ByteArrayInputStream(out.toByteArray()));
        assertNotNull(reopened.getPages().get(1));

        reopened.close();
        fileStamp.close();
        source.close();
    }
}
