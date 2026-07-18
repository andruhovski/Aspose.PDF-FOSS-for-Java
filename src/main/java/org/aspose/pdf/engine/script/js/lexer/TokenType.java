package org.aspose.pdf.engine.script.js.lexer;

/// Lexical token categories for the ECMAScript 3 grammar (ECMA-262, 3rd ed.).
///
/// Keywords, reserved words, boolean literals and the null literal are all
/// reported as [#KEYWORD]; the concrete word is carried in
/// [Token#value]. Punctuators are reported as [#PUNCT] with the
/// operator text in [Token#value]. This keeps the enum small while the
/// recursive-descent parser switches on the lexeme text.
public enum TokenType {
    /// Numeric literal (decimal, hex, legacy octal, float, exponent).
    NUMBER,
    /// String literal with all escapes already decoded into [Token#value].
    STRING,
    /// Regular-expression literal; body in [Token#value], flags in [Token#regexFlags].
    REGEXP,
    /// Identifier (not a keyword or reserved word).
    IDENT,
    /// Keyword, future-reserved word, `true`/`false` or `null`.
    KEYWORD,
    /// Punctuator / operator.
    PUNCT,
    /// End of input.
    EOF
}
