package org.aspose.pdf.drawing;

import org.aspose.pdf.Color;

import java.util.logging.Logger;

/**
 * Represents an axial (linear) gradient shading pattern.
 * <p>
 * An axial gradient transitions between a start color and an end color along
 * a line defined in user space. This class provides the color endpoints;
 * the actual shading dictionary generation is handled during PDF writing.
 * </p>
 *
 * @see PatternColorSpace
 */
public class GradientAxialShading {

    private static final Logger LOG = Logger.getLogger(GradientAxialShading.class.getName());

    private Color startColor;
    private Color endColor;

    /**
     * Creates a new axial shading with no colors set.
     */
    public GradientAxialShading() {
        // defaults
    }

    /**
     * Creates a new axial shading with the specified start and end colors.
     *
     * @param startColor the color at the start of the gradient
     * @param endColor   the color at the end of the gradient
     */
    public GradientAxialShading(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    /**
     * Gets the color at the start of the gradient.
     *
     * @return the start color, or {@code null} if not set
     */
    public Color getStartColor() {
        return startColor;
    }

    /**
     * Sets the color at the start of the gradient.
     *
     * @param startColor the start color
     */
    public void setStartColor(Color startColor) {
        this.startColor = startColor;
    }

    /**
     * Gets the color at the end of the gradient.
     *
     * @return the end color, or {@code null} if not set
     */
    public Color getEndColor() {
        return endColor;
    }

    /**
     * Sets the color at the end of the gradient.
     *
     * @param endColor the end color
     */
    public void setEndColor(Color endColor) {
        this.endColor = endColor;
    }
}
