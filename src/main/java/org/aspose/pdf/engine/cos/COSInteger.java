package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * PDF integer object (§7.3.3, ISO 32000-1:2008).
 * <p>
 * Represents an integer number backed by {@code long}. Values 0..255 are cached
 * using the Flyweight pattern for performance.
 * </p>
 */
public final class COSInteger extends COSBase {

    private static final Logger LOG = Logger.getLogger(COSInteger.class.getName());

    private final long value;

    private static final int CACHE_SIZE = 256;
    private static final COSInteger[] CACHE = new COSInteger[CACHE_SIZE];

    static {
        for (int i = 0; i < CACHE_SIZE; i++) {
            CACHE[i] = new COSInteger(i);
        }
    }

    /** The constant for value 0. */
    public static final COSInteger ZERO = CACHE[0];

    /** The constant for value 1. */
    public static final COSInteger ONE = CACHE[1];

    private COSInteger(long value) {
        this.value = value;
    }

    /**
     * Returns a COSInteger for the given value. Values 0..255 are cached.
     *
     * @param value the integer value
     * @return a COSInteger instance
     */
    public static COSInteger valueOf(long value) {
        if (value >= 0 && value < CACHE_SIZE) {
            return CACHE[(int) value];
        }
        return new COSInteger(value);
    }

    /**
     * Returns the value as a {@code long}.
     *
     * @return the long value
     */
    public long longValue() {
        return value;
    }

    /**
     * Returns the value as an {@code int}, throwing if it overflows.
     *
     * @return the int value
     * @throws ArithmeticException if the value does not fit in an int
     */
    public int intValue() {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ArithmeticException("Value " + value + " out of int range");
        }
        return (int) value;
    }

    /**
     * Returns the value as a {@code float}.
     *
     * @return the float value
     */
    public float floatValue() {
        return (float) value;
    }

    /**
     * Protects cached flyweight instances from object key assignment.
     * Non-cached instances behave normally.
     */
    @Override
    public void setObjectKey(COSObjectKey key) {
        if (value >= 0 && value < CACHE_SIZE) {
            // Flyweight: ignore object key assignment to protect cached instance
            return;
        }
        super.setObjectKey(key);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(Long.toString(value).getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        return visitor.visitInteger(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSInteger)) return false;
        return value == ((COSInteger) o).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return "COSInteger{" + value + "}";
    }
}
