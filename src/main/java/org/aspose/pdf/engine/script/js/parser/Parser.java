package org.aspose.pdf.engine.script.js.parser;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.lexer.Lexer;
import org.aspose.pdf.engine.script.js.lexer.Token;
import org.aspose.pdf.engine.script.js.lexer.TokenType;

import java.util.*;

/// Recursive-descent parser for ECMAScript 3 (ECMA-262 3rd ed., sec 11-14).
///
/// Implements the full statement and expression grammar with the complete
/// precedence ladder and Automatic Semicolon Insertion (sec 7.9). On malformed
/// input it throws [JSSyntaxError] with a source position.
public final class Parser {

    private static final Set<String> ASSIGN_OPS = new HashSet<>(Arrays.asList(
            "=", "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", ">>>=", "&=", "|=", "^="));

    private final List<Token> toks;
    private int i;

    private Parser(List<Token> toks) {
        this.toks = toks;
    }

    /// Parses a complete program from source text.
    ///
    /// @param source ES3 source
    /// @return the [Node.Program] root
    /// @throws JSSyntaxError on a lexical or syntactic error
    public static Node.Program parse(String source) {
        List<Token> toks = new Lexer(source).tokenize();
        return new Parser(toks).parseProgram();
    }

    /* ----------------------------- cursor ----------------------------- */

    private Token peek() {
        return toks.get(i);
    }

    private Token peek(int k) {
        int j = i + k;
        return j < toks.size() ? toks.get(j) : toks.get(toks.size() - 1);
    }

    private Token next() {
        return toks.get(i++);
    }

    private boolean isEof() {
        return peek().type == TokenType.EOF;
    }

    private boolean atPunct(String p) {
        return peek().isPunct(p);
    }

    private boolean atKeyword(String k) {
        return peek().isKeyword(k);
    }

    private boolean eatPunct(String p) {
        if (atPunct(p)) {
            i++;
            return true;
        }
        return false;
    }

    private boolean eatKeyword(String k) {
        if (atKeyword(k)) {
            i++;
            return true;
        }
        return false;
    }

    private Token expectPunct(String p) {
        if (!atPunct(p)) {
            throw error("Expected '" + p + "' but found '" + peek().value + "'");
        }
        return next();
    }

    private Token expectKeyword(String k) {
        if (!atKeyword(k)) {
            throw error("Expected '" + k + "' but found '" + peek().value + "'");
        }
        return next();
    }

    private String expectIdent() {
        Token t = peek();
        if (t.type != TokenType.IDENT) {
            throw error("Expected identifier but found '" + t.value + "'");
        }
        i++;
        return t.value;
    }

    private JSSyntaxError error(String msg) {
        Token t = peek();
        return new JSSyntaxError(msg, t.line, t.column);
    }

    /// Automatic Semicolon Insertion (sec 7.9).
    private void semicolon() {
        if (eatPunct(";")) {
            return;
        }
        if (atPunct("}") || isEof() || peek().newlineBefore) {
            return; // inserted
        }
        throw error("Expected ';' but found '" + peek().value + "'");
    }

    private <T extends Node> T pos(Token start, T node) {
        node.at(start.line, start.column);
        return node;
    }

    /* ============================ program ============================= */

    private Node.Program parseProgram() {
        List<Node> body = new ArrayList<>();
        while (!isEof()) {
            body.add(parseSourceElement());
        }
        return new Node.Program(body);
    }

    private Node parseSourceElement() {
        if (atKeyword("function")) {
            return parseFunctionDecl();
        }
        return parseStatement();
    }

    /* =========================== statements ========================== */

