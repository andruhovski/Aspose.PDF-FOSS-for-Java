package org.aspose.pdf.tests;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.aspose.pdf.operators.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for typed operator subclasses and ContentStreamParser typed output.
 */
public class TypedOperatorsTest {

    // ---- No-operand operators ----

    @Test
    public void testGSaveCreation() {
        GSave op = new GSave();
        assertEquals("q", op.getName());
        assertTrue(op.getOperands().isEmpty());
        assertTrue(op instanceof Operator);
    }

    @Test
    public void testGRestoreCreation() {
        GRestore op = new GRestore();
        assertEquals("Q", op.getName());
    }

    @Test
    public void testBTCreation() {
        BT op = new BT();
        assertEquals("BT", op.getName());
        assertTrue(op instanceof BlockTextOperator);
        assertTrue(op instanceof TextOperator);
    }

    @Test
    public void testETCreation() {
        ET op = new ET();
        assertEquals("ET", op.getName());
        assertTrue(op instanceof BlockTextOperator);
    }

    @Test
    public void testStrokeCreation() {
        Stroke op = new Stroke();
        assertEquals("S", op.getName());
    }

    @Test
    public void testFillCreation() {
        Fill op = new Fill();
        assertEquals("f", op.getName());
    }

    @Test
    public void testEOFillCreation() {
        EOFill op = new EOFill();
        assertEquals("f*", op.getName());
    }

    @Test
    public void testClipCreation() {
        Clip op = new Clip();
        assertEquals("W", op.getName());
    }

    @Test
    public void testClosePathCreation() {
        ClosePath op = new ClosePath();
        assertEquals("h", op.getName());
    }

    @Test
    public void testEndPathCreation() {
        EndPath op = new EndPath();
        assertEquals("n", op.getName());
    }

    @Test
    public void testMoveToNextLineCreation() {
        MoveToNextLine op = new MoveToNextLine();
        assertEquals("T*", op.getName());
        assertTrue(op instanceof TextPlaceOperator);
    }

    @Test
    public void testEMCCreation() {
        EMC op = new EMC();
        assertEquals("EMC", op.getName());
    }

    // ---- Text operators ----

    @Test
    public void testSelectFontCreation() {
        SelectFont op = new SelectFont("F1", 12);
        assertEquals("Tf", op.getName());
        assertEquals("F1", op.getFontName());
        assertEquals(12.0, op.getSize(), 0.01);
        assertTrue(op instanceof TextStateOperator);
    }

    @Test
    public void testShowTextCreation() {
        ShowText op = new ShowText("Hello");
        assertEquals("Tj", op.getName());
        assertEquals("Hello", op.getText());
        assertTrue(op instanceof TextShowOperator);
    }

    @Test
    public void testShowTextSetText() {
        ShowText op = new ShowText("old");
        op.setText("new");
        assertEquals("new", op.getText());
    }

    @Test
    public void testSetCharacterSpacing() {
        SetCharacterSpacing op = new SetCharacterSpacing(1.5);
        assertEquals("Tc", op.getName());
        assertEquals(1.5, op.getCharSpace(), 0.01);
    }

    @Test
    public void testSetWordSpacing() {
        SetWordSpacing op = new SetWordSpacing(2.0);
        assertEquals("Tw", op.getName());
        assertEquals(2.0, op.getWordSpace(), 0.01);
    }

    @Test
    public void testSetHorizontalTextScaling() {
        SetHorizontalTextScaling op = new SetHorizontalTextScaling(150);
        assertEquals("Tz", op.getName());
        assertEquals(150.0, op.getScale(), 0.01);
    }

    @Test
    public void testSetTextLeading() {
        SetTextLeading op = new SetTextLeading(14);
        assertEquals("TL", op.getName());
        assertEquals(14.0, op.getLeading(), 0.01);
    }

    @Test
    public void testSetTextRenderingMode() {
        SetTextRenderingMode op = new SetTextRenderingMode(2);
        assertEquals("Tr", op.getName());
        assertEquals(2, op.getMode());
    }

    @Test
    public void testSetTextRise() {
        SetTextRise op = new SetTextRise(5);
        assertEquals("Ts", op.getName());
        assertEquals(5.0, op.getRise(), 0.01);
    }

    @Test
    public void testMoveTextPosition() {
        MoveTextPosition op = new MoveTextPosition(100, 700);
        assertEquals("Td", op.getName());
        assertEquals(100.0, op.getX(), 0.01);
        assertEquals(700.0, op.getY(), 0.01);
        assertTrue(op instanceof TextPlaceOperator);
    }

