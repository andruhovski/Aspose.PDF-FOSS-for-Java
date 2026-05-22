package org.aspose.pdf.text;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Extracts table structures from PDF pages by analyzing text positions
 * and ruling lines to identify rows, columns, and cells.
 * <p>
 * The algorithm clusters text fragments by Y-coordinate to detect rows,
 * then by X-coordinate within each row to detect columns/cells.
 * A group of consecutive rows with a consistent column structure is
 * recognized as a table.
 * </p>
 */
public class TableAbsorber {

    private static final Logger LOG = Logger.getLogger(TableAbsorber.class.getName());

    /** Tolerance in points for same-row Y-coordinate detection. */
    private static final double ROW_TOLERANCE = 3.0;

    /** Minimum number of columns required to consider a group of rows as a table. */
    private static final int MIN_COLUMNS = 2;

    /** Minimum number of rows required to consider a group as a table. */
    private static final int MIN_ROWS = 2;

    private final List<AbsorbedTable> tables = new ArrayList<>();

    /**
     * Visits a page and extracts table structures.
     *
     * @param page the page to analyze
     * @throws IOException if text extraction fails
     */
    public void visit(Page page) throws IOException {
        tables.clear();

        // 1. Extract all text fragments using TextFragmentAbsorber
        TextFragmentAbsorber tfa = new TextFragmentAbsorber();
        tfa.visit(page);
        TextFragmentCollection fragments = tfa.getTextFragments();
        if (fragments == null || fragments.size() == 0) {
            return;
        }

        // 2. Collect fragments with valid positions into a sortable list
        List<TextFragment> positioned = new ArrayList<>();
        for (TextFragment f : fragments) {
            if (f.getPosition() != null) {
                positioned.add(f);
            }
        }
        if (positioned.isEmpty()) {
            return;
        }

        // Sort by Y descending (top to bottom), then X ascending (left to right)
        positioned.sort(Comparator
                .comparingDouble((TextFragment f) -> -f.getPosition().getYIndent())
                .thenComparingDouble(f -> f.getPosition().getXIndent()));

        // 3. Group fragments into rows by Y coordinate with tolerance
        List<List<TextFragment>> rows = groupByY(positioned);

        // 4. Detect tables: groups of consecutive rows with >= MIN_COLUMNS columns
        List<List<List<TextFragment>>> tableGroups = detectTableGroups(rows);

        // 5. Build AbsorbedTable structures
        for (List<List<TextFragment>> group : tableGroups) {
            AbsorbedTable table = buildTable(group);
            if (table != null) {
                tables.add(table);
            }
        }

        LOG.fine(() -> "TableAbsorber found " + tables.size() + " table(s)");
    }

    /**
     * Returns the list of tables detected on the last visited page.
     *
     * @return unmodifiable list of absorbed tables
     */
    public List<AbsorbedTable> getTableList() {
        return Collections.unmodifiableList(tables);
    }

    /**
     * Groups text fragments into rows based on Y-coordinate proximity.
     */
    private List<List<TextFragment>> groupByY(List<TextFragment> fragments) {
        List<List<TextFragment>> rows = new ArrayList<>();
        List<TextFragment> currentRow = new ArrayList<>();
        double currentY = Double.NaN;

        for (TextFragment f : fragments) {
            double y = f.getPosition().getYIndent();
            if (Double.isNaN(currentY) || Math.abs(y - currentY) <= ROW_TOLERANCE) {
                currentRow.add(f);
                if (Double.isNaN(currentY)) {
                    currentY = y;
                }
            } else {
                if (!currentRow.isEmpty()) {
                    // Sort row by X coordinate
                    currentRow.sort(Comparator.comparingDouble(
                            fr -> fr.getPosition().getXIndent()));
                    rows.add(currentRow);
                }
                currentRow = new ArrayList<>();
                currentRow.add(f);
                currentY = y;
            }
        }
        if (!currentRow.isEmpty()) {
            currentRow.sort(Comparator.comparingDouble(
                    fr -> fr.getPosition().getXIndent()));
            rows.add(currentRow);
        }
        return rows;
    }

    /**
     * Detects groups of consecutive rows that form tables.
     * A table is a sequence of rows where each row has at least MIN_COLUMNS fragments.
     */
    private List<List<List<TextFragment>>> detectTableGroups(List<List<TextFragment>> rows) {
        List<List<List<TextFragment>>> groups = new ArrayList<>();
        List<List<TextFragment>> currentGroup = new ArrayList<>();

        for (List<TextFragment> row : rows) {
            if (row.size() >= MIN_COLUMNS) {
                currentGroup.add(row);
            } else {
                if (currentGroup.size() >= MIN_ROWS) {
                    groups.add(currentGroup);
                }
                currentGroup = new ArrayList<>();
            }
        }
        if (currentGroup.size() >= MIN_ROWS) {
            groups.add(currentGroup);
        }
        return groups;
    }

    /**
     * Builds an AbsorbedTable from a group of rows.
     */
    private AbsorbedTable buildTable(List<List<TextFragment>> rowFragments) {
        AbsorbedTable table = new AbsorbedTable();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (List<TextFragment> rowFrags : rowFragments) {
            AbsorbedRow row = new AbsorbedRow();
            for (TextFragment f : rowFrags) {
                AbsorbedCell cell = new AbsorbedCell();
                cell.addTextFragment(f);
                if (f.getRectangle() != null) {
                    cell.setRectangle(f.getRectangle());
                    Rectangle r = f.getRectangle();
                    minX = Math.min(minX, r.getLLX());
                    minY = Math.min(minY, r.getLLY());
                    maxX = Math.max(maxX, r.getURX());
                    maxY = Math.max(maxY, r.getURY());
                }
                row.addCell(cell);
            }
            table.addRow(row);
        }

        if (minX != Double.MAX_VALUE) {
            table.setRectangle(new Rectangle(minX, minY, maxX, maxY));
        }
        return table;
    }
}
