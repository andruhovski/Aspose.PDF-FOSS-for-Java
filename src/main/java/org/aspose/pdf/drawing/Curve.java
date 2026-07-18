package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/// Represents a Bezier curve drawing shape.
///
/// The curve is defined by an array of control point coordinates. For a cubic
/// Bezier curve, the array contains 8 elements: the start point, two control
/// points, and the end point (x1, y1, cx1, cy1, cx2, cy2, x2, y2).
///
public class Curve extends Shape {

    private static final Logger LOG = Logger.getLogger(Curve.class.getName());

    private float[] positionArray;

    /// Creates a new Bezier curve with the specified control points.
    ///
    /// @param positionArray alternating x,y coordinates for the control points;
    ///                      must have an even number of elements
    /// @throws IllegalArgumentException if the array is null or has an odd length
    public Curve(float[] positionArray) {
        if (positionArray == null) {
            throw new IllegalArgumentException("Position array must not be null");
        }
        if (positionArray.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Position array must have an even number of elements");
        }
        this.positionArray = positionArray;
    }

    /// Gets the array of control point coordinates.
    ///
    /// @return the position array
    public float[] getPositionArray() {
        return positionArray;
    }

    /// Sets the array of control point coordinates.
    ///
    /// @param positionArray the position array
    public void setPositionArray(float[] positionArray) {
        this.positionArray = positionArray;
    }

    /// {@inheritDoc}
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
                        "Curve control point x=" + x + " at index " + i +
                        " is outside container width (" + width + ")");
            }
            if (y < 0 || y > height) {
                throw new BoundsOutOfRangeException(
                        "Curve control point y=" + y + " at index " + (i + 1) +
                        " is outside container height (" + height + ")");
            }
        }
    }
}
