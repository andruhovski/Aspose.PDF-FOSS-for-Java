package org.aspose.pdf.engine.script.js.interp;

/// Thrown when a script exceeds the interpreter's execution-step budget
/// (see `-Dxfa.js.maxSteps`). An untrusted XFA form containing
/// `while(true){}` (or equivalent non-terminating code) would otherwise
/// hang the processing thread indefinitely — a denial of service.
///
/// Deliberately a plain [RuntimeException], NOT a [JSException]:
/// a script-level `try/catch` must not be able to swallow the budget
/// abort and keep looping. The script _host_ (XFA calculate/validate
/// execution) catches `RuntimeException` and reports the script as
/// failed, exactly like any other script error, then continues processing.
public class JsExecutionLimitError extends RuntimeException {

    /// Creates the error.
    ///
    /// @param message description including the exceeded budget
    public JsExecutionLimitError(String message) {
        super(message);
    }
}
