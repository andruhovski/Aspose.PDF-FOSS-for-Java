package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XForm}.
 */
public class XFormTest {

    @Test
    public void testGetBBox() {
        COSStream stream = new COSStream();
        COSArray bbox = new COSArray();
        bbox.add(COSInteger.valueOf(0));
        bbox.add(COSInteger.valueOf(0));
        bbox.add(COSInteger.valueOf(200));
        bbox.add(COSInteger.valueOf(100));
        stream.set(COSName.of("BBox"), bbox);

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
        COSStream stream = new COSStream();
        COSArray matrix = new COSArray();
        matrix.add(new COSFloat(2));
        matrix.add(COSInteger.valueOf(0));
        matrix.add(COSInteger.valueOf(0));
        matrix.add(new COSFloat(2));
        matrix.add(new COSFloat(50));
        matrix.add(new COSFloat(100));
        stream.set(COSName.of("Matrix"), matrix);

        XForm form = new XForm(stream, "Fm1", null);
        Matrix m = form.getMatrix();
        assertNotNull(m);
        assertEquals(2.0, m.getA(), 0.01);
        assertEquals(50.0, m.getE(), 0.01);
        assertEquals(100.0, m.getF(), 0.01);
    }

    @Test
    public void testDefaultMatrix() {
        COSStream stream = new COSStream();
        XForm form = new XForm(stream, "Fm1", null);
        Matrix m = form.getMatrix();
        assertEquals(Matrix.IDENTITY, m);
    }

    @Test
    public void testGetResources() {
        COSStream stream = new COSStream();
        COSDictionary res = new COSDictionary();
        res.set(COSName.of("Font"), new COSDictionary());
        stream.set(COSName.of("Resources"), res);

        XForm form = new XForm(stream, "Fm1", null);
        Resources r = form.getResources();
        assertNotNull(r);
        assertNotNull(r.getFonts());
    }

    @Test
    public void testNullResources() {
        COSStream stream = new COSStream();
        XForm form = new XForm(stream, "Fm1", null);
        assertNull(form.getResources());
    }

    @Test
    public void testGetContents() throws IOException {
        byte[] content = "q 1 0 0 1 0 0 cm Q".getBytes();
        COSStream stream = new COSStream(content);
        XForm form = new XForm(stream, "Fm1", null);

        OperatorCollection ops = form.getContents();
        assertNotNull(ops);
        assertTrue(ops.size() >= 3); // q, cm, Q
    }

    @Test
    public void testGetName() {
        COSStream stream = new COSStream();
        XForm form = new XForm(stream, "TestForm", null);
        assertEquals("TestForm", form.getName());
    }

    @Test
    public void testNoBBox() {
        COSStream stream = new COSStream();
        XForm form = new XForm(stream, "Fm1", null);
        assertNull(form.getBBox());
    }
}
