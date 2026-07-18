package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [XForm].
public class XFormTest {

    @Test
    public void testGetBBox() {
        PdfStream stream = new PdfStream();
        PdfArray bbox = new PdfArray();
        bbox.add(PdfInteger.valueOf(0));
        bbox.add(PdfInteger.valueOf(0));
        bbox.add(PdfInteger.valueOf(200));
        bbox.add(PdfInteger.valueOf(100));
        stream.set(PdfName.of("BBox"), bbox);

        XForm form = new XForm(stream, "Fm1", null);
        Rectangle r = form.getBBox();
        assertNotNull(r);
        assertEquals(0, r.getLLX());
        assertEquals(0, r.getLLY());
        assertEquals(200, r.getURX());
        assertEquals(100, r.getURY());
    }

    @Test
    public void testGetMatrix() {
        PdfStream stream = new PdfStream();
        PdfArray matrix = new PdfArray();
        matrix.add(new PdfFloat(2));
        matrix.add(PdfInteger.valueOf(0));
        matrix.add(PdfInteger.valueOf(0));
        matrix.add(new PdfFloat(2));
        matrix.add(new PdfFloat(50));
        matrix.add(new PdfFloat(100));
        stream.set(PdfName.of("Matrix"), matrix);

        XForm form = new XForm(stream, "Fm1", null);
        Matrix m = form.getMatrix();
        assertNotNull(m);
        assertEquals(2.0, m.getA(), 0.01);
        assertEquals(50.0, m.getE(), 0.01);
        assertEquals(100.0, m.getF(), 0.01);
    }

    @Test
    public void testDefaultMatrix() {
        PdfStream stream = new PdfStream();
        XForm form = new XForm(stream, "Fm1", null);
        Matrix m = form.getMatrix();
        assertEquals(Matrix.IDENTITY, m);
    }

    @Test
    public void testGetResources() {
        PdfStream stream = new PdfStream();
        PdfDictionary res = new PdfDictionary();
        res.set(PdfName.of("Font"), new PdfDictionary());
        stream.set(PdfName.of("Resources"), res);

        XForm form = new XForm(stream, "Fm1", null);
        Resources r = form.getResources();
        assertNotNull(r);
        assertNotNull(r.getFonts());
    }

    @Test
    public void testNullResources() {
        PdfStream stream = new PdfStream();
        XForm form = new XForm(stream, "Fm1", null);
        assertNull(form.getResources());
    }

    @Test
    public void testGetContents() throws IOException {
        byte[] content = "q 1 0 0 1 0 0 cm Q".getBytes();
        PdfStream stream = new PdfStream(content);
        XForm form = new XForm(stream, "Fm1", null);

        OperatorCollection ops = form.getContents();
        assertNotNull(ops);
        assertTrue(ops.size() >= 3); // q, cm, Q
    }

    @Test
    public void testGetName() {
        PdfStream stream = new PdfStream();
        XForm form = new XForm(stream, "TestForm", null);
        assertEquals("TestForm", form.getName());
    }

    @Test
    public void testNoBBox() {
        PdfStream stream = new PdfStream();
        XForm form = new XForm(stream, "Fm1", null);
        assertNull(form.getBBox());
    }
}
