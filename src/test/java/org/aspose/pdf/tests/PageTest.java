package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Page}.
 */
public class PageTest {

    private static COSArray makeBox(double llx, double lly, double urx, double ury) {
        COSArray box = new COSArray(4);
        box.add(new COSFloat(llx));
        box.add(new COSFloat(lly));
        box.add(new COSFloat(urx));
        box.add(new COSFloat(ury));
        return box;
    }

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Page(null, null));
    }

    @Test
    public void getMediaBoxFromPageDict() {
        COSDictionary dict = new COSDictionary();
        dict.set("Type", COSName.PAGE);
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(0, mb.getLLX(), 1e-10);
        assertEquals(0, mb.getLLY(), 1e-10);
        assertEquals(612, mb.getURX(), 1e-10);
        assertEquals(792, mb.getURY(), 1e-10);
    }

    @Test
    public void getMediaBoxInheritedFromParent() {
        // Parent /Pages node has MediaBox
        COSDictionary parentDict = new COSDictionary();
        parentDict.set("Type", COSName.PAGES);
        parentDict.set("MediaBox", makeBox(0, 0, 595, 842));

        // Page dict has NO MediaBox, but /Parent points to parentDict
        COSDictionary pageDict = new COSDictionary();
        pageDict.set("Type", COSName.PAGE);
        pageDict.set("Parent", parentDict);

        Page page = new Page(pageDict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(595, mb.getURX(), 1e-10);
        assertEquals(842, mb.getURY(), 1e-10);
    }

    @Test
    public void getMediaBoxInheritedFromGrandparent() {
        // Grandparent
        COSDictionary grandparent = new COSDictionary();
        grandparent.set("Type", COSName.PAGES);
        grandparent.set("MediaBox", makeBox(0, 0, 400, 600));

        // Parent - no MediaBox
        COSDictionary parent = new COSDictionary();
        parent.set("Type", COSName.PAGES);
        parent.set("Parent", grandparent);

        // Page - no MediaBox
        COSDictionary pageDict = new COSDictionary();
        pageDict.set("Type", COSName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(400, mb.getURX(), 1e-10);
    }

    @Test
    public void getCropBoxDefaultsToMediaBox() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        Rectangle crop = page.getCropBox();
        assertNotNull(crop);
        assertEquals(612, crop.getURX(), 1e-10);
    }

    @Test
    public void getCropBoxExplicit() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));
        dict.set("CropBox", makeBox(10, 10, 600, 780));

        Page page = new Page(dict, null);
        Rectangle crop = page.getCropBox();
        assertNotNull(crop);
        assertEquals(10, crop.getLLX(), 1e-10);
        assertEquals(600, crop.getURX(), 1e-10);
    }

    @Test
    public void getArtBoxDefaultsToCropBox() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        Rectangle art = page.getArtBox();
        assertEquals(page.getCropBox(), art);
    }

    @Test
    public void getBleedBoxDefaultsToCropBox() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getBleedBox());
    }

    @Test
    public void getTrimBoxDefaultsToCropBox() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getTrimBox());
    }

    @Test
    public void getArtBoxExplicit() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));
        dict.set("ArtBox", makeBox(20, 20, 500, 700));

        Page page = new Page(dict, null);
        Rectangle art = page.getArtBox();
        assertEquals(20, art.getLLX(), 1e-10);
        assertEquals(500, art.getURX(), 1e-10);
    }

    @Test
    public void getRectSameAsCropBox() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));
        dict.set("CropBox", makeBox(5, 5, 607, 787));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getRect());
    }

    @Test
    public void getRotateDefault() {
        COSDictionary dict = new COSDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(0, page.getRotate());
    }

    @Test
    public void getRotateExplicit() {
        COSDictionary dict = new COSDictionary();
        dict.set("Rotate", COSInteger.valueOf(90));

        Page page = new Page(dict, null);
        assertEquals(90, page.getRotate());
    }

    @Test
    public void getRotateInheritedFromParent() {
        COSDictionary parent = new COSDictionary();
        parent.set("Type", COSName.PAGES);
        parent.set("Rotate", COSInteger.valueOf(180));

        COSDictionary pageDict = new COSDictionary();
        pageDict.set("Type", COSName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        assertEquals(180, page.getRotate());
    }

    @Test
    public void getResourcesFromPageDict() {
        COSDictionary resourcesDict = new COSDictionary();
        COSDictionary fontDict = new COSDictionary();
        resourcesDict.set("Font", fontDict);

        COSDictionary dict = new COSDictionary();
        dict.set("Resources", resourcesDict);

        Page page = new Page(dict, null);
        Resources res = page.getResources();
        assertNotNull(res);
        assertSame(resourcesDict, res.getCOSDictionary());
    }

    @Test
    public void getResourcesInheritedFromParent() {
        COSDictionary resourcesDict = new COSDictionary();

        COSDictionary parent = new COSDictionary();
        parent.set("Type", COSName.PAGES);
        parent.set("Resources", resourcesDict);

        COSDictionary pageDict = new COSDictionary();
        pageDict.set("Type", COSName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        Resources res = page.getResources();
        assertNotNull(res);
        assertSame(resourcesDict, res.getCOSDictionary());
    }

    @Test
    public void getContentsReturnsEmptyWhenAbsent() throws Exception {
        COSDictionary dict = new COSDictionary();
        Page page = new Page(dict, null);
        assertNotNull(page.getContents());
        assertEquals(0, page.getContents().size());
    }

    @Test
    public void getRawContentsReturnsNullWhenAbsent() {
        COSDictionary dict = new COSDictionary();
        Page page = new Page(dict, null);
        assertNull(page.getRawContents());
    }

    @Test
    public void getAnnotationsReturnsEmptyWhenAbsent() {
        COSDictionary dict = new COSDictionary();
        Page page = new Page(dict, null);
        assertNotNull(page.getAnnotations());
        assertEquals(0, page.getAnnotations().getCount());
    }

    @Test
    public void getAnnotationsReturnsCollection() {
        COSArray annots = new COSArray(2);
        COSDictionary annotDict = new COSDictionary();
        annotDict.set(COSName.of("Subtype"), COSName.of("Text"));
        annots.add(annotDict);
        COSDictionary dict = new COSDictionary();
        dict.set("Annots", annots);

        Page page = new Page(dict, null);
        assertNotNull(page.getAnnotations());
        assertEquals(1, page.getAnnotations().getCount());
    }

    @Test
    public void getCOSDictionaryReturnsUnderlying() {
        COSDictionary dict = new COSDictionary();
        Page page = new Page(dict, null);
        assertSame(dict, page.getCOSDictionary());
    }

    @Test
    public void setAndGetNumber() {
        Page page = new Page(new COSDictionary(), null);
        page.setNumber(5);
        assertEquals(5, page.getNumber());
    }

    @Test
    public void indirectReferenceParentIsResolved() {
        // Parent dict behind a COSObjectReference
        COSDictionary parentDict = new COSDictionary();
        parentDict.set("Type", COSName.PAGES);
        parentDict.set("MediaBox", makeBox(0, 0, 300, 400));

        COSObjectKey key = new COSObjectKey(2, 0);
        COSObjectReference parentRef = new COSObjectReference(key, k -> parentDict);

        COSDictionary pageDict = new COSDictionary();
        pageDict.set("Type", COSName.PAGE);
        pageDict.set("Parent", parentRef);

        Page page = new Page(pageDict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(300, mb.getURX(), 1e-10);
        assertEquals(400, mb.getURY(), 1e-10);
    }

    @Test
    public void indirectReferenceMediaBoxIsResolved() {
        COSArray boxArray = makeBox(0, 0, 500, 700);
        COSObjectKey key = new COSObjectKey(10, 0);
        COSObjectReference boxRef = new COSObjectReference(key, k -> boxArray);

        COSDictionary dict = new COSDictionary();
        dict.set("Type", COSName.PAGE);
        dict.set(COSName.MEDIABOX, boxRef);

        Page page = new Page(dict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(500, mb.getURX(), 1e-10);
    }
}
