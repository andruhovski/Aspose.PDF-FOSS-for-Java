package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/// Represents a circle drawing shape.
///
/// The circle is defined by its center (`posX`, `posY`) and
/// `radius` in user-space units.
///
public class Circle extends Shape {

    private static final Logger LOG = Logger.getLogger(Circle.class.getName());

    private float posX;
    private float posY;
    private float radius;

    /// Creates a new circle with the specified center and radius.
    ///
    /// @param posX   the x-coordinate of the center
    /// @param posY   the y-coordinate of the center
    /// @param radius the radius of the circle
    public Circle(float posX, float posY, float radius) {
        this.posX = posX;
        this.posY = posY;
        this.radius = radius;
    }

    /// Gets the x-coordinate of the center.
    ///
    /// @return the center x-coordinate
    public float getPosX() {
        return posX;
    }

    /// Sets the x-coordinate of the center.
    ///
    /// @param posX the center x-coordinate
    public void setPosX(float posX) {
        this.posX = posX;
    }

    /// Gets the y-coordinate of the center.
    ///
    /// @return the center y-coordinate
    public float getPosY() {
        return posY;
    }

    /// Sets the y-coordinate of the center.
    ///
    /// @param posY the center y-coordinate
    public void setPosY(float posY) {
        this.posY = posY;
    }

    /// Gets the radius.
    ///
    /// @return the radius
    public float getRadius() {
        return radius;
    }

    /// Sets the radius.
    ///
    /// @param radius the radius
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /// {@inheritDoc}
    @Override
    public void checkBounds(double width, double height) {
        if (posX - radius < 0) {
            throw new BoundsOutOfRangeException(
                    "Circle left edge (" + (posX - radius) + ") is negative");
        }
        if (posY - radius < 0) {
            throw new BoundsOutOfRangeException(
                    "Circle bottom edge (" + (posY - radius) + ") is negative");
        }
        if (posX + radius > width) {
            throw new BoundsOutOfRangeException(
                    "Circle right edge (" + (posX + radius) +
                    ") exceeds container width (" + width + ")");
        }
        if (posY + radius > height) {
            throw new BoundsOutOfRangeException(
                    "Circle top edge (" + (posY + radius) +
                    ") exceeds container height (" + height + ")");
        }
    }
}