    private Node parseStatement() {
        Token t = peek();
        if (t.type == TokenType.PUNCT) {
            switch (t.value) {
                case "{": return parseBlock();
                case ";": next(); return pos(t, new Node.EmptyStmt());
                default: break;
            }
        }
        if (t.type == TokenType.KEYWORD) {
            switch (t.value) {
                case "var": return parseVarStatement();
                case "if": return parseIf();
                case "do": return parseDoWhile();
                case "while": return parseWhile();
                case "for": return parseFor();
                case "continue": return parseContinue();
                case "break": return parseBreak();
                case "return": return parseReturn();
                case "with": return parseWith();
                case "switch": return parseSwitch();
                case "throw": return parseThrow();
                case "try": return parseTry();
                case "function": return parseFunctionDecl();
                case "debugger": next(); semicolon(); return pos(t, new Node.DebuggerStmt());
                default: break;
            }
        }
        // labelled statement: IDENT ':'
        if (t.type == TokenType.IDENT && peek(1).isPunct(":")) {
            String label = next().value;
            next(); // ':'
            Node body = parseStatement();
            return pos(t, new Node.LabeledStmt(label, body));
        }
        // expression statement
        Node expr = parseExpression(false);
        semicolon();
        return pos(t, new Node.ExprStmt(expr));
    }

    private Node.Block parseBlock() {
        Token start = expectPunct("{");
        List<Node> body = new ArrayList<>();
        while (!atPunct("}") && !isEof()) {
            body.add(parseSourceElement());
        }
        expectPunct("}");
        return pos(start, new Node.Block(body));
    }

    private Node parseVarStatement() {
        Token start = peek();
        Node.VarStmt v = parseVarDeclarations(false);
        semicolon();
        return pos(start, v);
    }

    private Node.VarStmt parseVarDeclarations(boolean noIn) {
        expectKeyword("var");
        List<Node.VarDeclarator> decls = new ArrayList<>();
        do {
            String name = expectIdent();
            Node init = null;
            if (eatPunct("=")) {
                init = parseAssignment(noIn);
            }
            decls.add(new Node.VarDeclarator(name, init));
        } while (eatPunct(","));
        return new Node.VarStmt(decls);
    }

    private Node parseIf() {
        Token start = expectKeyword("if");
        expectPunct("(");
        Node test = parseExpression(false);
        expectPunct(")");
        Node cons = parseStatement();
        Node alt = null;
        if (eatKeyword("else")) {
            alt = parseStatement();
        }
        return pos(start, new Node.IfStmt(test, cons, alt));
    }

    private Node parseDoWhile() {
        Token start = expectKeyword("do");
        Node body = parseStatement();
        expectKeyword("while");
        expectPunct("(");
        Node test = parseExpression(false);
        expectPunct(")");
        eatPunct(";"); // optional per ASI
        return pos(start, new Node.DoWhileStmt(body, test));
    }

    private Node parseWhile() {
        Token start = expectKeyword("while");
        expectPunct("(");
        Node test = parseExpression(false);
        expectPunct(")");
        Node body = parseStatement();
        return pos(start, new Node.WhileStmt(test, body));
    }

    private Node parseFor() {
        Token start = expectKeyword("for");
        expectPunct("(");
        Node init = null;
        if (atKeyword("var")) {
            Node.VarStmt vars = parseVarDeclarations(true);
            if (atKeyword("in")) {
                if (vars.declarations.size() != 1) {
                    throw error("Invalid for-in: multiple declarations");
                }
                next(); // 'in'
                Node right = parseExpression(false);
                expectPunct(")");
                Node body = parseStatement();
                return pos(start, new Node.ForInStmt(vars, right, body));
            }
            init = vars;
        } else if (!atPunct(";")) {
            Node expr = parseExpression(true);
            if (atKeyword("in")) {
                next();
                Node right = parseExpression(false);
                expectPunct(")");
                Node body = parseStatement();
                return pos(start, new Node.ForInStmt(expr, right, body));
            }
            init = expr;
        }
        expectPunct(";");
        Node test = atPunct(";") ? null : parseExpression(false);
        expectPunct(";");
        Node update = atPunct(")") ? null : parseExpression(false);
        expectPunct(")");
        Node body = parseStatement();
        return pos(start, new Node.ForStmt(init, test, update, body));
    }

    private Node parseContinue() {
        Token start = expectKeyword("continue");
        String label = null;
        if (!peek().newlineBefore && peek().type == TokenType.IDENT) {
            label = next().value;
        }
        semicolon();
        return pos(start, new Node.ContinueStmt(label));
    }

    private Node parseBreak() {
        Token start = expectKeyword("break");
        String label = null;
        if (!peek().newlineBefore && peek().type == TokenType.IDENT) {
            label = next().value;
        }
        semicolon();
        return pos(start, new Node.BreakStmt(label));
    }

