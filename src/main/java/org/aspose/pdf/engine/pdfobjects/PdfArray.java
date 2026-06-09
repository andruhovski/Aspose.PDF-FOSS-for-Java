package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * PDF array object (§7.3.6, ISO 32000-1:2008).
 * <p>
 * An ordered collection of PDF objects. Elements may be of different types.
 * Provides typed getters for convenience to avoid casts in client code.
 * </p>
 */
public class PdfArray extends PdfBase implements Iterable<PdfBase> {

    private static final Logger LOG = Logger.getLogger(PdfArray.class.getName());

    private final List<PdfBase> items;

    /**
     * Creates an empty array with default initial capacity.
     */
    public PdfArray() {
        this.items = new ArrayList<>(8);
    }

    /**
     * Creates an empty array with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public PdfArray(int initialCapacity) {
        this.items = new ArrayList<>(initialCapacity);
    }

    /**
     * Returns the number of elements.
     *
     * @return the size
     */
    public int size() {
        return items.size();
    }

    /**
     * Returns whether the array is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the element at the given index.
     *
     * @param index the index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public PdfBase get(int index) {
        return items.get(index);
    }

    /**
     * Appends an element to the end.
     *
     * @param item the element to add
     * @throws IllegalArgumentException if item is null
     */
    public void add(PdfBase item) {
        if (item == null) {
            item = PdfNull.INSTANCE;
        }
        items.add(item);
        markDirty();
    }

    /**
     * Inserts an element at the given index.
     *
     * @param index the insertion index
     * @param item  the element to insert; null is treated as PdfNull
     */
    public void add(int index, PdfBase item) {
        if (item == null) {
            item = PdfNull.INSTANCE;
        }
        items.add(index, item);
        markDirty();
    }

    /**
     * Replaces the element at the given index.
     *
     * @param index the index
     * @param item  the new element; null is treated as PdfNull
     * @return the previous element
     */
    public PdfBase set(int index, PdfBase item) {
        if (item == null) {
            item = PdfNull.INSTANCE;
        }
        markDirty();
        return items.set(index, item);
    }

    /**
     * Removes the element at the given index.
     *
     * @param index the index
     * @return the removed element
     */
    public PdfBase remove(int index) {
        markDirty();
        return items.remove(index);
    }

    /**
     * Removes all elements.
     */
    public void clear() {
        items.clear();
        markDirty();
    }

    /**
     * Returns the element at the given index as an int, or the default value
     * if the element is not a numeric type.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the int value or default
     */
    public int getInt(int index, int defaultValue) {
        if (index < 0 || index >= items.size()) return defaultValue;
        PdfBase obj = items.get(index);
        if (obj instanceof PdfInteger) return ((PdfInteger) obj).intValue();
        if (obj instanceof PdfFloat) return (int) ((PdfFloat) obj).doubleValue();
        return defaultValue;
    }

    /**
     * Returns the element at the given index as a long, or the default value.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the long value or default
     */
    public long getLong(int index, long defaultValue) {
        if (index < 0 || index >= items.size()) return defaultValue;
        PdfBase obj = items.get(index);
        if (obj instanceof PdfInteger) return ((PdfInteger) obj).longValue();
        if (obj instanceof PdfFloat) return (long) ((PdfFloat) obj).doubleValue();
        return defaultValue;
    }

    /**
     * Returns the element at the given index as a float, or the default value.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the float value or default
     */
    public float getFloat(int index, float defaultValue) {
        if (index < 0 || index >= items.size()) return defaultValue;
        PdfBase obj = items.get(index);
        if (obj instanceof PdfFloat) return ((PdfFloat) obj).floatValue();
        if (obj instanceof PdfInteger) return ((PdfInteger) obj).floatValue();
        return defaultValue;
    }

    /**
     * Returns the element at the given index as a name string, or null.
     *
     * @param index the index
     * @return the name value or null
     */
    public String getName(int index) {
        if (index < 0 || index >= items.size()) return null;
        PdfBase obj = items.get(index);
        return (obj instanceof PdfName) ? ((PdfName) obj).getName() : null;
    }

    /**
     * Returns the element at the given index as a string value, or null.
     *
     * @param index the index
     * @return the string value or null
     */
    public String getString(int index) {
        if (index < 0 || index >= items.size()) return null;
        PdfBase obj = items.get(index);
        return (obj instanceof PdfString) ? ((PdfString) obj).getString() : null;
    }

    /**
     * Returns the element at the given index as a PdfDictionary, or null.
     *
     * @param index the index
     * @return the dictionary or null
     */
    public PdfDictionary getDictionary(int index) {
        if (index < 0 || index >= items.size()) return null;
        PdfBase obj = items.get(index);
        return (obj instanceof PdfDictionary) ? (PdfDictionary) obj : null;
    }

    /**
     * Returns the element at the given index as a PdfArray, or null.
     *
     * @param index the index
     * @return the array or null
     */
    public PdfArray getArray(int index) {
        if (index < 0 || index >= items.size()) return null;
        PdfBase obj = items.get(index);
        return (obj instanceof PdfArray) ? (PdfArray) obj : null;
    }

    /**
     * Converts this array to a float[]. Non-numeric elements become 0f.
     *
     * @return the float array
     */
    public float[] toFloatArray() {
        float[] result = new float[items.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getFloat(i, 0f);
        }
        return result;
    }

    /**
     * Converts this array to an int[]. Non-numeric elements become 0.
     *
     * @return the int array
     */
    public int[] toIntArray() {
        int[] result = new int[items.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getInt(i, 0);
        }
        return result;
    }

    @Override
    public Iterator<PdfBase> iterator() {
        return items.iterator();
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        if (!enterWriting(this)) {
            // Direct cyclic reference back to this array (rare, but possible
            // after in-place mutation). Emit /null rather than blow the stack.
            os.write(new byte[]{'n', 'u', 'l', 'l'});
            return;
        }
        try {
            os.write('[');
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    os.write(' ');
                }
                PdfBase item = items.get(i);
                // If the item is an indirect object, write as reference to prevent cycles
                if (item != null && !(item instanceof PdfObjectReference)
                        && item.getObjectKey() != null && item.getObjectKey().getObjectNumber() > 0) {
                    String ref = item.getObjectKey().getObjectNumber() + " "
                            + item.getObjectKey().getGenerationNumber() + " R";
                    os.write(ref.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                } else {
                    item.writeTo(os);
                }
            }
            os.write(']');
        } finally {
            leaveWriting(this);
        }
    }

    @Override
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitArray(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfArray)) return false;
        return items.equals(((PdfArray) o).items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        return "PdfArray{size=" + items.size() + "}";
    }
}
