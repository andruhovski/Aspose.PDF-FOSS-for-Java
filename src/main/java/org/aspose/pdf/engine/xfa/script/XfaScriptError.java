package org.aspose.pdf.engine.xfa.script;

/// Raised when an XFA script fails to parse or throws during execution. The Stage-B executor catches
/// this per script so one failing calculate/validate/event does not abort the whole load-time pass;
/// the failure is categorised in the execution result.
public final class XfaScriptError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /// @param message the failure description
    /// @param cause   the underlying cause
    public XfaScriptError(String message, Throwable cause) {
        super(message, cause);
    }
}