    private Node parseReturn() {
        Token start = expectKeyword("return");
        Node arg = null;
        if (!peek().newlineBefore && !atPunct(";") && !atPunct("}") && !isEof()) {
            arg = parseExpression(false);
        }
        semicolon();
        return pos(start, new Node.ReturnStmt(arg));
    }

    private Node parseWith() {
        Token start = expectKeyword("with");
        expectPunct("(");
        Node obj = parseExpression(false);
        expectPunct(")");
        Node body = parseStatement();
        return pos(start, new Node.WithStmt(obj, body));
    }

    private Node parseSwitch() {
        Token start = expectKeyword("switch");
        expectPunct("(");
        Node disc = parseExpression(false);
        expectPunct(")");
        expectPunct("{");
        List<Node.SwitchCase> cases = new ArrayList<>();
        boolean sawDefault = false;
        while (!atPunct("}") && !isEof()) {
            Node test = null;
            if (eatKeyword("case")) {
                test = parseExpression(false);
            } else if (eatKeyword("default")) {
                if (sawDefault) {
                    throw error("Multiple default clauses");
                }
                sawDefault = true;
            } else {
                throw error("Expected 'case' or 'default'");
            }
            expectPunct(":");
            List<Node> body = new ArrayList<>();
            while (!atPunct("}") && !atKeyword("case") && !atKeyword("default") && !isEof()) {
                body.add(parseSourceElement());
            }
            cases.add(new Node.SwitchCase(test, body));
        }
        expectPunct("}");
        return pos(start, new Node.SwitchStmt(disc, cases));
    }

    private Node parseThrow() {
        Token start = expectKeyword("throw");
        if (peek().newlineBefore) {
            throw error("Illegal newline after throw");
        }
        Node arg = parseExpression(false);
        semicolon();
        return pos(start, new Node.ThrowStmt(arg));
    }

    private Node parseTry() {
        Token start = expectKeyword("try");
        Node block = parseBlock();
        String catchParam = null;
        Node catchBlock = null;
        Node finallyBlock = null;
        if (eatKeyword("catch")) {
            expectPunct("(");
            catchParam = expectIdent();
            expectPunct(")");
            catchBlock = parseBlock();
        }
        if (eatKeyword("finally")) {
            finallyBlock = parseBlock();
        }
        if (catchBlock == null && finallyBlock == null) {
            throw error("Missing catch or finally after try");
        }
        return pos(start, new Node.TryStmt(block, catchParam, catchBlock, finallyBlock));
    }

    private Node parseFunctionDecl() {
        Token start = expectKeyword("function");
        String name = expectIdent();
        List<String> params = parseParams();
        Node.Block body = parseBlock();
        return pos(start, new Node.FunctionDecl(name, params, body));
    }

    private List<String> parseParams() {
        expectPunct("(");
        List<String> params = new ArrayList<>();
        if (!atPunct(")")) {
            do {
                params.add(expectIdent());
            } while (eatPunct(","));
        }
        expectPunct(")");
        return params;
    }

    /* =========================== expressions ========================= */

    private Node parseExpression(boolean noIn) {
        Token start = peek();
        Node first = parseAssignment(noIn);
        if (!atPunct(",")) {
            return first;
        }
        List<Node> seq = new ArrayList<>();
        seq.add(first);
        while (eatPunct(",")) {
            seq.add(parseAssignment(noIn));
        }
        return pos(start, new Node.SequenceExpr(seq));
    }

    private Node parseAssignment(boolean noIn) {
        Token start = peek();
        Node left = parseConditional(noIn);
        if (peek().type == TokenType.PUNCT && ASSIGN_OPS.contains(peek().value)) {
            if (!isAssignTarget(left)) {
                throw error("Invalid assignment target");
            }
            String op = next().value;
            Node right = parseAssignment(noIn);
            return pos(start, new Node.AssignExpr(op, left, right));
        }
        return left;
    }

    private static boolean isAssignTarget(Node n) {
        return n instanceof Node.Ident || n instanceof Node.MemberExpr;
    }

