package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.annotations.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.facades.PdfAnnotationEditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for XFDF import and export functionality per XFDF Specification 3.0.
public class XfdfTest {

    // ==================== EXPORT TESTS ====================

    @Test
    public void testExportTextAnnotation() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        TextAnnotation annot = new TextAnnotation(page, new Rectangle(100, 200, 150, 250));
        annot.setContents("Note text");
        annot.setColor(Color.RED);
        annot.setName("test-annot-1");
        annot.setTitle("Author");
        annot.setCreationDate("D:20110225124935+02'00'");
        annot.setOpacity(0.8);
        annot.setSubject("Note");
        annot.setIcon("Check");
        annot.setState("Accepted");
        annot.setStateModel("Review");
        page.getAnnotations().add(annot);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("<xfdf"), "Should contain xfdf root element");
        assertTrue(xfdf.contains("<annots"), "Should contain annots element");
        assertTrue(xfdf.contains("<text"), "Should contain text annotation element");
        assertTrue(xfdf.contains("page=\"0\""), "Should have 0-based page index");
        assertTrue(xfdf.contains("name=\"test-annot-1\""), "Should have name attribute");
        assertTrue(xfdf.contains("#FF0000"), "Should contain red color hex");
        assertTrue(xfdf.contains("title=\"Author\""), "Should have title attribute");
        assertTrue(xfdf.contains("creationdate=\"D:20110225124935+02'00'\""), "Should have creationdate");
        assertTrue(xfdf.contains("opacity=\"0.8\""), "Should have opacity attribute");
        assertTrue(xfdf.contains("subject=\"Note\""), "Should have subject attribute");
        assertTrue(xfdf.contains("icon=\"Check\""), "Should have icon attribute");
        assertTrue(xfdf.contains("state=\"Accepted\""), "Should have state attribute");
        assertTrue(xfdf.contains("statemodel=\"Review\""), "Should have statemodel attribute");
        assertTrue(xfdf.contains("<contents>Note text</contents>"), "Should have contents child element");

        doc.close();
    }

    @Test
    public void testExportHighlightWithCoords() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        HighlightAnnotation hl = new HighlightAnnotation(page, new Rectangle(73, 463, 135, 477));
        hl.setColor(Color.fromRgb(1, 1, 0));
        hl.setQuadPoints(new double[]{76.74, 476.55, 132.15, 476.55, 76.74, 463.97, 132.15, 463.97});
        page.getAnnotations().add(hl);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("<highlight"), "Should contain highlight element");
        assertTrue(xfdf.contains("coords=\""), "Should have coords attribute");

        doc.close();
    }

    @Test
    public void testExportFlagsAsString() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        TextAnnotation annot = new TextAnnotation(page, new Rectangle(0, 0, 50, 50));
        annot.setFlags(0x04 | 0x08 | 0x10); // print,nozoom,norotate
        page.getAnnotations().add(annot);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("flags=\"print,nozoom,norotate\""),
                "Should export flags as comma-separated names, not integer. Got: " + xfdf);

        doc.close();
    }

    @Test
    public void testExportContentsAsChildElement() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        TextAnnotation annot = new TextAnnotation(page, new Rectangle(0, 0, 50, 50));
        annot.setContents("Test content");
        page.getAnnotations().add(annot);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("<contents>Test content</contents>"),
                "Contents should be a child element, not an attribute");

        doc.close();
    }

    @Test
    public void testExportInkAnnotation() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        InkAnnotation ink = new InkAnnotation(page, new Rectangle(0, 0, 300, 300));
        ink.setColor(Color.fromRgb(0, 0, 0));
        // Set ink list via COS
        PdfArray outerArray = new PdfArray();
        PdfArray stroke1 = new PdfArray();
        stroke1.add(new PdfFloat(100));
        stroke1.add(new PdfFloat(200));
        stroke1.add(new PdfFloat(150));
        stroke1.add(new PdfFloat(250));
        outerArray.add(stroke1);
        ink.getPdfDictionary().set(PdfName.of("InkList"), outerArray);
        page.getAnnotations().add(ink);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("<ink"), "Should contain ink element");
        assertTrue(xfdf.contains("<inklist>"), "Should contain inklist child element");
        assertTrue(xfdf.contains("<gesture>"), "Should contain gesture element");

        doc.close();
    }

    @Test
    public void testExportLineAnnotation() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        LineAnnotation line = new LineAnnotation(page, new Rectangle(100, 200, 300, 400), 100, 200, 300, 400);
        page.getAnnotations().add(line);

        String xfdf = exportToString(doc);

        assertTrue(xfdf.contains("<line"), "Should contain line element");
        assertTrue(xfdf.contains("start=\"100,200\""), "Should have start attribute");
        assertTrue(xfdf.contains("end=\"300,400\""), "Should have end attribute");

        doc.close();
    }

    // ==================== IMPORT TESTS ====================

    @Test
    public void testImportTextAnnotation() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:space=\"preserve\">\n"
                + "  <annots>\n"
                + "    <text page=\"0\" rect=\"156.331,721.265,175.332,738.266\"\n"
                + "          color=\"#46A2B9\" date=\"D:20110225130902+02'00'\"\n"
                + "          flags=\"print,nozoom,norotate\" name=\"unique-uuid\"\n"
                + "          title=\"AuthorName\" creationdate=\"D:20110225124935+02'00'\"\n"
                + "          opacity=\"0.804993\" subject=\"Note\" icon=\"Check\"\n"
                + "          state=\"Accepted\" statemodel=\"Review\">\n"
                + "      <contents>Plain text comment</contents>\n"
                + "    </text>\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();

        importFromString(doc, xfdf);

        Page page = doc.getPages().get(1);
        // text annotation + its auto-paired Popup (Adobe/Aspose XFDF-import semantics)
        assertEquals(2, page.getAnnotations().size());

        Annotation first = page.getAnnotations().get(1);
        assertEquals("Text", first.getSubtype());
        assertEquals("Plain text comment", first.getContents());
        assertEquals("unique-uuid", first.getName());

        // Verify rect
        Rectangle rect = first.getRect();
        assertNotNull(rect);
        assertEquals(156.331, rect.getLLX(), 0.01);

        // Verify color
        Color color = first.getColor();
        assertNotNull(color);

        // Verify flags parsed from string
        assertTrue(first.isPrint(), "Print flag should be set");
        assertTrue(first.isNoZoom(), "NoZoom flag should be set");
        assertTrue(first.isNoRotate(), "NoRotate flag should be set");

        // Verify markup properties
        assertTrue(first instanceof MarkupAnnotation);
        MarkupAnnotation markup = (MarkupAnnotation) first;
        assertEquals("AuthorName", markup.getTitle());
        assertEquals("D:20110225124935+02'00'", markup.getCreationDate());
        assertEquals(0.804993, markup.getOpacity(), 0.001);
        assertEquals("Note", markup.getSubject());

        // Verify text-specific properties
        assertTrue(first instanceof TextAnnotation);
        TextAnnotation textAnnot = (TextAnnotation) first;
        assertEquals("Check", textAnnot.getIcon());
        assertEquals("Accepted", textAnnot.getState());
        assertEquals("Review", textAnnot.getStateModel());

        doc.close();
    }

    @Test
    public void testImportHighlightWithCoords() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <highlight page=\"0\" rect=\"73,463,135,477\"\n"
                + "               coords=\"76.74,476.55,132.15,476.55,76.74,463.97,132.15,463.97\"\n"
                + "               color=\"#FFFF00\" title=\"Author\" opacity=\"0.5\" subject=\"Highlight\">\n"
                + "      <contents>Highlighted text note</contents>\n"
                + "    </highlight>\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        HighlightAnnotation hl = (HighlightAnnotation) doc.getPages().get(1).getAnnotations().get(1);
        assertEquals("Highlight", hl.getSubtype());
        assertEquals("Highlighted text note", hl.getContents());
        assertEquals("Author", hl.getTitle());
        assertEquals(0.5, hl.getOpacity(), 0.01);
        assertEquals("Highlight", hl.getSubject());

        double[] qp = hl.getQuadPoints();
        assertNotNull(qp, "QuadPoints should be set from coords attribute");
        assertEquals(8, qp.length);
        assertEquals(76.74, qp[0], 0.01);

        doc.close();
    }

    @Test
    public void testImportFlagsFromString() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <text page=\"0\" rect=\"0,0,50,50\" flags=\"print,locked\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        Annotation annot = doc.getPages().get(1).getAnnotations().get(1);
        assertTrue(annot.isPrint(), "Print flag should be set");
        assertTrue(annot.isLocked(), "Locked flag should be set");
        assertFalse(annot.isHidden(), "Hidden flag should not be set");

        doc.close();
    }

    @Test
    public void testImportFlagsFromInteger() throws IOException {
        // Backwards compatibility: integer flags should still work
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <text page=\"0\" rect=\"0,0,50,50\" flags=\"4\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        Annotation annot = doc.getPages().get(1).getAnnotations().get(1);
        assertEquals(4, annot.getFlags());
        assertTrue(annot.isPrint());

        doc.close();
    }

    @Test
    public void testImportInkAnnotation() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <ink page=\"0\" rect=\"0,0,400,400\" color=\"#000000\">\n"
                + "      <inklist>\n"
                + "        <gesture>100,200;150,250;200,300</gesture>\n"
                + "        <gesture>300,400;350,450</gesture>\n"
                + "      </inklist>\n"
                + "    </ink>\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        InkAnnotation ink = (InkAnnotation) doc.getPages().get(1).getAnnotations().get(1);
        assertEquals("Ink", ink.getSubtype());

        List<double[]> inkList = ink.getInkList();
        assertEquals(2, inkList.size(), "Should have 2 strokes");
        assertEquals(6, inkList.get(0).length, "First stroke should have 6 coordinates");
        assertEquals(100, inkList.get(0)[0], 0.01);
        assertEquals(200, inkList.get(0)[1], 0.01);

        doc.close();
    }

    @Test
    public void testImportSquigglyAnnotation() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <squiggly page=\"0\" rect=\"50,300,400,320\"\n"
                + "              coords=\"50,320,400,320,50,300,400,300\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        SquigglyAnnotation sq = (SquigglyAnnotation) doc.getPages().get(1).getAnnotations().get(1);
        assertEquals("Squiggly", sq.getSubtype());
        double[] qp = sq.getQuadPoints();
        assertNotNull(qp);
        assertEquals(8, qp.length);

        doc.close();
    }

    @Test
    public void testImportPolygonAnnotation() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <polygon page=\"0\" rect=\"0,0,300,300\">\n"
                + "      <vertices>100,100,200,100,200,200,100,200</vertices>\n"
                + "    </polygon>\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        PolygonAnnotation poly = (PolygonAnnotation) doc.getPages().get(1).getAnnotations().get(1);
        assertEquals("Polygon", poly.getSubtype());
        double[] verts = poly.getVertices();
        assertNotNull(verts);
        assertEquals(8, verts.length);
        assertEquals(100, verts[0], 0.01);

        doc.close();
    }

    @Test
    public void testImportRedactAnnotation() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <redact page=\"0\" rect=\"50,50,200,80\" color=\"#000000\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        Annotation annot = doc.getPages().get(1).getAnnotations().get(1);
        assertEquals("Redact", annot.getSubtype());
        assertTrue(annot instanceof RedactionAnnotation);

        doc.close();
    }

    // ==================== ROUND-TRIP TESTS ====================

    @Test
    public void testRoundTrip() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        TextAnnotation text = new TextAnnotation(page, new Rectangle(10, 20, 30, 40));
        text.setContents("Round trip test");
        text.setColor(Color.BLUE);
        text.setName("rt-1");
        text.setTitle("Author");
        text.setOpacity(0.75);
        text.setSubject("Note");
        text.setIcon("Comment");
        text.setState("Rejected");
        text.setStateModel("Review");
        text.setFlags(0x04); // print
        page.getAnnotations().add(text);

        HighlightAnnotation hl = new HighlightAnnotation(page, new Rectangle(50, 60, 200, 70));
        hl.setContents("Highlighted");
        hl.setColor(Color.fromRgb(1, 1, 0));
        hl.setQuadPoints(new double[]{50, 70, 200, 70, 50, 60, 200, 60});
        page.getAnnotations().add(hl);

        // Export
        String xfdf = exportToString(doc);

        // Create new document and import
        Document doc2 = new Document();
        doc2.getPages().add();
        importFromString(doc2, xfdf);

        Page page2 = doc2.getPages().get(1);
        // 2 markup annotations + their auto-paired Popups
        assertEquals(4, page2.getAnnotations().size(), "Should have 2 annotations + 2 popups after round trip");

        // Verify text annotation
        TextAnnotation imported1 = (TextAnnotation) page2.getAnnotations().get(1);
        assertEquals("Text", imported1.getSubtype());
        assertEquals("Round trip test", imported1.getContents());
        assertEquals("rt-1", imported1.getName());
        assertEquals("Author", imported1.getTitle());
        assertEquals(0.75, imported1.getOpacity(), 0.01);
        assertEquals("Note", imported1.getSubject());
        assertEquals("Comment", imported1.getIcon());
        assertEquals("Rejected", imported1.getState());
        assertEquals("Review", imported1.getStateModel());
        assertTrue(imported1.isPrint());

        // Verify highlight annotation with coords (index 3: the Text's Popup sits at 2)
        HighlightAnnotation imported2 = (HighlightAnnotation) page2.getAnnotations().get(3);
        assertEquals("Highlight", imported2.getSubtype());
        assertEquals("Highlighted", imported2.getContents());
        double[] qp = imported2.getQuadPoints();
        assertNotNull(qp);
        assertEquals(8, qp.length);
        assertEquals(50, qp[0], 0.01);

        doc.close();
        doc2.close();
    }

    @Test
    public void testRoundTripLine() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        LineAnnotation line = new LineAnnotation(page, new Rectangle(100, 200, 300, 400), 100, 200, 300, 400);
        line.setContents("Line note");
        page.getAnnotations().add(line);

        String xfdf = exportToString(doc);

        Document doc2 = new Document();
        doc2.getPages().add();
        importFromString(doc2, xfdf);

        Annotation imported = doc2.getPages().get(1).getAnnotations().get(1);
        assertEquals("Line", imported.getSubtype());

        // Check line coordinates were imported via COS
        PdfArray l = (PdfArray) imported.getPdfDictionary().get("L");
        assertNotNull(l, "Line coordinates should be imported");
        assertEquals(100, l.getFloat(0, 0), 0.01);
        assertEquals(200, l.getFloat(1, 0), 0.01);
        assertEquals(300, l.getFloat(2, 0), 0.01);
        assertEquals(400, l.getFloat(3, 0), 0.01);

        doc.close();
        doc2.close();
    }

    // ==================== DOCUMENT CONVENIENCE TESTS ====================

    @Test
    public void testExportViaDocument() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();
        TextAnnotation annot = new TextAnnotation(page, new Rectangle(0, 0, 50, 50));
        annot.setContents("Test");
        page.getAnnotations().add(annot);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.exportAnnotationsToXfdf(baos);
        String xfdf = baos.toString("UTF-8");
        assertTrue(xfdf.contains("<text"), "Export via Document should work");

        doc.close();
    }

    @Test
    public void testImportViaDocument() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <square page=\"0\" rect=\"10,10,50,50\" color=\"#00FF00\">\n"
                + "      <contents>Green box</contents>\n"
                + "    </square>\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();

        importFromString(doc, xfdf);

        // markup annotation + its auto-paired Popup
        assertEquals(2, doc.getPages().get(1).getAnnotations().size());
        assertEquals("Square", doc.getPages().get(1).getAnnotations().get(1).getSubtype());
        assertEquals("Green box", doc.getPages().get(1).getAnnotations().get(1).getContents());

        doc.close();
    }

    // ==================== PDFANNOTATIONEDITOR XFDF TESTS ====================

    @Test
    public void testPdfAnnotationEditorExportXfdf() throws IOException {
        Document doc = new Document();
        Page page1 = doc.getPages().add();
        Page page2 = doc.getPages().add();

        TextAnnotation t1 = new TextAnnotation(page1, new Rectangle(10, 10, 50, 50));
        t1.setContents("Page 1 text");
        page1.getAnnotations().add(t1);

        HighlightAnnotation h2 = new HighlightAnnotation(page2, new Rectangle(20, 20, 100, 30));
        h2.setContents("Page 2 highlight");
        page2.getAnnotations().add(h2);

        PdfAnnotationEditor editor = new PdfAnnotationEditor();
        editor.bindPdf(doc);

        // Export only page 1
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        editor.exportAnnotationsXfdf(baos, 1, 1, null);
        String xfdf = baos.toString("UTF-8");

        assertTrue(xfdf.contains("<text"), "Should contain text annotation from page 1");
        assertFalse(xfdf.contains("<highlight"), "Should NOT contain highlight from page 2");

        // Export only highlights from all pages
        baos.reset();
        editor.exportAnnotationsXfdf(baos, 1, 2, new AnnotationType[]{AnnotationType.Highlight});
        xfdf = baos.toString("UTF-8");

        assertFalse(xfdf.contains("<text"), "Should NOT contain text when filtered to Highlight");
        assertTrue(xfdf.contains("<highlight"), "Should contain highlight");

        editor.close();
    }

    @Test
    public void testPdfAnnotationEditorImportXfdf() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <text page=\"0\" rect=\"10,10,50,50\">\n"
                + "      <contents>Imported via editor</contents>\n"
                + "    </text>\n"
                + "    <highlight page=\"0\" rect=\"60,60,200,70\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();

        PdfAnnotationEditor editor = new PdfAnnotationEditor();
        editor.bindPdf(doc);

        ByteArrayInputStream bais = new ByteArrayInputStream(xfdf.getBytes(StandardCharsets.UTF_8));
        editor.importAnnotationFromXfdf(bais);

        // 2 markup annotations + their auto-paired Popups
        assertEquals(4, editor.getDocument().getPages().get(1).getAnnotations().size());

        // Import with type filter
        Document doc2 = new Document();
        doc2.getPages().add();
        PdfAnnotationEditor editor2 = new PdfAnnotationEditor();
        editor2.bindPdf(doc2);

        bais = new ByteArrayInputStream(xfdf.getBytes(StandardCharsets.UTF_8));
        editor2.importAnnotationFromXfdf(bais, new AnnotationType[]{AnnotationType.Text});

        // the filtered Text annotation + its auto-paired Popup
        assertEquals(2, editor2.getDocument().getPages().get(1).getAnnotations().size(),
                "Should only import Text annotations (plus their popups) when filtered");
        assertEquals("Text", editor2.getDocument().getPages().get(1).getAnnotations().get(1).getSubtype());

        editor.close();
        editor2.close();
    }

    // ==================== UTILITY / EDGE CASE TESTS ====================

    @Test
    public void testEscapeXml() {
        assertEquals("a &amp; b", XfdfExporter.escapeXml("a & b"));
        assertEquals("&lt;tag&gt;", XfdfExporter.escapeXml("<tag>"));
        assertEquals("&quot;quoted&quot;", XfdfExporter.escapeXml("\"quoted\""));
    }

    @Test
    public void testUnescapeXml() {
        assertEquals("a & b", XfdfImporter.unescapeXml("a &amp; b"));
        assertEquals("<tag>", XfdfImporter.unescapeXml("&lt;tag&gt;"));
        assertEquals("\"quoted\"", XfdfImporter.unescapeXml("&quot;quoted&quot;"));
    }

    @Test
    public void testFlagsToString() {
        assertEquals("print", XfdfExporter.flagsToString(0x04));
        assertEquals("print,nozoom,norotate", XfdfExporter.flagsToString(0x04 | 0x08 | 0x10));
        assertEquals("", XfdfExporter.flagsToString(0));
        assertEquals("invisible,hidden", XfdfExporter.flagsToString(0x03));
    }

    @Test
    public void testParseFlagsString() {
        assertEquals(0x04, XfdfImporter.parseFlagsString("print"));
        assertEquals(0x04 | 0x08 | 0x10, XfdfImporter.parseFlagsString("print,nozoom,norotate"));
        assertEquals(4, XfdfImporter.parseFlagsString("4")); // backwards compat
        assertEquals(0, XfdfImporter.parseFlagsString(""));
    }

    @Test
    public void testParseHexColor() {
        Color c = XfdfImporter.parseHexColor("#FF0000");
        assertNotNull(c);
        assertEquals(1.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
        assertEquals(0.0, c.getB(), 0.01);

        // Extended format
        Color c2 = XfdfImporter.parseHexColor("#FFFF00000000");
        assertNotNull(c2);
        assertEquals(1.0, c2.getR(), 0.01);
        assertEquals(0.0, c2.getG(), 0.01);
    }

    @Test
    public void testImportMultipleAnnotationTypes() throws IOException {
        String xfdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\">\n"
                + "  <annots>\n"
                + "    <text page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <highlight page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <underline page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <strikeout page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <squiggly page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <freetext page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <line page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <circle page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <square page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <ink page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <stamp page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <caret page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <polygon page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <polyline page=\"0\" rect=\"0,0,50,50\" />\n"
                + "    <redact page=\"0\" rect=\"0,0,50,50\" />\n"
                + "  </annots>\n"
                + "</xfdf>";

        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        // 15 imported annotations + an auto-paired Popup for each markup type
        // except FreeText (Adobe/Aspose XFDF-import semantics) = 15 + 14
        assertEquals(29, doc.getPages().get(1).getAnnotations().size(),
                "Should import all 15 annotation types (+14 popups)");

        doc.close();
    }

    // ==================== HELPERS ====================

    private String exportToString(Document doc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XfdfExporter.export(doc, baos);
        return baos.toString("UTF-8");
    }

    @Test
    public void testImportPairsMarkupWithPopup() throws IOException {
        String xfdf = "<?xml version=\"1.0\"?><xfdf>"
                + "<annots>"
                + "<square page=\"0\" rect=\"10,700,110,760\" name=\"sq1\"><contents>box</contents></square>"
                + "<freetext page=\"0\" rect=\"10,600,110,660\" name=\"ft1\"><contents>label</contents></freetext>"
                + "</annots></xfdf>";
        Document doc = new Document();
        doc.getPages().add();
        importFromString(doc, xfdf);

        Page page = doc.getPages().get(1);
        // square + its popup + freetext (freetext displays its own text: no popup)
        assertEquals(3, page.getAnnotations().size());
        SquareAnnotation sq = (SquareAnnotation) page.getAnnotations().get(1);
        Annotation popup = page.getAnnotations().get(2);
        assertEquals("Popup", popup.getSubtype());
        assertNotNull(sq.getPopup(), "square must be linked to its popup via /Popup");
        assertEquals("Popup", sq.getPopup().getSubtype());
        assertEquals(sq.getPdfDictionary(),
                ((PopupAnnotation) popup).getParent().getPdfDictionary(),
                "popup /Parent must point back to the square");
        assertEquals("FreeText", page.getAnnotations().get(3).getSubtype());
        // popup is top-aligned with its parent in the right page margin
        assertEquals(760.0, popup.getRect().getURY(), 0.01);
        assertTrue(popup.getRect().getLLX() >= page.getRect().getURX() - 0.01);
        doc.close();
    }

    private void importFromString(Document doc, String xfdf) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(xfdf.getBytes(StandardCharsets.UTF_8));
        XfdfImporter.importXfdf(doc, bais);
    }
}
