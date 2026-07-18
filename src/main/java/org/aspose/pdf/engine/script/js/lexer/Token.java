package org.aspose.pdf.engine.script.js.lexer;

/// A single lexical token produced by [Lexer].
///
/// Immutable value holder. For [TokenType#STRING] the [#value]
/// field holds the decoded string; for [TokenType#NUMBER] the numeric
/// value is in [#number] (and [#value] holds the raw source); for
/// [TokenType#REGEXP] [#value] holds the pattern body and
/// [#regexFlags] the flag characters.
public final class Token {

    /// Token category.
    public final TokenType type;
    /// Lexeme text (identifiers/keywords/punctuators) or decoded string / regex body.
    public final String value;
    /// Numeric value for [TokenType#NUMBER]; otherwise `0`.
    public final double number;
    /// Flag characters for [TokenType#REGEXP]; otherwise `null`.
    public final String regexFlags;
    /// 1-based line of the first character.
    public final int line;
    /// 1-based column of the first character.
    public final int column;
    /// Character offset (0-based) of the first character in the source.
    public final int start;
    /// `true` if a line terminator occurred between the previous token and this one (drives ASI).
    public final boolean newlineBefore;

    /// Creates a token.
    ///
    /// @param type          token category
    /// @param value         lexeme / decoded string / regex body
    /// @param number        numeric value (for NUMBER tokens)
    /// @param regexFlags    regex flags (for REGEXP tokens) or `null`
    /// @param line          1-based line
    /// @param column        1-based column
    /// @param start         0-based character offset
    /// @param newlineBefore whether a newline preceded this token
    public Token(TokenType type, String value, double number, String regexFlags,
                 int line, int column, int start, boolean newlineBefore) {
        this.type = type;
        this.value = value;
        this.number = number;
        this.regexFlags = regexFlags;
        this.line = line;
        this.column = column;
        this.start = start;
        this.newlineBefore = newlineBefore;
    }

    /// Tests whether this token is a punctuator with the given text.
    ///
    /// @param p punctuator text
    /// @return `true` if matched
    public boolean isPunct(String p) {
        return type == TokenType.PUNCT && value.equals(p);
    }

    /// Tests whether this token is a keyword with the given text.
    ///
    /// @param k keyword text
    /// @return `true` if matched
    public boolean isKeyword(String k) {
        return type == TokenType.KEYWORD && value.equals(k);
    }

    @Override
    public String toString() {
        return type + "(" + value + ")@" + line + ":" + column;
    }
}
