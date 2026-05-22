package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * PDF null object (§7.3.9, ISO 32000-1:2008).
 * <p>
 * Represents the single "empty" value. Equivalent to a missing entry in a dictionary.
 * Singleton: use {@link #INSTANCE} or {@link #getInstance()}.
 * </p>
 */
public final class COSNull extends COSBase {

    private static final Logger LOG = Logger.getLogger(COSNull.class.getName());

    private static final byte[] BYTES = {'n', 'u', 'l', 'l'};

    /** The singleton null instance. */
    public static final COSNull INSTANCE = new COSNull();

    private COSNull() {
    }

    /**
     * Returns the singleton null instance.
     *
     * @return the null instance
     */
    public static COSNull getInstance() {
        return INSTANCE;
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
        os.write(BYTES);
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        return visitor.visitNull(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof COSNull;
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
