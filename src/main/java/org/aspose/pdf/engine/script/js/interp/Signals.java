package org.aspose.pdf.engine.script.js.interp;

/// Internal abrupt-completion signals (ECMA-262 3rd ed., sec 8.9) used by the
/// tree-walking interpreter to implement `return`, `break` and
/// `continue`. They extend [RuntimeException] with stack traces
/// suppressed for speed; they never escape `Engine.eval`.
final class Signals {
    private Signals() { }
}

/// Non-local return carrying the return value.
final class ReturnSignal extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final transient Object value;
    ReturnSignal(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}

/// `break`, optionally to a label.
final class BreakSignal extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final String label;
    BreakSignal(String label) {
        super(null, null, false, false);
        this.label = label;
    }
}

/// `continue`, optionally to a label.
final class ContinueSignal extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final String label;
    ContinueSignal(String label) {
        super(null, null, false, false);
        this.label = label;
    }
}
