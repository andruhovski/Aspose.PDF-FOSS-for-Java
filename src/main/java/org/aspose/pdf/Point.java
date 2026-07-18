package org.aspose.pdf;

/// Represents a point in 2D space with double-precision coordinates.
///
/// Used throughout the PDF API for positioning elements on pages.
///
public class Point {

    private double x;
    private double y;

    /// Creates a point at the specified coordinates.
    ///
    /// @param x the x coordinate
    /// @param y the y coordinate
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /// Returns the x coordinate.
    ///
    /// @return the x coordinate
    public double getX() {
        return x;
    }

    /// Sets the x coordinate.
    ///
    /// @param x the x coordinate
    public void setX(double x) {
        this.x = x;
    }

    /// Returns the y coordinate.
    ///
    /// @return the y coordinate
    public double getY() {
        return y;
    }

    /// Sets the y coordinate.
    ///
    /// @param y the y coordinate
    public void setY(double y) {
        this.y = y;
    }

    /// Returns a string representation of this point.
    ///
    /// @return a string in the format "Point[x, y]"
    @Override
    public String toString() {
        return "Point[" + x + ", " + y + "]";
    }
}
