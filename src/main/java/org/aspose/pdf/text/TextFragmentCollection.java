package org.aspose.pdf.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of {@link TextFragment}s extracted from PDF pages.
 * <p>
 * Uses <strong>1-based indexing</strong> to match the Aspose.PDF API convention:
 * {@code get(1)} returns the first fragment.
 * </p>
 */
public class TextFragmentCollection implements Iterable<TextFragment> {

    private final List<TextFragment> fragments;

    /**
     * Creates an empty TextFragmentCollection.
     */
    public TextFragmentCollection() {
        this.fragments = new ArrayList<>();
    }

    /**
     * Returns the text fragment at the given 1-based index.
     *
     * @param index the 1-based index
     * @return the text fragment
     * @throws IndexOutOfBoundsException if index is out of range [1, getCount()]
     */
    public TextFragment get(int index) {
        if (index < 1 || index > fragments.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of range [1, " + fragments.size() + "]");
        }
        return fragments.get(index - 1);
    }

    /**
     * Returns the number of fragments in this collection.
     *
     * @return the fragment count
     */
    public int getCount() {
        return fragments.size();
    }

    /**
     * Returns the number of fragments (alias for {@link #getCount()}).
     *
     * @return the fragment count
     */
    public int size() {
        return fragments.size();
    }

    @Override
    public Iterator<TextFragment> iterator() {
        return fragments.iterator();
    }

    /**
     * Adds a text fragment to this collection.
     *
     * @param fragment the fragment to add
     */
    public void add(TextFragment fragment) {
        if (fragment != null) {
            fragments.add(fragment);
        }
    }

    /**
     * Removes the specified text fragment from this collection.
     *
     * @param fragment the fragment to remove
     * @return {@code true} if the fragment was found and removed
     */
    public boolean remove(TextFragment fragment) {
        return fragments.remove(fragment);
    }

    /**
     * Removes the text fragment at the given 1-based index.
     *
     * @param index the 1-based index
     * @return the removed fragment
     * @throws IndexOutOfBoundsException if index is out of range [1, getCount()]
     */
    public TextFragment remove(int index) {
        if (index < 1 || index > fragments.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of range [1, " + fragments.size() + "]");
        }
        return fragments.remove(index - 1);
    }

    /**
     * Removes all fragments from this collection.
     */
    public void clear() {
        fragments.clear();
    }

    /**
     * Returns whether this collection contains the specified fragment.
     *
     * @param fragment the fragment to check
     * @return {@code true} if contained
     */
    public boolean contains(TextFragment fragment) {
        return fragments.contains(fragment);
    }
}
