package org.aspose.pdf.text;

import org.aspose.pdf.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A table detected on a PDF page during absorption.
///
/// Contains the rows of the table and the overall bounding rectangle.
///
public class AbsorbedTable {

    private final List<AbsorbedRow> rows = new ArrayList<>();
    private Rectangle rectangle;

    /// Returns the list of rows in this table.
    ///
    /// @return unmodifiable list of rows
    public List<AbsorbedRow> getRowList() {
        return Collections.unmodifiableList(rows);
    }

    /// Adds a row to this table.
    ///
    /// @param row the row to add
    public void addRow(AbsorbedRow row) {
        if (row != null) {
            rows.add(row);
        }
    }

    /// Returns the bounding rectangle of this table.
    ///
    /// @return the rectangle, or null if not set
    public Rectangle getRectangle() {
        return rectangle;
    }

    /// Sets the bounding rectangle of this table.
    ///
    /// @param rectangle the bounding rectangle
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }
}
