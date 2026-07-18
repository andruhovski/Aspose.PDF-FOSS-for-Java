package org.aspose.pdf.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A row detected in a table during absorption.
///
/// Contains the cells that were found in this row, ordered left to right.
///
public class AbsorbedRow {

    private final List<AbsorbedCell> cells = new ArrayList<>();

    /// Returns the list of cells in this row.
    ///
    /// @return unmodifiable list of cells
    public List<AbsorbedCell> getCellList() {
        return Collections.unmodifiableList(cells);
    }

    /// Adds a cell to this row.
    ///
    /// @param cell the cell to add
    public void addCell(AbsorbedCell cell) {
        if (cell != null) {
            cells.add(cell);
        }
    }
}
