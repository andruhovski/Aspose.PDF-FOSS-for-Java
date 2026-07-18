package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.html.*;
import org.aspose.pdf.text.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/// Tests for HTML → PDF conversion.
public class HtmlToPdfTest {

    private Document convertHtml(String html) throws IOException {
        HtmlToPdfConverter converter = new HtmlToPdfConverter();
        return converter.convert(html, null);
    }

    @Test
    public void testParagraphText() throws IOException {
        Document doc = convertHtml("<html><body><p>Hello World</p></body></html>");
        Page page = doc.getPages().get(1);
        assertNotNull(page.getParagraphs());
        assertTrue(page.getParagraphs().size() > 0);
    }

    @Test
    public void testBoldText() throws IOException {
        Document doc = convertHtml("<html><body><b>Bold text</b></body></html>");
        Page page = doc.getPages().get(1);
        assertTrue(page.getParagraphs().size() > 0);
        BaseParagraph bp = page.getParagraphs().get(0);
        assertTrue(bp instanceof TextFragment);
        TextFragment tf = (TextFragment) bp;
        assertTrue(tf.getTextState().getFontName().contains("Bold"),
            "Font should be bold, got: " + tf.getTextState().getFontName());
    }

    @Test
    public void testItalicText() throws IOException {
        Document doc = convertHtml("<html><body><em>Italic text</em></body></html>");
        Page page = doc.getPages().get(1);
        assertTrue(page.getParagraphs().size() > 0);
        BaseParagraph bp = page.getParagraphs().get(0);
        assertTrue(bp instanceof TextFragment);
        TextFragment tf = (TextFragment) bp;
        String fn = tf.getTextState().getFontName();
        assertTrue(fn.contains("Italic") || fn.contains("Oblique"),
            "Font should be italic, got: " + fn);
    }

    @Test
    public void testHeadingFontSizes() throws IOException {
        Document doc = convertHtml("<html><body><h1>H1</h1><h2>H2</h2><h3>H3</h3></body></html>");
        Page page = doc.getPages().get(1);
        assertTrue(page.getParagraphs().size() >= 3);

        TextFragment h1 = (TextFragment) page.getParagraphs().get(0);
        TextFragment h2 = (TextFragment) page.getParagraphs().get(1);
        TextFragment h3 = (TextFragment) page.getParagraphs().get(2);
        assertTrue(h1.getTextState().getFontSize() > h2.getTextState().getFontSize(),
            "H1 should be larger than H2");
        assertTrue(h2.getTextState().getFontSize() > h3.getTextState().getFontSize(),
            "H2 should be larger than H3");
    }

    @Test
    public void testTableCreation() throws IOException {
        Document doc = convertHtml(
            "<html><body><table border=\"1\"><tr><td>A</td><td>B</td></tr></table></body></html>");
        Page page = doc.getPages().get(1);
        boolean hasTable = false;
        for (int i = 0; i < page.getParagraphs().size(); i++) {
            if (page.getParagraphs().get(i) instanceof Table) {
                hasTable = true;
                break;
            }
        }
        assertTrue(hasTable, "Should contain a Table paragraph");
    }

    @Test
    public void testTableHeaderBold() throws IOException {
        Document doc = convertHtml(
            "<html><body><table><tr><th>Header</th></tr><tr><td>Data</td></tr></table></body></html>");
        Page page = doc.getPages().get(1);
        for (int i = 0; i < page.getParagraphs().size(); i++) {
            if (page.getParagraphs().get(i) instanceof Table) {
                Table table = (Table) page.getParagraphs().get(i);
                Cell headerCell = table.getRows().get(0).getCells().get(0);
                TextFragment tf = (TextFragment) headerCell.getParagraphs().get(0);
                assertTrue(tf.getTextState().getFontName().contains("Bold"),
                    "TH should produce bold text");
                return;
            }
        }
        fail("No table found");
    }

