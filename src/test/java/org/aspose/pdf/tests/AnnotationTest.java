package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.annotations.*;
import org.aspose.pdf.engine.cos.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for annotation classes.
 */
public class AnnotationTest {

    private Page createPage() {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        return new Page(pageDict, null);
    }

    // ── Factory dispatch ──

    @Test
    public void testFactoryText() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("Text"));
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof TextAnnotation);
    }

    @Test
    public void testFactoryLink() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("Link"));
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof LinkAnnotation);
    }

    @Test
    public void testFactoryHighlight() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("Highlight"));
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof HighlightAnnotation);
    }

    @Test
    public void testFactoryWidget() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof WidgetAnnotation);
    }

    @Test
    public void testFactoryUnknown() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("XYZUnknown"));
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof GenericAnnotation);
    }

    @Test
    public void testFactoryNoSubtype() {
        COSDictionary dict = new COSDictionary();
        Annotation a = Annotation.fromDictionary(dict, null);
        assertTrue(a instanceof GenericAnnotation);
    }

    // ── AnnotationCollection ──

    @Test
    public void testCollectionFromArray() {
        COSArray arr = new COSArray();
        COSDictionary d1 = new COSDictionary();
        d1.set(COSName.of("Subtype"), COSName.of("Text"));
        COSDictionary d2 = new COSDictionary();
        d2.set(COSName.of("Subtype"), COSName.of("Link"));
        COSDictionary d3 = new COSDictionary();
        d3.set(COSName.of("Subtype"), COSName.of("Highlight"));
        arr.add(d1); arr.add(d2); arr.add(d3);

        AnnotationCollection coll = new AnnotationCollection(arr, null, null);
        assertEquals(3, coll.getCount());
        assertTrue(coll.get(1) instanceof TextAnnotation);
        assertTrue(coll.get(2) instanceof LinkAnnotation);
        assertTrue(coll.get(3) instanceof HighlightAnnotation);
    }

    @Test
    public void testCollectionOneBasedIndex() {
        COSArray arr = new COSArray();
        arr.add(new COSDictionary());
        AnnotationCollection coll = new AnnotationCollection(arr, null, null);
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(2));
    }

    @Test
    public void testCollectionAdd() {
        COSArray arr = new COSArray();
        AnnotationCollection coll = new AnnotationCollection(arr, null, null);
        assertEquals(0, coll.getCount());

        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 100, 100));
        coll.add(ta);
        assertEquals(1, coll.getCount());
        assertSame(ta, coll.get(1));
    }

    @Test
    public void testCollectionDelete() {
        COSArray arr = new COSArray();
        COSDictionary d1 = new COSDictionary();
        d1.set(COSName.of("Subtype"), COSName.of("Text"));
        d1.set(COSName.of("Contents"), new COSString("First".getBytes()));
        COSDictionary d2 = new COSDictionary();
        d2.set(COSName.of("Subtype"), COSName.of("Text"));
        d2.set(COSName.of("Contents"), new COSString("Second".getBytes()));
        arr.add(d1); arr.add(d2);

        AnnotationCollection coll = new AnnotationCollection(arr, null, null);
        assertEquals(2, coll.getCount());
        coll.delete(1);
        assertEquals(1, coll.getCount());
        assertEquals("Second", coll.get(1).getContents());
    }

    @Test
    public void testCollectionIteration() {
        COSArray arr = new COSArray();
        for (int i = 0; i < 5; i++) {
            COSDictionary d = new COSDictionary();
            d.set(COSName.of("Subtype"), COSName.of("Text"));
            arr.add(d);
        }
        AnnotationCollection coll = new AnnotationCollection(arr, null, null);
        int count = 0;
        for (Annotation a : coll) {
            assertNotNull(a);
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    public void testEmptyCollection() {
        AnnotationCollection coll = new AnnotationCollection(new COSArray(), null, null);
        assertEquals(0, coll.getCount());
    }

    // ── Annotation base properties ──

    @Test
    public void testContents() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(10, 20, 100, 50));
        ta.setContents("Hello World");
        assertEquals("Hello World", ta.getContents());
    }

    @Test
    public void testRect() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(10, 20, 100, 50));
        Rectangle r = ta.getRect();
        assertNotNull(r);
        assertEquals(10, r.getLLX());
        assertEquals(20, r.getLLY());
        assertEquals(100, r.getURX());
        assertEquals(50, r.getURY());
    }

    @Test
    public void testAnnotationColor() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        ta.setColor(Color.RED);
        Color c = ta.getColor();
        assertNotNull(c);
        assertEquals(1.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
        assertEquals(0.0, c.getB(), 0.01);
    }

    @Test
    public void testAnnotationFlags() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        ta.setFlags(4); // Print
        assertTrue(ta.isPrint());
        assertFalse(ta.isHidden());
        assertFalse(ta.isReadOnly());
    }

    @Test
    public void testAnnotationName() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        ta.setName("annot-001");
        assertEquals("annot-001", ta.getName());
    }

    // ── TextAnnotation ──

    @Test
    public void testTextAnnotationOpen() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        assertFalse(ta.getOpen());
        ta.setOpen(true);
        assertTrue(ta.getOpen());
    }

    @Test
    public void testTextAnnotationIcon() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        assertEquals("Note", ta.getIcon()); // default
        ta.setIcon("Comment");
        assertEquals("Comment", ta.getIcon());
    }

    // ── LinkAnnotation ──

    @Test
    public void testLinkAnnotationAction() throws IOException {
        LinkAnnotation link = new LinkAnnotation(null, new Rectangle(0, 0, 100, 20));
        UriAction action = new UriAction("https://example.com");
        link.setAction(action);

        PdfAction readBack = link.getAction();
        assertNotNull(readBack);
        assertTrue(readBack instanceof UriAction);
        assertEquals("https://example.com", ((UriAction) readBack).getUri());
    }

    // ── MarkupAnnotation ──

    @Test
    public void testMarkupTitle() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        ta.setTitle("John Doe");
        assertEquals("John Doe", ta.getTitle());
    }

    @Test
    public void testMarkupOpacity() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        assertEquals(1.0, ta.getOpacity(), 0.01); // default
        ta.setOpacity(0.5);
        assertEquals(0.5, ta.getOpacity(), 0.01);
    }

    @Test
    public void testMarkupSubject() {
        TextAnnotation ta = new TextAnnotation(null, new Rectangle(0, 0, 10, 10));
        ta.setSubject("Review Comment");
        assertEquals("Review Comment", ta.getSubject());
    }

    // ── HighlightAnnotation ──

    @Test
    public void testHighlightQuadPoints() {
        HighlightAnnotation ha = new HighlightAnnotation(null, new Rectangle(0, 0, 200, 20));
        double[] qp = {10, 20, 100, 20, 10, 10, 100, 10};
        ha.setQuadPoints(qp);
        double[] readBack = ha.getQuadPoints();
        assertNotNull(readBack);
        assertEquals(8, readBack.length);
        assertEquals(10, readBack[0], 0.01);
        assertEquals(10, readBack[7], 0.01);
    }

    // ── FreeTextAnnotation ──

    @Test
    public void testFreeTextDefaultAppearance() {
        FreeTextAnnotation ft = new FreeTextAnnotation(null, new Rectangle(0, 0, 200, 50));
        ft.setDefaultAppearance("/Helv 12 Tf 0 0 0 rg");
        assertEquals("/Helv 12 Tf 0 0 0 rg", ft.getDefaultAppearance());
    }

    @Test
    public void testFreeTextJustification() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("FreeText"));
        dict.set(COSName.of("Q"), COSInteger.valueOf(1));
        FreeTextAnnotation ft = new FreeTextAnnotation(dict, null);
        assertEquals(1, ft.getJustification());
    }

    // ── LineAnnotation ──

    @Test
    public void testLineAnnotationEndpoints() {
        LineAnnotation line = new LineAnnotation(null, new Rectangle(0, 0, 200, 200),
                10, 20, 180, 190);
        double[] l = line.getLine();
        assertNotNull(l);
        assertEquals(4, l.length);
        assertEquals(10, l[0], 0.01);
        assertEquals(20, l[1], 0.01);
        assertEquals(180, l[2], 0.01);
        assertEquals(190, l[3], 0.01);
    }

    // ── StampAnnotation ──

    @Test
    public void testStampAnnotationIcon() {
        StampAnnotation stamp = new StampAnnotation(null, new Rectangle(0, 0, 100, 100));
        assertEquals("Draft", stamp.getIcon());
        stamp.setIcon("Approved");
        assertEquals("Approved", stamp.getIcon());
    }

    // ── PopupAnnotation ──

    @Test
    public void testPopupOpen() {
        PopupAnnotation popup = new PopupAnnotation(null, new Rectangle(0, 0, 200, 100));
        assertFalse(popup.getOpen());
        popup.setOpen(true);
        assertTrue(popup.getOpen());
    }

    // ── WidgetAnnotation ──

    @Test
    public void testWidgetFieldType() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        dict.set(COSName.of("FT"), COSName.of("Tx"));
        WidgetAnnotation w = new WidgetAnnotation(dict, null);
        assertEquals("Tx", w.getFieldType());
    }

    // ── Page.getAnnotations() ──

    @Test
    public void testPageGetAnnotations() {
        Page page = createPage();
        AnnotationCollection coll = page.getAnnotations();
        assertNotNull(coll);
        assertEquals(0, coll.getCount());
    }

    @Test
    public void testPageAddAnnotation() {
        Page page = createPage();
        AnnotationCollection coll = page.getAnnotations();
        TextAnnotation ta = new TextAnnotation(page, new Rectangle(10, 10, 50, 50));
        ta.setContents("A note");
        coll.add(ta);

        // Re-fetch to verify persistence in COSArray
        AnnotationCollection coll2 = page.getAnnotations();
        assertEquals(1, coll2.getCount());
    }

}