    private Node parseConditional(boolean noIn) {
        Token start = peek();
        Node test = parseBinary(0, noIn);
        if (eatPunct("?")) {
            Node cons = parseAssignment(false);
            expectPunct(":");
            Node alt = parseAssignment(noIn);
            return pos(start, new Node.ConditionalExpr(test, cons, alt));
        }
        return test;
    }

    /// Binary precedence climbing. Levels (low to high): || && | ^ &
    /// equality relational shift additive multiplicative.
    private Node parseBinary(int minLevel, boolean noIn) {
        Node left = parseUnary(noIn);
        while (true) {
            Token t = peek();
            int level = binaryLevel(t, noIn);
            if (level < minLevel || level < 0) {
                break;
            }
            String op = next().value;
            Node right = parseBinary(level + 1, noIn);
            if (op.equals("&&") || op.equals("||")) {
                left = pos(tokenAt(left), new Node.LogicalExpr(op, left, right));
            } else {
                left = pos(tokenAt(left), new Node.BinaryExpr(op, left, right));
            }
        }
        return left;
    }

    private Token tokenAt(Node n) {
        // synthetic token carrying the node's own position
        return new Token(TokenType.EOF, "", 0, null, n.line, n.column, 0, false);
    }

    private int binaryLevel(Token t, boolean noIn) {
        if (t.type == TokenType.PUNCT) {
            switch (t.value) {
                case "||": return 1;
                case "&&": return 2;
                case "|": return 3;
                case "^": return 4;
                case "&": return 5;
                case "==": case "!=": case "===": case "!==": return 6;
                case "<": case ">": case "<=": case ">=": return 7;
                case "<<": case ">>": case ">>>": return 8;
                case "+": case "-": return 9;
                case "*": case "/": case "%": return 10;
                default: return -1;
            }
        }
        if (t.type == TokenType.KEYWORD) {
            if (t.value.equals("instanceof")) {
                return 7;
            }
            if (t.value.equals("in") && !noIn) {
                return 7;
            }
        }
        return -1;
    }

    private Node parseUnary(boolean noIn) {
        Token t = peek();
        if (t.type == TokenType.PUNCT) {
            switch (t.value) {
                case "!": case "~": case "+": case "-":
                    next();
                    return pos(t, new Node.UnaryExpr(t.value, parseUnary(noIn)));
                case "++": case "--":
                    next();
                    return pos(t, new Node.UpdateExpr(t.value, parseUnary(noIn), true));
                default: break;
            }
        }
        if (t.type == TokenType.KEYWORD) {
            switch (t.value) {
                case "typeof": case "void": case "delete":
                    next();
                    return pos(t, new Node.UnaryExpr(t.value, parseUnary(noIn)));
                default: break;
            }
        }
        return parsePostfix(noIn);
    }

    private Node parsePostfix(boolean noIn) {
        Token start = peek();
        Node expr = parseLeftHandSide();
        Token t = peek();
        if (!t.newlineBefore && t.type == TokenType.PUNCT
                && (t.value.equals("++") || t.value.equals("--"))) {
            next();
            return pos(start, new Node.UpdateExpr(t.value, expr, false));
        }
        return expr;
    }

    private Node parseLeftHandSide() {
        Node node = atKeyword("new") ? parseNewExpr() : parsePrimary();
        return parseCallMemberTail(node);
    }

    private Node parseNewExpr() {
        Token start = expectKeyword("new");
        Node callee = atKeyword("new") ? parseNewExpr() : parsePrimary();
        callee = parseMemberTail(callee);
        List<Node> args = atPunct("(") ? parseArguments() : new ArrayList<>();
        return pos(start, new Node.NewExpr(callee, args));
    }

    private Node parseMemberTail(Node node) {
        while (true) {
            if (eatPunct(".")) {
                String name = identifierName();
                node = pos(tokenAt(node), new Node.MemberExpr(node, new Node.Ident(name), false));
            } else if (atPunct("[")) {
                next();
                Node prop = parseExpression(false);
                expectPunct("]");
                node = pos(tokenAt(node), new Node.MemberExpr(node, prop, true));
            } else {
                return node;
            }
        }
    }

