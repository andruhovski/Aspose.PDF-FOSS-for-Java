package org.aspose.pdf.engine.script.js;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.parser.JSSyntaxError;
import org.aspose.pdf.engine.script.js.parser.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** P2 parser tests: AST shape assertions and syntax-error reporting. */
public class ParserAstTest {

    private static Node first(String s) {
        return Parser.parse(s).body.get(0);
    }

    @Test
    void precedenceMultiplicationOverAddition() {
        Node.ExprStmt es = (Node.ExprStmt) first("1 + 2 * 3;");
        Node.BinaryExpr add = assertInstanceOf(Node.BinaryExpr.class, es.expression);
        assertEquals("+", add.operator);
        Node.BinaryExpr mul = assertInstanceOf(Node.BinaryExpr.class, add.right);
        assertEquals("*", mul.operator);
    }

    @Test
    void assignmentIsRightAssociative() {
        Node.ExprStmt es = (Node.ExprStmt) first("a = b = 1;");
        Node.AssignExpr outer = assertInstanceOf(Node.AssignExpr.class, es.expression);
        assertInstanceOf(Node.AssignExpr.class, outer.value);
    }

    @Test
    void conditionalAndLogical() {
        Node.ExprStmt es = (Node.ExprStmt) first("a || b ? c : d;");
        Node.ConditionalExpr cond = assertInstanceOf(Node.ConditionalExpr.class, es.expression);
        assertInstanceOf(Node.LogicalExpr.class, cond.test);
    }

    @Test
    void memberAndCallChains() {
        Node.ExprStmt es = (Node.ExprStmt) first("a.b[c]().d;");
        assertInstanceOf(Node.MemberExpr.class, es.expression);
    }

    @Test
    void newExpressionBindsArguments() {
        Node.ExprStmt es = (Node.ExprStmt) first("new A.B(1, 2);");
        Node.NewExpr ne = assertInstanceOf(Node.NewExpr.class, es.expression);
        assertEquals(2, ne.arguments.size());
        assertInstanceOf(Node.MemberExpr.class, ne.callee);
    }

    @Test
    void functionDeclarationAndParams() {
        Node.FunctionDecl fd = assertInstanceOf(Node.FunctionDecl.class,
                first("function f(a, b, c) { return a; }"));
        assertEquals("f", fd.name);
        assertEquals(3, fd.params.size());
    }

    @Test
    void controlStatements() {
        assertInstanceOf(Node.IfStmt.class, first("if (a) b; else c;"));
        assertInstanceOf(Node.ForStmt.class, first("for (var i = 0; i < 10; i++) {}"));
        assertInstanceOf(Node.ForInStmt.class, first("for (var k in obj) {}"));
        assertInstanceOf(Node.WhileStmt.class, first("while (a) {}"));
        assertInstanceOf(Node.DoWhileStmt.class, first("do {} while (a);"));
        assertInstanceOf(Node.SwitchStmt.class, first("switch (a) { case 1: break; default: }"));
        assertInstanceOf(Node.TryStmt.class, first("try {} catch (e) {} finally {}"));
        assertInstanceOf(Node.WithStmt.class, first("with (o) {}"));
        assertInstanceOf(Node.LabeledStmt.class, first("loop: while (a) { break loop; }"));
    }

    @Test
    void objectAndArrayLiterals() {
        Node.ExprStmt o = (Node.ExprStmt) first("({a: 1, 'b': 2, 3: 4});");
        Node.ObjectLit ol = assertInstanceOf(Node.ObjectLit.class, o.expression);
        assertEquals(3, ol.properties.size());

        Node.ExprStmt a = (Node.ExprStmt) first("[1, , 3];");
        Node.ArrayLit al = assertInstanceOf(Node.ArrayLit.class, a.expression);
        assertEquals(3, al.elements.size());
        assertTrue(al.elements.get(1) == null, "elision is a null element");
    }

    @Test
    void automaticSemicolonInsertion() {
        // No semicolons, separated by newlines -> two statements.
        Node.Program p = Parser.parse("var a = 1\nvar b = 2\na + b");
        assertEquals(3, p.body.size());
    }

    @Test
    void returnWithNewlineInsertsSemicolon() {
        // ASI: `return` then newline -> returns undefined; the next line is separate.
        Node.FunctionDecl fd = (Node.FunctionDecl) first("function f() { return\n 5; }");
        Node.ReturnStmt ret = (Node.ReturnStmt) fd.body.body.get(0);
        assertTrue(ret.argument == null, "newline after return forces empty return");
    }

    @Test
    void syntaxErrorHasPosition() {
        JSSyntaxError ex = assertThrows(JSSyntaxError.class, () -> Parser.parse("var = ;"));
        assertTrue(ex.line >= 1 && ex.column >= 1);
    }

    @Test
    void syntaxErrorOnUnclosedParen() {
        assertThrows(JSSyntaxError.class, () -> Parser.parse("foo(1, 2;"));
    }
}
