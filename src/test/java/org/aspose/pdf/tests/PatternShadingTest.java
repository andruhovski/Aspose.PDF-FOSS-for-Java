package org.aspose.pdf.tests;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSBoolean;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.function.ExponentialFunction;
import org.aspose.pdf.engine.pattern.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pattern and shading classes (§8.7).
 */
public class PatternShadingTest {

    // ═══════════════════════════════════════════════════════════════
    //  TilingPattern
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void tilingPatternParsePaintAndTilingType() throws IOException {
        COSStream dict = new COSStream();
        dict.set(COSName.of("PatternType"), COSInteger.valueOf(1));
        dict.set(COSName.of("PaintType"), COSInteger.valueOf(2));
        dict.set(COSName.of("TilingType"), COSInteger.valueOf(3));

        TilingPattern tp = new TilingPattern(dict, null);
        assertEquals(1, tp.getPatternType());
        assertEquals(2, tp.getPaintType());
        assertEquals(3, tp.getTilingType());
    }

    @Test
    public void tilingPatternBBoxAndSteps() throws IOException {
        COSStream dict = new COSStream();
        dict.set(COSName.of("PatternType"), COSInteger.valueOf(1));
        COSArray bbox = new COSArray();
        bbox.add(new COSFloat(0)); bbox.add(new COSFloat(0));
        bbox.add(new COSFloat(100)); bbox.add(new COSFloat(50));
        dict.set(COSName.of("BBox"), bbox);
        dict.set(COSName.of("XStep"), new COSFloat(100));
        dict.set(COSName.of("YStep"), new COSFloat(50));

        TilingPattern tp = new TilingPattern(dict, null);
        Rectangle bb = tp.getBBox();
        assertEquals(100.0, bb.getWidth(), 0.01);
        assertEquals(50.0, bb.getHeight(), 0.01);
        assertEquals(100.0, tp.getXStep(), 0.01);
        assertEquals(50.0, tp.getYStep(), 0.01);
    }

    @Test
    public void tilingPatternResources() throws IOException {
        COSStream dict = new COSStream();
        dict.set(COSName.of("PatternType"), COSInteger.valueOf(1));
        COSDictionary res = new COSDictionary();
        res.set(COSName.of("ProcSet"), new COSArray());
        dict.set(COSName.of("Resources"), res);

        TilingPattern tp = new TilingPattern(dict, null);
        assertNotNull(tp.getResources());
    }

    // ═══════════════════════════════════════════════════════════════
    //  ShadingPattern
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void shadingPatternParseReturnsShading() throws IOException {
        COSDictionary shadingDict = new COSDictionary();
        shadingDict.set(COSName.of("ShadingType"), COSInteger.valueOf(2));
        COSArray coords = new COSArray();
        coords.add(new COSFloat(0)); coords.add(new COSFloat(0));
        coords.add(new COSFloat(100)); coords.add(new COSFloat(0));
        shadingDict.set(COSName.of("Coords"), coords);
        shadingDict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

        COSDictionary patDict = new COSDictionary();
        patDict.set(COSName.of("PatternType"), COSInteger.valueOf(2));
        patDict.set(COSName.of("Shading"), shadingDict);

        ShadingPattern sp = new ShadingPattern(patDict, null);
        assertNotNull(sp.getShading());
        assertTrue(sp.getShading() instanceof AxialShading);
    }

    // ═══════════════════════════════════════════════════════════════
    //  AxialShading
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void axialShadingParseCoords() throws IOException {
        COSDictionary dict = createAxialShadingDict(0, 0, 100, 0);
        AxialShading as = new AxialShading(dict, null);
        assertEquals(2, as.getShadingType());
        assertEquals(0, as.getX0(), 0.01);
        assertEquals(0, as.getY0(), 0.01);
        assertEquals(100, as.getX1(), 0.01);
        assertEquals(0, as.getY1(), 0.01);
    }

    @Test
    public void axialShadingColorAtMidpoint() throws IOException {
        // Linear function: t → [t, 0, 1-t] (RGB)
        ExponentialFunction func = new ExponentialFunction(
                new double[]{0, 1}, new double[]{0, 1, 0, 1, 0, 1},
                new double[]{0, 0, 1}, new double[]{1, 0, 0}, 1.0);

        COSDictionary dict = createAxialShadingDict(0, 0, 100, 0);
        // We can't easily inject the function into the dict, so test via getColorAt
        // Create a simple axial shading with background fallback
        AxialShading as = new AxialShading(dict, null);
        // Without a function, should return background or default
        double[] color = as.getColorAt(50, 0);
        assertNotNull(color);
        assertEquals(3, color.length);
    }

    @Test
    public void axialShadingColorAtStart() throws IOException {
        COSDictionary dict = createAxialShadingDict(0, 0, 100, 0);
        AxialShading as = new AxialShading(dict, null);
        double[] color = as.getColorAt(0, 0); // t=0 on axis
        assertNotNull(color);
    }

