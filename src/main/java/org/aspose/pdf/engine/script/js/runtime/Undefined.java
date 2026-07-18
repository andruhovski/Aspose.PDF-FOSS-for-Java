package org.aspose.pdf.engine.script.js.runtime;

/// The ECMAScript `undefined` value (the sole instance of the Undefined type).
///
/// JavaScript values are represented as Java objects: `Undefined.INSTANCE`,
/// [JSNull#NULL], [Boolean], [Double], [String] and
/// [JSObject]. This singleton stands in for `undefined`.
public final class Undefined {

    /// The single `undefined` value.
    public static final Undefined INSTANCE = new Undefined();

    private Undefined() { }

    @Override
    public String toString() {
        return "undefined";
    }
}
