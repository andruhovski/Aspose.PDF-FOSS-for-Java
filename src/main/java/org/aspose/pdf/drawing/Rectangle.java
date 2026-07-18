package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/// Represents a rectangle drawing shape.
///
/// The rectangle is defined by its lower-left corner (`left`, `bottom`)
/// and its dimensions (`width`, `height`) in user-space units.
/// An optional `roundedCornerRadius` produces rounded corners.
///
public class Rectangle extends Shape {

    private static final Logger LOG = Logger.getLogger(Rectangle.class.getName());

    private float left;
    private float bottom;
    private float width;
    private float height;
    private float roundedCornerRadius;

    /// Creates a new rectangle with the specified position and dimensions.
    ///
    /// @param left   the x-coordinate of the lower-left corner
    /// @param bottom the y-coordinate of the lower-left corner
    /// @param width  the width of the rectangle
    /// @param height the height of the rectangle
    public Rectangle(float left, float bottom, float width, float height) {
        this.left = left;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
    }

    /// Gets the x-coordinate of the lower-left corner.
    ///
    /// @return the left position
    public float getLeft() {
        return left;
    }

    /// Sets the x-coordinate of the lower-left corner.
    ///
    /// @param left the left position
    public void setLeft(float left) {
        this.left = left;
    }

    /// Gets the y-coordinate of the lower-left corner.
    ///
    /// @return the bottom position
    public float getBottom() {
        return bottom;
    }

    /// Sets the y-coordinate of the lower-left corner.
    ///
    /// @param bottom the bottom position
    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    /// Gets the width of the rectangle.
    ///
    /// @return the width
    public float getWidth() {
        return width;
    }

    /// Sets the width of the rectangle.
    ///
    /// @param width the width
    public void setWidth(float width) {
        this.width = width;
    }

    /// Gets the height of the rectangle.
    ///
    /// @return the height
    public float getHeight() {
        return height;
    }

    /// Sets the height of the rectangle.
    ///
    /// @param height the height
    public void setHeight(float height) {
        this.height = height;
    }

    /// Gets the corner radius for rounded rectangles.
    ///
    /// @return the corner radius; 0 means sharp corners
    public float getRoundedCornerRadius() {
        return roundedCornerRadius;
    }

    /// Sets the corner radius for rounded rectangles.
    ///
    /// @param roundedCornerRadius the corner radius; 0 for sharp corners
    public void setRoundedCornerRadius(float roundedCornerRadius) {
        this.roundedCornerRadius = roundedCornerRadius;
    }

    /// {@inheritDoc}
    @Override
    public void checkBounds(double containerWidth, double containerHeight) {
        if (left < 0) {
            throw new BoundsOutOfRangeException(
                    "Rectangle left (" + left + ") is negative");
        }
        if (bottom < 0) {
            throw new BoundsOutOfRangeException(
                    "Rectangle bottom (" + bottom + ") is negative");
        }
        if (left + width > containerWidth) {
            throw new BoundsOutOfRangeException(
                    "Rectangle right edge (" + (left + width) +
                    ") exceeds container width (" + containerWidth + ")");
        }
        if (bottom + height > containerHeight) {
            throw new BoundsOutOfRangeException(
                    "Rectangle top edge (" + (bottom + height) +
                    ") exceeds container height (" + containerHeight + ")");
        }
    }
}
