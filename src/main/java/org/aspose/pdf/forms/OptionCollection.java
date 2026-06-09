package org.aspose.pdf.forms;

import org.aspose.pdf.engine.pdfobjects.*;
import java.util.*;

/**
 * Collection of options for choice fields (ComboBox/ListBox).
 * <p>
 * Wraps the /Opt {@link PdfArray}. Each element is either a simple string
 * (value = display name) or a two-element array [export-value, display-name]
 * per ISO 32000-1:2008, §12.7.4.4.
 * </p>
 */
public class OptionCollection implements Iterable<Option> {

    /** The underlying /Opt array. */
    private final PdfArray optArray;

    /**
     * Constructs an option collection from a /Opt PdfArray.
     *
     * @param optArray the /Opt array (must not be null)
     */
    public OptionCollection(PdfArray optArray) {
        this.optArray = optArray;
    }

    /**
     * Returns the number of options.
     *
     * @return the count
     */
    public int getCount() {
        return optArray.size();
    }

    /**
     * Returns the number of options (alias for {@link #getCount()}).
     *
     * @return the count
     */
    public int size() {
        return getCount();
    }

    /**
     * Returns the option at the given 0-based index.
     *
     * @param index the 0-based index
     * @return the option
     */
    public Option get(int index) {
        PdfBase item = optArray.get(index);
        if (item instanceof PdfArray) {
            PdfArray pair = (PdfArray) item;
            return new Option(
                    getString(pair.get(0)),
                    getString(pair.size() > 1 ? pair.get(1) : pair.get(0)));
        }
        String s = getString(item);
        return new Option(s, s);
    }

    /**
     * Returns an iterator over the options.
     *
     * @return the iterator
     */
    @Override
    public Iterator<Option> iterator() {
        return new Iterator<Option>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < optArray.size();
            }

            @Override
            public Option next() {
                return get(i++);
            }
        };
    }

    /**
     * Extracts a string from a PDF value (PdfString or PdfName).
     */
    private String getString(PdfBase val) {
        if (val instanceof PdfString) return ((PdfString) val).getString();
        if (val instanceof PdfName) return ((PdfName) val).getName();
        return "";
    }
}
