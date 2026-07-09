package org.aspose.pdf.engine.script.js.interp;

/**
 * Thrown when a script exceeds the interpreter's execution-step budget
 * (see {@code -Dxfa.js.maxSteps}). An untrusted XFA form containing
 * {@code while(true){}} (or equivalent non-terminating code) would otherwise
 * hang the processing thread indefinitely — a denial of service.
 *
 * <p>Deliberately a plain {@link RuntimeException}, NOT a {@link JSException}:
 * a script-level {@code try/catch} must not be able to swallow the budget
 * abort and keep looping. The script <em>host</em> (XFA calculate/validate
 * execution) catches {@code RuntimeException} and reports the script as
 * failed, exactly like any other script error, then continues processing.</p>
 */
public class JsExecutionLimitError extends RuntimeException {

    /**
     * Creates the error.
     *
     * @param message description including the exceeded budget
     */
    public JsExecutionLimitError(String message) {
        super(message);
    }
}
