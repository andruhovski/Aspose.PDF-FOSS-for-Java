package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.text.TextFragment;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for [PdfPageEditor] zoom + movePosition functionality.
public class PdfPageEditorZoomTests {

    @Test
    public void setZoom_storesValue() {
        PdfPageEditor editor = new PdfPageEditor();
        editor.setZoom(0.5f);
        assertEquals(0.5f, editor.getZoom(), 0.0001f);
    }

    @Test
    public void setZoom_rejectsNonPositive() {
        PdfPageEditor editor = new PdfPageEditor();
        editor.setZoom(-1.0f);
        assertEquals(1.0f, editor.getZoom(), 0.0001f, "Negative zoom must be ignored");
        editor.setZoom(0f);
        assertEquals(1.0f, editor.getZoom(), 0.0001f, "Zero zoom must be ignored");
    }

    @Test
    public void zoom_appliedOnSave_emitsCmMatrixInContentStream() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfPageEditor editor = new PdfPageEditor()) {
            editor.bindPdf(new ByteArrayInputStream(inBytes));
            editor.setZoom(0.5f);
            assertTrue(editor.save(out), "save() must succeed");
        }
        assertTrue(out.size() > 0, "Output must not be empty");

        // Reopen and inspect the content stream of page 1 — the operator list
        // should contain a `q  0.5 0 0 0.5 0 0 cm` prefix and a `Q` suffix.
        try (Document loaded = new Document(new ByteArrayInputStream(out.toByteArray()))) {
            org.aspose.pdf.OperatorCollection ops = loaded.getPages().get(1).getContents();
            assertTrue(ops.size() >= 3, "Need at least q + cm + Q");
            assertTrue(ops.getAt(0) instanceof org.aspose.pdf.operators.GSave,
                    "First op must be q. Was: " + ops.getAt(0));
            assertTrue(ops.getAt(1) instanceof org.aspose.pdf.operators.ConcatenateMatrix,
                    "Second op must be cm. Was: " + ops.getAt(1));
            assertTrue(ops.getAt(ops.size() - 1) instanceof org.aspose.pdf.operators.GRestore,
                    "Last op must be Q. Was: " + ops.getAt(ops.size() - 1));
            org.aspose.pdf.Matrix m =
                    ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
            assertEquals(0.5, m.getA(), 1e-6, "scaleX");
            assertEquals(0.5, m.getD(), 1e-6, "scaleY");
            assertEquals(0.0, m.getE(), 1e-6, "tx");
            assertEquals(0.0, m.getF(), 1e-6, "ty");
        }
    }

    @Test
    public void movePosition_appliedOnSave_emitsTranslationInContentStream() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfPageEditor editor = new PdfPageEditor()) {
            editor.bindPdf(new ByteArrayInputStream(inBytes));
            editor.movePosition(15, 25);
            assertTrue(editor.save(out));
        }

        try (Document loaded = new Document(new ByteArrayInputStream(out.toByteArray()))) {
            org.aspose.pdf.OperatorCollection ops = loaded.getPages().get(1).getContents();
            org.aspose.pdf.Matrix m =
                    ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
            assertEquals(1.0, m.getA(), 1e-6);
            assertEquals(1.0, m.getD(), 1e-6);
            assertEquals(15.0, m.getE(), 1e-6);
            assertEquals(25.0, m.getF(), 1e-6);
        }
    }

    @Test
    public void zoomAndMove_combineIntoSingleMatrix() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfPageEditor editor = new PdfPageEditor()) {
            editor.bindPdf(new ByteArrayInputStream(inBytes));
            editor.setZoom(0.5f);
            editor.movePosition(10, 20);
            assertTrue(editor.save(out));
        }
        // Combined matrix: a=0.5 d=0.5 e=10*0.5=5 f=20*0.5=10
        try (Document loaded = new Document(new ByteArrayInputStream(out.toByteArray()))) {
            org.aspose.pdf.OperatorCollection ops = loaded.getPages().get(1).getContents();
            org.aspose.pdf.Matrix m =
                    ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
            assertEquals(0.5, m.getA(), 1e-6);
            assertEquals(0.5, m.getD(), 1e-6);
            assertEquals(5.0, m.getE(), 1e-6);
            assertEquals(10.0, m.getF(), 1e-6);
        }
    }

    @Test
    public void zoom_processPagesFilter_appliesOnlyToTargetedPages() throws Exception {
        byte[] inBytes = buildTwoPageDocBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfPageEditor editor = new PdfPageEditor()) {
            editor.bindPdf(new ByteArrayInputStream(inBytes));
            editor.setProcessPages(new int[]{2});
            editor.setZoom(0.5f);
            assertTrue(editor.save(out));
        }
        try (Document loaded = new Document(new ByteArrayInputStream(out.toByteArray()))) {
            assertTrue(!pageHasZoom(loaded.getPages().get(1)),
                    "Page 1 must not have zoom cm");
            assertTrue(pageHasZoom(loaded.getPages().get(2)),
                    "Page 2 must have zoom cm");
        }
    }

    /// True iff the page starts with q + ConcatenateMatrix(0.5,0,0,0.5,...).
    private static boolean pageHasZoom(Page page) throws java.io.IOException {
        org.aspose.pdf.OperatorCollection ops = page.getContents();
        if (ops.size() < 3) return false;
        if (!(ops.getAt(0) instanceof org.aspose.pdf.operators.GSave)) return false;
        if (!(ops.getAt(1) instanceof org.aspose.pdf.operators.ConcatenateMatrix)) return false;
        org.aspose.pdf.Matrix m =
                ((org.aspose.pdf.operators.ConcatenateMatrix) ops.getAt(1)).getMatrix();
        return Math.abs(m.getA() - 0.5) < 1e-6 && Math.abs(m.getD() - 0.5) < 1e-6;
    }

    @Test
    public void getPageSize_returnsExpectedDimensions() throws Exception {
        byte[] inBytes = buildOnePageDocBytes();
        try (PdfPageEditor editor = new PdfPageEditor()) {
            editor.bindPdf(new ByteArrayInputStream(inBytes));
            float[] size = editor.getPageSize(1);
            assertTrue(size != null && size.length == 2);
            assertTrue(size[0] > 0 && size[1] > 0);
        }
    }

    private static byte[] buildOnePageDocBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getParagraphs().add(new TextFragment("zoom-test"));
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
}
