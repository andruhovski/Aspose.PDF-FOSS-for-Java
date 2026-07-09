package org.aspose.pdf.engine.script.js.parser;

/**
 * Thrown by the lexer or parser when the source text is not valid ECMAScript 3.
 *
 * <p>Carries a 1-based line/column so callers can report the offending
 * position. This is a compile-time (parse-time) failure, distinct from a
 * runtime {@code throw} (which surfaces as a {@code JSException}).</p>
 */
public class JSSyntaxError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 1-based line of the error. */
    public final int line;
    /** 1-based column of the error. */
    public final int column;

    /**
     * Creates a syntax error.
     *
     * @param message human-readable description
     * @param line    1-based line
     * @param column  1-based column
     */
    public JSSyntaxError(String message, int line, int column) {
        super("SyntaxError: " + message + " (line " + line + ", col " + column + ")");
        this.line = line;
        this.column = column;
    }
}
