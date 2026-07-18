package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.pdfobjects.*;
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

/// Tests for [TextExtractor], [TextAbsorber], and [TextFragmentAbsorber].
public class TextExtractorTest {

    /// Helper: creates a minimal Page with a content stream and Helvetica font resource.
    private Page createPageWithText(String contentStreamText) throws IOException {
        // Build font dictionary for F1 = Helvetica
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.TYPE, PdfName.of("Font"));
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        PdfDictionary fontsDict = new PdfDictionary();
        fontsDict.set(PdfName.of("F1"), fontDict);

        PdfDictionary resourcesDict = new PdfDictionary();
        resourcesDict.set(PdfName.of("Font"), fontsDict);

        // Build content stream
        byte[] streamBytes = contentStreamText.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        PdfStream contentStream = new PdfStream(streamBytes);

        // Build page dictionary
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        pageDict.set(PdfName.RESOURCES, resourcesDict);
        pageDict.set(PdfName.CONTENTS, contentStream);

        return new Page(pageDict, null);
    }

    @Test
    public void testExtractSimpleText() throws IOException {
        Page page = createPageWithText("BT /F1 12 Tf 100 700 Td (Hello World) Tj ET");
        TextExtractor extractor = new TextExtractor(null);
        String text = extractor.extractText(page);
        assertTrue(text.contains("Hello World"), "Expected 'Hello World', got: " + text);
    }

    /// Decoration detection: a thin filled rule just below text → underline; a thin
    /// rule through the middle → strikeout; a large (non-thin) filled rect is NOT a
    /// rule and must not flag text; a thin rule more than one line-height below the
    /// text is out of band and must not flag it. This locks in the thin-rule filter
    /// and the bounded Y-band search added to keep `detectTextDecorations` off
    /// the catastrophic O(fragments × filledRects) path (corpus 57236.pdf: 18,363
    /// fragments × 5,027,972 filled rects ≈ 9×10^10 iterations, a 900s+ hang).
    @Test
    public void testUnderlineAndStrikeoutDetectionIgnoresNonRuleRects() throws IOException {
        // Helvetica 12pt "Hello" at (100,700) extracts to rect ≈ [100, 697.6, 127.3, 710.7].
        Page page = createPageWithText(
                "BT /F1 12 Tf 100 700 Td (Hello) Tj ET\n"
                + "100 698 40 1 re f\n"      // thin underline rule just below text
                + "90 695 60 30 re f");       // large non-thin block over text → not a rule
        TextExtractor extractor = new TextExtractor(null);
        List<TextFragment> frags = extractor.extract(page);
        assertEquals(1, frags.size());
        TextFragment f = frags.get(0);
        assertTrue(f.getTextState().isUnderline(), "thin rule below text should mark underline");
        assertFalse(f.getTextState().isStrikeOut(),
                "large non-thin block must not be treated as a strikeout rule");

        // Strikeout: a thin rule through the vertical middle of the box.
        Page page2 = createPageWithText(
                "BT /F1 12 Tf 100 700 Td (Hello) Tj ET\n100 703 40 1 re f");
        TextFragment f2 = new TextExtractor(null).extract(page2).get(0);
        assertTrue(f2.getTextState().isStrikeOut(), "thin rule through middle should mark strikeout");

        // Out of band: a thin rule far (>1 line-height) below the text is not its
        // decoration and must not flag it.
        Page page3 = createPageWithText(
                "BT /F1 12 Tf 100 700 Td (Hello) Tj ET\n100 680 40 1 re f");
        TextFragment f3 = new TextExtractor(null).extract(page3).get(0);
        assertFalse(f3.getTextState().isUnderline(), "rule far below text must not mark underline");
        assertFalse(f3.getTextState().isStrikeOut(), "rule far below text must not mark strikeout");
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
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
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
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        Page page = new Page(pageDict, null);
        assertThrows(IllegalArgumentException.class, () -> page.accept((org.aspose.pdf.text.TextAbsorber) null));
    }
}
