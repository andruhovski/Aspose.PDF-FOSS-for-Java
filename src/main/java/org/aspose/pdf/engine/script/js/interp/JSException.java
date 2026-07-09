package org.aspose.pdf.engine.script.js.interp;

/**
 * Carries a value thrown by ECMAScript {@code throw} (or by a built-in raising
 * a native error) up the Java stack until a {@code try}/{@code catch} handles
 * it or it escapes {@code Engine.eval}.
 *
 * <p>The thrown value can be any JS value (commonly an Error object), held in
 * {@link #value} as the engine's Java representation.</p>
 */
public final class JSException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The thrown JavaScript value. */
    public final transient Object value;

    /**
     * Creates a JS exception wrapping a thrown value.
     *
     * @param value the thrown JS value
     */
    public JSException(Object value) {
        super(String.valueOf(value), null, false, false);
        this.value = value;
    }
}
