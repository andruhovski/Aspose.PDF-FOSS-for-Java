package org.aspose.pdf.engine.pdfobjects;

/// Key identifying an indirect PDF object by its object number and generation number.
///
/// In PDF, every indirect object is uniquely identified by a pair (objectNumber, generationNumber).
/// See ISO 32000-1:2008, §7.3.10.
///
public final class PdfObjectKey implements Comparable<PdfObjectKey> {

    private final int objectNumber;
    private final int generationNumber;

    /// Creates a new indirect object key.
    ///
    /// @param objectNumber     the object number (must be ≥ 1 for valid indirect objects)
    /// @param generationNumber the generation number (0..65535)
    /// @throws IllegalArgumentException if objectNumber < 0 or generationNumber < 0 or > 65535
    public PdfObjectKey(int objectNumber, int generationNumber) {
        if (objectNumber < 0) {
            throw new IllegalArgumentException("Object number must be non-negative: " + objectNumber);
        }
        if (generationNumber < 0 || generationNumber > 65535) {
            throw new IllegalArgumentException(
                "Generation number must be in [0, 65535]: " + generationNumber);
        }
        this.objectNumber = objectNumber;
        this.generationNumber = generationNumber;
    }

    /// Returns the object number.
    ///
    /// @return the object number
    public int getObjectNumber() {
        return objectNumber;
    }

    /// Returns the generation number.
    ///
    /// @return the generation number
    public int getGenerationNumber() {
        return generationNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfObjectKey)) return false;
        PdfObjectKey that = (PdfObjectKey) o;
        return objectNumber == that.objectNumber && generationNumber == that.generationNumber;
    }

    @Override
    public int hashCode() {
        return 31 * objectNumber + generationNumber;
    }

    @Override
    public int compareTo(PdfObjectKey other) {
        int cmp = Integer.compare(this.objectNumber, other.objectNumber);
        if (cmp != 0) return cmp;
        return Integer.compare(this.generationNumber, other.generationNumber);
    }

    @Override
    public String toString() {
        return objectNumber + " " + generationNumber;
    }
}
