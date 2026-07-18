package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for PdfAction subclasses and ExplicitDestination types.
public class ActionTest {

    private Page createPage() {
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        return new Page(pageDict, null);
    }

    // ── GoToAction ──

    @Test
    public void testGoToActionFromPage() {
        Page page = createPage();
        GoToAction action = new GoToAction(page);
        assertEquals("GoTo", action.getType());
        assertNotNull(action.getDestination());
    }

    @Test
    public void testGoToActionFromDestination() {
        Page page = createPage();
        FitExplicitDestination dest = new FitExplicitDestination(page);
        GoToAction action = new GoToAction(dest);
        assertEquals("GoTo", action.getType());
        assertSame(dest, action.getDestination());
    }

    @Test
    public void testGoToActionSetDestination() {
        Page page = createPage();
        GoToAction action = new GoToAction(page);
        XYZExplicitDestination newDest = new XYZExplicitDestination(page, 100, 500, 2.0);
        action.setDestination(newDest);
        assertSame(newDest, action.getDestination());
    }

    // ── UriAction ──

    @Test
    public void testUriAction() {
        UriAction action = new UriAction("https://example.com");
        assertEquals("URI", action.getType());
        assertEquals("https://example.com", action.getUri());
    }

    @Test
    public void testUriActionSetUri() {
        UriAction action = new UriAction("https://old.com");
        action.setUri("https://new.com");
        assertEquals("https://new.com", action.getUri());
    }

    // ── NamedAction ──

    @Test
    public void testNamedActionNextPage() {
        NamedAction action = new NamedAction("NextPage");
        assertEquals("Named", action.getType());
        assertEquals("NextPage", action.getActionName());
    }

    @Test
    public void testNamedActionPrevPage() {
        NamedAction action = new NamedAction("PrevPage");
        assertEquals("PrevPage", action.getActionName());
    }

    // ── GoToRemoteAction ──

