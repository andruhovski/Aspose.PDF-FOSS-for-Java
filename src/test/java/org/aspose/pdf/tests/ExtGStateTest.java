package org.aspose.pdf.tests;

import org.aspose.pdf.ExtGState;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ExtGState}.
 */
public class ExtGStateTest {

    @Test
    public void testStrokingAlpha() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("CA"), new PdfFloat(0.5));
        ExtGState gs = new ExtGState(dict);
        assertEquals(0.5, gs.getStrokingAlpha(), 0.01);
    }

    @Test
    public void testNonStrokingAlpha() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("ca"), new PdfFloat(0.75));
        ExtGState gs = new ExtGState(dict);
        assertEquals(0.75, gs.getNonStrokingAlpha(), 0.01);
    }

    @Test
    public void testDefaultAlpha() {
        PdfDictionary dict = new PdfDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals(1.0, gs.getStrokingAlpha(), 0.01);
        assertEquals(1.0, gs.getNonStrokingAlpha(), 0.01);
    }

    @Test
    public void testBlendMode() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("BM"), PdfName.of("Multiply"));
        ExtGState gs = new ExtGState(dict);
        assertEquals("Multiply", gs.getBlendMode());
    }

    @Test
    public void testDefaultBlendMode() {
        PdfDictionary dict = new PdfDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals("Normal", gs.getBlendMode());
    }

    @Test
    public void testLineWidth() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("LW"), new PdfFloat(2.5));
        ExtGState gs = new ExtGState(dict);
        assertEquals(2.5, gs.getLineWidth(), 0.01);
    }

    @Test
    public void testLineCap() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("LC"), PdfInteger.valueOf(1));
        ExtGState gs = new ExtGState(dict);
        assertEquals(1, gs.getLineCap());
    }

    @Test
    public void testLineJoin() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("LJ"), PdfInteger.valueOf(2));
        ExtGState gs = new ExtGState(dict);
        assertEquals(2, gs.getLineJoin());
    }

    @Test
    public void testOverprint() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("OP"), PdfBoolean.TRUE);
        ExtGState gs = new ExtGState(dict);
        assertTrue(gs.getOverprint());
    }

    @Test
    public void testDefaultOverprint() {
        PdfDictionary dict = new PdfDictionary();
        ExtGState gs = new ExtGState(dict);
        assertFalse(gs.getOverprint());
    }

    @Test
    public void testNullDictThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ExtGState(null));
    }

    @Test
    public void testGetPdfDictionary() {
        PdfDictionary dict = new PdfDictionary();
        ExtGState gs = new ExtGState(dict);
        assertSame(dict, gs.getPdfDictionary());
    }

    @Test
    public void testMiterLimit() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("ML"), new PdfFloat(10));
        ExtGState gs = new ExtGState(dict);
        assertEquals(10.0, gs.getMiterLimit(), 0.01);
    }

    @Test
    public void testDefaultLineWidth() {
        PdfDictionary dict = new PdfDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals(-1.0, gs.getLineWidth(), 0.01);
    }
}
