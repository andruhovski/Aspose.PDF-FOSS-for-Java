package org.aspose.pdf.engine.render;

import org.junit.jupiter.api.Test;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BlendComposite} — the /BM Multiply blend (§11.3.5).
 * Regression for corpus 30894: Highlight annotation appearance streams use
 * {@code /BM /Multiply}; painting them SRC_OVER covered the text below.
 */
public class BlendCompositeTest {

    @Test
    public void fillCompositeReturnsSrcOverForNormal() {
        GraphicsState state = new GraphicsState();
        assertSame(AlphaComposite.SrcOver, BlendComposite.fillComposite(state));
    }

    @Test
    public void fillCompositeReturnsAlphaForTransparentNormal() {
        GraphicsState state = new GraphicsState();
        state.setNonStrokingAlpha(0.5f);
        Composite c = BlendComposite.fillComposite(state);
        assertTrue(c instanceof AlphaComposite);
        assertEquals(0.5f, ((AlphaComposite) c).getAlpha(), 1e-6);
    }

    @Test
    public void multiplyDarkensInsteadOfCovering() {
        // Black text on white, then a yellow Multiply rectangle on top:
        // white backdrop → yellow (255,255,0), black glyph → stays black.
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(5, 5, 4, 4); // the "glyph"

        GraphicsState state = new GraphicsState();
        state.setBlendMode("Multiply");
        g2d.setComposite(BlendComposite.fillComposite(state));
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(0, 0, 20, 20);
        g2d.dispose();

        // White backdrop multiplied by yellow → yellow
        assertEquals(0xFFFFFF00, img.getRGB(15, 15));
        // Black glyph multiplied by yellow → black (still visible!)
        assertEquals(0xFF000000, img.getRGB(6, 6));
    }

    @Test
    public void multiplyHonoursConstantAlpha() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 4, 4);

        GraphicsState state = new GraphicsState();
        state.setBlendMode("Multiply");
        state.setNonStrokingAlpha(0.5f);
        g2d.setComposite(BlendComposite.fillComposite(state));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 4, 4);
        g2d.dispose();

        // 50% multiply of black over white → mid gray
        int rgb = img.getRGB(1, 1);
        int r = (rgb >> 16) & 0xFF;
        assertTrue(r > 100 && r < 155, "expected ~128 gray, got " + r);
    }

    @Test
    public void blendModeDefaultsToNormalAndSurvivesClone() {
        GraphicsState state = new GraphicsState();
        assertEquals("Normal", state.getBlendMode());
        state.setBlendMode("Multiply");
        GraphicsState copy = state.clone();
        assertEquals("Multiply", copy.getBlendMode());
        state.setBlendMode(null);
        assertEquals("Normal", state.getBlendMode());
        assertEquals("Multiply", copy.getBlendMode());
    }
}
