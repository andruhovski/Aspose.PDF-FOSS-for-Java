package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * PDF boolean object (§7.3.2, ISO 32000-1:2008).
 * <p>
 * Represents the values {@code true} and {@code false}. Uses the Flyweight pattern:
 * exactly two instances exist ({@link #TRUE} and {@link #FALSE}).
 * </p>
 */
public final class COSBoolean extends COSBase {

    private static final Logger LOG = Logger.getLogger(COSBoolean.class.getName());

    private final boolean value;

    private static final byte[] BYTES_TRUE = {'t', 'r', 'u', 'e'};
    private static final byte[] BYTES_FALSE = {'f', 'a', 'l', 's', 'e'};

    /** The singleton {@code true} instance. */
    public static final COSBoolean TRUE = new COSBoolean(true);

    /** The singleton {@code false} instance. */
    public static final COSBoolean FALSE = new COSBoolean(false);

    private COSBoolean(boolean value) {
        this.value = value;
    }

    /**
     * Returns the singleton instance for the given boolean value.
     *
     * @param b the boolean value
     * @return {@link #TRUE} or {@link #FALSE}
     */
    public static COSBoolean valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    /**
     * Returns the boolean value.
     *
     * @return the value
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Flyweight singleton — object key assignment is ignored.
     */
    @Override
    public void setObjectKey(COSObjectKey key) {
        // Flyweight: ignore object key assignment to protect singleton
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(value ? BYTES_TRUE : BYTES_FALSE);
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        return visitor.visitBoolean(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSBoolean)) return false;
        return value == ((COSBoolean) o).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public String toString() {
        return "COSBoolean{" + value + "}";
    }
}