    private Node parseCallMemberTail(Node node) {
        while (true) {
            if (eatPunct(".")) {
                String name = identifierName();
                node = pos(tokenAt(node), new Node.MemberExpr(node, new Node.Ident(name), false));
            } else if (atPunct("[")) {
                next();
                Node prop = parseExpression(false);
                expectPunct("]");
                node = pos(tokenAt(node), new Node.MemberExpr(node, prop, true));
            } else if (atPunct("(")) {
                List<Node> args = parseArguments();
                node = pos(tokenAt(node), new Node.CallExpr(node, args));
            } else {
                return node;
            }
        }
    }

    private String identifierName() {
        // After '.', any identifier name including keywords is allowed (sec 11.2.1).
        Token t = peek();
        if (t.type == TokenType.IDENT || t.type == TokenType.KEYWORD) {
            i++;
            return t.value;
        }
        throw error("Expected property name after '.'");
    }

    private List<Node> parseArguments() {
        expectPunct("(");
        List<Node> args = new ArrayList<>();
        if (!atPunct(")")) {
            do {
                args.add(parseAssignment(false));
            } while (eatPunct(","));
        }
        expectPunct(")");
        return args;
    }

    private Node parsePrimary() {
        Token t = peek();
        switch (t.type) {
            case NUMBER:
                next();
                return pos(t, new Node.NumberLit(t.number));
            case STRING:
                next();
                return pos(t, new Node.StringLit(t.value));
            case REGEXP:
                next();
                return pos(t, new Node.RegexLit(t.value, t.regexFlags));
            case IDENT:
                next();
                return pos(t, new Node.Ident(t.value));
            case KEYWORD:
                switch (t.value) {
                    case "this": next(); return pos(t, new Node.ThisExpr());
                    case "null": next(); return pos(t, new Node.NullLit());
                    case "true": next(); return pos(t, new Node.BoolLit(true));
                    case "false": next(); return pos(t, new Node.BoolLit(false));
                    case "function": return parseFunctionExpr();
                    default:
                        throw error("Unexpected keyword '" + t.value + "'");
                }
            case PUNCT:
                switch (t.value) {
                    case "(": {
                        next();
                        Node e = parseExpression(false);
                        expectPunct(")");
                        return e;
                    }
                    case "[": return parseArrayLiteral();
                    case "{": return parseObjectLiteral();
                    default:
                        throw error("Unexpected token '" + t.value + "'");
                }
            default:
                throw error("Unexpected token '" + t.value + "'");
        }
    }

    private Node parseFunctionExpr() {
        Token start = expectKeyword("function");
        String name = peek().type == TokenType.IDENT ? next().value : null;
        List<String> params = parseParams();
        Node.Block body = parseBlock();
        return pos(start, new Node.FunctionExpr(name, params, body));
    }

    private Node parseArrayLiteral() {
        Token start = expectPunct("[");
        List<Node> elements = new ArrayList<>();
        while (!atPunct("]")) {
            if (atPunct(",")) {
                elements.add(null); // elision
                next();
            } else {
                elements.add(parseAssignment(false));
                if (!atPunct("]")) {
                    expectPunct(",");
                }
            }
        }
        expectPunct("]");
        return pos(start, new Node.ArrayLit(elements));
    }

    private Node parseObjectLiteral() {
        Token start = expectPunct("{");
        List<Node.Property> props = new ArrayList<>();
        while (!atPunct("}")) {
            String key = propertyName();
            expectPunct(":");
            Node value = parseAssignment(false);
            props.add(new Node.Property(key, value));
            if (!atPunct("}")) {
                expectPunct(",");
            }
        }
        expectPunct("}");
        return pos(start, new Node.ObjectLit(props));
    }

    private String propertyName() {
        Token t = peek();
        if (t.type == TokenType.IDENT || t.type == TokenType.KEYWORD) {
            i++;
            return t.value;
        }
        if (t.type == TokenType.STRING) {
            i++;
            return t.value;
        }
        if (t.type == TokenType.NUMBER) {
            i++;
            return org.aspose.pdf.engine.script.js.runtime.JSNumber.toStr(t.number);
        }
        throw error("Expected property name");
    }
}