    @Test
    public void testMoveTextPositionSetLeading() {
        MoveTextPositionSetLeading op = new MoveTextPositionSetLeading(0, -14);
        assertEquals("TD", op.getName());
        assertEquals(0.0, op.getX(), 0.01);
        assertEquals(-14.0, op.getY(), 0.01);
    }

    @Test
    public void testSetTextMatrix() {
        Matrix m = new Matrix(1, 0, 0, 1, 100, 200);
        SetTextMatrix op = new SetTextMatrix(m);
        assertEquals("Tm", op.getName());
        assertNotNull(op.getMatrix());
        assertEquals(100, op.getMatrix().getE(), 0.01);
        assertEquals(200, op.getMatrix().getF(), 0.01);
    }

    // ---- Path operators ----

    @Test
    public void testMoveToCreation() {
        MoveTo op = new MoveTo(10, 20);
        assertEquals("m", op.getName());
        assertEquals(10.0, op.getX(), 0.01);
        assertEquals(20.0, op.getY(), 0.01);
    }

    @Test
    public void testLineToCreation() {
        LineTo op = new LineTo(30, 40);
        assertEquals("l", op.getName());
        assertEquals(30.0, op.getX(), 0.01);
        assertEquals(40.0, op.getY(), 0.01);
    }

    @Test
    public void testReCreation() {
        Re op = new Re(10, 20, 100, 50);
        assertEquals("re", op.getName());
        assertEquals(10.0, op.getX(), 0.01);
        assertEquals(20.0, op.getY(), 0.01);
        assertEquals(100.0, op.getWidth(), 0.01);
        assertEquals(50.0, op.getHeight(), 0.01);
    }

    @Test
    public void testCurveToCreation() {
        CurveTo op = new CurveTo(1, 2, 3, 4, 5, 6);
        assertEquals("c", op.getName());
        assertEquals(1.0, op.getX1(), 0.01);
        assertEquals(6.0, op.getY3(), 0.01);
    }

    // ---- Graphics state operators ----

    @Test
    public void testConcatenateMatrixCreation() {
        ConcatenateMatrix op = new ConcatenateMatrix(1, 0, 0, 1, 100, 200);
        assertEquals("cm", op.getName());
        assertNotNull(op.getMatrix());
        assertEquals(1.0, op.getMatrix().getA(), 0.01);
        assertEquals(100.0, op.getMatrix().getE(), 0.01);
    }

    @Test
    public void testConcatenateMatrixFromMatrix() {
        Matrix m = new Matrix(2, 0, 0, 2, 50, 50);
        ConcatenateMatrix op = new ConcatenateMatrix(m);
        assertEquals(2.0, op.getMatrix().getA(), 0.01);
        assertEquals(50.0, op.getMatrix().getE(), 0.01);
    }

    @Test
    public void testSetLineWidth() {
        SetLineWidth op = new SetLineWidth(2.5);
        assertEquals("w", op.getName());
        assertEquals(2.5, op.getWidth(), 0.01);
    }

    @Test
    public void testDoCreation() {
        Do op = new Do("Im1");
        assertEquals("Do", op.getName());
        assertEquals("Im1", op.getXObjectName());
    }

    @Test
    public void testGSCreation() {
        GS op = new GS("GS0");
        assertEquals("gs", op.getName());
        assertEquals("GS0", op.getDictName());
    }

    // ---- Color operators ----

    @Test
    public void testSetRGBColor() {
        SetRGBColor op = new SetRGBColor(1.0, 0.0, 0.0);
        assertEquals("rg", op.getName());
        assertEquals(1.0, op.getR(), 0.01);
        assertEquals(0.0, op.getG(), 0.01);
        assertEquals(0.0, op.getB(), 0.01);
        assertTrue(op instanceof SetColorOperator);
    }

    @Test
    public void testSetGray() {
        SetGray op = new SetGray(0.5);
        assertEquals("g", op.getName());
        assertEquals(0.5, op.getGray(), 0.01);
    }

    @Test
    public void testSetCMYKColor() {
        SetCMYKColor op = new SetCMYKColor(0.1, 0.2, 0.3, 0.4);
        assertEquals("k", op.getName());
        assertEquals(0.1, op.getC(), 0.01);
        assertEquals(0.4, op.getK(), 0.01);
    }

    @Test
    public void testSetColorSpace() {
        SetColorSpace op = new SetColorSpace("DeviceRGB");
        assertEquals("cs", op.getName());
        assertEquals("DeviceRGB", op.getColorSpaceName());
    }

    // ---- Marked content ----

    @Test
    public void testBMCCreation() {
        BMC op = new BMC("Span");
        assertEquals("BMC", op.getName());
        assertEquals("Span", op.getTag());
    }

