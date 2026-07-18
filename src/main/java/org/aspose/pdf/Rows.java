package org.aspose.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/// Represents an ordered collection of [Row] instances within a [Table].
///
/// Implements [Iterable] for use in enhanced for-loops.
///
public class Rows implements Iterable<Row> {

    private static final Logger LOG = Logger.getLogger(Rows.class.getName());

    private final List<Row> items = new ArrayList<>();

    /// Creates an empty Rows collection.
    public Rows() {
        // empty
    }

    /// Creates a new empty [Row], adds it to this collection, and returns it.
    ///
    /// @return the newly created row
    public Row add() {
        Row row = new Row();
        items.add(row);
        LOG.fine("Added new row; collection size = " + items.size());
        return row;
    }

    /// Adds an existing [Row] to this collection.
    ///
    /// @param row the row to add; must not be `null`
    /// @throws NullPointerException if `row` is `null`
    public void add(Row row) {
        Objects.requireNonNull(row, "row must not be null");
        items.add(row);
    }

    /// Returns the row at the specified index.
    ///
    /// @param index zero-based index
    /// @return the row at the given index
    /// @throws IndexOutOfBoundsException if the index is out of range
    public Row get(int index) {
        return items.get(index);
    }

    /// Returns the number of rows in this collection.
    ///
    /// @return the row count
    public int size() {
        return items.size();
    }

    /// Returns the number of rows in this collection.
    /// Alias for [#size()].
    ///
    /// @return the row count
    public int getCount() {
        return items.size();
    }

    /// Returns an iterator over the rows in this collection.
    ///
    /// @return an iterator
    @Override
    public Iterator<Row> iterator() {
        return items.iterator();
    }
}
