package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/// Represents an ellipse drawing shape.
///
/// The ellipse is defined by its bounding box: the lower-left corner
/// (`left`, `bottom`) and dimensions (`width`, `height`)
/// in user-space units. When width equals height, the ellipse becomes a circle.
///
public class Ellipse extends Shape {

    private static final Logger LOG = Logger.getLogger(Ellipse.class.getName());

    private float left;
    private float bottom;
    private float width;
    private float height;

    /// Creates a new ellipse with the specified bounding box.
    ///
    /// @param left   the x-coordinate of the lower-left corner of the bounding box
    /// @param bottom the y-coordinate of the lower-left corner of the bounding box
    /// @param width  the width of the bounding box
    /// @param height the height of the bounding box
    public Ellipse(float left, float bottom, float width, float height) {
        this.left = left;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
    }

    /// Gets the x-coordinate of the lower-left corner of the bounding box.
    ///
    /// @return the left position
    public float getLeft() {
        return left;
    }

    /// Sets the x-coordinate of the lower-left corner of the bounding box.
    ///
    /// @param left the left position
    public void setLeft(float left) {
        this.left = left;
    }

    /// Gets the y-coordinate of the lower-left corner of the bounding box.
    ///
    /// @return the bottom position
    public float getBottom() {
        return bottom;
    }

    /// Sets the y-coordinate of the lower-left corner of the bounding box.
    ///
    /// @param bottom the bottom position
    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    /// Gets the width of the bounding box.
    ///
    /// @return the width
    public float getWidth() {
        return width;
    }

    /// Sets the width of the bounding box.
    ///
    /// @param width the width
    public void setWidth(float width) {
        this.width = width;
    }

    /// Gets the height of the bounding box.
    ///
    /// @return the height
    public float getHeight() {
        return height;
    }

    /// Sets the height of the bounding box.
    ///
    /// @param height the height
    public void setHeight(float height) {
        this.height = height;
    }

    /// {@inheritDoc}
    @Override
    public void checkBounds(double containerWidth, double containerHeight) {
        if (left < 0) {
            throw new BoundsOutOfRangeException(
                    "Ellipse left (" + left + ") is negative");
        }
        if (bottom < 0) {
            throw new BoundsOutOfRangeException(
                    "Ellipse bottom (" + bottom + ") is negative");
        }
        if (left + width > containerWidth) {
            throw new BoundsOutOfRangeException(
                    "Ellipse right edge (" + (left + width) +
                    ") exceeds container width (" + containerWidth + ")");
        }
        if (bottom + height > containerHeight) {
            throw new BoundsOutOfRangeException(
                    "Ellipse top edge (" + (bottom + height) +
                    ") exceeds container height (" + containerHeight + ")");
        }
    }
}
