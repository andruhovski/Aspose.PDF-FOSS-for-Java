package org.aspose.pdf.tests;

import org.aspose.pdf.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Color].
public class ColorTest {

    @Test
    public void fromRgb() {
        Color c = Color.fromRgb(0.5, 0.6, 0.7);
        assertEquals(Color.ColorSpace.RGB, c.getColorSpace());
        double[] comp = c.getComponents();
        assertEquals(3, comp.length);
        assertEquals(0.5, comp[0], 1e-10);
        assertEquals(0.6, comp[1], 1e-10);
        assertEquals(0.7, comp[2], 1e-10);
    }

    @Test
    public void fromGray() {
        Color c = Color.fromGray(0.5);
        assertEquals(Color.ColorSpace.GRAY, c.getColorSpace());
        double[] comp = c.getComponents();
        assertEquals(1, comp.length);
        assertEquals(0.5, comp[0], 1e-10);
    }

    @Test
    public void fromCmyk() {
        Color c = Color.fromCmyk(0.1, 0.2, 0.3, 0.4);
        assertEquals(Color.ColorSpace.CMYK, c.getColorSpace());
        double[] comp = c.getComponents();
        assertEquals(4, comp.length);
        assertEquals(0.1, comp[0], 1e-10);
        assertEquals(0.2, comp[1], 1e-10);
        assertEquals(0.3, comp[2], 1e-10);
        assertEquals(0.4, comp[3], 1e-10);
    }

    @Test
    public void predefinedBlack() {
        assertEquals(Color.ColorSpace.GRAY, Color.BLACK.getColorSpace());
        assertEquals(0.0, Color.BLACK.getComponents()[0], 1e-10);
    }

    @Test
    public void predefinedWhite() {
        assertEquals(Color.ColorSpace.GRAY, Color.WHITE.getColorSpace());
        assertEquals(1.0, Color.WHITE.getComponents()[0], 1e-10);
    }

    @Test
    public void predefinedRed() {
        assertEquals(Color.ColorSpace.RGB, Color.RED.getColorSpace());
        assertArrayEquals(new double[]{1, 0, 0}, Color.RED.getComponents(), 1e-10);
    }

    @Test
    public void predefinedGreen() {
        assertArrayEquals(new double[]{0, 1, 0}, Color.GREEN.getComponents(), 1e-10);
    }

    @Test
    public void predefinedBlue() {
        assertArrayEquals(new double[]{0, 0, 1}, Color.BLUE.getComponents(), 1e-10);
    }

    @Test
    public void getComponentsReturnsClone() {
        Color c = Color.fromRgb(1, 0, 0);
        double[] comp = c.getComponents();
        comp[0] = 999;
        assertEquals(1.0, c.getComponents()[0], 1e-10);
    }

    @Test
    public void equalsAndHashCode() {
        Color c1 = Color.fromRgb(0.1, 0.2, 0.3);
        Color c2 = Color.fromRgb(0.1, 0.2, 0.3);
        Color c3 = Color.fromRgb(0.1, 0.2, 0.4);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }

    @Test
    public void differentColorSpacesNotEqual() {
        Color gray = Color.fromGray(0);
        Color rgb = Color.fromRgb(0, 0, 0);
        assertNotEquals(gray, rgb);
    }

    @Test
    public void toStringContainsColorSpace() {
        Color c = Color.fromRgb(1, 0, 0);
        assertTrue(c.toString().contains("RGB"));
    }
}
