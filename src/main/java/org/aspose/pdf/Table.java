package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents a table element that can be added to a PDF page's paragraph collection.
///
/// A table consists of [Rows], each containing [Cells]. Column widths are
/// defined as a space-separated string (e.g., "100 200 100"). The table supports
/// default cell borders, default cell padding, background color, and repeating header rows.
///
/// ```
/// Table table = new Table();
/// table.setColumnWidths("100 200 150");
/// table.setDefaultCellBorder(new BorderInfo(BorderSide.All, 0.5));
/// Row row = table.getRows().add();
/// row.getCells().add("Column 1");
/// row.getCells().add("Column 2");
/// row.getCells().add("Column 3");
/// page.getParagraphs().add(table);
/// ```
public class Table extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(Table.class.getName());

    private String columnWidths;
    private Rows rows;
    private BorderInfo defaultCellBorder;
    private BorderInfo border;
    private Color backgroundColor;
    private MarginInfo defaultCellPadding;
    private int repeatingRowsCount;
    private double columnAdjustment;
    private boolean broken;
    private double left;
    private double top;
    private BorderCornerStyle cornerStyle = BorderCornerStyle.None;

    /// Creates a new empty Table with default settings.
    public Table() {
        // defaults
    }

    /// Returns the column widths specification string.
    ///
    /// Column widths are space-separated values in points, e.g., "100 200 100".
    ///
    /// @return the column widths string, or `null` if not set
    public String getColumnWidths() {
        return columnWidths;
    }

    /// Sets the column widths as a space-separated string of point values.
    ///
    /// @param columnWidths the column widths string, e.g., "100 200 100"
    public void setColumnWidths(String columnWidths) {
        this.columnWidths = columnWidths;
    }

    /// Returns the rows collection for this table, creating it lazily if needed.
    ///
    /// @return the rows collection; never `null`
    public Rows getRows() {
        if (rows == null) {
            rows = new Rows();
        }
        return rows;
    }

    /// Sets the rows collection for this table.
    ///
    /// @param rows the rows collection
    public void setRows(Rows rows) {
        this.rows = rows;
    }

    /// Returns the default border applied to all cells that do not have their own border set.
    ///
    /// @return the default cell border, or `null`
    public BorderInfo getDefaultCellBorder() {
        return defaultCellBorder;
    }

    /// Sets the default border applied to all cells that do not have their own border set.
    ///
    /// @param defaultCellBorder the default cell border
    public void setDefaultCellBorder(BorderInfo defaultCellBorder) {
        this.defaultCellBorder = defaultCellBorder;
    }

    /// Returns the border around the entire table.
    ///
    /// @return the table border, or `null`
    public BorderInfo getBorder() {
        return border;
    }

    /// Sets the border around the entire table.
    ///
    /// @param border the table border
    public void setBorder(BorderInfo border) {
        this.border = border;
    }

    /// Returns the background color of the table.
    ///
    /// @return the background color, or `null`
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /// Sets the background color of the table.
    ///
    /// @param backgroundColor the background color
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /// Returns the default padding (margin) applied to all cells.
    ///
    /// @return the default cell padding, or `null`
    public MarginInfo getDefaultCellPadding() {
        return defaultCellPadding;
    }

    /// Sets the default padding (margin) applied to all cells.
    ///
    /// @param defaultCellPadding the default cell padding
    public void setDefaultCellPadding(MarginInfo defaultCellPadding) {
        this.defaultCellPadding = defaultCellPadding;
    }

    /// Returns the number of rows at the top of the table that should repeat
    /// when the table spans multiple pages.
    ///
    /// @return the repeating rows count; 0 means no repeating header
    public int getRepeatingRowsCount() {
        return repeatingRowsCount;
    }

    /// Sets the number of rows at the top of the table that should repeat
    /// when the table spans multiple pages.
    ///
    /// @param repeatingRowsCount the repeating rows count
    public void setRepeatingRowsCount(int repeatingRowsCount) {
        this.repeatingRowsCount = repeatingRowsCount;
    }

    /// Returns the column width adjustment factor.
    ///
    /// @return the column adjustment value
    public double getColumnAdjustment() {
        return columnAdjustment;
    }

    /// Sets the column width adjustment factor.
    ///
    /// @param columnAdjustment the column adjustment value
    public void setColumnAdjustment(double columnAdjustment) {
        this.columnAdjustment = columnAdjustment;
    }

    /// Returns whether the table is broken across pages.
    ///
    /// @return `true` if the table can be broken across pages
    public boolean isBroken() {
        return broken;
    }

    /// Sets whether the table can be broken across pages.
    ///
    /// @param broken`true` to allow page breaking
    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    /// Returns the left position offset in points.
    ///
    /// @return the left offset
    public double getLeft() {
        return left;
    }

    /// Sets the left position offset in points.
    ///
    /// @param left the left offset
    public void setLeft(double left) {
        this.left = left;
    }

    /// Returns the top position offset in points.
    ///
    /// @return the top offset
    public double getTop() {
        return top;
    }

    /// Sets the top position offset in points.
    ///
    /// @param top the top offset
    public void setTop(double top) {
        this.top = top;
    }

    /// Returns the corner style for the table border.
    ///
    /// @return the border corner style
    public BorderCornerStyle getCornerStyle() {
        return cornerStyle;
    }

    /// Sets the corner style for the table border.
    ///
    /// When set to [BorderCornerStyle#Round], the corners of the table border
    /// are drawn as rounded arcs using the radius from [BorderInfo#getRoundedBorderRadius()].
    ///
    /// @param cornerStyle the border corner style
    public void setCornerStyle(BorderCornerStyle cornerStyle) {
        this.cornerStyle = cornerStyle != null ? cornerStyle : BorderCornerStyle.None;
    }
}
