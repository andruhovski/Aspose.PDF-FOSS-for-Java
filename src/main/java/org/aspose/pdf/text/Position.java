package org.aspose.pdf.text;

/// Represents a position on a PDF page (x, y coordinates in page space).
///
/// Used by [TextFragment] and [TextSegment] to store the
/// starting position of text on the page.
///
public class Position {

    private final double xIndent;
    private final double yIndent;

    /// Creates a Position with the given coordinates.
    ///
    /// @param xIndent the X coordinate (horizontal offset from left edge)
    /// @param yIndent the Y coordinate (vertical offset from bottom edge)
    public Position(double xIndent, double yIndent) {
        this.xIndent = xIndent;
        this.yIndent = yIndent;
    }

    /// Returns the X coordinate.
    ///
    /// @return the X indent
    public double getXIndent() {
        return xIndent;
    }

    /// Returns the Y coordinate.
    ///
    /// @return the Y indent
    public double getYIndent() {
        return yIndent;
    }

    @Override
    public String toString() {
        return "Position{x=" + xIndent + ", y=" + yIndent + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position other = (Position) o;
        return Double.compare(xIndent, other.xIndent) == 0
                && Double.compare(yIndent, other.yIndent) == 0;
    }

    @Override
    public int hashCode() {
        long x = Double.doubleToLongBits(xIndent);
        long y = Double.doubleToLongBits(yIndent);
        return (int) (x ^ (x >>> 32)) * 31 + (int) (y ^ (y >>> 32));
    }
}