    @Test
    public void testColspanRowspan() throws IOException {
        Document doc = convertHtml(
            "<html><body><table><tr><td colspan=\"2\">Wide</td></tr>" +
            "<tr><td rowspan=\"2\">Tall</td><td>B</td></tr></table></body></html>");
        Page page = doc.getPages().get(1);
        for (int i = 0; i < page.getParagraphs().size(); i++) {
            if (page.getParagraphs().get(i) instanceof Table) {
                Table table = (Table) page.getParagraphs().get(i);
                assertEquals(2, table.getRows().get(0).getCells().get(0).getColSpan());
                assertEquals(2, table.getRows().get(1).getCells().get(0).getRowSpan());
                return;
            }
        }
        fail("No table found");
    }

    @Test
    public void testUnorderedList() throws IOException {
        Document doc = convertHtml(
            "<html><body><ul><li>Item 1</li><li>Item 2</li></ul></body></html>");
        Page page = doc.getPages().get(1);
        assertTrue(page.getParagraphs().size() >= 2);
        TextFragment tf = (TextFragment) page.getParagraphs().get(0);
        assertTrue(tf.getText().contains("\u2022"), "Should contain bullet marker");
        assertTrue(tf.getText().contains("Item 1"), "Should contain item text");
    }

    @Test
    public void testOrderedList() throws IOException {
        Document doc = convertHtml(
            "<html><body><ol><li>First</li><li>Second</li></ol></body></html>");
        Page page = doc.getPages().get(1);
        assertTrue(page.getParagraphs().size() >= 2);
        TextFragment tf = (TextFragment) page.getParagraphs().get(0);
        assertTrue(tf.getText().startsWith("1."), "Should start with '1.'");
    }

    @Test
    public void testInlineCssFontSize() throws IOException {
        Document doc = convertHtml(
            "<html><body><p style=\"font-size:20px\">Large</p></body></html>");
        Page page = doc.getPages().get(1);
        TextFragment tf = (TextFragment) page.getParagraphs().get(0);
        assertEquals(20.0, tf.getTextState().getFontSize(), 0.01);
    }

    @Test
    public void testInlineCssColor() throws IOException {
        Document doc = convertHtml(
            "<html><body><p style=\"color:#ff0000\">Red</p></body></html>");
        Page page = doc.getPages().get(1);
        TextFragment tf = (TextFragment) page.getParagraphs().get(0);
        Color fg = tf.getTextState().getForegroundColor();
        assertNotNull(fg);
        assertEquals(1.0, fg.getR(), 0.01, "Red component should be 1.0");
        assertEquals(0.0, fg.getG(), 0.01, "Green component should be 0.0");
    }

    @Test
    public void testMalformedHtml_noCrash() throws IOException {
        // Unclosed tags, missing html/body
        Document doc = convertHtml("<p>Unclosed paragraph<p>Another");
        assertNotNull(doc);
        assertTrue(doc.getPages().getCount() > 0);
    }

    @Test
    public void testHtmlFromInputStream() throws IOException {
        String html = "<html><body><p>Stream test</p></body></html>";
        InputStream is = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        HtmlToPdfConverter converter = new HtmlToPdfConverter();
        Document doc = converter.convert(is, null);
        assertNotNull(doc);
        assertTrue(doc.getPages().getCount() > 0);
    }

    @Test
    public void testHtmlEntities() throws IOException {
        Document doc = convertHtml("<html><body><p>A&#160;B &amp; C</p></body></html>");
        Page page = doc.getPages().get(1);
        TextFragment tf = (TextFragment) page.getParagraphs().get(0);
        assertTrue(tf.getText().contains("&"), "Should decode &amp; to &");
    }

    @Test
    public void testDocumentHtmlConstructor() throws IOException {
        String html = "<html><body><p>Constructor test</p></body></html>";
        InputStream is = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = new Document(is, new HtmlLoadOptions());
        assertNotNull(doc);
        assertTrue(doc.getPages().getCount() > 0);
    }
}
