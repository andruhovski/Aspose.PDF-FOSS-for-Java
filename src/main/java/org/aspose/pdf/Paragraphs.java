package org.aspose.pdf;

import org.aspose.pdf.text.TextFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/// Represents an ordered collection of [BaseParagraph] elements that make up the
/// content of a page, cell, or other container.
///
/// This class implements [Iterable] so it can be used in enhanced for-loops.
///
public class Paragraphs implements Iterable<BaseParagraph> {

    private static final Logger LOG = Logger.getLogger(Paragraphs.class.getName());

    private final List<BaseParagraph> items = new ArrayList<>();

    /// Creates an empty Paragraphs collection.
    public Paragraphs() {
        // empty
    }

    /// Adds a paragraph to the end of this collection.
    ///
    /// @param paragraph the paragraph to add; must not be `null`
    /// @throws NullPointerException if `paragraph` is `null`
    public void add(BaseParagraph paragraph) {
        Objects.requireNonNull(paragraph, "paragraph must not be null");
        items.add(paragraph);
    }

    /// Convenience method that creates a [TextFragment] from the given text
    /// and adds it to this collection.
    ///
    /// @param text the text content; must not be `null`
    /// @throws NullPointerException if `text` is `null`
    public void add(String text) {
        Objects.requireNonNull(text, "text must not be null");
        items.add(new TextFragment(text));
    }

    /// Returns the paragraph at the specified index.
    ///
    /// @param index zero-based index
    /// @return the paragraph at the given index
    /// @throws IndexOutOfBoundsException if the index is out of range
    public BaseParagraph get(int index) {
        return items.get(index);
    }

    /// Returns the number of paragraphs in this collection.
    ///
    /// @return the count of paragraphs
    public int size() {
        return items.size();
    }

    /// Returns the number of paragraphs in this collection.
    /// Alias for [#size()].
    ///
    /// @return the count of paragraphs
    public int getCount() {
        return items.size();
    }

    /// Removes the paragraph at the specified index.
    ///
    /// @param index zero-based index of the paragraph to remove
    /// @return the removed paragraph
    /// @throws IndexOutOfBoundsException if the index is out of range
    public BaseParagraph remove(int index) {
        return items.remove(index);
    }

    /// Removes all paragraphs from this collection.
    public void clear() {
        items.clear();
    }

    /// Returns whether this collection contains no paragraphs.
    ///
    /// @return `true` if the collection is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /// Returns an iterator over the paragraphs in this collection.
    ///
    /// @return an iterator
    @Override
    public Iterator<BaseParagraph> iterator() {
        return items.iterator();
    }
}
