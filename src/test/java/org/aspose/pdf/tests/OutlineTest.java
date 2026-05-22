package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OutlineCollection and OutlineItemCollection.
 */
public class OutlineTest {

    @Test
    public void testCreateEmptyOutlines() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        assertEquals(0, outlines.getCount());
    }

    @Test
    public void testAddItems() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item1 = new OutlineItemCollection(outlines);
        item1.setTitle("Chapter 1");
        OutlineItemCollection item2 = new OutlineItemCollection(outlines);
        item2.setTitle("Chapter 2");
        OutlineItemCollection item3 = new OutlineItemCollection(outlines);
        item3.setTitle("Chapter 3");

        outlines.add(item1);
        outlines.add(item2);
        outlines.add(item3);

        assertEquals(3, outlines.getCount());
    }

    @Test
    public void testGetByIndex() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        item.setTitle("First");
        outlines.add(item);

        assertEquals("First", outlines.get(1).getTitle()); // 1-based
    }

    @Test
    public void testOneBasedIndexing() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        outlines.add(new OutlineItemCollection(outlines));

        assertThrows(IndexOutOfBoundsException.class, () -> outlines.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> outlines.get(2));
    }

    @Test
    public void testTitle() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        item.setTitle("My Bookmark");
        assertEquals("My Bookmark", item.getTitle());
    }

    @Test
    public void testBold() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        assertFalse(item.getBold());
        item.setBold(true);
        assertTrue(item.getBold());
        item.setBold(false);
        assertFalse(item.getBold());
    }

    @Test
    public void testItalic() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        assertFalse(item.getItalic());
        item.setItalic(true);
        assertTrue(item.getItalic());
    }

    @Test
    public void testBoldAndItalic() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        item.setBold(true);
        item.setItalic(true);
        assertTrue(item.getBold());
        assertTrue(item.getItalic());
    }

    @Test
    public void testColor() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        // Default should be black
        assertNotNull(item.getColor());

        item.setColor(Color.RED);
        Color c = item.getColor();
        assertEquals(1.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
        assertEquals(0.0, c.getB(), 0.01);
    }

    @Test
    public void testNestedBookmarks() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection parent = new OutlineItemCollection(outlines);
        parent.setTitle("Parent");

        OutlineItemCollection child1 = new OutlineItemCollection(outlines);
        child1.setTitle("Child 1");
        OutlineItemCollection child2 = new OutlineItemCollection(outlines);
        child2.setTitle("Child 2");

        parent.add(child1);
        parent.add(child2);
        outlines.add(parent);

        assertEquals(1, outlines.getCount());
        assertEquals(2, outlines.get(1).getCount());
        assertEquals("Child 1", outlines.get(1).get(1).getTitle());
        assertEquals("Child 2", outlines.get(1).get(2).getTitle());
    }

    @Test
    public void testVisibleCount() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item1 = new OutlineItemCollection(outlines);
        item1.setTitle("Item 1");
        OutlineItemCollection item2 = new OutlineItemCollection(outlines);
        item2.setTitle("Item 2");
        outlines.add(item1);
        outlines.add(item2);

        // Top-level items are always visible
        assertEquals(2, outlines.getVisibleCount());
    }

    @Test
    public void testVisibleCountWithOpen() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection parent = new OutlineItemCollection(outlines);
        parent.setTitle("Parent");
        OutlineItemCollection child = new OutlineItemCollection(outlines);
        child.setTitle("Child");
        parent.add(child);
        parent.setOpen(true);
        outlines.add(parent);

        // 1 top-level + 1 visible child
        assertEquals(2, outlines.getVisibleCount());
    }

    @Test
    public void testIteration() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        for (int i = 0; i < 5; i++) {
            OutlineItemCollection item = new OutlineItemCollection(outlines);
            item.setTitle("Item " + i);
            outlines.add(item);
        }

        int count = 0;
        for (OutlineItemCollection item : outlines) {
            assertNotNull(item.getTitle());
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    public void testDelete() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item1 = new OutlineItemCollection(outlines);
        item1.setTitle("A");
        OutlineItemCollection item2 = new OutlineItemCollection(outlines);
        item2.setTitle("B");
        outlines.add(item1);
        outlines.add(item2);

        outlines.delete(1);
        assertEquals(1, outlines.getCount());
        assertEquals("B", outlines.get(1).getTitle());
    }

    @Test
    public void testClear() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        outlines.add(new OutlineItemCollection(outlines));
        outlines.add(new OutlineItemCollection(outlines));
        outlines.clear();
        assertEquals(0, outlines.getCount());
    }

    @Test
    public void testSetDestination() throws IOException {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        item.setTitle("Go to page");

        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        Page page = new Page(pageDict, null);
        XYZExplicitDestination dest = new XYZExplicitDestination(page, 0, 792, 1.0);
        item.setDestination(dest);

        ExplicitDestination readBack = item.getDestination();
        assertNotNull(readBack);
        assertTrue(readBack instanceof XYZExplicitDestination);
    }

    @Test
    public void testSetAction() throws IOException {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);
        item.setTitle("Open URL");

        UriAction action = new UriAction("https://example.com");
        item.setAction(action);

        PdfAction readBack = item.getAction();
        assertNotNull(readBack);
        assertTrue(readBack instanceof UriAction);
        assertEquals("https://example.com", ((UriAction) readBack).getUri());
    }

    @Test
    public void testSetActionRemovesDest() throws IOException {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection item = new OutlineItemCollection(outlines);

        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        item.setDestination(new FitExplicitDestination(new Page(pageDict, null)));

        // Setting action should remove destination
        item.setAction(new UriAction("https://example.com"));
        assertNull(item.getDestination());
    }

    @Test
    public void testParseLinkedList() {
        // Simulate a /First → /Next linked list
        COSDictionary item1 = new COSDictionary();
        item1.set(COSName.of("Title"), new COSString("First".getBytes()));
        COSDictionary item2 = new COSDictionary();
        item2.set(COSName.of("Title"), new COSString("Second".getBytes()));
        COSDictionary item3 = new COSDictionary();
        item3.set(COSName.of("Title"), new COSString("Third".getBytes()));

        item1.set(COSName.of("Next"), item2);
        item2.set(COSName.of("Next"), item3);

        COSDictionary outlinesDict = new COSDictionary();
        outlinesDict.set(COSName.of("First"), item1);

        OutlineCollection outlines = new OutlineCollection(outlinesDict, null, null);
        assertEquals(3, outlines.getCount());
        assertEquals("First", outlines.get(1).getTitle());
        assertEquals("Second", outlines.get(2).getTitle());
        assertEquals("Third", outlines.get(3).getTitle());
    }

    @Test
    public void testChildIteration() {
        OutlineCollection outlines = new OutlineCollection(null, null);
        OutlineItemCollection parent = new OutlineItemCollection(outlines);
        parent.setTitle("Parent");

        for (int i = 0; i < 3; i++) {
            OutlineItemCollection child = new OutlineItemCollection(outlines);
            child.setTitle("Child " + i);
            parent.add(child);
        }

        int count = 0;
        for (OutlineItemCollection child : parent) {
            assertNotNull(child);
            count++;
        }
        assertEquals(3, count);
    }
}
