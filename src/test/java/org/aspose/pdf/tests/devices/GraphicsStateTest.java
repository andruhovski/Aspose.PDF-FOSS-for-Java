package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.render.GraphicsState;
import org.junit.jupiter.api.Test;

import java.awt.BasicStroke;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [GraphicsState].
public class GraphicsStateTest {

    @Test
    public void defaultsMatchPdfSpec() {
        GraphicsState gs = new GraphicsState();
        assertEquals(Matrix.IDENTITY, gs.getCTM());
        assertEquals(java.awt.Color.BLACK, gs.getFillColor());
        assertEquals(java.awt.Color.BLACK, gs.getStrokeColor());
        assertEquals(1.0, gs.getLineWidth());
        assertEquals(0, gs.getLineCap());
        assertEquals(0, gs.getLineJoin());
        assertEquals(10.0, gs.getMiterLimit());
        assertNull(gs.getDashArray());
        assertEquals(1.0f, gs.getStrokingAlpha());
        assertEquals(1.0f, gs.getNonStrokingAlpha());
        assertEquals(12, gs.getFontSize());
        assertEquals(0, gs.getCharSpacing());
        assertEquals(0, gs.getWordSpacing());
        assertEquals(100, gs.getHorizontalScaling());
        assertEquals(0, gs.getTextLeading());
        assertEquals(0, gs.getTextRenderingMode());
        assertEquals(0, gs.getTextRise());
    }

    @Test
    public void cloneIsIndependent() {
        GraphicsState gs = new GraphicsState();
        gs.setFillColorRGB(1, 0, 0);
        gs.setLineWidth(5);

        GraphicsState clone = gs.clone();
        assertEquals(5.0, clone.getLineWidth());

        clone.setLineWidth(10);
        assertEquals(5.0, gs.getLineWidth());
        assertEquals(10.0, clone.getLineWidth());
    }

    @Test
    public void concatMatrix() {
        GraphicsState gs = new GraphicsState();
        Matrix translate = new Matrix(1, 0, 0, 1, 100, 200);
        gs.concatMatrix(translate);
        assertEquals(100, gs.getCTM().getE());
        assertEquals(200, gs.getCTM().getF());
    }

    @Test
    public void fillColorRGB() {
        GraphicsState gs = new GraphicsState();
        gs.setFillColorRGB(1.0, 0.0, 0.0);
        java.awt.Color c = gs.getFillColor();
        assertEquals(255, c.getRed());
        assertEquals(0, c.getGreen());
        assertEquals(0, c.getBlue());
    }

    @Test
    public void fillColorGray() {
        GraphicsState gs = new GraphicsState();
        gs.setFillColorGray(0.5);
        java.awt.Color c = gs.getFillColor();
        assertEquals(c.getRed(), c.getGreen());
        assertEquals(c.getGreen(), c.getBlue());
        assertTrue(c.getRed() > 100 && c.getRed() < 150); // ~128
    }

    @Test
    public void fillColorCMYK() {
        GraphicsState gs = new GraphicsState();
        // Rendering uses the press-characterized display conversion
        // (CmykDisplay, CGATS LUT): pure cyan ink shows as the print cyan
        // ~rgb(0,174,239), NOT the algebraic (0,255,255). The algebraic
        // mapping remains the public-API contract on DeviceCMYK.toRGBInt.
        gs.setFillColorCMYK(1.0, 0.0, 0.0, 0.0);
        java.awt.Color c = gs.getFillColor();
        assertTrue(c.getRed() <= 16, "print cyan has near-zero R, got " + c.getRed());
        assertTrue(Math.abs(c.getGreen() - 174) <= 10,
                "print cyan G ~174, got " + c.getGreen());
        assertTrue(Math.abs(c.getBlue() - 239) <= 10,
                "print cyan B ~239, got " + c.getBlue());
        // Print-neutral gray (C > M = Y) must come out NEUTRAL - the old
        // algebraic formula rendered it greenish (corpus 10734 background).
        gs.setFillColorCMYK(0.20, 0.14, 0.14, 0.04);
        java.awt.Color g = gs.getFillColor();
        assertTrue(Math.abs(g.getRed() - g.getGreen()) <= 8
                        && Math.abs(g.getGreen() - g.getBlue()) <= 8,
                "print gray must be neutral, got " + g);
    }