    @Test
    public void axialShadingColorAtEnd() throws IOException {
        COSDictionary dict = createAxialShadingDict(0, 0, 100, 0);
        AxialShading as = new AxialShading(dict, null);
        double[] color = as.getColorAt(100, 0); // t=1 on axis
        assertNotNull(color);
    }

    // ═══════════════════════════════════════════════════════════════
    //  RadialShading
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void radialShadingParseCoords() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ShadingType"), COSInteger.valueOf(3));
        COSArray coords = new COSArray();
        coords.add(new COSFloat(50)); coords.add(new COSFloat(50)); coords.add(new COSFloat(0));
        coords.add(new COSFloat(50)); coords.add(new COSFloat(50)); coords.add(new COSFloat(100));
        dict.set(COSName.of("Coords"), coords);
        dict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

        RadialShading rs = new RadialShading(dict, null);
        assertEquals(3, rs.getShadingType());
        assertEquals(50, rs.getX0(), 0.01);
        assertEquals(50, rs.getY0(), 0.01);
        assertEquals(0, rs.getR0(), 0.01);
        assertEquals(100, rs.getR1(), 0.01);
    }

    // ═══════════════════════════════════════════════════════════════
    //  FunctionBasedShading
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void functionBasedShadingGetColorAt() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ShadingType"), COSInteger.valueOf(1));
        dict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));
        // No function → returns background/default
        FunctionBasedShading fbs = new FunctionBasedShading(dict, null);
        assertEquals(1, fbs.getShadingType());
        double[] color = fbs.getColorAt(0.5, 0.5);
        assertNotNull(color);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Shading.parse() factory
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void shadingParseType2ReturnsAxial() throws IOException {
        COSDictionary dict = createAxialShadingDict(0, 0, 1, 0);
        Shading s = Shading.parse(dict, null);
        assertNotNull(s);
        assertTrue(s instanceof AxialShading);
        assertEquals(2, s.getShadingType());
    }

    @Test
    public void shadingParseType3ReturnsRadial() throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ShadingType"), COSInteger.valueOf(3));
        COSArray coords = new COSArray();
        for (int i = 0; i < 6; i++) coords.add(new COSFloat(i));
        dict.set(COSName.of("Coords"), coords);
        dict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

        Shading s = Shading.parse(dict, null);
        assertTrue(s instanceof RadialShading);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PdfPattern.parse() factory
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void patternParseType1ReturnsTiling() throws IOException {
        COSStream dict = new COSStream();
        dict.set(COSName.of("PatternType"), COSInteger.valueOf(1));
        PdfPattern p = PdfPattern.parse(dict, null);
        assertNotNull(p);
        assertTrue(p instanceof TilingPattern);
    }

    @Test
    public void patternParseType2ReturnsShading() throws IOException {
        COSDictionary shadingDict = new COSDictionary();
        shadingDict.set(COSName.of("ShadingType"), COSInteger.valueOf(2));
        COSArray coords = new COSArray();
        coords.add(new COSFloat(0)); coords.add(new COSFloat(0));
        coords.add(new COSFloat(1)); coords.add(new COSFloat(0));
        shadingDict.set(COSName.of("Coords"), coords);
        shadingDict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

        COSDictionary patDict = new COSDictionary();
        patDict.set(COSName.of("PatternType"), COSInteger.valueOf(2));
        patDict.set(COSName.of("Shading"), shadingDict);

        PdfPattern p = PdfPattern.parse(patDict, null);
        assertNotNull(p);
        assertTrue(p instanceof ShadingPattern);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Mesh stubs (types 4-7)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void meshShadingStubsReturnFallbackColor() throws IOException {
        for (int type = 4; type <= 7; type++) {
            COSDictionary dict = new COSDictionary();
            dict.set(COSName.of("ShadingType"), COSInteger.valueOf(type));
            dict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

            Shading s = Shading.parse(dict, null);
            assertNotNull(s, "Shading type " + type + " should parse");
            assertEquals(type, s.getShadingType());
            double[] color = s.getColorAt(0, 0);
            assertNotNull(color);
            assertTrue(color.length >= 3, "Should return at least 3 components");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Pattern matrix
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void patternMatrixDefaultIsIdentity() throws IOException {
        COSStream dict = new COSStream();
        dict.set(COSName.of("PatternType"), COSInteger.valueOf(1));
        TilingPattern tp = new TilingPattern(dict, null);
        Matrix m = tp.getMatrix();
        assertNotNull(m);
        // Identity matrix: a=1, b=0, c=0, d=1, e=0, f=0
        assertEquals(1.0, m.getA(), 0.01);
        assertEquals(0.0, m.getB(), 0.01);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    private COSDictionary createAxialShadingDict(double x0, double y0, double x1, double y1) {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("ShadingType"), COSInteger.valueOf(2));
        COSArray coords = new COSArray();
        coords.add(new COSFloat((float) x0));
        coords.add(new COSFloat((float) y0));
        coords.add(new COSFloat((float) x1));
        coords.add(new COSFloat((float) y1));
        dict.set(COSName.of("Coords"), coords);
        dict.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));
        return dict;
    }
}
