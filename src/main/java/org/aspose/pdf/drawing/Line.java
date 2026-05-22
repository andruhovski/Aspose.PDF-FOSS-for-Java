package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/**
 * Represents a line (or polyline) drawing shape.
 * <p>
 * The line is defined by an array of alternating x,y coordinate pairs
 * in user-space units. For a simple line segment, the array has 4 elements
 * (x1, y1, x2, y2). For a polyline, more pairs are included.
 * </p>
 */
public class Line extends Shape {

    private static final Logger LOG = Logger.getLogger(Line.class.getName());

    private float[] positionArray;

    /**
     * Creates a new line with the specified position array.
     *
     * @param positionArray alternating x,y coordinate pairs; must have an even
     *                      number of elements and at least 4 (two points)
     * @throws IllegalArgumentException if the array is null, has fewer than
     *                                  4 elements, or has an odd length
     */
    public Line(float[] positionArray) {
        if (positionArray == null) {
            throw new IllegalArgumentException("Position array must not be null");
        }
        if (positionArray.length < 4) {
            throw new IllegalArgumentException(
                    "Position array must have at least 4 elements (two points)");
        }
        if (positionArray.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Position array must have an even number of elements");
        }
        this.positionArray = positionArray;
    }

    /**
     * Gets the position array of alternating x,y coordinate pairs.
     *
     * @return the position array
     */
    public float[] getPositionArray() {
        return positionArray;
    }

    /**
     * Sets the position array of alternating x,y coordinate pairs.
     *
     * @param positionArray the position array
     */
    public void setPositionArray(float[] positionArray) {
        this.positionArray = positionArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkBounds(double width, double height) {
        if (positionArray == null) {
            return;
        }
        for (int i = 0; i < positionArray.length; i += 2) {
            float x = positionArray[i];
            float y = positionArray[i + 1];
            if (x < 0 || x > width) {
                throw new BoundsOutOfRangeException(
                        "Line point x=" + x + " at index " + i +
                        " is outside container width (" + width + ")");
            }
            if (y < 0 || y > height) {
                throw new BoundsOutOfRangeException(
                        "Line point y=" + y + " at index " + (i + 1) +
                        " is outside container height (" + height + ")");
            }
        }
    }
}
