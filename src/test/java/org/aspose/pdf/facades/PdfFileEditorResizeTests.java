package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.text.TextFragment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for [PdfFileEditor]`.resizeContents(...)`.
public class PdfFileEditorResizeTests {

    @TempDir
    Path tempDir;

    @Test
    public void resizeContents_units_emitsScaleAndTranslate() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();
        try (Document doc = new Document(new ByteArrayInputStream(inBytes))) {
            double pageW = doc.getPages().get(1).getMediaBox().getWidth();
            double pageH = doc.getPages().get(1).getMediaBox().getHeight();
            // sumX = lm+cw+rm = 50+(pageW-100)+50 = pageW (exact), no auto-adjust
            // sumY = 50+(pageH-100)+50 = pageH (exact), no auto-adjust
            double cw = pageW - 100;
            double ch = pageH - 100;
            PdfFileEditor.ContentsResizeParameters params = new PdfFileEditor.ContentsResizeParameters(
                    PdfFileEditor.ContentsResizeValue.units(50),
                    PdfFileEditor.ContentsResizeValue.units(cw),
                    PdfFileEditor.ContentsResizeValue.units(50),
                    PdfFileEditor.ContentsResizeValue.units(50),
                    PdfFileEditor.ContentsResizeValue.units(ch),
                    PdfFileEditor.ContentsResizeValue.units(50));
            new PdfFileEditor().resizeContents(doc, params);

            org.aspose.pdf.OperatorCollection ops = doc.getPages().get(1).getContents();
            assertTrue(ops.getAt(0) instanceof org.aspose.pdf.operators.GSave);
            org.aspose.pdf.Matrix m = ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
            assertEquals(cw / pageW, m.getA(), 1e-6, "scaleX");
            assertEquals(ch / pageH, m.getD(), 1e-6, "scaleY");
            assertEquals(50.0, m.getE(), 1e-6, "tx = leftMargin");
            assertEquals(50.0, m.getF(), 1e-6, "ty = bottomMargin");
            assertTrue(ops.getAt(ops.size() - 1) instanceof org.aspose.pdf.operators.GRestore);
        }
    }

    @Test
    public void resizeContents_percents_resolvedAgainstPageDimensions() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();
        try (Document doc = new Document(new ByteArrayInputStream(inBytes))) {
            double pageH = doc.getPages().get(1).getMediaBox().getHeight();
            // 0% lm + 50% cw + 50% rm = 100% (sums to pageWidth). 0% tm + 50% ch + 50% bm = 100%.
            // scaleX = 0.5 (50% of width), tx = 0 (left margin = 0)
            // scaleY = 0.5, ty = bottomMargin = 50% of pageHeight
            PdfFileEditor.ContentsResizeParameters params = new PdfFileEditor.ContentsResizeParameters(
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50));
            new PdfFileEditor().resizeContents(doc, params);

            org.aspose.pdf.OperatorCollection ops = doc.getPages().get(1).getContents();
            org.aspose.pdf.Matrix m = ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
            assertEquals(0.5, m.getA(), 1e-6, "scaleX");
            assertEquals(0.5, m.getD(), 1e-6, "scaleY");
            assertEquals(0.0, m.getE(), 1e-6, "tx = leftMargin");
            assertEquals(pageH * 0.5, m.getF(), 1e-6, "ty = 50% bottom margin");
        }
    }

    @Test
    public void resizeContents_filePathOverload_writesToOutput() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();
        String inputPath = tempDir.resolve("resize_in.pdf").toString();
        String outputPath = tempDir.resolve("resize_out.pdf").toString();
        java.nio.file.Files.write(java.nio.file.Paths.get(inputPath), inBytes);

        PdfFileEditor.ContentsResizeParameters params = new PdfFileEditor.ContentsResizeParameters(
                PdfFileEditor.ContentsResizeValue.units(0),
                PdfFileEditor.ContentsResizeValue.percents(50),
                PdfFileEditor.ContentsResizeValue.units(0),
                PdfFileEditor.ContentsResizeValue.units(0),
                PdfFileEditor.ContentsResizeValue.percents(50),
                PdfFileEditor.ContentsResizeValue.units(0));

        assertTrue(new PdfFileEditor().resizeContents(inputPath, outputPath, params),
                "resizeContents(file, file, params) should succeed");
        assertTrue(java.nio.file.Files.size(java.nio.file.Paths.get(outputPath)) > 0,
                "Output file must not be empty");

        try (Document loaded = new Document(outputPath)) {
            org.aspose.pdf.OperatorCollection ops = loaded.getPages().get(1).getContents();
            assertTrue(ops.getAt(0) instanceof org.aspose.pdf.operators.GSave,
                    "First op must be q");
            assertTrue(ops.getAt(1) instanceof org.aspose.pdf.operators.ConcatenateMatrix,
                    "Second op must be cm");
        }
    }

    @Test
    public void resizeContents_pageNumbersFilter_appliesOnlyToTargeted() throws Exception {
        byte[] inBytes = buildTwoPageDocBytes();
        try (Document doc = new Document(new ByteArrayInputStream(inBytes))) {
            PdfFileEditor.ContentsResizeParameters params = new PdfFileEditor.ContentsResizeParameters(
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50));
            new PdfFileEditor().resizeContents(doc, new int[]{2}, params);

            assertTrue(!pageHasHalfScale(doc.getPages().get(1)),
                    "Page 1 should not be resized");
            assertTrue(pageHasHalfScale(doc.getPages().get(2)),
                    "Page 2 should be resized to 0.5 scale");
        }
    }

    private static boolean pageHasHalfScale(Page page) throws java.io.IOException {
        org.aspose.pdf.OperatorCollection ops = page.getContents();
        if (ops.size() < 3) return false;
        if (!(ops.getAt(0) instanceof org.aspose.pdf.operators.GSave)) return false;
        if (!(ops.getAt(1) instanceof org.aspose.pdf.operators.ConcatenateMatrix)) return false;
        org.aspose.pdf.Matrix m =
                ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
        return Math.abs(m.getA() - 0.5) < 1e-6 && Math.abs(m.getD() - 0.5) < 1e-6;
    }

    @Test
    public void resizeContents_nullArgs_doesNotThrow() {
        PdfFileEditor editor = new PdfFileEditor();
        // Null document — silent no-op (logs WARNING but does not throw).
        editor.resizeContents((Document) null,
                new PdfFileEditor.ContentsResizeParameters(null, null, null, null, null, null));
        // Null parameters — also silent no-op.
        editor.resizeContents((Document) null, (PdfFileEditor.ContentsResizeParameters) null);
    }

    private static byte[] buildOnePageDocBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getParagraphs().add(new TextFragment("resize-test"));
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildTwoPageDocBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document()) {
            doc.getPages().add().getParagraphs().add(new TextFragment("p1"));
            doc.getPages().add().getParagraphs().add(new TextFragment("p2"));
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    private static String truncate(String s) {
        return s.length() > 200 ? s.substring(0, 200) + "…" : s;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ContentsResizeParameters.pageResize(width, height) factory
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void pageResize_factoryCreatesParametersWithGivenDimensions() {
        PdfFileEditor.ContentsResizeParameters params =
                PdfFileEditor.ContentsResizeParameters.pageResize(400, 600);
        assertEquals(400.0, params.getContentsWidth().getValue(), 0.001);
        assertEquals(600.0, params.getContentsHeight().getValue(), 0.001);
        assertTrue(!params.getContentsWidth().isPercent(),
                "pageResize uses absolute units, not percent");
        // All margins should be zero (absolute units).
        assertEquals(0.0, params.getLeftMargin().getValue(), 0.001);
        assertEquals(0.0, params.getRightMargin().getValue(), 0.001);
        assertEquals(0.0, params.getTopMargin().getValue(), 0.001);
        assertEquals(0.0, params.getBottomMargin().getValue(), 0.001);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bug #7 — annotation coords transformed on resizeContents
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void resizeContents_transformsAnnotationRect() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            org.aspose.pdf.annotations.HighlightAnnotation h =
                    new org.aspose.pdf.annotations.HighlightAnnotation(page,
                            new org.aspose.pdf.Rectangle(100, 200, 200, 250));
            page.getAnnotations().add(h);

            // Default page is A4 (595×842). lm=0%, cw=50%, rm=50%, tm=50%, ch=50%, bm=0%
            // → scaleX=scaleY=0.5, tx=ty=0 (and sums match page exactly so no auto-adjust).
            PdfFileEditor editor = new PdfFileEditor();
            PdfFileEditor.ContentsResizeParameters params =
                    new PdfFileEditor.ContentsResizeParameters(
                            PdfFileEditor.ContentsResizeValue.percents(0),
                            PdfFileEditor.ContentsResizeValue.percents(50),
                            PdfFileEditor.ContentsResizeValue.percents(50),
                            PdfFileEditor.ContentsResizeValue.percents(50),
                            PdfFileEditor.ContentsResizeValue.percents(50),
                            PdfFileEditor.ContentsResizeValue.percents(0));
            editor.resizeContents(doc, params);

            org.aspose.pdf.annotations.HighlightAnnotation h2 =
                    (org.aspose.pdf.annotations.HighlightAnnotation)
                            page.getAnnotations().get(1);
            org.aspose.pdf.Rectangle r = h2.getRect();
            assertEquals(50.0, r.getLLX(), 0.5, "LLX should be 100 * 0.5");
            assertEquals(100.0, r.getLLY(), 0.5, "LLY should be 200 * 0.5");
            assertEquals(100.0, r.getURX(), 0.5, "URX should be 200 * 0.5");
            assertEquals(125.0, r.getURY(), 0.5, "URY should be 250 * 0.5");
        }
    }

    @Test
    public void resizeContents_transformsQuadPoints() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            org.aspose.pdf.annotations.HighlightAnnotation h =
                    new org.aspose.pdf.annotations.HighlightAnnotation(page,
                            new org.aspose.pdf.Rectangle(100, 100, 200, 150));
            page.getAnnotations().add(h);
            double[] qpBefore = h.getQuadPoints();
            assertNotNull(qpBefore);

            PdfFileEditor editor = new PdfFileEditor();
            // Default page is A4 (595×842). For pure 0.5× scale with ZERO
            // translation we need ty=bm=0, so put the right margin / top margin
            // on the OUTER edges instead of the inner ones. percents work because
            // 50%+50%+0 = 100% which matches the page exactly (no auto-adjust).
            editor.resizeContents(doc, new PdfFileEditor.ContentsResizeParameters(
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(0)));

            double[] qpAfter = ((org.aspose.pdf.annotations.HighlightAnnotation)
                    page.getAnnotations().get(1)).getQuadPoints();
            assertNotNull(qpAfter);
            assertEquals(qpBefore.length, qpAfter.length);
            for (int i = 0; i < qpBefore.length; i++) {
                assertEquals(qpBefore[i] * 0.5, qpAfter[i], 0.5,
                        "QuadPoint[" + i + "] should be scaled by 0.5");
            }
        }
    }

    @Test
    public void resizeContents_transformsInkList() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            org.aspose.pdf.annotations.InkAnnotation ink =
                    new org.aspose.pdf.annotations.InkAnnotation(page,
                            new org.aspose.pdf.Rectangle(0, 0, 100, 100));
            java.util.List<double[]> strokes = new java.util.ArrayList<>();
            strokes.add(new double[] {10, 20, 30, 40, 50, 60});
            ink.setInkList(strokes);
            page.getAnnotations().add(ink);

            PdfFileEditor editor = new PdfFileEditor();
            // Default page is A4 (595×842). For pure 0.5× scale with ZERO
            // translation we need ty=bm=0, so put the right margin / top margin
            // on the OUTER edges instead of the inner ones. percents work because
            // 50%+50%+0 = 100% which matches the page exactly (no auto-adjust).
            editor.resizeContents(doc, new PdfFileEditor.ContentsResizeParameters(
                    PdfFileEditor.ContentsResizeValue.percents(0),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(50),
                    PdfFileEditor.ContentsResizeValue.percents(0)));

            java.util.List<double[]> after = ((org.aspose.pdf.annotations.InkAnnotation)
                    page.getAnnotations().get(1)).getInkList();
            assertEquals(1, after.size());
            double[] s = after.get(0);
            assertEquals(5.0,  s[0], 0.5);
            assertEquals(10.0, s[1], 0.5);
            assertEquals(15.0, s[2], 0.5);
            assertEquals(20.0, s[3], 0.5);
            assertEquals(25.0, s[4], 0.5);
            assertEquals(30.0, s[5], 0.5);
        }
    }

    @Test
    public void pageResize_marginsCanBeSetAfter() {
        PdfFileEditor.ContentsResizeParameters params =
                PdfFileEditor.ContentsResizeParameters.pageResize(400, 600);
        params.setLeftMargin(PdfFileEditor.ContentsResizeValue.units(50));
        params.setRightMargin(PdfFileEditor.ContentsResizeValue.units(40));
        params.setTopMargin(PdfFileEditor.ContentsResizeValue.units(30));
        params.setBottomMargin(PdfFileEditor.ContentsResizeValue.units(20));
        assertEquals(50.0, params.getLeftMargin().getValue(), 0.001);
        assertEquals(40.0, params.getRightMargin().getValue(), 0.001);
        assertEquals(30.0, params.getTopMargin().getValue(), 0.001);
        assertEquals(20.0, params.getBottomMargin().getValue(), 0.001);
    }
}
