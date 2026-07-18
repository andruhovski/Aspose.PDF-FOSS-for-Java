package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/// PDF integer object (§7.3.3, ISO 32000-1:2008).
///
/// Represents an integer number backed by `long`. Values 0..255 are cached
/// using the Flyweight pattern for performance.
///
public final class PdfInteger extends PdfBase {

    private static final Logger LOG = Logger.getLogger(PdfInteger.class.getName());

    private final long value;

    private static final int CACHE_SIZE = 256;
    private static final PdfInteger[] CACHE = new PdfInteger[CACHE_SIZE];

    static {
        for (int i = 0; i < CACHE_SIZE; i++) {
            CACHE[i] = new PdfInteger(i);
        }
    }

    /// The constant for value 0.
    public static final PdfInteger ZERO = CACHE[0];

    /// The constant for value 1.
    public static final PdfInteger ONE = CACHE[1];

    private PdfInteger(long value) {
        this.value = value;
    }

    /// Returns a PdfInteger for the given value. Values 0..255 are cached.
    ///
    /// @param value the integer value
    /// @return a PdfInteger instance
    public static PdfInteger valueOf(long value) {
        if (value >= 0 && value < CACHE_SIZE) {
            return CACHE[(int) value];
        }
        return new PdfInteger(value);
    }

    /// Returns the value as a `long`.
    ///
    /// @return the long value
    public long longValue() {
        return value;
    }

    /// Returns the value as an `int`, throwing if it overflows.
    ///
    /// @return the int value
    /// @throws ArithmeticException if the value does not fit in an int
    public int intValue() {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ArithmeticException("Value " + value + " out of int range");
        }
        return (int) value;
    }

    /// Returns the value as a `float`.
    ///
    /// @return the float value
    public float floatValue() {
        return (float) value;
    }

    /// Protects cached flyweight instances from object key assignment.
    /// Non-cached instances behave normally.
    @Override
    public void setObjectKey(PdfObjectKey key) {
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
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitInteger(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfInteger)) return false;
        return value == ((PdfInteger) o).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return "PdfInteger{" + value + "}";
    }
}
