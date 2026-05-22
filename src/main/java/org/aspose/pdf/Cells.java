package org.aspose.pdf;

import org.aspose.pdf.text.TextFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents an ordered collection of {@link Cell} instances within a {@link Row}.
 * <p>
 * Provides convenience methods for adding cells with text content directly.
 * Implements {@link Iterable} for use in enhanced for-loops.
 * </p>
 */
public class Cells implements Iterable<Cell> {

    private static final Logger LOG = Logger.getLogger(Cells.class.getName());

    private final List<Cell> items = new ArrayList<>();

    /**
     * Creates an empty Cells collection.
     */
    public Cells() {
        // empty
    }

    /**
     * Creates a new empty {@link Cell}, adds it to this collection, and returns it.
     *
     * @return the newly created cell
     */
    public Cell add() {
        Cell cell = new Cell();
        items.add(cell);
        LOG.fine("Added new empty cell; collection size = " + items.size());
        return cell;
    }

    /**
     * Creates a new {@link Cell} containing a {@link TextFragment} with the given text,
     * adds it to this collection, and returns it.
     *
     * @param text the text content for the cell; must not be {@code null}
     * @return the newly created cell
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public Cell add(String text) {
        Objects.requireNonNull(text, "text must not be null");
        Cell cell = new Cell();
        cell.getParagraphs().add(new TextFragment(text));
        items.add(cell);
        LOG.fine("Added cell with text; collection size = " + items.size());
        return cell;
    }

    /**
     * Adds an existing {@link Cell} to this collection.
     *
     * @param cell the cell to add; must not be {@code null}
     * @throws NullPointerException if {@code cell} is {@code null}
     */
    public void add(Cell cell) {
        Objects.requireNonNull(cell, "cell must not be null");
        items.add(cell);
    }

    /**
     * Returns the cell at the specified index.
     *
     * @param index zero-based index
     * @return the cell at the given index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Cell get(int index) {
        return items.get(index);
    }

    /**
     * Returns the number of cells in this collection.
     *
     * @return the cell count
     */
    public int size() {
        return items.size();
    }

    /**
     * Returns the number of cells in this collection.
     * Alias for {@link #size()}.
     *
     * @return the cell count
     */
    public int getCount() {
        return items.size();
    }

    /**
     * Returns an iterator over the cells in this collection.
     *
     * @return an iterator
     */
    @Override
    public Iterator<Cell> iterator() {
        return items.iterator();
    }
}
