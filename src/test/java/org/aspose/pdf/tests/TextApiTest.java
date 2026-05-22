package org.aspose.pdf.tests;

import org.aspose.pdf.Color;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.text.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for text API value types: Position, TextState, TextSegment,
 * TextFragment, TextFragmentCollection, TextExtractionOptions, TextSearchOptions.
 */
public class TextApiTest {

    // ---- Position ----

    @Test
    public void testPosition() {
        Position pos = new Position(100.5, 200.3);
        assertEquals(100.5, pos.getXIndent());
        assertEquals(200.3, pos.getYIndent());
    }

    @Test
    public void testPositionEquals() {
        Position a = new Position(10, 20);
        Position b = new Position(10, 20);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testPositionToString() {
        Position pos = new Position(1, 2);
        assertNotNull(pos.toString());
        assertTrue(pos.toString().contains("1"));
    }

    // ---- TextState ----

    @Test
    public void testTextStateDefaults() {
        TextState ts = new TextState();
        assertEquals(0, ts.getFontSize());
        assertEquals(100, ts.getHorizontalScaling());
        assertEquals(0, ts.getCharacterSpacing());
        assertFalse(ts.isInvisible());
        assertNotNull(ts.getForegroundColor());
    }

    @Test
    public void testTextStateSetters() {
        TextState ts = new TextState();
        ts.setFontName("Helvetica");
        ts.setFontSize(12);
        ts.setCharacterSpacing(1.5);
        ts.setWordSpacing(2.0);
        ts.setHorizontalScaling(110);
        ts.setRenderingMode(0);
        assertEquals("Helvetica", ts.getFontName());
        assertEquals(12, ts.getFontSize());
        assertEquals(1.5, ts.getCharacterSpacing());
        assertEquals(2.0, ts.getWordSpacing());
        assertEquals(110, ts.getHorizontalScaling());
    }

    @Test
    public void testTextStateInvisible() {
        TextState ts = new TextState();
        ts.setRenderingMode(3);
        assertTrue(ts.isInvisible());
    }

    // ---- TextSegment ----

    @Test
    public void testTextSegment() {
        TextSegment seg = new TextSegment("Hello");
        assertEquals("Hello", seg.getText());
        assertNotNull(seg.getTextState());
        assertNull(seg.getPosition());
        assertNull(seg.getRectangle());
    }

    @Test
    public void testTextSegmentSetters() {
        TextSegment seg = new TextSegment("test");
        Position pos = new Position(10, 20);
        seg.setPosition(pos);
        assertEquals(pos, seg.getPosition());

        Rectangle rect = new Rectangle(10, 20, 100, 40);
        seg.setRectangle(rect);
        assertEquals(rect, seg.getRectangle());
    }

    // ---- TextFragment ----

    @Test
    public void testTextFragment() {
        TextFragment frag = new TextFragment("Hello World");
        assertEquals("Hello World", frag.getText());
        assertEquals(1, frag.getSegments().size());
        assertNotNull(frag.getTextState());
    }

    @Test
    public void testTextFragmentSetPosition() {
        TextFragment frag = new TextFragment("test");
        Position pos = new Position(100, 700);
        frag.setPosition(pos);
        assertEquals(pos, frag.getPosition());
    }

    @Test
    public void testTextFragmentPage() {
        TextFragment frag = new TextFragment("test");
        assertNull(frag.getPage());
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        Page page = new Page(pageDict, null);
        frag.setPage(page);
        assertSame(page, frag.getPage());
    }

    @Test
    public void testTextFragmentAddSegment() {
        TextFragment frag = new TextFragment("Hello");
        TextSegment seg2 = new TextSegment(" World");
        frag.addSegment(seg2);
        assertEquals(2, frag.getSegments().size());
    }

    // ---- TextFragmentCollection ----

    @Test
    public void testCollectionEmpty() {
        TextFragmentCollection coll = new TextFragmentCollection();
        assertEquals(0, coll.getCount());
        assertEquals(0, coll.size());
    }

    @Test
    public void testCollectionAddAndGet() {
        TextFragmentCollection coll = new TextFragmentCollection();
        coll.add(new TextFragment("First"));
        coll.add(new TextFragment("Second"));
        assertEquals(2, coll.getCount());
        assertEquals("First", coll.get(1).getText()); // 1-based
        assertEquals("Second", coll.get(2).getText());
    }

    @Test
    public void testCollectionOneBasedIndexing() {
        TextFragmentCollection coll = new TextFragmentCollection();
        coll.add(new TextFragment("only"));
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(2));
    }

    @Test
    public void testCollectionIteration() {
        TextFragmentCollection coll = new TextFragmentCollection();
        coll.add(new TextFragment("A"));
        coll.add(new TextFragment("B"));
        int count = 0;
        for (TextFragment f : coll) {
            assertNotNull(f);
            count++;
        }
        assertEquals(2, count);
    }

    // ---- TextExtractionOptions ----

    @Test
    public void testExtractionOptions() {
        TextExtractionOptions opts = new TextExtractionOptions(
                TextExtractionOptions.TextFormattingMode.Raw);
        assertEquals(TextExtractionOptions.TextFormattingMode.Raw, opts.getFormattingMode());
        opts.setFormattingMode(TextExtractionOptions.TextFormattingMode.Pure);
        assertEquals(TextExtractionOptions.TextFormattingMode.Pure, opts.getFormattingMode());
    }

    // ---- TextSearchOptions ----

    @Test
    public void testSearchOptions() {
        TextSearchOptions opts = new TextSearchOptions(false);
        assertFalse(opts.isRegularExpressionUsed());
        assertNull(opts.getRectangle());
    }

    @Test
    public void testSearchOptionsRegex() {
        TextSearchOptions opts = new TextSearchOptions(true);
        assertTrue(opts.isRegularExpressionUsed());
    }

    @Test
    public void testSearchOptionsRectangle() {
        TextSearchOptions opts = new TextSearchOptions(false);
        Rectangle rect = new Rectangle(0, 0, 100, 100);
        opts.setRectangle(rect);
        assertEquals(rect, opts.getRectangle());
    }
}
