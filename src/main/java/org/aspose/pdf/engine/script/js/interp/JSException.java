package org.aspose.pdf.engine.script.js.interp;

/// Carries a value thrown by ECMAScript `throw` (or by a built-in raising
/// a native error) up the Java stack until a `try`/`catch` handles
/// it or it escapes `Engine.eval`.
///
/// The thrown value can be any JS value (commonly an Error object), held in
/// [#value] as the engine's Java representation.
public final class JSException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /// The thrown JavaScript value.
    public final transient Object value;

    /// Creates a JS exception wrapping a thrown value.
    ///
    /// @param value the thrown JS value
    public JSException(Object value) {
        super(String.valueOf(value), null, false, false);
        this.value = value;
    }
}
