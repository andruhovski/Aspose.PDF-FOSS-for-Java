package org.aspose.pdf.engine.script.js.ast;

import java.util.List;

/**
 * Abstract syntax tree for ECMAScript 3 (ECMA-262 3rd ed.).
 *
 * <p>The whole node hierarchy is expressed as nested static classes of
 * {@code Node} to keep the grammar in one place. Every node carries the
 * 1-based source position of its first token for diagnostics. Fields are
 * {@code public final} as this is an internal, immutable parse product.</p>
 */
public abstract class Node {

    /** 1-based source line of the node's first token. */
    public int line;
    /** 1-based source column of the node's first token. */
    public int column;

    /** Sets the source position; returns {@code this} for chaining. */
    public Node at(int line, int column) {
        this.line = line;
        this.column = column;
        return this;
    }

    /* ======================= Program & statements ===================== */

    /** A complete parsed program (list of top-level statements). */
    public static final class Program extends Node {
        public final List<Node> body;
        public Program(List<Node> body) { this.body = body; }
    }

    /** {@code { ... }} block. */
    public static final class Block extends Node {
        public final List<Node> body;
        public Block(List<Node> body) { this.body = body; }
    }

    /** A single declarator inside a {@code var} statement. */
    public static final class VarDeclarator {
        public final String name;
        public final Node init; // may be null
        public VarDeclarator(String name, Node init) { this.name = name; this.init = init; }
    }

    /** {@code var a = 1, b;} */
    public static final class VarStmt extends Node {
        public final List<VarDeclarator> declarations;
        public VarStmt(List<VarDeclarator> declarations) { this.declarations = declarations; }
    }

    /** Lone {@code ;} */
    public static final class EmptyStmt extends Node { }

    /** Debugger statement (parsed, no-op). */
    public static final class DebuggerStmt extends Node { }

    /** Expression used as a statement. */
    public static final class ExprStmt extends Node {
        public final Node expression;
        public ExprStmt(Node expression) { this.expression = expression; }
    }

    /** {@code if (test) consequent else alternate} */
    public static final class IfStmt extends Node {
        public final Node test;
        public final Node consequent;
        public final Node alternate; // may be null
        public IfStmt(Node test, Node consequent, Node alternate) {
            this.test = test; this.consequent = consequent; this.alternate = alternate;
        }
    }

    /** {@code do body while (test)} */
    public static final class DoWhileStmt extends Node {
        public final Node body;
        public final Node test;
        public DoWhileStmt(Node body, Node test) { this.body = body; this.test = test; }
    }

    /** {@code while (test) body} */
    public static final class WhileStmt extends Node {
        public final Node test;
        public final Node body;
        public WhileStmt(Node test, Node body) { this.test = test; this.body = body; }
    }

    /** {@code for (init; test; update) body} */
    public static final class ForStmt extends Node {
        public final Node init;   // VarStmt or Expr or null
        public final Node test;   // Expr or null
        public final Node update; // Expr or null
        public final Node body;
        public ForStmt(Node init, Node test, Node update, Node body) {
            this.init = init; this.test = test; this.update = update; this.body = body;
        }
    }

    /** {@code for (left in right) body} */
    public static final class ForInStmt extends Node {
        public final Node left;  // VarStmt (single) or LHS expression
        public final Node right;
        public final Node body;
        public ForInStmt(Node left, Node right, Node body) {
            this.left = left; this.right = right; this.body = body;
        }
    }

    /** {@code continue label?;} */
    public static final class ContinueStmt extends Node {
        public final String label; // may be null
        public ContinueStmt(String label) { this.label = label; }
    }

    /** {@code break label?;} */
    public static final class BreakStmt extends Node {
        public final String label; // may be null
        public BreakStmt(String label) { this.label = label; }
    }

    /** {@code return arg?;} */
    public static final class ReturnStmt extends Node {
        public final Node argument; // may be null
        public ReturnStmt(Node argument) { this.argument = argument; }
    }

    /** {@code with (object) body} */
    public static final class WithStmt extends Node {
        public final Node object;
        public final Node body;
        public WithStmt(Node object, Node body) { this.object = object; this.body = body; }
    }

    /** One {@code case x:} or {@code default:} clause. */
    public static final class SwitchCase {
        public final Node test; // null = default
        public final List<Node> body;
        public SwitchCase(Node test, List<Node> body) { this.test = test; this.body = body; }
    }

    /** {@code switch (disc) { ... }} */
    public static final class SwitchStmt extends Node {
        public final Node discriminant;
        public final List<SwitchCase> cases;
        public SwitchStmt(Node discriminant, List<SwitchCase> cases) {
            this.discriminant = discriminant; this.cases = cases;
        }
    }

    /** {@code throw arg;} */
    public static final class ThrowStmt extends Node {
        public final Node argument;
        public ThrowStmt(Node argument) { this.argument = argument; }
    }

    /** {@code try { } catch (p) { } finally { }} */
    public static final class TryStmt extends Node {
        public final Node block;
        public final String catchParam;   // may be null
        public final Node catchBlock;     // may be null
        public final Node finallyBlock;   // may be null
        public TryStmt(Node block, String catchParam, Node catchBlock, Node finallyBlock) {
            this.block = block; this.catchParam = catchParam;
            this.catchBlock = catchBlock; this.finallyBlock = finallyBlock;
        }
    }

