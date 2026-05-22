package org.aspose.pdf.tests;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.function.ExponentialFunction;
import org.aspose.pdf.engine.function.PostScriptFunction;
import org.aspose.pdf.engine.function.StitchingFunction;
import org.aspose.pdf.engine.function.PdfFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PDF functions (§7.10).
 */
public class PdfFunctionTest {

    // ═══════════════════════════════════════════════════════════════
    //  ExponentialFunction (Type 2)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void exponentialLinearAtEndpoints() {
        // N=1 (linear): f(0) = C0, f(1) = C1
        ExponentialFunction f = new ExponentialFunction(
                new double[]{0, 1}, new double[]{0, 1, 0, 1, 0, 1},
                new double[]{0, 0, 0}, new double[]{1, 0.5, 0.8}, 1.0);
        double[] at0 = f.evaluate(new double[]{0});
        double[] at1 = f.evaluate(new double[]{1});
        assertArrayEquals(new double[]{0, 0, 0}, at0, 1e-6);
        assertArrayEquals(new double[]{1, 0.5, 0.8}, at1, 1e-6);
    }

    @Test
    public void exponentialLinearMidpoint() {
        // N=1, f(0.5) = (C0 + C1)/2
        ExponentialFunction f = new ExponentialFunction(
                new double[]{0, 1}, null,
                new double[]{0}, new double[]{1}, 1.0);
        double[] result = f.evaluate(new double[]{0.5});
        assertEquals(0.5, result[0], 1e-6);
    }

    @Test
    public void exponentialQuadratic() {
        // N=2: f(0.5) = C0 + 0.25 * (C1 - C0)
        ExponentialFunction f = new ExponentialFunction(
                new double[]{0, 1}, null,
                new double[]{0}, new double[]{1}, 2.0);
        double[] result = f.evaluate(new double[]{0.5});
        assertEquals(0.25, result[0], 1e-6);
    }

    @Test
    public void exponentialClampsInputToDomain() {
        ExponentialFunction f = new ExponentialFunction(
                new double[]{0, 1}, null,
                new double[]{10}, new double[]{20}, 1.0);
        double[] result = f.evaluate(new double[]{2.0}); // beyond domain
        assertEquals(20.0, result[0], 1e-6);
    }

    @Test
    public void exponentialFromDict() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("FunctionType"), COSInteger.valueOf(2));
        dict.set(COSName.of("N"), new COSFloat(1.0f));
        COSArray domain = new COSArray();
        domain.add(COSInteger.valueOf(0)); domain.add(COSInteger.valueOf(1));
        dict.set(COSName.of("Domain"), domain);
        COSArray c0 = new COSArray(); c0.add(new COSFloat(0f));
        COSArray c1 = new COSArray(); c1.add(new COSFloat(1f));
        dict.set(COSName.of("C0"), c0);
        dict.set(COSName.of("C1"), c1);

        ExponentialFunction f = new ExponentialFunction(dict,
                new double[]{0, 1}, new double[]{0, 1});
        double[] result = f.evaluate(new double[]{0.5});
        assertEquals(0.5, result[0], 1e-6);
    }

    // ═══════════════════════════════════════════════════════════════
    //  StitchingFunction (Type 3)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void stitchingTwoFunctions() {
        // Two linear subfunctions: [0,0.5) → func1, [0.5,1] → func2
        ExponentialFunction f1 = new ExponentialFunction(
                new double[]{0, 1}, null, new double[]{0}, new double[]{0.5}, 1.0);
        ExponentialFunction f2 = new ExponentialFunction(
                new double[]{0, 1}, null, new double[]{0.5}, new double[]{1.0}, 1.0);

        StitchingFunction sf = new StitchingFunction(
                new double[]{0, 1}, null,
                new PdfFunction[]{f1, f2},
                new double[]{0.5},
                new double[]{0, 1, 0, 1});

        double[] at0 = sf.evaluate(new double[]{0});
        assertEquals(0.0, at0[0], 1e-6);

        double[] atMid = sf.evaluate(new double[]{0.5});
        assertEquals(0.5, atMid[0], 1e-6);

        double[] at1 = sf.evaluate(new double[]{1.0});
        assertEquals(1.0, at1[0], 1e-6);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PostScriptFunction (Type 4)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void postScriptInvert() {
        // { 1 exch sub } → inverts the input
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1}, new double[]{0, 1}, "{ 1 exch sub }");
        double[] result = f.evaluate(new double[]{0.3});
        assertEquals(0.7, result[0], 1e-6);
    }

    @Test
    public void postScriptSquare() {
        // { dup mul } → squares the input
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1}, new double[]{0, 1}, "{ dup mul }");
        double[] result = f.evaluate(new double[]{0.5});
        assertEquals(0.25, result[0], 1e-6);
    }

    @Test
    public void postScriptNegation() {
        PostScriptFunction f = new PostScriptFunction(
                new double[]{-1, 1}, new double[]{-1, 1}, "{ neg }");
        double[] result = f.evaluate(new double[]{0.7});
        assertEquals(-0.7, result[0], 1e-6);
    }

    @Test
    public void postScriptMultiOutput() {
        // { dup 0.5 mul exch 0.3 mul } → two outputs from one input
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1}, new double[]{0, 1, 0, 1},
                "{ dup 0.5 mul exch 0.3 mul }");
        double[] result = f.evaluate(new double[]{1.0});
        assertEquals(2, result.length);
        // Stack after: 1.0 dup → 1.0 1.0; 0.5 mul → 1.0 0.5; exch → 0.5 1.0; 0.3 mul → 0.5 0.3
        // Pop 2 outputs: result[1]=0.3 (top), result[0]=0.5 (bottom)
        assertEquals(0.5, result[0], 1e-6);
        assertEquals(0.3, result[1], 1e-6);
    }

    @Test
    public void postScriptArithmetic() {
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 10}, new double[]{0, 100}, "{ dup mul }");
        double[] result = f.evaluate(new double[]{3.0});
        assertEquals(9.0, result[0], 1e-6);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PdfFunction.parse()
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void parseType2() throws Exception {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("FunctionType"), COSInteger.valueOf(2));
        dict.set(COSName.of("N"), new COSFloat(1.0f));
        COSArray domain = new COSArray();
        domain.add(new COSFloat(0f)); domain.add(new COSFloat(1f));
        dict.set(COSName.of("Domain"), domain);

        PdfFunction f = PdfFunction.parse(dict, null);
        assertNotNull(f);
        assertTrue(f instanceof ExponentialFunction);
    }
}
