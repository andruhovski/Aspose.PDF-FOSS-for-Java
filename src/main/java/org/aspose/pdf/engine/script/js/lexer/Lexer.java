package org.aspose.pdf.engine.script.js.lexer;

import org.aspose.pdf.engine.script.js.parser.JSSyntaxError;

import java.util.*;

/// Hand-written lexer for the ECMAScript 3 lexical grammar (ECMA-262 3rd ed., sec 7).
///
/// Produces the complete token list in one pass. Regular-expression literals
/// are disambiguated from the division operator using the previous significant
/// token (sec 7.8.5 note): a `/` begins a regex unless the previous token
/// is one after which division is legal (an operand: identifier, literal,
/// `)`, `]`, ``} or `this`).
///
/// This lexer is independent of `engine.parser.PDFLexer`.
public final class Lexer {

    private static final int ZWNJ = 0x200C;
    private static final int ZWJ = 0x200D;
    private static final int NBSP = 0x00A0;
    private static final int BOM = 0xFEFF;
    private static final int VTAB = 0x000B;
    private static final int LS = 0x2028;
    private static final int PS = 0x2029;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            // ES3 keywords (sec 7.5.2)
            "break", "case", "catch", "continue", "default", "delete", "do",
            "else", "finally", "for", "function", "if", "in", "instanceof",
            "new", "return", "switch", "this", "throw", "try", "typeof", "var",
            "void", "while", "with",
            // ES3 future reserved words (sec 7.5.3)
            "abstract", "boolean", "byte", "char", "class", "const", "debugger",
            "double", "enum", "export", "extends", "final", "float", "goto",
            "implements", "import", "int", "interface", "long", "native",
            "package", "private", "protected", "public", "short", "static",
            "super", "synchronized", "throws", "transient", "volatile",
            // literals that are reserved words lexically
            "null", "true", "false"));

    private final String src;
    private final int n;
    private int pos;
    private int line = 1;
    private int col = 1;
    private boolean pendingNewline;

    /// Creates a lexer over the given source.
    ///
    /// @param source ECMAScript 3 source text (must not be `null`)
    public Lexer(String source) {
        this.src = source == null ? "" : source;
        this.n = this.src.length();
    }

    /// Tokenizes the entire source.
    ///
    /// @return list of tokens terminated by a single [TokenType#EOF] token
    /// @throws JSSyntaxError on a lexical error
    public List<Token> tokenize() {
        List<Token> out = new ArrayList<>();
        Token prev = null;
        while (true) {
            skipTrivia();
            if (pos >= n) {
                out.add(new Token(TokenType.EOF, "", 0, null, line, col, pos, pendingNewline));
                return out;
            }
            Token t = scan(prev);
            out.add(t);
            prev = t;
        }
    }

    /* ------------------------------------------------------------------ */

    private Token scan(Token prev) {
        boolean nl = pendingNewline;
        pendingNewline = false;
        int sl = line;
        int sc = col;
        int sp = pos;
        char c = src.charAt(pos);

        if (isIdentStart(c)) {
            return scanIdentifier(sl, sc, sp, nl);
        }
        if (c >= '0' && c <= '9') {
            return scanNumber(sl, sc, sp, nl);
        }
        if (c == '.' && pos + 1 < n && Character.isDigit(src.charAt(pos + 1))) {
            return scanNumber(sl, sc, sp, nl);
        }
        if (c == '"' || c == '\'') {
            return scanString(sl, sc, sp, nl);
        }
        if (c == '/' && regexAllowed(prev)) {
            return scanRegex(sl, sc, sp, nl);
        }
        return scanPunct(sl, sc, sp, nl);
    }

    private boolean regexAllowed(Token prev) {
        if (prev == null) {
            return true;
        }
        switch (prev.type) {
            case NUMBER:
            case STRING:
            case REGEXP:
            case IDENT:
                return false;
            case KEYWORD:
                return !(prev.value.equals("this") || prev.value.equals("true")
                        || prev.value.equals("false") || prev.value.equals("null"));
            case PUNCT:
                return !(prev.value.equals(")") || prev.value.equals("]")
                        || prev.value.equals("}"));
            default:
                return true;
        }
    }

    private Token scanIdentifier(int sl, int sc, int sp, boolean nl) {
        StringBuilder sb = new StringBuilder();
        while (pos < n) {
            char c = src.charAt(pos);
            if (c == '\\' && pos + 1 < n && src.charAt(pos + 1) == 'u') {
                advance();
                advance();
                sb.append((char) readHex(4));
            } else if (isIdentPart(c)) {
                sb.append(c);
                advance();
            } else {
                break;
            }
        }
        String name = sb.toString();
        TokenType tt = KEYWORDS.contains(name) ? TokenType.KEYWORD : TokenType.IDENT;
        return new Token(tt, name, 0, null, sl, sc, sp, nl);
    }

    private Token scanNumber(int sl, int sc, int sp, boolean nl) {
        int begin = pos;
        double value;
        if (src.charAt(pos) == '0' && pos + 1 < n
                && (src.charAt(pos + 1) == 'x' || src.charAt(pos + 1) == 'X')) {
            advance();
            advance();
            int hs = pos;
            while (pos < n && isHex(src.charAt(pos))) {
                advance();
            }
            if (pos == hs) {
                throw err("Missing hexadecimal digits");
            }
            value = (double) Long.parseLong(src.substring(hs, pos), 16);
        } else if (src.charAt(pos) == '0' && pos + 1 < n && isOctalDigit(src.charAt(pos + 1))) {
            // Legacy octal literal (ES3 Annex B / non-strict).
            advance();
            int os = pos;
            while (pos < n && isOctalDigit(src.charAt(pos))) {
                advance();
            }
            value = (double) Long.parseLong(src.substring(os, pos), 8);
        } else {
            while (pos < n && Character.isDigit(src.charAt(pos))) {
                advance();
            }
            if (pos < n && src.charAt(pos) == '.') {
                advance();
                while (pos < n && Character.isDigit(src.charAt(pos))) {
                    advance();
                }
            }
            if (pos < n && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                advance();
                if (pos < n && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                    advance();
                }
                int es = pos;
                while (pos < n && Character.isDigit(src.charAt(pos))) {
                    advance();
                }
                if (pos == es) {
                    throw err("Missing exponent digits");
                }
            }
            value = Double.parseDouble(src.substring(begin, pos));
        }
        if (pos < n && isIdentStart(src.charAt(pos))) {
            throw err("Identifier directly after number");
        }
        return new Token(TokenType.NUMBER, src.substring(begin, pos), value, null, sl, sc, sp, nl);
    }

    private Token scanString(int sl, int sc, int sp, boolean nl) {
        char quote = src.charAt(pos);
        advance();
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= n) {
                throw err("Unterminated string literal");
            }
            char c = src.charAt(pos);
            if (c == quote) {
                advance();
                break;
            }
            if (isLineTerminator(c)) {
                throw err("Unterminated string literal (newline)");
            }
            if (c == '\\') {
                advance();
                readEscape(sb);
            } else {
                sb.append(c);
                advance();
            }
        }
        return new Token(TokenType.STRING, sb.toString(), 0, null, sl, sc, sp, nl);
    }

    private void readEscape(StringBuilder sb) {
        if (pos >= n) {
            throw err("Unterminated escape");
        }
        char c = src.charAt(pos);
        if (isLineTerminator(c)) {
            // Line continuation: backslash followed by a line terminator -> nothing.
            if (c == '\r' && pos + 1 < n && src.charAt(pos + 1) == '\n') {
                advance();
            }
            advanceNewline();
            return;
        }
        advance();
        switch (c) {
            case 'n': sb.append('\n'); break;
            case 't': sb.append('\t'); break;
            case 'r': sb.append('\r'); break;
            case 'b': sb.append('\b'); break;
            case 'f': sb.append('\f'); break;
            case 'v': sb.append((char) VTAB); break;
            case '0':
                if (pos < n && Character.isDigit(src.charAt(pos))) {
                    sb.append((char) readOctalEscape('0'));
                } else {
                    sb.append('\0');
                }
                break;
            case 'x': sb.append((char) readHex(2)); break;
            case 'u': sb.append((char) readHex(4)); break;
            default:
                if (c >= '1' && c <= '7') {
                    sb.append((char) readOctalEscape(c));
                } else {
                    sb.append(c); // identity escape
                }
        }
    }

    private int readOctalEscape(char first) {
        int val = first - '0';
        int count = 1;
        int max = first <= '3' ? 3 : 2;
        while (count < max && pos < n && isOctalDigit(src.charAt(pos))) {
            val = val * 8 + (src.charAt(pos) - '0');
            advance();
            count++;
        }
        return val;
    }

    private int readHex(int digits) {
        int val = 0;
        for (int i = 0; i < digits; i++) {
            if (pos >= n || !isHex(src.charAt(pos))) {
                throw err("Invalid " + (digits == 2 ? "\\x" : "\\u") + " escape");
            }
            val = val * 16 + Character.digit(src.charAt(pos), 16);
            advance();
        }
        return val;
    }

    private Token scanRegex(int sl, int sc, int sp, boolean nl) {
        advance(); // consume '/'
        StringBuilder body = new StringBuilder();
        boolean inClass = false;
        while (true) {
            if (pos >= n) {
                throw err("Unterminated regular expression");
            }
            char c = src.charAt(pos);
            if (isLineTerminator(c)) {
                throw err("Unterminated regular expression (newline)");
            }
            if (c == '\\') {
                body.append(c);
                advance();
                if (pos >= n) {
                    throw err("Unterminated regular expression");
                }
                body.append(src.charAt(pos));
                advance();
                continue;
            }
            if (c == '[') {
                inClass = true;
            } else if (c == ']') {
                inClass = false;
            } else if (c == '/' && !inClass) {
                advance();
                break;
            }
            body.append(c);
            advance();
        }
        StringBuilder flags = new StringBuilder();
        while (pos < n && isIdentPart(src.charAt(pos))) {
            flags.append(src.charAt(pos));
            advance();
        }
        return new Token(TokenType.REGEXP, body.toString(), 0, flags.toString(), sl, sc, sp, nl);
    }

    private static final String[] PUNCTUATORS = {
            ">>>=", "===", "!==", ">>>", "<<=", ">>=",
            "==", "!=", "<=", ">=", "&&", "||", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=",
            "<<", ">>",
            "{", "}", "(", ")", "[", "]", ".", ";", ",", "<", ">",
            "+", "-", "*", "/", "%", "&", "|", "^", "!", "~", "?", ":", "="
    };

    private Token scanPunct(int sl, int sc, int sp, boolean nl) {
        for (String p : PUNCTUATORS) {
            if (src.regionMatches(pos, p, 0, p.length())) {
                for (int i = 0; i < p.length(); i++) {
                    advance();
                }
                return new Token(TokenType.PUNCT, p, 0, null, sl, sc, sp, nl);
            }
        }
        throw err("Unexpected character '" + src.charAt(pos) + "'");
    }

    /* ------------------------------------------------------------------ */

    private void skipTrivia() {
        while (pos < n) {
            char c = src.charAt(pos);
            if (isLineTerminator(c)) {
                if (c == '\r' && pos + 1 < n && src.charAt(pos + 1) == '\n') {
                    advance();
                }
                advanceNewline();
                pendingNewline = true;
            } else if (isWhitespace(c)) {
                advance();
            } else if (c == '/' && pos + 1 < n && src.charAt(pos + 1) == '/') {
                advance();
                advance();
                while (pos < n && !isLineTerminator(src.charAt(pos))) {
                    advance();
                }
            } else if (c == '/' && pos + 1 < n && src.charAt(pos + 1) == '*') {
                advance();
                advance();
                boolean closed = false;
                while (pos < n) {
                    char d = src.charAt(pos);
                    if (d == '*' && pos + 1 < n && src.charAt(pos + 1) == '/') {
                        advance();
                        advance();
                        closed = true;
                        break;
                    }
                    if (isLineTerminator(d)) {
                        if (d == '\r' && pos + 1 < n && src.charAt(pos + 1) == '\n') {
                            advance();
                        }
                        advanceNewline();
                        pendingNewline = true;
                    } else {
                        advance();
                    }
                }
                if (!closed) {
                    throw err("Unterminated block comment");
                }
            } else {
                break;
            }
        }
    }

    private void advance() {
        pos++;
        col++;
    }

    private void advanceNewline() {
        pos++;
        line++;
        col = 1;
    }

    private JSSyntaxError err(String msg) {
        return new JSSyntaxError(msg, line, col);
    }

    /* ----------------------- character classes ------------------------ */

    private static boolean isLineTerminator(char c) {
        return c == '\n' || c == '\r' || c == LS || c == PS;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == VTAB || c == '\f' || c == NBSP
                || c == BOM || Character.getType(c) == Character.SPACE_SEPARATOR;
    }

    private static boolean isIdentStart(char c) {
        return c == '$' || c == '_' || c == '\\' || Character.isLetter(c);
    }

    private static boolean isIdentPart(char c) {
        return c == '$' || c == '_' || c == ZWNJ || c == ZWJ
                || Character.isLetterOrDigit(c);
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }
}