    @Test
    public void testBDCCreation() {
        BDC op = new BDC("Span", org.aspose.pdf.engine.cos.COSName.of("Props1"));
        assertEquals("BDC", op.getName());
        assertEquals("Span", op.getTag());
        assertNotNull(op.getProperties());
    }

    // ---- Parser produces typed operators ----

    @Test
    public void testParserProducesTypedOperators() throws IOException {
        byte[] stream = "BT /F1 12 Tf (Hello) Tj ET".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertEquals(4, ops.size());
        assertTrue(ops.get(0) instanceof BT, "Expected BT, got: " + ops.get(0).getClass().getSimpleName());
        assertTrue(ops.get(1) instanceof SelectFont, "Expected SelectFont, got: " + ops.get(1).getClass().getSimpleName());
        assertTrue(ops.get(2) instanceof ShowText, "Expected ShowText, got: " + ops.get(2).getClass().getSimpleName());
        assertTrue(ops.get(3) instanceof ET, "Expected ET, got: " + ops.get(3).getClass().getSimpleName());
    }

    @Test
    public void testParserSelectFontValues() throws IOException {
        byte[] stream = "BT /F1 12 Tf ET".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        SelectFont sf = (SelectFont) ops.get(1);
        assertEquals("F1", sf.getFontName());
        assertEquals(12.0, sf.getSize(), 0.01);
    }

    @Test
    public void testParserShowTextValues() throws IOException {
        byte[] stream = "BT (Hello World) Tj ET".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        ShowText st = (ShowText) ops.get(1);
        assertEquals("Hello World", st.getText());
    }

    @Test
    public void testParserPathOperators() throws IOException {
        byte[] stream = "100 200 m 300 400 l 10 20 50 30 re S".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(0) instanceof MoveTo);
        assertTrue(ops.get(1) instanceof LineTo);
        assertTrue(ops.get(2) instanceof Re);
        assertTrue(ops.get(3) instanceof Stroke);

        MoveTo mt = (MoveTo) ops.get(0);
        assertEquals(100.0, mt.getX(), 0.01);
        assertEquals(200.0, mt.getY(), 0.01);

