package org.aspose.pdf.engine.xfa.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FormCalc front-end (B2.1) — a hand-written lexer + recursive-descent parser producing an AST for
 * the XFA FormCalc calculation language (XFA spec § object-model "FormCalc", p.964+). FormCalc is the
 * spec default for an untyped {@code <script>} and is DISTINCT from JavaScript: typeless,
 * expression-based, case-insensitive keywords/builtins, {@code if…then…elseif…else…endif} control,
 * and SOM node references (resolved by the SAME Stage-A {@link org.aspose.pdf.engine.xfa.binding.som.SomResolver}
 * the JS path uses).
 *
 * <p>Scoped to the grammar the B2.0 corpus probe measured: arithmetic ({@code + - * /}), comparison
 * ({@code == &lt;&gt; &lt; &gt; &lt;= &gt;=} and the word forms {@code eq ne lt le gt ge}), logical
 * ({@code and or not}), string concat ({@code &amp;}), {@code if/then/elseif/else/endif}, assignment,
 * function calls and SOM references with {@code [*]}/{@code [n]}/predicate indices. {@code for}/
 * {@code foreach}/{@code while} loops (zero corpus demand) are a tracked grammar gap — a {@code for}
 * token raises {@link FormCalcError} rather than mis-parsing.</p>
 */
final class FormCalcParser {

    /* ------------------------------ AST ------------------------------ */

    /** Base AST node. */
    abstract static class Node {
    }

    /** A numeric literal. */
    static final class Num extends Node {
        final double value;
        Num(double v) {
            this.value = v;
        }
    }

    /** A string literal. */
    static final class Str extends Node {
        final String value;
        Str(String v) {
            this.value = v;
        }
    }

    /** The {@code null} literal. */
    static final class Null extends Node {
    }

    /** A SOM reference (a node path: {@code numQty}, {@code Row4[*].cost}, {@code $}, {@code $.font.fill}). */
    static final class Ref extends Node {
        final String path;
        Ref(String p) {
            this.path = p;
        }
    }

    /** A unary operation ({@code - + not}). */
    static final class Unary extends Node {
        final String op;
        final Node operand;
        Unary(String op, Node operand) {
            this.op = op;
            this.operand = operand;
        }
    }

    /** A binary operation (arithmetic / comparison / logical / concat). */
    static final class Bin extends Node {
        final String op;
        final Node left;
        final Node right;
        Bin(String op, Node left, Node right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
    }

    /** A function call ({@code Sum(...)}, {@code Concat(...)}, {@code Exists(...)}). */
    static final class Call extends Node {
        final String name;
        final List<Node> args;
        Call(String name, List<Node> args) {
            this.name = name;
            this.args = args;
        }
    }

    /** An assignment ({@code $ = expr}, {@code Total.rawValue = expr}, {@code $.presence = "hidden"}). */
    static final class Assign extends Node {
        final Ref target;
        final Node value;
        Assign(Ref target, Node value) {
            this.target = target;
            this.value = value;
        }
    }

    /** An {@code if … then … [elseif …]* [else …] endif} expression (its value is the taken branch). */
    static final class If extends Node {
        final Node cond;
        final Block thenBlock;
        final Node elseBranch; // a nested If (elseif) or a Block (else) or null
        If(Node cond, Block thenBlock, Node elseBranch) {
            this.cond = cond;
            this.thenBlock = thenBlock;
            this.elseBranch = elseBranch;
        }
    }

    /** An expression list — its value is the value of the last expression. */
    static final class Block extends Node {
        final List<Node> stmts;
        Block(List<Node> stmts) {
            this.stmts = stmts;
        }
    }

    /* ------------------------------ tokens ------------------------------ */

    private enum T { NUM, STR, IDENT, KW, OP, LP, RP, LB, RB, COMMA, DOT, DOLLAR, SEP, EOF }

