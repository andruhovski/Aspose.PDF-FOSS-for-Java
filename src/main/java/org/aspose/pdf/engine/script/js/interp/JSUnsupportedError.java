package org.aspose.pdf.engine.script.js.interp;

/**
 * Thrown when the interpreter encounters an ES3 construct it does not (yet)
 * implement. This is a loud, typed failure carrying a source position &mdash;
 * the engine never silently ignores unsupported syntax.
 */
public final class JSUnsupportedError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 1-based source line. */
    public final int line;
    /** 1-based source column. */
    public final int column;

    /**
     * Creates an unsupported-construct error.
     *
     * @param message description of the unsupported construct
     * @param line    1-based line
     * @param column  1-based column
     */
    public JSUnsupportedError(String message, int line, int column) {
        super("Unsupported: " + message + " (line " + line + ", col " + column + ")");
        this.line = line;
        this.column = column;
    }
}
