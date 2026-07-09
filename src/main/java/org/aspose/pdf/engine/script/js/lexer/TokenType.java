package org.aspose.pdf.engine.script.js.lexer;

/**
 * Lexical token categories for the ECMAScript 3 grammar (ECMA-262, 3rd ed.).
 *
 * <p>Keywords, reserved words, boolean literals and the null literal are all
 * reported as {@link #KEYWORD}; the concrete word is carried in
 * {@link Token#value}. Punctuators are reported as {@link #PUNCT} with the
 * operator text in {@link Token#value}. This keeps the enum small while the
 * recursive-descent parser switches on the lexeme text.</p>
 */
public enum TokenType {
    /** Numeric literal (decimal, hex, legacy octal, float, exponent). */
    NUMBER,
    /** String literal with all escapes already decoded into {@link Token#value}. */
    STRING,
    /** Regular-expression literal; body in {@link Token#value}, flags in {@link Token#regexFlags}. */
    REGEXP,
    /** Identifier (not a keyword or reserved word). */
    IDENT,
    /** Keyword, future-reserved word, {@code true}/{@code false} or {@code null}. */
    KEYWORD,
    /** Punctuator / operator. */
    PUNCT,
    /** End of input. */
    EOF
}