    private static final class Tok {
        final T type;
        final String text;
        final int pos;
        Tok(T type, String text, int pos) {
            this.type = type;
            this.text = text;
            this.pos = pos;
        }
    }

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "if", "then", "elseif", "else", "endif", "and", "or", "not",
            "eq", "ne", "lt", "le", "gt", "ge", "null",
            "for", "foreach", "do", "endfor", "while", "endwhile", "func", "endfunc", "var", "return"));

    private final String src;
    private final List<Tok> toks = new ArrayList<>();
    private int p;

    private FormCalcParser(String src) {
        this.src = src;
    }

    /**
     * Parses FormCalc source into an AST.
     *
     * @param src the FormCalc script
     * @return the top-level {@link Block}
     * @throws FormCalcError on a lex/parse error (including an unimplemented {@code for}/{@code while})
     */
    static Block parse(String src) {
        FormCalcParser fp = new FormCalcParser(src == null ? "" : src);
        fp.lex();
        Block b = fp.parseBlock(new HashSet<>());
        fp.expect(T.EOF);
        return b;
    }

    /* ------------------------------ lexer ------------------------------ */

    private void lex() {
        int n = src.length();
        int i = 0;
        while (i < n) {
            char c = src.charAt(i);
            if (c == '\n' || c == '\r') {
                add(T.SEP, "\n", i);
                i++;
                continue;
            }
            if (c == ';') {
                add(T.SEP, ";", i);
                i++;
                continue;
            }
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n && src.charAt(i + 1) == '/') {
                while (i < n && src.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '"') {
                int start = i;
                StringBuilder sb = new StringBuilder();
                i++;
                while (i < n) {
                    char d = src.charAt(i);
                    if (d == '"') {
                        if (i + 1 < n && src.charAt(i + 1) == '"') { // "" escaped quote
                            sb.append('"');
                            i += 2;
                            continue;
                        }
                        i++;
                        break;
                    }
                    sb.append(d);
                    i++;
                }
                add(T.STR, sb.toString(), start);
                continue;
            }
            if (Character.isDigit(c) || (c == '.' && i + 1 < n && Character.isDigit(src.charAt(i + 1)))) {
                int start = i;
                while (i < n && (Character.isDigit(src.charAt(i)) || src.charAt(i) == '.')) {
                    i++;
                }
                add(T.NUM, src.substring(start, i), start);
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '_')) {
                    i++;
                }
                String w = src.substring(start, i);
                add(KEYWORDS.contains(w.toLowerCase()) ? T.KW : T.IDENT, w, start);
                continue;
            }
            // operators / punctuation
            if (c == '$') {
                add(T.DOLLAR, "$", i);
                i++;
                continue;
            }
            if (c == '(') {
                add(T.LP, "(", i);
                i++;
                continue;
            }
            if (c == ')') {
                add(T.RP, ")", i);
                i++;
                continue;
            }
            if (c == '[') {
                add(T.LB, "[", i);
                i++;
                continue;
            }
            if (c == ']') {
                add(T.RB, "]", i);
                i++;
                continue;
            }
            if (c == ',') {
                add(T.COMMA, ",", i);
                i++;
                continue;
            }
            if (c == '.') {
                add(T.DOT, ".", i);
                i++;
                continue;
            }
            // multi-char operators
            String two = i + 1 < n ? src.substring(i, i + 2) : "";
            if (two.equals("==") || two.equals("<>") || two.equals("<=") || two.equals(">=")) {
                add(T.OP, two, i);
                i += 2;
                continue;
            }
            if ("+-*/=<>&|".indexOf(c) >= 0) {
                add(T.OP, String.valueOf(c), i);
                i++;
                continue;
            }
            throw new FormCalcError("FormCalc lex error: unexpected '" + c + "' at " + i);
        }
        add(T.EOF, "", n);
    }

    private void add(T type, String text, int pos) {
        toks.add(new Tok(type, text, pos));
    }

    /* ------------------------------ parser ------------------------------ */

    private Tok peek() {
        return toks.get(p);
    }

    private boolean isKw(String kw) {
        Tok t = peek();
        return t.type == T.KW && t.text.equalsIgnoreCase(kw);
    }

    private boolean isOp(String op) {
        Tok t = peek();
        return t.type == T.OP && t.text.equals(op);
    }

    private Tok next() {
        return toks.get(p++);
    }

    private void expect(T type) {
        if (peek().type != type) {
            throw new FormCalcError("FormCalc parse error: expected " + type + " but saw '" + peek().text + "'");
        }
        p++;
    }

    private void skipSeps() {
        while (peek().type == T.SEP) {
            p++;
        }
    }

    /** Parses a statement list until EOF or a stop keyword (then/elseif/else/endif). */
    private Block parseBlock(Set<String> stops) {
        List<Node> stmts = new ArrayList<>();
        skipSeps();
        while (peek().type != T.EOF && !atStop(stops)) {
            stmts.add(parseStmt());
            skipSeps();
        }
        return new Block(stmts);
    }

    private boolean atStop(Set<String> stops) {
        Tok t = peek();
        return t.type == T.KW && stops.contains(t.text.toLowerCase());
    }

    private Node parseStmt() {
        if (isKw("if")) {
            return parseIf();
        }
        if (isKw("for") || isKw("foreach") || isKw("while") || isKw("func") || isKw("var") || isKw("return")) {
            throw new FormCalcError("FormCalc: unimplemented construct '" + peek().text + "' (tracked grammar gap)");
        }
        Node e = parseExpr();
        if (isOp("=")) { // assignment (single '=' is assignment; '==' is equality)
            next();
            Node v = parseExpr();
            if (!(e instanceof Ref)) {
                throw new FormCalcError("FormCalc: assignment target is not a reference");
            }
            return new Assign((Ref) e, v);
        }
        return e;
    }

    private static final Set<String> THEN_STOPS = new HashSet<>(Arrays.asList("elseif", "else", "endif"));

    private Node parseIf() {
        next(); // 'if'
        Node cond = parseExpr();
        if (!isKw("then")) {
            throw new FormCalcError("FormCalc: expected 'then' after if-condition");
        }
        next(); // 'then'
        Block thenB = parseBlock(THEN_STOPS);
        Node elseBranch = null;
        if (isKw("elseif")) {
            elseBranch = parseIf2(); // an elseif chains as a nested If (no leading 'if' token)
            return new If(cond, thenB, elseBranch);
        }
        if (isKw("else")) {
            next();
            elseBranch = parseBlock(THEN_STOPS);
        }
        if (isKw("endif")) {
            next();
        }
        return new If(cond, thenB, elseBranch);
    }

    /** Parses an {@code elseif} as a nested If (the chain shares the single trailing {@code endif}). */
    private Node parseIf2() {
        next(); // 'elseif'
        Node cond = parseExpr();
        if (isKw("then")) {
            next();
        }
        Block thenB = parseBlock(THEN_STOPS);
        Node elseBranch = null;
        if (isKw("elseif")) {
            elseBranch = parseIf2();
        } else if (isKw("else")) {
            next();
            elseBranch = parseBlock(THEN_STOPS);
            if (isKw("endif")) {
                next();
            }
        } else if (isKw("endif")) {
            next();
        }
        return new If(cond, thenB, elseBranch);
    }

    /* ----- expression precedence ----- */

    private Node parseExpr() {
        return parseOr();
    }

    private Node parseOr() {
        Node l = parseAnd();
        while (isKw("or") || isOp("|")) {
            next();
            l = new Bin("or", l, parseAnd());
        }
        return l;
    }

    private Node parseAnd() {
        Node l = parseEq();
        while (isKw("and")) {
            next();
            l = new Bin("and", l, parseEq());
        }
        return l;
    }

    private Node parseEq() {
        Node l = parseRel();
        while (isOp("==") || isOp("<>") || isKw("eq") || isKw("ne")) {
            String op = normOp(next());
            l = new Bin(op, l, parseRel());
        }
        return l;
    }

    private Node parseRel() {
        Node l = parseAdd();
        while (isOp("<") || isOp(">") || isOp("<=") || isOp(">=")
                || isKw("lt") || isKw("gt") || isKw("le") || isKw("ge")) {
            String op = normOp(next());
            l = new Bin(op, l, parseAdd());
        }
        return l;
    }

    private Node parseAdd() {
        Node l = parseMul();
        while (isOp("+") || isOp("-") || isOp("&")) {
            String op = next().text;
            l = new Bin(op, l, parseMul());
        }
        return l;
    }

    private Node parseMul() {
        Node l = parseUnary();
        while (isOp("*") || isOp("/")) {
            String op = next().text;
            l = new Bin(op, l, parseUnary());
        }
        return l;
    }

    private Node parseUnary() {
        if (isOp("-") || isOp("+") || isKw("not")) {
            String op = next().text.equalsIgnoreCase("not") ? "not" : peekPrevText();
            return new Unary(op, parseUnary());
        }
        return parsePrimary();
    }

    private String peekPrevText() {
        return toks.get(p - 1).text;
    }

    /** Normalises a comparison operator token to a canonical symbol. */
    private static String normOp(Tok t) {
        switch (t.text.toLowerCase()) {
            case "eq": return "==";
            case "ne": return "<>";
            case "lt": return "<";
            case "le": return "<=";
            case "gt": return ">";
            case "ge": return ">=";
            default: return t.text;
        }
    }

    private Node parsePrimary() {
        Tok t = peek();
        if (t.type == T.NUM) {
            next();
            return new Num(parseNum(t.text));
        }
        if (t.type == T.STR) {
            next();
            return new Str(t.text);
        }
        if (isKw("null")) {
            next();
            return new Null();
        }
        if (t.type == T.LP) {
            next();
            Node e = parseExpr();
            expect(T.RP);
            return e;
        }
        if (t.type == T.IDENT) {
            // function call vs SOM reference
            if (toks.get(p + 1).type == T.LP) {
                return parseCall();
            }
            return parseRef();
        }
        if (t.type == T.DOLLAR) {
            return parseRef();
        }
        throw new FormCalcError("FormCalc parse error: unexpected '" + t.text + "'");
    }

    private Node parseCall() {
        String name = next().text; // IDENT
        expect(T.LP);
        List<Node> args = new ArrayList<>();
        if (peek().type != T.RP) {
            args.add(parseExpr());
            while (peek().type == T.COMMA) {
                next();
                args.add(parseExpr());
            }
        }
        expect(T.RP);
        return new Call(name, args);
    }

    /** Builds a SOM path string ({@code Row4[*].cost}, {@code $.font.fill}) the resolver understands. */
    private Node parseRef() {
        StringBuilder path = new StringBuilder();
        Tok first = next();
        path.append(first.type == T.DOLLAR ? "$" : first.text);
        while (true) {
            if (peek().type == T.DOT) {
                next();
                Tok id = peek();
                if (id.type == T.IDENT || id.type == T.KW) { // a node may share a keyword spelling
                    path.append('.').append(next().text);
                } else if (id.type == T.OP && id.text.equals("*")) {
                    next();
                    path.append(".*");
                } else {
                    break;
                }
            } else if (peek().type == T.LB) {
                path.append('[').append(readBracket()).append(']');
            } else {
                break;
            }
        }
        return new Ref(path.toString());
    }

    /** Reads a bracket index/predicate, reconstructing the inner text (strings re-quoted). */
    private String readBracket() {
        expect(T.LB);
        StringBuilder inner = new StringBuilder();
        int depth = 1;
        while (peek().type != T.EOF) {
            Tok t = peek();
            if (t.type == T.LB) {
                depth++;
            } else if (t.type == T.RB) {
                depth--;
                if (depth == 0) {
                    next();
                    break;
                }
            }
            next();
            if (t.type == T.STR) {
                inner.append('"').append(t.text).append('"');
            } else {
                inner.append(t.text);
            }
        }
        return inner.toString();
    }

    private static double parseNum(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new FormCalcError("FormCalc: bad number '" + s + "'");
        }
    }
}
