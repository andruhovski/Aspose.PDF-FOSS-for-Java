package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.*;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.aspose.pdf.engine.text.TextExtractor;
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextFragment;
import org.aspose.pdf.text.TextFragmentAbsorber;
import org.aspose.pdf.text.TextFragmentCollection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TextExtractor}, {@link TextAbsorber}, and {@link TextFragmentAbsorber}.
 */
public class TextExtractorTest {

    /**
     * Helper: creates a minimal Page with a content stream and Helvetica font resource.
     */
    private Page createPageWithText(String contentStreamText) throws IOException {
        // Build font dictionary for F1 = Helvetica
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.TYPE, COSName.of("Font"));
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        COSDictionary fontsDict = new COSDictionary();
        fontsDict.set(COSName.of("F1"), fontDict);

        COSDictionary resourcesDict = new COSDictionary();
        resourcesDict.set(COSName.of("Font"), fontsDict);

        // Build content stream
        byte[] streamBytes = contentStreamText.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        COSStream contentStream = new COSStream(streamBytes);

        // Build page dictionary
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        pageDict.set(COSName.RESOURCES, resourcesDict);
        pageDict.set(COSName.CONTENTS, contentStream);

        return new Page(pageDict, null);
    }

    @Test
    public void testExtractSimpleText() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello World) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        String text = extractor.extractText(page);
        assertTrue(text.contains("Hello World"), "Expected 'Hello World', got: " + text);
    }

    @Test
    public void testExtractMultipleStrings() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello) Tj ( World) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        String text = extractor.extractText(page);
        assertTrue(text.contains("Hello"), "Should contain 'Hello'");
        assertTrue(text.contains("World"), "Should contain 'World'");
    }

    @Test
    public void testExtractTJArray() throws IOException {
        // TJ array: [(H) 20 (ello)] TJ
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td [(H) 20 (ello)] TJ ET");
        TextExtractor extractor = new TextExtractor(null);
        String text = extractor.extractText(page);
        assertTrue(text.contains("H"), "Should contain 'H'");
        assertTrue(text.contains("ello"), "Should contain 'ello'");
    }

    @Test
    public void testTextAbsorber() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello) Tj ET");
        TextAbsorber absorber = new TextAbsorber();
        page.accept(absorber);
        String text = absorber.getText();
        assertTrue(text.contains("Hello"));
    }

    @Test
    public void testTextFragmentAbsorberNoFilter() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello World) Tj ET");
        TextFragmentAbsorber absorber = new TextFragmentAbsorber();
        page.accept(absorber);
        TextFragmentCollection fragments = absorber.getTextFragments();
        assertTrue(fragments.getCount() > 0, "Should have at least one fragment");
    }

    @Test
    public void testTextFragmentAbsorberSearch() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello World) Tj ET");
        TextFragmentAbsorber absorber = new TextFragmentAbsorber("Hello");
        page.accept(absorber);
        TextFragmentCollection results = absorber.getTextFragments();
        assertTrue(results.getCount() > 0, "Should find 'Hello'");
        assertEquals("Hello", results.get(1).getText());
    }

    @Test
    public void testTextFragmentAbsorberRegex() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello World 123) Tj ET");
        TextFragmentAbsorber absorber = new TextFragmentAbsorber(
                "\\d+",
                new org.aspose.pdf.text.TextSearchOptions(true));
        page.accept(absorber);
        TextFragmentCollection results = absorber.getTextFragments();
        assertTrue(results.getCount() > 0, "Should find digits");
        assertEquals("123", results.get(1).getText());
    }

    @Test
    public void testTextFragmentPosition() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Test) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        List<TextFragment> fragments = extractor.extract(page);
        assertFalse(fragments.isEmpty());
        TextFragment frag = fragments.get(0);
        assertNotNull(frag.getPosition());
        assertEquals(100.0, frag.getPosition().getXIndent(), 1.0);
        assertEquals(700.0, frag.getPosition().getYIndent(), 1.0);
    }

    @Test
    public void testTextFragmentRectangle() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Test) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        List<TextFragment> fragments = extractor.extract(page);
        assertFalse(fragments.isEmpty());
        Rectangle rect = fragments.get(0).getRectangle();
        assertNotNull(rect, "Rectangle should be computed");
        assertTrue(rect.getWidth() > 0, "Rectangle should have positive width");
        assertTrue(rect.getHeight() > 0, "Rectangle should have positive height");
    }

    @Test
    public void testEmptyPage() throws IOException {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        Page page = new Page(pageDict, null);

        TextAbsorber absorber = new TextAbsorber();
        page.accept(absorber);
        assertEquals("", absorber.getText());
    }

    @Test
    public void testMultiLineText() throws IOException {
        String content = "BT /F1 12 Tf 100 700 Td (Line 1) Tj 0 -14 Td (Line 2) Tj ET";
        Page page = createPageWithText(content);
        TextExtractor extractor = new TextExtractor(null);
        String text = extractor.extractText(page);
        assertTrue(text.contains("Line 1"));
        assertTrue(text.contains("Line 2"));
    }

    @Test
    public void testTextStateOnFragment() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Test) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        List<TextFragment> fragments = extractor.extract(page);
        assertFalse(fragments.isEmpty());
        org.aspose.pdf.text.TextState state = fragments.get(0).getTextState();
        assertNotNull(state);
        assertEquals(12.0, state.getFontSize(), 0.1);
        // The extractor now resolves the resource alias /F1 to its /BaseFont
        // entry ("Helvetica") so callers see the real font family name rather
        // than the per-page alias. (See PdfFileSignatureTests session for the
        // same family-name vs resource-name distinction.)
        assertEquals("Helvetica", state.getFontName());
    }

    @Test
    public void testPageAcceptCallsVisit() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (OK) Tj ET");

        // Test that accept(absorber) works via polymorphism
        TextFragmentAbsorber fragAbsorber = new TextFragmentAbsorber("OK");
        page.accept(fragAbsorber);
        assertTrue(fragAbsorber.getTextFragments().getCount() > 0);
    }

    @Test
    public void testAcceptNullThrows() {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        Page page = new Page(pageDict, null);
        assertThrows(IllegalArgumentException.class, () -> page.accept((org.aspose.pdf.text.TextAbsorber) null));
    }
}
