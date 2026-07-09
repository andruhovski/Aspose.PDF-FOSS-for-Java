package org.aspose.pdf.engine.script.js.runtime;

/**
 * The ECMAScript {@code undefined} value (the sole instance of the Undefined type).
 *
 * <p>JavaScript values are represented as Java objects: {@code Undefined.INSTANCE},
 * {@link JSNull#NULL}, {@link Boolean}, {@link Double}, {@link String} and
 * {@link JSObject}. This singleton stands in for {@code undefined}.</p>
 */
public final class Undefined {

    /** The single {@code undefined} value. */
    public static final Undefined INSTANCE = new Undefined();

    private Undefined() { }

    @Override
    public String toString() {
        return "undefined";
    }
}
