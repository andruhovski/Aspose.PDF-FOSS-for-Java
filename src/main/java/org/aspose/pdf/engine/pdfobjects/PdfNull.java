package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/// PDF null object (§7.3.9, ISO 32000-1:2008).
///
/// Represents the single "empty" value. Equivalent to a missing entry in a dictionary.
/// Singleton: use [#INSTANCE] or [#getInstance()].
///
public final class PdfNull extends PdfBase {

    private static final Logger LOG = Logger.getLogger(PdfNull.class.getName());

    private static final byte[] BYTES = {'n', 'u', 'l', 'l'};

    /// The singleton null instance.
    public static final PdfNull INSTANCE = new PdfNull();

    private PdfNull() {
    }

    /// Returns the singleton null instance.
    ///
    /// @return the null instance
    public static PdfNull getInstance() {
        return INSTANCE;
    }

    /// Flyweight singleton — object key assignment is ignored.
    @Override
    public void setObjectKey(PdfObjectKey key) {
        // Flyweight: ignore object key assignment to protect singleton
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(BYTES);
    }

    @Override
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitNull(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PdfNull;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "null";
    }
}