        Re re = (Re) ops.get(2);
        assertEquals(10.0, re.getX(), 0.01);
        assertEquals(50.0, re.getWidth(), 0.01);
    }

    @Test
    public void testParserGraphicsState() throws IOException {
        byte[] stream = "q 1 0 0 1 100 200 cm Q".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(0) instanceof GSave);
        assertTrue(ops.get(1) instanceof ConcatenateMatrix);
        assertTrue(ops.get(2) instanceof GRestore);

        ConcatenateMatrix cm = (ConcatenateMatrix) ops.get(1);
        assertNotNull(cm.getMatrix());
        assertEquals(100.0, cm.getMatrix().getE(), 0.01);
        assertEquals(200.0, cm.getMatrix().getF(), 0.01);
    }

    @Test
    public void testParserColorOperators() throws IOException {
        byte[] stream = "1 0 0 rg 0.5 G".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(0) instanceof SetRGBColor);
        assertTrue(ops.get(1) instanceof SetGrayStroke);

        SetRGBColor rgb = (SetRGBColor) ops.get(0);
        assertEquals(1.0, rgb.getR(), 0.01);
        assertEquals(0.0, rgb.getG(), 0.01);
        assertEquals(0.0, rgb.getB(), 0.01);
    }

    @Test
    public void testParserDoOperator() throws IOException {
        byte[] stream = "q /Im1 Do Q".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(1) instanceof Do);
        assertEquals("Im1", ((Do) ops.get(1)).getXObjectName());
    }

    @Test
    public void testParserMarkedContent() throws IOException {
        byte[] stream = "/Span BMC (text) Tj EMC".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(0) instanceof BMC);
        assertTrue(ops.get(1) instanceof ShowText);
        assertTrue(ops.get(2) instanceof EMC);
        assertEquals("Span", ((BMC) ops.get(0)).getTag());
    }

    @Test
    public void testParserTJOperator() throws IOException {
        byte[] stream = "BT [(H) 20 (ello)] TJ ET".getBytes();
        List<Operator> ops = ContentStreamParser.parse(stream);
        assertTrue(ops.get(1) instanceof SetGlyphsPositionShowText);
    }

    // ---- Backward compatibility ----

    @Test
    public void testOperatorBackwardCompatibility() {
        ShowText st = new ShowText("test");
        assertEquals("Tj", st.getName());
        assertEquals(1, st.getOperands().size());
    }

    @Test
    public void testTypedOperatorGetNameWorks() {
        GSave q = new GSave();
        assertEquals("q", q.getName());
        assertTrue(q.getOperands().isEmpty());

        SelectFont tf = new SelectFont("F1", 12);
        assertEquals("Tf", tf.getName());
        assertEquals(2, tf.getOperands().size());
    }

    @Test
    public void testTypedOperatorIsAlsoOperator() {
        Operator op = new GSave();
        assertEquals("q", op.getName());
        assertTrue(op instanceof GSave);
    }

    // ---- Matrix additions ----

    @Test
    public void testMatrixFromArray() {
        double[] vals = {1, 0, 0, 1, 50, 100};
        Matrix m = new Matrix(vals);
        assertEquals(1.0, m.getA());
        assertEquals(50.0, m.getE());
        assertEquals(100.0, m.getF());
    }

    @Test
    public void testMatrixFromArrayInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[]{1, 2, 3}));
        assertThrows(IllegalArgumentException.class, () -> new Matrix((double[]) null));
    }

    @Test
    public void testMatrixRotation() {
        Matrix m = Matrix.rotation(Math.PI / 2); // 90 degrees
        assertEquals(0.0, m.getA(), 0.001);
        assertEquals(1.0, m.getB(), 0.001);
        assertEquals(-1.0, m.getC(), 0.001);
        assertEquals(0.0, m.getD(), 0.001);
    }

    // ---- GlyphPosition ----

    @Test
    public void testGlyphPositionText() {
        GlyphPosition gp = new GlyphPosition("Hello");
        assertTrue(gp.isText());
        assertEquals("Hello", gp.getText());
        assertEquals(0, gp.getAdjustment(), 0.01);
    }

    @Test
    public void testGlyphPositionAdjustment() {
        GlyphPosition gp = new GlyphPosition(-50);
        assertFalse(gp.isText());
        assertEquals(-50, gp.getAdjustment(), 0.01);
    }

    @Test
    public void testGlyphPositionTextWithAdjustment() {
        GlyphPosition gp = new GlyphPosition("H", 20);
        assertTrue(gp.isText());
        assertEquals("H", gp.getText());
        assertEquals(20, gp.getAdjustment(), 0.01);
    }

    // ---- Full pipeline test ----

    @Test
    public void testFullContentStreamPipeline() throws IOException {
        String content = "q 1 0 0 1 72 720 cm BT /F1 14 Tf 0 0 Td (Hello, PDF!) Tj ET Q";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes());

        // q, cm, BT, Tf, Td, Tj, ET, Q = 8 operators
        assertEquals(8, ops.size());
        assertTrue(ops.get(0) instanceof GSave);
        assertTrue(ops.get(1) instanceof ConcatenateMatrix);
        assertTrue(ops.get(2) instanceof BT);
        assertTrue(ops.get(3) instanceof SelectFont);
        assertTrue(ops.get(4) instanceof MoveTextPosition);
        assertTrue(ops.get(5) instanceof ShowText);
        assertTrue(ops.get(6) instanceof ET);
        assertTrue(ops.get(7) instanceof GRestore);

        // Verify typed values
        ConcatenateMatrix cm = (ConcatenateMatrix) ops.get(1);
        assertEquals(72.0, cm.getMatrix().getE(), 0.01);
        assertEquals(720.0, cm.getMatrix().getF(), 0.01);

        SelectFont sf = (SelectFont) ops.get(3);
        assertEquals("F1", sf.getFontName());
        assertEquals(14.0, sf.getSize(), 0.01);

        ShowText st = (ShowText) ops.get(5);
        assertEquals("Hello, PDF!", st.getText());
    }

    // ---- Operators used in Aspose regression test patterns ----

    @Test
    public void testAsposeStyleOperatorCreation() {
        // Typical Aspose regression test pattern:
        // page.Contents.Add(new Operators.GSave());
        // page.Contents.Add(new Operators.ConcatenateMatrix(matrix));
        // page.Contents.Add(new Operators.SelectFont(resName, 14));
        // page.Contents.Add(new Operators.ShowText("Hello"));
        // page.Contents.Add(new Operators.GRestore());

        GSave gsave = new GSave();
        ConcatenateMatrix cm = new ConcatenateMatrix(new Matrix(1, 0, 0, 1, 100, 200));
        SelectFont tf = new SelectFont("F1", 14);
        ShowText tj = new ShowText("Hello");
        GRestore grestore = new GRestore();

        // Verify all have correct operator names for serialization
        assertEquals("q", gsave.getName());
        assertEquals("cm", cm.getName());
        assertEquals("Tf", tf.getName());
        assertEquals("Tj", tj.getName());
        assertEquals("Q", grestore.getName());

        // Verify instanceof checks work
        Operator op = tj;
        assertTrue(op instanceof ShowText);
        assertTrue(op instanceof TextShowOperator);
        assertTrue(op instanceof TextOperator);
        assertTrue(op instanceof Operator);
    }
}
