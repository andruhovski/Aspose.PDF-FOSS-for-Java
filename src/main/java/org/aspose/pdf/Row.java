package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents a single row within a [Table].
///
/// Each row contains a [Cells] collection and supports individual styling
/// such as background color, border, fixed height, and minimum height.
///
public class Row {

    private static final Logger LOG = Logger.getLogger(Row.class.getName());

    private Cells cells;
    private double fixedRowHeight;
    private double minRowHeight;
    private Color backgroundColor;
    private BorderInfo border;

    /// Creates a new Row with default settings.
    public Row() {
        // defaults
    }

    /// Returns the cells collection for this row, creating it lazily if needed.
    ///
    /// @return the cells collection; never `null`
    public Cells getCells() {
        if (cells == null) {
            cells = new Cells();
        }
        return cells;
    }

    /// Sets the cells collection for this row.
    ///
    /// @param cells the cells collection
    public void setCells(Cells cells) {
        this.cells = cells;
    }

    /// Returns the fixed row height in points.
    ///
    /// @return the fixed height; 0 means auto-calculated
    public double getFixedRowHeight() {
        return fixedRowHeight;
    }

    /// Sets the fixed row height in points.
    /// A value of 0 means the height is auto-calculated based on content.
    ///
    /// @param fixedRowHeight the fixed height in points
    public void setFixedRowHeight(double fixedRowHeight) {
        this.fixedRowHeight = fixedRowHeight;
    }

    /// Returns the minimum row height in points.
    ///
    /// @return the minimum height; 0 means no minimum
    public double getMinRowHeight() {
        return minRowHeight;
    }

    /// Sets the minimum row height in points.
    ///
    /// @param minRowHeight the minimum height in points
    public void setMinRowHeight(double minRowHeight) {
        this.minRowHeight = minRowHeight;
    }

    /// Returns the background color of this row.
    ///
    /// @return the background color, or `null` if not set
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /// Sets the background color of this row.
    ///
    /// @param backgroundColor the background color
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /// Returns the border styling for this row.
    ///
    /// @return the border info, or `null` if not set
    public BorderInfo getBorder() {
        return border;
    }

    /// Sets the border styling for this row.
    ///
    /// @param border the border info
    public void setBorder(BorderInfo border) {
        this.border = border;
    }
}
