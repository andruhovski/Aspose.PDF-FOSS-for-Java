package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/// PDF boolean object (§7.3.2, ISO 32000-1:2008).
///
/// Represents the values `true` and `false`. Uses the Flyweight pattern:
/// exactly two instances exist ([#TRUE] and [#FALSE]).
///
public final class PdfBoolean extends PdfBase {

    private static final Logger LOG = Logger.getLogger(PdfBoolean.class.getName());

    private final boolean value;

    private static final byte[] BYTES_TRUE = {'t', 'r', 'u', 'e'};
    private static final byte[] BYTES_FALSE = {'f', 'a', 'l', 's', 'e'};

    /// The singleton `true` instance.
    public static final PdfBoolean TRUE = new PdfBoolean(true);

    /// The singleton `false` instance.
    public static final PdfBoolean FALSE = new PdfBoolean(false);

    private PdfBoolean(boolean value) {
        this.value = value;
    }

    /// Returns the singleton instance for the given boolean value.
    ///
    /// @param b the boolean value
    /// @return [#TRUE] or [#FALSE]
    public static PdfBoolean valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    /// Returns the boolean value.
    ///
    /// @return the value
    public boolean getValue() {
        return value;
    }

    /// Flyweight singleton — object key assignment is ignored.
    @Override
    public void setObjectKey(PdfObjectKey key) {
        // Flyweight: ignore object key assignment to protect singleton
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(value ? BYTES_TRUE : BYTES_FALSE);
    }

    @Override
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitBoolean(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfBoolean)) return false;
        return value == ((PdfBoolean) o).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public String toString() {
        return "PdfBoolean{" + value + "}";
    }
}
