package org.aspose.pdf.engine.parser;

/// Represents a single cross-reference entry in a PDF file.
/// An entry maps an object key (number + generation) to its location,
/// as defined in ISO 32000-1:2008, §7.5.4 and §7.5.8.
///
/// Three types of entries exist:
///
///   - [Type#FREE] — the object has been deleted
///   - [Type#IN\_USE] — the object exists at a byte offset in the file
///   - [Type#COMPRESSED] — the object is stored inside an object stream
public final class XRefEntry {

    /// The type of cross-reference entry.
    public enum Type {
        /// Free (deleted) object.
        FREE,
        /// In-use object at a specific byte offset.
        IN_USE,
        /// Compressed object stored in an object stream.
        COMPRESSED
    }

    private final Type type;
    private final int objectNumber;
    private final int generationNumber;
    private final long byteOffset;
    private final int objectStreamNumber;
    private final int indexWithinStream;

    private XRefEntry(Type type, int objectNumber, int generationNumber,
                      long byteOffset, int objectStreamNumber, int indexWithinStream) {
        this.type = type;
        this.objectNumber = objectNumber;
        this.generationNumber = generationNumber;
        this.byteOffset = byteOffset;
        this.objectStreamNumber = objectStreamNumber;
        this.indexWithinStream = indexWithinStream;
    }

    /// Creates an in-use entry with a byte offset in the file.
    ///
    /// @param objectNumber     the object number
    /// @param generationNumber the generation number
    /// @param byteOffset       the byte offset in the PDF file
    /// @return a new in-use XRefEntry
    public static XRefEntry inUse(int objectNumber, int generationNumber, long byteOffset) {
        return new XRefEntry(Type.IN_USE, objectNumber, generationNumber, byteOffset, 0, 0);
    }

    /// Creates a free entry (deleted object).
    ///
    /// @param objectNumber       the object number
    /// @param generationNumber   the generation number
    /// @param nextFreeObjectNumber the next free object number in the free list
    /// @return a new free XRefEntry
    public static XRefEntry free(int objectNumber, int generationNumber, long nextFreeObjectNumber) {
        return new XRefEntry(Type.FREE, objectNumber, generationNumber, nextFreeObjectNumber, 0, 0);
    }

    /// Creates a compressed entry stored in an object stream.
    ///
    /// @param objectNumber       the object number
    /// @param objectStreamNumber the object number of the object stream containing this object
    /// @param indexWithinStream  the index of this object within the object stream
    /// @return a new compressed XRefEntry
    public static XRefEntry compressed(int objectNumber, int objectStreamNumber, int indexWithinStream) {
        return new XRefEntry(Type.COMPRESSED, objectNumber, 0, 0, objectStreamNumber, indexWithinStream);
    }

    /// Returns the entry type.
    public Type getType() {
        return type;
    }

    /// Returns the object number.
    public int getObjectNumber() {
        return objectNumber;
    }

    /// Returns the generation number.
    public int getGenerationNumber() {
        return generationNumber;
    }

    /// Returns the byte offset for IN\_USE entries.
    public long getByteOffset() {
        return byteOffset;
    }

    /// Returns the object stream number for COMPRESSED entries.
    public int getObjectStreamNumber() {
        return objectStreamNumber;
    }

    /// Returns the index within the object stream for COMPRESSED entries.
    public int getIndexWithinStream() {
        return indexWithinStream;
    }

    @Override
    public String toString() {
        switch (type) {
            case FREE:
                return "XRefEntry{FREE, obj=" + objectNumber + ", gen=" + generationNumber + "}";
            case IN_USE:
                return "XRefEntry{IN_USE, obj=" + objectNumber + ", gen=" + generationNumber
                        + ", offset=" + byteOffset + "}";
            case COMPRESSED:
                return "XRefEntry{COMPRESSED, obj=" + objectNumber
                        + ", stream=" + objectStreamNumber + ", idx=" + indexWithinStream + "}";
            default:
                return "XRefEntry{UNKNOWN}";
        }
    }
}
