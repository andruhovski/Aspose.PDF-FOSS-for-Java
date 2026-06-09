package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PageCollection}.
 */
public class PageCollectionTest {

    private static PdfArray makeBox(double llx, double lly, double urx, double ury) {
        PdfArray box = new PdfArray(4);
        box.add(new PdfFloat(llx));
        box.add(new PdfFloat(lly));
        box.add(new PdfFloat(urx));
        box.add(new PdfFloat(ury));
        return box;
    }

    private static PdfDictionary makePageDict(PdfDictionary parent) {
        PdfDictionary page = new PdfDictionary();
        page.set("Type", PdfName.PAGE);
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
        PdfDictionary pagesDict = new PdfDictionary();
        pagesDict.set("Type", PdfName.PAGES);
        pagesDict.set("Count", PdfInteger.valueOf(1));

        PdfDictionary pageDict = makePageDict(pagesDict);

        PdfArray kids = new PdfArray(1);
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
        PdfDictionary pagesDict = new PdfDictionary();
        pagesDict.set("Type", PdfName.PAGES);

        PdfArray kids = new PdfArray(3);
        for (int i = 0; i < 3; i++) {
            kids.add(makePageDict(pagesDict));
        }
        pagesDict.set("Kids", kids);
        pagesDict.set("Count", PdfInteger.valueOf(3));

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
        PdfDictionary root = new PdfDictionary();
        root.set("Type", PdfName.PAGES);

        // Intermediate /Pages node
        PdfDictionary intermediate = new PdfDictionary();
        intermediate.set("Type", PdfName.PAGES);
        intermediate.set("Parent", root);

        PdfArray intermediateKids = new PdfArray(2);
        intermediateKids.add(makePageDict(intermediate));
        intermediateKids.add(makePageDict(intermediate));
        intermediate.set("Kids", intermediateKids);
        intermediate.set("Count", PdfInteger.valueOf(2));

        // Direct page under root
        PdfDictionary directPage = makePageDict(root);

        PdfArray rootKids = new PdfArray(2);
        rootKids.add(intermediate);
        rootKids.add(directPage);
        root.set("Kids", rootKids);
        root.set("Count", PdfInteger.valueOf(3));

        PageCollection pc = new PageCollection(root, null);
        assertEquals(3, pc.size());
        assertEquals(1, pc.get(1).getNumber());
        assertEquals(2, pc.get(2).getNumber());
        assertEquals(3, pc.get(3).getNumber());
    }

    @Test
    public void oneBasedIndexing() {
        PdfDictionary pagesDict = new PdfDictionary();
        pagesDict.set("Type", PdfName.PAGES);
        PdfArray kids = new PdfArray(1);
        kids.add(makePageDict(pagesDict));
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);

        assertThrows(IndexOutOfBoundsException.class, () -> pc.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> pc.get(2));
        assertNotNull(pc.get(1));
    }

    @Test
    public void iteratorWalksAllPages() {
        PdfDictionary pagesDict = new PdfDictionary();
        pagesDict.set("Type", PdfName.PAGES);
        PdfArray kids = new PdfArray(2);
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
        PdfDictionary pagesDict = new PdfDictionary();
        pagesDict.set("Type", PdfName.PAGES);

        PdfDictionary pageDict = makePageDict(pagesDict);
        PdfObjectKey key = new PdfObjectKey(3, 0);
        PdfObjectReference pageRef = new PdfObjectReference(key, k -> pageDict);

        PdfArray kids = new PdfArray(1);
        kids.add(pageRef);
        pagesDict.set("Kids", kids);

        PageCollection pc = new PageCollection(pagesDict, null);
        assertEquals(1, pc.size());
        assertNotNull(pc.get(1));
    }

    @Test
    public void deeplyNestedTree() {
        // 3 levels deep
        PdfDictionary root = new PdfDictionary();
        root.set("Type", PdfName.PAGES);

        PdfDictionary level1 = new PdfDictionary();
        level1.set("Type", PdfName.PAGES);
        level1.set("Parent", root);

        PdfDictionary level2 = new PdfDictionary();
        level2.set("Type", PdfName.PAGES);
        level2.set("Parent", level1);

        PdfArray level2Kids = new PdfArray(1);
        level2Kids.add(makePageDict(level2));
        level2.set("Kids", level2Kids);

        PdfArray level1Kids = new PdfArray(1);
        level1Kids.add(level2);
        level1.set("Kids", level1Kids);

        PdfArray rootKids = new PdfArray(1);
        rootKids.add(level1);
        root.set("Kids", rootKids);

        PageCollection pc = new PageCollection(root, null);
        assertEquals(1, pc.size());
        assertEquals(1, pc.get(1).getNumber());
    }
}
