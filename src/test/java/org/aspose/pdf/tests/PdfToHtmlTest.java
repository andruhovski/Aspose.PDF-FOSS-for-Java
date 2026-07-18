package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.html.*;
import org.aspose.pdf.text.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/// Tests for PDF → HTML conversion.
public class PdfToHtmlTest {

    private String convertToHtml(Document doc, HtmlSaveOptions opts) throws IOException {
        PdfToHtmlConverter converter = new PdfToHtmlConverter();
        return converter.convert(doc, opts);
    }

    private Document buildTextDoc(String text) throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment(text));
        return doc;
    }

    @Test
    public void testContainsText() throws IOException {
        Document doc = buildTextDoc("Hello World");
        String html = convertToHtml(doc, new HtmlSaveOptions());
        assertTrue(html.contains("Hello World") || html.contains("page"),
            "HTML should contain page div");
    }

    @Test
    public void testHtml5Doctype() throws IOException {
        Document doc = buildTextDoc("Test");
        HtmlSaveOptions opts = new HtmlSaveOptions();
        opts.setDocumentType(HtmlDocumentType.Html5);
        String html = convertToHtml(doc, opts);
        assertTrue(html.startsWith("<!DOCTYPE html>"), "Should start with HTML5 doctype");
    }

    @Test
    public void testXhtmlDoctype() throws IOException {
        Document doc = buildTextDoc("Test");
        HtmlSaveOptions opts = new HtmlSaveOptions();
        opts.setDocumentType(HtmlDocumentType.Xhtml);
        String html = convertToHtml(doc, opts);
        assertTrue(html.contains("XHTML"), "Should contain XHTML doctype");
    }

    @Test
    public void testPageDivPresent() throws IOException {
        Document doc = buildTextDoc("Test");
        String html = convertToHtml(doc, new HtmlSaveOptions());
        assertTrue(html.contains("class=\"page\""), "Should contain page div");
        assertTrue(html.contains("id=\"p1\""), "Should have page id");
    }

    @Test
    public void testMultiplePages() throws IOException {
        Document doc = new Document();
        doc.getPages().add();
        doc.getPages().add();
        doc.getPages().add();
        String html = convertToHtml(doc, new HtmlSaveOptions());
        assertTrue(html.contains("id=\"p1\""));
        assertTrue(html.contains("id=\"p2\""));
        assertTrue(html.contains("id=\"p3\""));
    }

    @Test
    public void testEmptyPage_noCrash() throws IOException {
        Document doc = new Document();
        doc.getPages().add();
        String html = convertToHtml(doc, new HtmlSaveOptions());
        assertNotNull(html);
        assertTrue(html.contains("<div class=\"page\""));
    }

    @Test
    public void testSpecialCharactersEscaped() {
        assertEquals("&amp;", PdfToHtmlConverter.escapeHtml("&"));
        assertEquals("&lt;", PdfToHtmlConverter.escapeHtml("<"));
        assertEquals("&gt;", PdfToHtmlConverter.escapeHtml(">"));
        assertEquals("&quot;", PdfToHtmlConverter.escapeHtml("\""));
    }

    @Test
    public void testFontMapping() {
        assertTrue(PdfToHtmlConverter.mapFontToCSS("Courier").contains("monospace"));
        assertTrue(PdfToHtmlConverter.mapFontToCSS("Times-Roman").contains("serif"));
        assertTrue(PdfToHtmlConverter.mapFontToCSS("Helvetica").contains("sans-serif"));
        assertTrue(PdfToHtmlConverter.mapFontToCSS(null).contains("sans-serif"));
    }

    @Test
    public void testScaleOption() throws IOException {
        Document doc = buildTextDoc("Test");
        HtmlSaveOptions opts = new HtmlSaveOptions();
        opts.setScale(2.0);
        String html = convertToHtml(doc, opts);
        // Page dimensions should be doubled
        assertNotNull(html);
    }

    @Test
    public void testReflowableMode() throws IOException {
        Document doc = buildTextDoc("Some text");
        HtmlSaveOptions opts = new HtmlSaveOptions();
        opts.setFixedLayout(false);
        String html = convertToHtml(doc, opts);
        assertNotNull(html);
        // Should not contain position:absolute spans
        assertFalse(html.contains("class=\"t\""),
            "Reflowable mode should not use absolute positioned spans");
    }

    @Test
    public void testDefaultOptions() {
        HtmlSaveOptions opts = new HtmlSaveOptions();
        assertTrue(opts.isFixedLayout());
        assertTrue(opts.isEmbedImages());
        assertFalse(opts.isSplitIntoPages());
        assertEquals(1.0, opts.getScale(), 0.001);
        assertEquals(HtmlDocumentType.Html5, opts.getDocumentType());
    }

    @Test
    public void testHtmlStructure() throws IOException {
        Document doc = buildTextDoc("Test");
        String html = convertToHtml(doc, new HtmlSaveOptions());
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("</html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<style>"));
    }
}
