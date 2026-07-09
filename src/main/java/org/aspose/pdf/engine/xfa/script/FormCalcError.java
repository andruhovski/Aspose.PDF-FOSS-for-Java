package org.aspose.pdf.engine.xfa.script;

/**
 * Raised on a FormCalc lex / parse / evaluate failure (B2). Mirrors {@link XfaScriptError}: a runtime
 * exception so {@link XfaScripting} catches and categorises it per script (the run never aborts).
 */
final class FormCalcError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    FormCalcError(String message) {
        super(message);
    }

    FormCalcError(String message, Throwable cause) {
        super(message, cause);
    }
}
