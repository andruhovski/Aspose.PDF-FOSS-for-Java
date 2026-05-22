package org.aspose.pdf.drawing;

import org.aspose.pdf.Color;

import java.util.logging.Logger;

/**
 * Represents a pattern-based color space for use in drawing operations.
 * <p>
 * Pattern color spaces allow shapes to be filled or stroked with patterns
 * (tiling patterns or shading patterns) as described in ISO 32000-1:2008
 * section 8.7. This stub provides a basic color property; full pattern
 * support will be expanded in later development stages.
 * </p>
 *
 * @see GradientAxialShading
 */
public class PatternColorSpace {

    private static final Logger LOG = Logger.getLogger(PatternColorSpace.class.getName());

    private Color color;

    /**
     * Creates a new pattern color space with no color set.
     */
    public PatternColorSpace() {
        // defaults
    }

    /**
     * Creates a new pattern color space with the specified color.
     *
     * @param color the base color
     */
    public PatternColorSpace(Color color) {
        this.color = color;
    }

    /**
     * Gets the base color of this pattern color space.
     *
     * @return the color, or {@code null} if not set
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the base color of this pattern color space.
     *
     * @param color the base color
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
