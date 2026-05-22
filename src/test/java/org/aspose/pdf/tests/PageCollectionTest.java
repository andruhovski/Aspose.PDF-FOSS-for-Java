package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PageCollection}.
 */
public class PageCollectionTest {

    private static COSArray makeBox(double llx, double lly, double urx, double ury) {
        COSArray box = new COSArray(4);
        box.add(new COSFloat(llx));
        box.add(new COSFloat(lly));
        box.add(new COSFloat(urx));
        box.add(new COSFloat(ury));
        return box;
    }

    private static COSDictionary makePageDict(COSDictionary parent) {
        COSDictionary page = new COSDictionary();
        page.set("Type", COSName.PAGE);
        page.set("MediaBox", makeBox(0, 0, 612, 792));
        if (parent != null) {
            page.set("Parent", parent);
        }
        return page;
    }

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PageCollection(null, null));
    }

    @Test
    public void singlePageDocument() {
        COSDictionary pagesDict = new COSDictionary();
        pagesDict.set("Type", COSName.PAGES);
        pagesDict.set("Count", COSInteger.valueOf(1));

        COSDictionary pageDict = makePageDict(pagesDict);

        COSArray kids = new COSArray(1);
        kids.add(pageDict);
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);
        assertEquals(1, pc.size());
        assertEquals(1, pc.getCount());

        Page page = pc.get(1);
        assertNotNull(page);
        assertEquals(1, page.getNumber());
        assertEquals(612, page.getMediaBox().getURX(), 1e-10);
    }

    @Test
    public void multiplePages() {
        COSDictionary pagesDict = new COSDictionary();
        pagesDict.set("Type", COSName.PAGES);

        COSArray kids = new COSArray(3);
        for (int i = 0; i < 3; i++) {
            kids.add(makePageDict(pagesDict));
        }
        pagesDict.set("Kids", kids);
        pagesDict.set("Count", COSInteger.valueOf(3));

        PageCollection pc = new PageCollection(pagesDict, null);
        assertEquals(3, pc.size());

        for (int i = 1; i <= 3; i++) {
            Page p = pc.get(i);
            assertEquals(i, p.getNumber());
        }
    }

    @Test
    public void nestedPageTree() {
        // Root /Pages
        COSDictionary root = new COSDictionary();
        root.set("Type", COSName.PAGES);

        // Intermediate /Pages node
        COSDictionary intermediate = new COSDictionary();
        intermediate.set("Type", COSName.PAGES);
        intermediate.set("Parent", root);

        COSArray intermediateKids = new COSArray(2);
        intermediateKids.add(makePageDict(intermediate));
        intermediateKids.add(makePageDict(intermediate));
        intermediate.set("Kids", intermediateKids);
        intermediate.set("Count", COSInteger.valueOf(2));

        // Direct page under root
        COSDictionary directPage = makePageDict(root);

        COSArray rootKids = new COSArray(2);
        rootKids.add(intermediate);
        rootKids.add(directPage);
        root.set("Kids", rootKids);
        root.set("Count", COSInteger.valueOf(3));

        PageCollection pc = new PageCollection(root, null);
        assertEquals(3, pc.size());
        assertEquals(1, pc.get(1).getNumber());
        assertEquals(2, pc.get(2).getNumber());
        assertEquals(3, pc.get(3).getNumber());
    }

    @Test
    public void oneBasedIndexing() {
        COSDictionary pagesDict = new COSDictionary();
        pagesDict.set("Type", COSName.PAGES);
        COSArray kids = new COSArray(1);
        kids.add(makePageDict(pagesDict));
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);

        assertThrows(IndexOutOfBoundsException.class, () -> pc.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> pc.get(2));
        assertNotNull(pc.get(1));
    }

    @Test
    public void iteratorWalksAllPages() {
        COSDictionary pagesDict = new COSDictionary();
        pagesDict.set("Type", COSName.PAGES);
        COSArray kids = new COSArray(2);
        kids.add(makePageDict(pagesDict));
        kids.add(makePageDict(pagesDict));
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);
        int count = 0;
        for (Page p : pc) {
            count++;
            assertNotNull(p);
        }
        assertEquals(2, count);
    }

    @Test
    public void indirectReferenceKidsAreResolved() {
        COSDictionary pagesDict = new COSDictionary();
        pagesDict.set("Type", COSName.PAGES);

        COSDictionary pageDict = makePageDict(pagesDict);
        COSObjectKey key = new COSObjectKey(3, 0);
        COSObjectReference pageRef = new COSObjectReference(key, k -> pageDict);

        COSArray kids = new COSArray(1);
        kids.add(pageRef);
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);
        assertEquals(1, pc.size());
        assertNotNull(pc.get(1));
    }

    @Test
    public void deeplyNestedTree() {
        // 3 levels deep
        COSDictionary root = new COSDictionary();
        root.set("Type", COSName.PAGES);

        COSDictionary level1 = new COSDictionary();
        level1.set("Type", COSName.PAGES);
        level1.set("Parent", root);

        COSDictionary level2 = new COSDictionary();
        level2.set("Type", COSName.PAGES);
        level2.set("Parent", level1);

        COSArray level2Kids = new COSArray(1);
        level2Kids.add(makePageDict(level2));
        level2.set("Kids", level2Kids);

        COSArray level1Kids = new COSArray(1);
        level1Kids.add(level2);
        level1.set("Kids", level1Kids);

        COSArray rootKids = new COSArray(1);
        rootKids.add(level1);
        root.set("Kids", rootKids);

        PageCollection pc = new PageCollection(root, null);
        assertEquals(1, pc.size());
        assertEquals(1, pc.get(1).getNumber());
    }
}
