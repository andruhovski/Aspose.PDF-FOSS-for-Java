package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Page].
public class PageTest {

    private static PdfArray makeBox(double llx, double lly, double urx, double ury) {
        PdfArray box = new PdfArray(4);
        box.add(new PdfFloat(llx));
        box.add(new PdfFloat(lly));
        box.add(new PdfFloat(urx));
        box.add(new PdfFloat(ury));
        return box;
    }

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Page(null, null));
    }

    @Test
    public void getMediaBoxFromPageDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Type", PdfName.PAGE);
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
        PdfDictionary parentDict = new PdfDictionary();
        parentDict.set("Type", PdfName.PAGES);
        parentDict.set("MediaBox", makeBox(0, 0, 595, 842));

        // Page dict has NO MediaBox, but /Parent points to parentDict
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set("Type", PdfName.PAGE);
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
        PdfDictionary grandparent = new PdfDictionary();
        grandparent.set("Type", PdfName.PAGES);
        grandparent.set("MediaBox", makeBox(0, 0, 400, 600));

        // Parent - no MediaBox
        PdfDictionary parent = new PdfDictionary();
        parent.set("Type", PdfName.PAGES);
        parent.set("Parent", grandparent);

        // Page - no MediaBox
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set("Type", PdfName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(400, mb.getURX(), 1e-10);
    }

    @Test
    public void getCropBoxDefaultsToMediaBox() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        Rectangle crop = page.getCropBox();
        assertNotNull(crop);
        assertEquals(612, crop.getURX(), 1e-10);
    }

    @Test
    public void getCropBoxExplicit() {
        PdfDictionary dict = new PdfDictionary();
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
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        Rectangle art = page.getArtBox();
        assertEquals(page.getCropBox(), art);
    }

    @Test
    public void getBleedBoxDefaultsToCropBox() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getBleedBox());
    }

    @Test
    public void getTrimBoxDefaultsToCropBox() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getTrimBox());
    }

    @Test
    public void getArtBoxExplicit() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));
        dict.set("ArtBox", makeBox(20, 20, 500, 700));

        Page page = new Page(dict, null);
        Rectangle art = page.getArtBox();
        assertEquals(20, art.getLLX(), 1e-10);
        assertEquals(500, art.getURX(), 1e-10);
    }

    @Test
    public void getRectSameAsCropBox() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));
        dict.set("CropBox", makeBox(5, 5, 607, 787));

        Page page = new Page(dict, null);
        assertEquals(page.getCropBox(), page.getRect());
    }

    @Test
    public void getRotateDefault() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("MediaBox", makeBox(0, 0, 612, 792));

        Page page = new Page(dict, null);
        assertEquals(0, page.getRotate());
    }

    @Test
    public void getRotateExplicit() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Rotate", PdfInteger.valueOf(90));

        Page page = new Page(dict, null);
        assertEquals(90, page.getRotate());
    }

    @Test
    public void getRotateInheritedFromParent() {
        PdfDictionary parent = new PdfDictionary();
        parent.set("Type", PdfName.PAGES);
        parent.set("Rotate", PdfInteger.valueOf(180));

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set("Type", PdfName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        assertEquals(180, page.getRotate());
    }

    @Test
    public void getResourcesFromPageDict() {
        PdfDictionary resourcesDict = new PdfDictionary();
        PdfDictionary fontDict = new PdfDictionary();
        resourcesDict.set("Font", fontDict);

        PdfDictionary dict = new PdfDictionary();
        dict.set("Resources", resourcesDict);

        Page page = new Page(dict, null);
        Resources res = page.getResources();
        assertNotNull(res);
        assertSame(resourcesDict, res.getPdfDictionary());
    }

    @Test
    public void getResourcesInheritedFromParent() {
        PdfDictionary resourcesDict = new PdfDictionary();

        PdfDictionary parent = new PdfDictionary();
        parent.set("Type", PdfName.PAGES);
        parent.set("Resources", resourcesDict);

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set("Type", PdfName.PAGE);
        pageDict.set("Parent", parent);

        Page page = new Page(pageDict, null);
        Resources res = page.getResources();
        assertNotNull(res);
        assertSame(resourcesDict, res.getPdfDictionary());
    }

    @Test
    public void getContentsReturnsEmptyWhenAbsent() throws Exception {
        PdfDictionary dict = new PdfDictionary();
        Page page = new Page(dict, null);
        assertNotNull(page.getContents());
        assertEquals(0, page.getContents().size());
    }

    @Test
    public void getRawContentsReturnsNullWhenAbsent() {
        PdfDictionary dict = new PdfDictionary();
        Page page = new Page(dict, null);
        assertNull(page.getRawContents());
    }

    @Test
    public void getAnnotationsReturnsEmptyWhenAbsent() {
        PdfDictionary dict = new PdfDictionary();
        Page page = new Page(dict, null);
        assertNotNull(page.getAnnotations());
        assertEquals(0, page.getAnnotations().getCount());
    }

    @Test
    public void getAnnotationsReturnsCollection() {
        PdfArray annots = new PdfArray(2);
        PdfDictionary annotDict = new PdfDictionary();
        annotDict.set(PdfName.of("Subtype"), PdfName.of("Text"));
        annots.add(annotDict);
        PdfDictionary dict = new PdfDictionary();
        dict.set("Annots", annots);

        Page page = new Page(dict, null);
        assertNotNull(page.getAnnotations());
        assertEquals(1, page.getAnnotations().getCount());
    }

    @Test
    public void getPdfDictionaryReturnsUnderlying() {
        PdfDictionary dict = new PdfDictionary();
        Page page = new Page(dict, null);
        assertSame(dict, page.getPdfDictionary());
    }

    @Test
    public void setAndGetNumber() {
        Page page = new Page(new PdfDictionary(), null);
        page.setNumber(5);
        assertEquals(5, page.getNumber());
    }

    @Test
    public void indirectReferenceParentIsResolved() {
        // Parent dict behind a PdfObjectReference
        PdfDictionary parentDict = new PdfDictionary();
        parentDict.set("Type", PdfName.PAGES);
        parentDict.set("MediaBox", makeBox(0, 0, 300, 400));

        PdfObjectKey key = new PdfObjectKey(2, 0);
        PdfObjectReference parentRef = new PdfObjectReference(key, k -> parentDict);

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set("Type", PdfName.PAGE);
        pageDict.set("Parent", parentRef);

        Page page = new Page(pageDict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(300, mb.getURX(), 1e-10);
        assertEquals(400, mb.getURY(), 1e-10);
    }

    @Test
    public void indirectReferenceMediaBoxIsResolved() {
        PdfArray boxArray = makeBox(0, 0, 500, 700);
        PdfObjectKey key = new PdfObjectKey(10, 0);
        PdfObjectReference boxRef = new PdfObjectReference(key, k -> boxArray);

        PdfDictionary dict = new PdfDictionary();
        dict.set("Type", PdfName.PAGE);
        dict.set(PdfName.MEDIABOX, boxRef);

        Page page = new Page(dict, null);
        Rectangle mb = page.getMediaBox();
        assertNotNull(mb);
        assertEquals(500, mb.getURX(), 1e-10);
    }
}
