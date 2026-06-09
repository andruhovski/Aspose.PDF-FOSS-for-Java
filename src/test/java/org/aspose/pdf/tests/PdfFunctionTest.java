package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;
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
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("FunctionType"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("N"), new PdfFloat(1.0f));
        PdfArray domain = new PdfArray();
        domain.add(PdfInteger.valueOf(0)); domain.add(PdfInteger.valueOf(1));
        dict.set(PdfName.of("Domain"), domain);
        PdfArray c0 = new PdfArray(); c0.add(new PdfFloat(0f));
        PdfArray c1 = new PdfArray(); c1.add(new PdfFloat(1f));
        dict.set(PdfName.of("C0"), c0);
        dict.set(PdfName.of("C1"), c1);

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
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("FunctionType"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("N"), new PdfFloat(1.0f));
        PdfArray domain = new PdfArray();
        domain.add(new PdfFloat(0f)); domain.add(new PdfFloat(1f));
        dict.set(PdfName.of("Domain"), domain);

        PdfFunction f = PdfFunction.parse(dict, null);
        assertNotNull(f);
        assertTrue(f instanceof ExponentialFunction);
    }

    @org.junit.jupiter.api.Test
    public void postScriptInputsPushedInOrder() {
        // ISO 32000 7.10.5.1: first input deepest, LAST input on top.
        // exch swaps [first, last] -> [last, first]; pop drops first ->
        // the LAST input (0.75) remains. The old reversed push order
        // yielded 0.25 here (and swapped DeviceN colorants in 29077.pdf).
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1, 0, 1}, new double[]{0, 1}, "{ exch pop }");
        double[] r = f.evaluate(new double[]{0.25, 0.75});
        assertEquals(0.75, r[0], 1e-6);
    }

    @org.junit.jupiter.api.Test
    public void postScriptRoll() {
        // 1 2 3 -> "3 1 roll" -> 3 1 2
        PostScriptFunction f = new PostScriptFunction(
                new double[]{}, new double[]{0, 10, 0, 10, 0, 10},
                "{ 1 2 3 3 1 roll }");
        double[] r = f.evaluate(new double[]{});
        assertEquals(3.0, r[0], 1e-6);
        assertEquals(1.0, r[1], 1e-6);
        assertEquals(2.0, r[2], 1e-6);
    }

    @org.junit.jupiter.api.Test
    public void postScriptRollNegative() {
        // 1 2 3 -> "3 -1 roll" -> 2 3 1
        PostScriptFunction f = new PostScriptFunction(
                new double[]{}, new double[]{0, 10, 0, 10, 0, 10},
                "{ 1 2 3 3 -1 roll }");
        double[] r = f.evaluate(new double[]{});
        assertEquals(2.0, r[0], 1e-6);
        assertEquals(3.0, r[1], 1e-6);
        assertEquals(1.0, r[2], 1e-6);
    }

    @org.junit.jupiter.api.Test
    public void postScriptCvrCvi() {
        // 3 cvr -> 3.0; 2.7 cvi -> 2; sum 5
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 10}, new double[]{0, 10}, "{ cvr 2.7 cvi add }");
        assertEquals(5.0, f.evaluate(new double[]{3.0})[0], 1e-6);
    }

    @org.junit.jupiter.api.Test
    public void postScriptIfTrueExecutesBody() {
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1}, new double[]{0, 10},
                "{ 0.6 gt { 5 } if }");
        assertEquals(5.0, f.evaluate(new double[]{0.9})[0], 1e-6);
    }

    @org.junit.jupiter.api.Test
    public void postScriptIfElse() {
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1}, new double[]{0, 10},
                "{ 0.5 lt { 1 } { 2 } ifelse }");
        assertEquals(1.0, f.evaluate(new double[]{0.2})[0], 1e-6);
        assertEquals(2.0, f.evaluate(new double[]{0.8})[0], 1e-6);
    }

    /**
     * The exact DeviceN [Cyan Magenta] -> CMYK tint transform from corpus
     * 29077.pdf (llPDFLib): expected output is [c, m, 0, 0]. With roll
     * unimplemented the result degenerated to [1,1,1,1] = black label.
     */
    @org.junit.jupiter.api.Test
    public void postScript29077TintTransform() {
        String prog = "{1 index 1.000000 cvr exch sub 3 1 roll 0 index 1.000000 cvr exch sub "
                + "3 1 roll 1.000000 3 1 roll 1.000000 3 1 roll 6 -1 roll 1.000000 "
                + "cvr exch sub 6 1 roll 5 -1 roll 1.000000 cvr exch sub 5 1 "
                + "roll 4 -1 roll 1.000000 cvr exch sub 4 1 roll 3 -1 roll 1.000000 "
                + "cvr exch sub 3 1 roll pop pop }";
        PostScriptFunction f = new PostScriptFunction(
                new double[]{0, 1, 0, 1}, new double[]{0, 1, 0, 1, 0, 1, 0, 1}, prog);
        double[] r = f.evaluate(new double[]{0.96, 0.69});
        assertEquals(0.96, r[0], 1e-6, "C");
        assertEquals(0.69, r[1], 1e-6, "M");
        assertEquals(0.0,  r[2], 1e-6, "Y");
        assertEquals(0.0,  r[3], 1e-6, "K");
    }
}
