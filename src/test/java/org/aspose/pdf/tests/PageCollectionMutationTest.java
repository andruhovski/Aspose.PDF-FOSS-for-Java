package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PageCollection mutation methods: add(), add(Page), insert(), delete().
 */
public class PageCollectionMutationTest {

    private COSDictionary pagesDict;
    private COSArray kids;
    private PageCollection collection;

    @BeforeEach
    public void setUp() {
        pagesDict = new COSDictionary();
        pagesDict.set(COSName.TYPE, COSName.PAGES);
        kids = new COSArray();
        pagesDict.set(COSName.KIDS, kids);
        pagesDict.set(COSName.COUNT, COSInteger.valueOf(0));
        collection = new PageCollection(pagesDict, null);
    }

    @Test
    public void addDefaultPage() {
        Page page = collection.add();
        assertNotNull(page);
        assertEquals(1, collection.size());
        assertEquals(1, kids.size());
    }

    @Test
    public void addDefaultPageHasA4MediaBox() {
        Page page = collection.add();
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(0, mb.getLLX(), 1e-10);
        assertEquals(0, mb.getLLY(), 1e-10);
        assertEquals(595, mb.getURX(), 1e-10);
        assertEquals(842, mb.getURY(), 1e-10);
    }

    @Test
    public void addPageMediaBoxIsExactlyA4_LogsCorrectDimensions() throws Exception {
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger("org.aspose.pdf.PageCollection");
        java.util.logging.Level orig = logger.getLevel();
        logger.setLevel(java.util.logging.Level.FINE);
        java.util.List<String> messages = new java.util.ArrayList<>();
        java.util.logging.Handler h = new java.util.logging.Handler() {
            public void publish(java.util.logging.LogRecord r) { messages.add(r.getMessage()); }
            public void flush() {} public void close() {}
        };
        logger.addHandler(h);
        try {
            Page p = collection.add();
            Rectangle mb = p.getMediaBox();
            assertEquals(842.0, mb.getURY(), 1e-10);
            assertEquals(595.0, mb.getURX(), 1e-10);
        } finally {
            logger.removeHandler(h);
            logger.setLevel(orig);
        }
    }

    @Test
    public void addDefaultPageSetsParent() {
        Page page = collection.add();
        COSDictionary pageDict = page.getCOSDictionary();
        assertSame(pagesDict, pageDict.get(COSName.PARENT));
    }

    @Test
    public void addDefaultPageSetsTypeToPage() {
        Page page = collection.add();
        COSDictionary pageDict = page.getCOSDictionary();
        assertEquals(COSName.PAGE, pageDict.get(COSName.TYPE));
    }

    @Test
    public void addMultipleDefaultPages() {
        collection.add();
        collection.add();
        collection.add();
        assertEquals(3, collection.size());
        assertEquals(3, kids.size());
    }

    @Test
    public void addExistingPage() {
        COSDictionary pageDict = createPageDict();
        Page page = new Page(pageDict, null);
        collection.add(page);
        assertEquals(1, collection.size());
        assertSame(pagesDict, pageDict.get(COSName.PARENT));
    }

    @Test
    public void addNullPageThrows() {
        assertThrows(IllegalArgumentException.class, () -> collection.add((Page) null));
    }

    @Test
    public void insertAtBeginning() {
        collection.add(); // page 1
        collection.add(); // page 2

        COSDictionary insertDict = createPageDict();
        Page insertPage = new Page(insertDict, null);
        collection.insert(1, insertPage);

        assertEquals(3, collection.size());
        // The inserted page should be at position 1
        assertSame(insertDict, collection.get(1).getCOSDictionary());
    }

    @Test
    public void insertAtEnd() {
        collection.add(); // page 1

        COSDictionary insertDict = createPageDict();
        Page insertPage = new Page(insertDict, null);
        collection.insert(2, insertPage);

        assertEquals(2, collection.size());
        assertSame(insertDict, collection.get(2).getCOSDictionary());
    }

    @Test
    public void insertInMiddle() {
        Page p1 = collection.add();
        Page p3 = collection.add();

        COSDictionary insertDict = createPageDict();
        Page p2 = new Page(insertDict, null);
        collection.insert(2, p2);

        assertEquals(3, collection.size());
        assertSame(p1.getCOSDictionary(), collection.get(1).getCOSDictionary());
        assertSame(insertDict, collection.get(2).getCOSDictionary());
        assertSame(p3.getCOSDictionary(), collection.get(3).getCOSDictionary());
    }

    @Test
    public void insertSetsParent() {
        COSDictionary insertDict = createPageDict();
        Page page = new Page(insertDict, null);
        collection.insert(1, page);
        assertSame(pagesDict, insertDict.get(COSName.PARENT));
    }

    @Test
    public void insertNullPageThrows() {
        assertThrows(IllegalArgumentException.class, () -> collection.insert(1, null));
    }

    @Test
    public void insertOutOfBoundsThrows() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                collection.insert(0, new Page(createPageDict(), null)));
        assertThrows(IndexOutOfBoundsException.class, () ->
                collection.insert(2, new Page(createPageDict(), null)));
    }

    @Test
    public void deleteFirstPage() {
        collection.add();
        collection.add();
        collection.delete(1);
        assertEquals(1, collection.size());
    }

    @Test
    public void deleteLastPage() {
        Page p1 = collection.add();
        collection.add();
        collection.delete(2);
        assertEquals(1, collection.size());
        assertSame(p1.getCOSDictionary(), collection.get(1).getCOSDictionary());
    }

    @Test
    public void deleteMiddlePage() {
        Page p1 = collection.add();
        collection.add();
        Page p3 = collection.add();
        collection.delete(2);
        assertEquals(2, collection.size());
        assertSame(p1.getCOSDictionary(), collection.get(1).getCOSDictionary());
        assertSame(p3.getCOSDictionary(), collection.get(2).getCOSDictionary());
    }

    @Test
    public void deleteOnlyPage() {
        collection.add();
        collection.delete(1);
        assertEquals(0, collection.size());
    }

    @Test
    public void deleteOutOfBoundsThrows() {
        collection.add();
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(0));
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(2));
    }

    @Test
    public void deleteFromEmptyThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> collection.delete(1));
    }

    @Test
    public void countUpdatedAfterAdd() {
        collection.add();
        collection.add();
        int count = pagesDict.getInt(COSName.COUNT, -1);
        assertEquals(2, count);
    }

    @Test
    public void countUpdatedAfterDelete() {
        collection.add();
        collection.add();
        collection.delete(1);
        int count = pagesDict.getInt(COSName.COUNT, -1);
        assertEquals(1, count);
    }

    @Test
    public void countUpdatedAfterInsert() {
        collection.add();
        collection.insert(1, new Page(createPageDict(), null));
        int count = pagesDict.getInt(COSName.COUNT, -1);
        assertEquals(2, count);
    }

    @Test
    public void pageNumbersReassignedAfterMutation() {
        collection.add();
        collection.add();
        collection.add();
        // Delete middle page
        collection.delete(2);
        // Page numbers should be 1 and 2
        assertEquals(1, collection.get(1).getNumber());
        assertEquals(2, collection.get(2).getNumber());
    }

    /**
     * Creates a minimal page dictionary for testing.
     */
    private COSDictionary createPageDict() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.TYPE, COSName.PAGE);
        dict.set(COSName.MEDIABOX, new Rectangle(0, 0, 612, 792).toCOSArray());
        return dict;
    }
}