    /** {@code function name(params) body} declaration. */
    public static final class FunctionDecl extends Node {
        public final String name;
        public final List<String> params;
        public final Block body;
        public FunctionDecl(String name, List<String> params, Block body) {
            this.name = name; this.params = params; this.body = body;
        }
    }

    /** {@code label: statement} */
    public static final class LabeledStmt extends Node {
        public final String label;
        public final Node body;
        public LabeledStmt(String label, Node body) { this.label = label; this.body = body; }
    }

    /* ========================== Expressions =========================== */

    /** Numeric literal. */
    public static final class NumberLit extends Node {
        public final double value;
        public NumberLit(double value) { this.value = value; }
    }

    /** String literal (already decoded). */
    public static final class StringLit extends Node {
        public final String value;
        public StringLit(String value) { this.value = value; }
    }

    /** {@code true} / {@code false}. */
    public static final class BoolLit extends Node {
        public final boolean value;
        public BoolLit(boolean value) { this.value = value; }
    }

    /** {@code null}. */
    public static final class NullLit extends Node { }

    /** {@code /pat/flags} literal. */
    public static final class RegexLit extends Node {
        public final String pattern;
        public final String flags;
        public RegexLit(String pattern, String flags) { this.pattern = pattern; this.flags = flags; }
    }

    /** Identifier reference. */
    public static final class Ident extends Node {
        public final String name;
        public Ident(String name) { this.name = name; }
    }

    /** {@code this}. */
    public static final class ThisExpr extends Node { }

    /** {@code [a, , c]} array literal; {@code null} elements are elisions. */
    public static final class ArrayLit extends Node {
        public final List<Node> elements;
        public ArrayLit(List<Node> elements) { this.elements = elements; }
    }

    /** One {@code key: value} entry of an object literal. */
    public static final class Property {
        public final String key;
        public final Node value;
        public Property(String key, Node value) { this.key = key; this.value = value; }
    }

    /** {@code { k: v, ... }} object literal. */
    public static final class ObjectLit extends Node {
        public final List<Property> properties;
        public ObjectLit(List<Property> properties) { this.properties = properties; }
    }

    /** {@code function name?(params) body} expression. */
    public static final class FunctionExpr extends Node {
        public final String name; // may be null
        public final List<String> params;
        public final Block body;
        public FunctionExpr(String name, List<String> params, Block body) {
            this.name = name; this.params = params; this.body = body;
        }
    }

    /** Unary prefix operator: {@code ! ~ + - typeof void delete}. */
    public static final class UnaryExpr extends Node {
        public final String operator;
        public final Node argument;
        public UnaryExpr(String operator, Node argument) {
            this.operator = operator; this.argument = argument;
        }
    }

    /** {@code ++}/{@code --}, prefix or postfix. */
    public static final class UpdateExpr extends Node {
        public final String operator;
        public final Node argument;
        public final boolean prefix;
        public UpdateExpr(String operator, Node argument, boolean prefix) {
            this.operator = operator; this.argument = argument; this.prefix = prefix;
        }
    }

    /** Binary operator (arithmetic, relational, equality, bitwise, shift, {@code instanceof}, {@code in}). */
    public static final class BinaryExpr extends Node {
        public final String operator;
        public final Node left;
        public final Node right;
        public BinaryExpr(String operator, Node left, Node right) {
            this.operator = operator; this.left = left; this.right = right;
        }
    }

    /** {@code &&} / {@code ||} short-circuit operator. */
    public static final class LogicalExpr extends Node {
        public final String operator;
        public final Node left;
        public final Node right;
        public LogicalExpr(String operator, Node left, Node right) {
            this.operator = operator; this.left = left; this.right = right;
        }
    }

    /** Assignment {@code = += -= ...}. */
    public static final class AssignExpr extends Node {
        public final String operator;
        public final Node target;
        public final Node value;
        public AssignExpr(String operator, Node target, Node value) {
            this.operator = operator; this.target = target; this.value = value;
        }
    }

    /** {@code test ? cons : alt}. */
    public static final class ConditionalExpr extends Node {
        public final Node test;
        public final Node consequent;
        public final Node alternate;
        public ConditionalExpr(Node test, Node consequent, Node alternate) {
            this.test = test; this.consequent = consequent; this.alternate = alternate;
        }
    }

    /** {@code callee(args)}. */
    public static final class CallExpr extends Node {
        public final Node callee;
        public final List<Node> arguments;
        public CallExpr(Node callee, List<Node> arguments) {
            this.callee = callee; this.arguments = arguments;
        }
    }

    /** {@code new callee(args)}. */
    public static final class NewExpr extends Node {
        public final Node callee;
        public final List<Node> arguments;
        public NewExpr(Node callee, List<Node> arguments) {
            this.callee = callee; this.arguments = arguments;
        }
    }

    /** {@code object.property} or {@code object[property]}. */
    public static final class MemberExpr extends Node {
        public final Node object;
        public final Node property; // Ident (static) or Expr (computed)
        public final boolean computed;
        public MemberExpr(Node object, Node property, boolean computed) {
            this.object = object; this.property = property; this.computed = computed;
        }
    }

    /** Comma operator {@code a, b, c}. */
    public static final class SequenceExpr extends Node {
        public final List<Node> expressions;
        public SequenceExpr(List<Node> expressions) { this.expressions = expressions; }
    }
}
