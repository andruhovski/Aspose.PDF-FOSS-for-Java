package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ViewerPreferences.
 */
public class ViewerPreferencesTest {

    @Test
    public void testDefaults() {
        ViewerPreferences vp = new ViewerPreferences(null);
        assertFalse(vp.getHideToolbar());
        assertFalse(vp.getHideMenubar());
        assertFalse(vp.getHideWindowUI());
        assertFalse(vp.getFitWindow());
        assertFalse(vp.getCenterWindow());
        assertFalse(vp.getDisplayDocTitle());
        assertEquals("UseNone", vp.getNonFullScreenPageMode());
        assertEquals("L2R", vp.getDirection());
        assertEquals("AppDefault", vp.getPrintScaling());
        assertNull(vp.getDuplex());
        assertEquals(0, vp.getNumCopies());
    }

    @Test
    public void testSetHideToolbar() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setHideToolbar(true);
        assertTrue(vp.getHideToolbar());
        vp.setHideToolbar(false);
        assertFalse(vp.getHideToolbar());
    }

    @Test
    public void testSetHideMenubar() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setHideMenubar(true);
        assertTrue(vp.getHideMenubar());
    }

    @Test
    public void testSetHideWindowUI() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setHideWindowUI(true);
        assertTrue(vp.getHideWindowUI());
    }

    @Test
    public void testSetFitWindow() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setFitWindow(true);
        assertTrue(vp.getFitWindow());
    }

    @Test
    public void testSetCenterWindow() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setCenterWindow(true);
        assertTrue(vp.getCenterWindow());
    }

    @Test
    public void testSetDisplayDocTitle() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setDisplayDocTitle(true);
        assertTrue(vp.getDisplayDocTitle());
    }

    @Test
    public void testDirection() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setDirection("R2L");
        assertEquals("R2L", vp.getDirection());
    }

    @Test
    public void testDuplex() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setDuplex("DuplexFlipShortEdge");
        assertEquals("DuplexFlipShortEdge", vp.getDuplex());
    }

    @Test
    public void testPrintScaling() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setPrintScaling("None");
        assertEquals("None", vp.getPrintScaling());
    }

    @Test
    public void testNumCopies() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setNumCopies(3);
        assertEquals(3, vp.getNumCopies());
    }

    @Test
    public void testDocumentViewerPrefs() throws Exception {
        Document doc = new Document();
        ViewerPreferences vp = doc.getViewerPreferences();
        assertNotNull(vp);
        assertFalse(vp.getHideMenubar());
        doc.close();
    }

    @Test
    public void testDocumentConvenienceMethods() throws Exception {
        Document doc = new Document();
        doc.setHideMenubar(true);
        assertTrue(doc.getHideMenubar());
        doc.setFitWindow(true);
        assertTrue(doc.getFitWindow());
        doc.setDisplayDocTitle(true);
        assertTrue(doc.getDisplayDocTitle());
        doc.close();
    }

    @Test
    public void testDocumentPageLayout() throws Exception {
        Document doc = new Document();
        assertEquals("SinglePage", doc.getPageLayout());
        doc.close();
    }

    @Test
    public void testNonFullScreenPageMode() {
        ViewerPreferences vp = new ViewerPreferences(new PdfDictionary());
        vp.setNonFullScreenPageMode("UseOutlines");
        assertEquals("UseOutlines", vp.getNonFullScreenPageMode());
    }

    @Test
    public void testReadFromDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("HideToolbar"), PdfBoolean.TRUE);
        dict.set(PdfName.of("CenterWindow"), PdfBoolean.TRUE);
        dict.set(PdfName.of("Direction"), PdfName.of("R2L"));
        ViewerPreferences vp = new ViewerPreferences(dict);
        assertTrue(vp.getHideToolbar());
        assertTrue(vp.getCenterWindow());
        assertEquals("R2L", vp.getDirection());
    }
}