    @Test
    public void strokeColorRGB() {
        GraphicsState gs = new GraphicsState();
        gs.setStrokeColorRGB(0.0, 0.0, 1.0);
        assertEquals(java.awt.Color.BLUE, gs.getStrokeColor());
    }

    @Test
    public void createStrokeSolid() {
        GraphicsState gs = new GraphicsState();
        gs.setLineWidth(3);
        gs.setLineCap(1);
        gs.setLineJoin(2);
        BasicStroke stroke = gs.createStroke();
        assertEquals(3.0f, stroke.getLineWidth());
        assertEquals(BasicStroke.CAP_ROUND, stroke.getEndCap());
        assertEquals(BasicStroke.JOIN_BEVEL, stroke.getLineJoin());
    }

    @Test
    public void createStrokeDashed() {
        GraphicsState gs = new GraphicsState();
        gs.setDash(new float[]{5, 3}, 2);
        BasicStroke stroke = gs.createStroke();
        assertNotNull(stroke.getDashArray());
        assertEquals(2, stroke.getDashArray().length);
        assertEquals(2.0f, stroke.getDashPhase());
    }

    @Test
    public void textMatrixAndPosition() {
        GraphicsState gs = new GraphicsState();
        gs.beginText();
        assertEquals(Matrix.IDENTITY, gs.getTextMatrix());

        gs.setTextMatrix(new Matrix(1, 0, 0, 1, 100, 700));
        assertEquals(100, gs.getTextMatrix().getE());
        assertEquals(700, gs.getTextMatrix().getF());

        // Td
        gs.moveTextPosition(50, -20);
        assertEquals(150, gs.getTextMatrix().getE());
        assertEquals(680, gs.getTextMatrix().getF());
    }

    @Test
    public void nextLineUsesLeading() {
        GraphicsState gs = new GraphicsState();
        gs.beginText();
        gs.setTextMatrix(new Matrix(1, 0, 0, 1, 0, 700));
        gs.setTextLeading(14);
        gs.nextLine();
        assertEquals(686, gs.getTextMatrix().getF(), 0.001);
    }

    @Test
    public void pathRectangle() {
        GraphicsState gs = new GraphicsState();
        gs.rect(10, 20, 100, 50);
        assertFalse(gs.getCurrentPath().getBounds2D().isEmpty());
        assertEquals(10, gs.getCurrentPath().getBounds2D().getX(), 0.1);
        assertEquals(20, gs.getCurrentPath().getBounds2D().getY(), 0.1);
    }

    @Test
    public void pathClearAfterClear() {
        GraphicsState gs = new GraphicsState();
        gs.rect(0, 0, 100, 100);
        assertFalse(gs.getCurrentPath().getBounds2D().isEmpty());
        gs.clearPath();
        assertTrue(gs.getCurrentPath().getBounds2D().isEmpty());
    }

    @Test
    public void pendingClipCycle() {
        GraphicsState gs = new GraphicsState();
        assertFalse(gs.hasPendingClip());
        gs.setPendingClip();
        assertTrue(gs.hasPendingClip());
        assertFalse(gs.isPendingClipEvenOdd());
        gs.clearPendingClip();
        assertFalse(gs.hasPendingClip());
    }

    @Test
    public void pendingClipEvenOdd() {
        GraphicsState gs = new GraphicsState();
        gs.setPendingClipEvenOdd();
        assertTrue(gs.hasPendingClip());
        assertTrue(gs.isPendingClipEvenOdd());
    }

    @Test
    public void transparencyDefaults() {
        GraphicsState gs = new GraphicsState();
        assertEquals(1.0f, gs.getStrokingAlpha());
        assertEquals(1.0f, gs.getNonStrokingAlpha());
        gs.setStrokingAlpha(0.5f);
        gs.setNonStrokingAlpha(0.3f);
        assertEquals(0.5f, gs.getStrokingAlpha());
        assertEquals(0.3f, gs.getNonStrokingAlpha());
    }
}
