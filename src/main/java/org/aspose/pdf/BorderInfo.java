package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents border styling information for a content element such as a table, row, or cell.
///
/// A border consists of individual side settings (top, bottom, left, right), each with
/// its own color and width. The [#getRoundedBorderRadius()] property allows rounded corners.
///
public class BorderInfo {

    private static final Logger LOG = Logger.getLogger(BorderInfo.class.getName());

    private GraphInfo top;
    private GraphInfo bottom;
    private GraphInfo left;
    private GraphInfo right;
    private double roundedBorderRadius;

    /// Creates a BorderInfo with no borders defined.
    public BorderInfo() {
        // all sides null = no border
    }

    /// Creates a BorderInfo that applies the same styling to the specified sides.
    ///
    /// @param side  the side(s) to apply the border to
    /// @param width the border line width in points
    public BorderInfo(BorderSide side, double width) {
        GraphInfo info = new GraphInfo();
        info.setLineWidth(width);
        info.setColor(Color.BLACK);
        applySide(side, info);
    }

    /// Creates a BorderInfo that applies the given color and width to the specified sides.
    ///
    /// @param side  the side(s) to apply the border to
    /// @param width the border line width in points
    /// @param color the border color
    public BorderInfo(BorderSide side, double width, Color color) {
        GraphInfo info = new GraphInfo();
        info.setLineWidth(width);
        info.setColor(color != null ? color : Color.BLACK);
        applySide(side, info);
    }

    /// Creates a BorderInfo that applies the given [GraphInfo] to the specified sides.
    ///
    /// @param side the side(s) to apply the border to
    /// @param info the graphical styling to apply
    public BorderInfo(BorderSide side, GraphInfo info) {
        applySide(side, info);
    }

    private void applySide(BorderSide side, GraphInfo info) {
        if (side == null || side == BorderSide.None) {
            return;
        }
        if (side == BorderSide.All) {
            this.top = info;
            this.bottom = info;
            this.left = info;
            this.right = info;
        } else if (side == BorderSide.Top) {
            this.top = info;
        } else if (side == BorderSide.Bottom) {
            this.bottom = info;
        } else if (side == BorderSide.Left) {
            this.left = info;
        } else if (side == BorderSide.Right) {
            this.right = info;
        }
    }

    /// Returns the top border styling.
    ///
    /// @return the top border [GraphInfo], or `null`
    public GraphInfo getTop() {
        return top;
    }

    /// Sets the top border styling.
    ///
    /// @param top the top border styling
    public void setTop(GraphInfo top) {
        this.top = top;
    }

    /// Returns the bottom border styling.
    ///
    /// @return the bottom border [GraphInfo], or `null`
    public GraphInfo getBottom() {
        return bottom;
    }

    /// Sets the bottom border styling.
    ///
    /// @param bottom the bottom border styling
    public void setBottom(GraphInfo bottom) {
        this.bottom = bottom;
    }

    /// Returns the left border styling.
    ///
    /// @return the left border [GraphInfo], or `null`
    public GraphInfo getLeft() {
        return left;
    }

    /// Sets the left border styling.
    ///
    /// @param left the left border styling
    public void setLeft(GraphInfo left) {
        this.left = left;
    }

    /// Returns the right border styling.
    ///
    /// @return the right border [GraphInfo], or `null`
    public GraphInfo getRight() {
        return right;
    }

    /// Sets the right border styling.
    ///
    /// @param right the right border styling
    public void setRight(GraphInfo right) {
        this.right = right;
    }

    /// Returns the rounded border corner radius.
    ///
    /// @return the corner radius in points; 0 means sharp corners
    public double getRoundedBorderRadius() {
        return roundedBorderRadius;
    }

    /// Sets the rounded border corner radius.
    ///
    /// @param roundedBorderRadius the corner radius in points
    public void setRoundedBorderRadius(double roundedBorderRadius) {
        this.roundedBorderRadius = roundedBorderRadius;
    }

    /// Holds graphical properties for a single border side (color, line width, dash pattern).
    public static class GraphInfo {

        private Color color = Color.BLACK;
        private double lineWidth = 1.0;
        private float[] dashArray;
        private double dashPhase;

        /// Creates a GraphInfo with default values (black, 1pt width).
        public GraphInfo() {
            // defaults
        }

        /// Returns the border line color.
        ///
        /// @return the color
        public Color getColor() {
            return color;
        }

        /// Sets the border line color.
        ///
        /// @param color the color
        public void setColor(Color color) {
            this.color = color;
        }

        /// Returns the border line width in points.
        ///
        /// @return the line width
        public double getLineWidth() {
            return lineWidth;
        }

        /// Sets the border line width in points.
        ///
        /// @param lineWidth the line width
        public void setLineWidth(double lineWidth) {
            this.lineWidth = lineWidth;
        }

        /// Returns the dash array for dashed borders.
        ///
        /// @return the dash array, or `null` for solid borders
        public float[] getDashArray() {
            return dashArray;
        }

        /// Sets the dash array for dashed borders.
        ///
        /// @param dashArray the dash array, or `null` for solid borders
        public void setDashArray(float[] dashArray) {
            this.dashArray = dashArray;
        }

        /// Returns the dash phase offset.
        ///
        /// @return the dash phase
        public double getDashPhase() {
            return dashPhase;
        }

        /// Sets the dash phase offset.
        ///
        /// @param dashPhase the dash phase
        public void setDashPhase(double dashPhase) {
            this.dashPhase = dashPhase;
        }
    }
}