    @Test
    public void testGoToRemoteAction() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("GoToR"));
        dict.set(PdfName.of("F"), new PdfString("other.pdf".getBytes()));
        GoToRemoteAction action = new GoToRemoteAction(dict);
        assertEquals("GoToR", action.getType());
        assertEquals("other.pdf", action.getFile());
    }

    // ── PdfAction factory ──

    @Test
    public void testFromDictionaryGoTo() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("GoTo"));
        PdfArray dest = new PdfArray();
        dest.add(PdfNull.INSTANCE);
        dest.add(PdfName.of("Fit"));
        dict.set(PdfName.of("D"), dest);

        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof GoToAction);
    }

    @Test
    public void testFromDictionaryUri() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("URI"));
        dict.set(PdfName.of("URI"), new PdfString("https://test.com".getBytes()));

        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof UriAction);
        assertEquals("https://test.com", ((UriAction) action).getUri());
    }

    @Test
    public void testFromDictionaryNamed() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Named"));
        dict.set(PdfName.of("N"), PdfName.of("FirstPage"));

        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof NamedAction);
        assertEquals("FirstPage", ((NamedAction) action).getActionName());
    }

    @Test
    public void testFromDictionaryNull() throws IOException {
        assertNull(PdfAction.fromDictionary(null, null));
    }

    @Test
    public void testFromDictionaryUnknown() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("SomeUnknownType"));

        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof GenericAction);
    }

    // ── New action types ──

    @Test
    public void testLaunchActionConstruct() {
        LaunchAction action = new LaunchAction("/path/to/file.pdf");
        assertEquals("Launch", action.getType());
        assertEquals("/path/to/file.pdf", action.getFile());
    }

    @Test
    public void testLaunchActionFromDict() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Launch"));
        dict.set(PdfName.of("F"), new PdfString("test.doc"));
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof LaunchAction);
        assertEquals("test.doc", ((LaunchAction) action).getFile());
    }

    @Test
    public void testLaunchActionNewWindow() {
        LaunchAction action = new LaunchAction("file.pdf");
        assertFalse(action.isNewWindow());
        action.setNewWindow(true);
        assertTrue(action.isNewWindow());
    }

    @Test
    public void testGoToEmbeddedAction() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("GoToE"));
        PdfDictionary target = new PdfDictionary();
        target.set(PdfName.of("R"), PdfName.of("C"));
        dict.set(PdfName.of("T"), target);
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof GoToEmbeddedAction);
        assertNotNull(((GoToEmbeddedAction) action).getTarget());
    }

    @Test
    public void testHideActionSingleName() {
        HideAction action = new HideAction("field1", true);
        assertEquals("Hide", action.getType());
        assertTrue(action.isHide());
        String[] names = action.getAnnotationNames();
        assertEquals(1, names.length);
        assertEquals("field1", names[0]);
    }

    @Test
    public void testHideActionMultipleNames() {
        HideAction action = new HideAction(new String[]{"f1", "f2", "f3"}, false);
        assertFalse(action.isHide());
        String[] names = action.getAnnotationNames();
        assertEquals(3, names.length);
        assertEquals("f1", names[0]);
        assertEquals("f2", names[1]);
        assertEquals("f3", names[2]);
    }

    @Test
    public void testHideActionDefaultIsHide() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Hide"));
        dict.set(PdfName.of("T"), new PdfString("field"));
        HideAction action = new HideAction(dict);
        assertTrue(action.isHide()); // default true per spec
    }

    @Test
    public void testJavaScriptActionConstruct() {
        JavaScriptAction action = new JavaScriptAction("app.alert('hello');");
        assertEquals("JavaScript", action.getType());
        assertEquals("app.alert('hello');", action.getScript());
    }

    @Test
    public void testJavaScriptActionFromDict() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("JavaScript"));
        dict.set(PdfName.of("JS"), new PdfString("var x = 1;"));
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof JavaScriptAction);
        assertEquals("var x = 1;", ((JavaScriptAction) action).getScript());
    }

    @Test
    public void testJavaScriptActionSetScript() {
        JavaScriptAction action = new JavaScriptAction("old");
        action.setScript("new");
        assertEquals("new", action.getScript());
    }

    @Test
    public void testSubmitFormActionConstruct() {
        SubmitFormAction action = new SubmitFormAction("https://example.com/submit");
        assertEquals("SubmitForm", action.getType());
        assertEquals("https://example.com/submit", action.getUrl());
    }

    @Test
    public void testSubmitFormActionFlags() {
        SubmitFormAction action = new SubmitFormAction("https://example.com/submit");
        assertEquals(0, action.getFlags());
        action.setFlags(SubmitFormAction.FLAG_GET_METHOD | SubmitFormAction.FLAG_XFDF);
        assertEquals(0x28, action.getFlags());
    }

    @Test
    public void testResetFormActionDefault() {
        ResetFormAction action = new ResetFormAction();
        assertEquals("ResetForm", action.getType());
        assertEquals(0, action.getFlags());
        assertNull(action.getFields());
    }

    @Test
    public void testResetFormActionFields() {
        ResetFormAction action = new ResetFormAction();
        PdfArray fields = new PdfArray();
        fields.add(new PdfString("Name"));
        fields.add(new PdfString("Address"));
        action.setFields(fields);
        assertNotNull(action.getFields());
        assertEquals(2, action.getFields().size());
    }

    @Test
    public void testImportDataAction() {
        ImportDataAction action = new ImportDataAction("data.fdf");
        assertEquals("ImportData", action.getType());
        assertEquals("data.fdf", action.getFile());
    }

    @Test
    public void testSetOCGStateAction() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("SetOCGState"));
        PdfArray state = new PdfArray();
        state.add(PdfName.of("ON"));
        dict.set(PdfName.of("State"), state);
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof SetOCGStateAction);
        assertNotNull(((SetOCGStateAction) action).getState());
        assertTrue(((SetOCGStateAction) action).isPreserveRB());
    }

    @Test
    public void testRenditionAction() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Rendition"));
        dict.setInt("OP", RenditionAction.OP_PLAY);
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof RenditionAction);
        assertEquals(RenditionAction.OP_PLAY, ((RenditionAction) action).getOperation());
    }

    @Test
    public void testTransitionAction() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Trans"));
        PdfDictionary trans = new PdfDictionary();
        trans.set(PdfName.of("Type"), PdfName.of("Trans"));
        dict.set(PdfName.of("Trans"), trans);
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof TransitionAction);
        assertNotNull(((TransitionAction) action).getTransition());
    }

    @Test
    public void testFromDictionaryJavaScript() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("JavaScript"));
        dict.set(PdfName.of("JS"), new PdfString("alert(1)"));
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof JavaScriptAction);
    }

    @Test
    public void testFromDictionaryHide() throws IOException {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("Hide"));
        PdfAction action = PdfAction.fromDictionary(dict, null);
        assertTrue(action instanceof HideAction);
    }

    // ── ExplicitDestination types ──

    @Test
    public void testXYZDestination() {
        Page page = createPage();
        XYZExplicitDestination dest = new XYZExplicitDestination(page, 72, 720, 1.5);
        assertEquals(72.0, dest.getLeft());
        assertEquals(720.0, dest.getTop());
        assertEquals(1.5, dest.getZoom());
        assertSame(page, dest.getPage());
    }

    @Test
    public void testXYZToPdfArray() {
        Page page = createPage();
        XYZExplicitDestination dest = new XYZExplicitDestination(page, 100, 200, 2.0);
        PdfArray arr = dest.toPdfArray();
        assertNotNull(arr);
        assertEquals(5, arr.size());
        assertEquals("XYZ", ((PdfName) arr.get(1)).getName());
    }

    @Test
    public void testFitDestination() {
        Page page = createPage();
        FitExplicitDestination dest = new FitExplicitDestination(page);
        PdfArray arr = dest.toPdfArray();
        assertEquals(2, arr.size());
        assertEquals("Fit", ((PdfName) arr.get(1)).getName());
    }

    @Test
    public void testFitHDestination() {
        Page page = createPage();
        FitHExplicitDestination dest = new FitHExplicitDestination(page, 500);
        assertEquals(500.0, dest.getTop());
        PdfArray arr = dest.toPdfArray();
        assertEquals(3, arr.size());
        assertEquals("FitH", ((PdfName) arr.get(1)).getName());
    }

    @Test
    public void testFitVDestination() {
        Page page = createPage();
        FitVExplicitDestination dest = new FitVExplicitDestination(page, 72);
        assertEquals(72.0, dest.getLeft());
    }

    @Test
    public void testFitRDestination() {
        Page page = createPage();
        FitRExplicitDestination dest = new FitRExplicitDestination(page, 0, 0, 200, 300);
        assertEquals(0.0, dest.getLeft());
        assertEquals(300.0, dest.getTop());
        PdfArray arr = dest.toPdfArray();
        assertEquals(6, arr.size());
    }

    @Test
    public void testFitBDestination() {
        Page page = createPage();
        FitBExplicitDestination dest = new FitBExplicitDestination(page);
        PdfArray arr = dest.toPdfArray();
        assertEquals(2, arr.size());
        assertEquals("FitB", ((PdfName) arr.get(1)).getName());
    }

    @Test
    public void testParseXYZFromArray() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfNull.INSTANCE); // page ref
        arr.add(PdfName.of("XYZ"));
        arr.add(PdfInteger.valueOf(100));
        arr.add(PdfInteger.valueOf(700));
        arr.add(new PdfFloat(1.5));

        ExplicitDestination dest = ExplicitDestination.fromPdfArray(arr, null);
        assertNotNull(dest);
        assertTrue(dest instanceof XYZExplicitDestination);
        XYZExplicitDestination xyz = (XYZExplicitDestination) dest;
        assertEquals(100.0, xyz.getLeft());
        assertEquals(700.0, xyz.getTop());
        assertEquals(1.5, xyz.getZoom(), 0.01);
    }

    @Test
    public void testParseFitFromArray() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfNull.INSTANCE);
        arr.add(PdfName.of("Fit"));

        ExplicitDestination dest = ExplicitDestination.fromPdfArray(arr, null);
        assertTrue(dest instanceof FitExplicitDestination);
    }

    @Test
    public void testParseFitHFromArray() throws IOException {
        PdfArray arr = new PdfArray();
        arr.add(PdfNull.INSTANCE);
        arr.add(PdfName.of("FitH"));
        arr.add(PdfInteger.valueOf(500));

        ExplicitDestination dest = ExplicitDestination.fromPdfArray(arr, null);
        assertTrue(dest instanceof FitHExplicitDestination);
        assertEquals(500.0, ((FitHExplicitDestination) dest).getTop());
    }

    @Test
    public void testParseNullArray() throws IOException {
        assertNull(ExplicitDestination.fromPdfArray(null, null));
    }

    @Test
    public void testParseEmptyArray() throws IOException {
        assertNull(ExplicitDestination.fromPdfArray(new PdfArray(), null));
    }
}
