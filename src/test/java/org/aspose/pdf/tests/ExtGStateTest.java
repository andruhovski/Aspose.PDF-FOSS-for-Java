package org.aspose.pdf.tests;

import org.aspose.pdf.ExtGState;
import org.aspose.pdf.engine.cos.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ExtGState}.
 */
public class ExtGStateTest {

    @Test
    public void testStrokingAlpha() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("CA"), new COSFloat(0.5));
        ExtGState gs = new ExtGState(dict);
        assertEquals(0.5, gs.getStrokingAlpha(), 0.01);
    }

    @Test
    public void testNonStrokingAlpha() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ca"), new COSFloat(0.75));
        ExtGState gs = new ExtGState(dict);
        assertEquals(0.75, gs.getNonStrokingAlpha(), 0.01);
    }

    @Test
    public void testDefaultAlpha() {
        COSDictionary dict = new COSDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals(1.0, gs.getStrokingAlpha(), 0.01);
        assertEquals(1.0, gs.getNonStrokingAlpha(), 0.01);
    }

    @Test
    public void testBlendMode() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("BM"), COSName.of("Multiply"));
        ExtGState gs = new ExtGState(dict);
        assertEquals("Multiply", gs.getBlendMode());
    }

    @Test
    public void testDefaultBlendMode() {
        COSDictionary dict = new COSDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals("Normal", gs.getBlendMode());
    }

    @Test
    public void testLineWidth() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("LW"), new COSFloat(2.5));
        ExtGState gs = new ExtGState(dict);
        assertEquals(2.5, gs.getLineWidth(), 0.01);
    }

    @Test
    public void testLineCap() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("LC"), COSInteger.valueOf(1));
        ExtGState gs = new ExtGState(dict);
        assertEquals(1, gs.getLineCap());
    }

    @Test
    public void testLineJoin() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("LJ"), COSInteger.valueOf(2));
        ExtGState gs = new ExtGState(dict);
        assertEquals(2, gs.getLineJoin());
    }

    @Test
    public void testOverprint() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("OP"), COSBoolean.TRUE);
        ExtGState gs = new ExtGState(dict);
        assertTrue(gs.getOverprint());
    }

    @Test
    public void testDefaultOverprint() {
        COSDictionary dict = new COSDictionary();
        ExtGState gs = new ExtGState(dict);
        assertFalse(gs.getOverprint());
    }

    @Test
    public void testNullDictThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ExtGState(null));
    }

    @Test
    public void testGetCOSDictionary() {
        COSDictionary dict = new COSDictionary();
        ExtGState gs = new ExtGState(dict);
        assertSame(dict, gs.getCOSDictionary());
    }

    @Test
    public void testMiterLimit() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ML"), new COSFloat(10));
        ExtGState gs = new ExtGState(dict);
        assertEquals(10.0, gs.getMiterLimit(), 0.01);
    }

    @Test
    public void testDefaultLineWidth() {
        COSDictionary dict = new COSDictionary();
        ExtGState gs = new ExtGState(dict);
        assertEquals(-1.0, gs.getLineWidth(), 0.01);
    }
}
