package org.aspose.pdf.engine.script.js.interp;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.builtins.Realm;
import org.aspose.pdf.engine.script.js.runtime.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/// Tree-walking evaluator for ECMAScript 3 (ECMA-262 3rd ed.).
///
/// Holds a [Realm] (the intrinsics + global object) and walks the AST
/// produced by the parser. Abstract operations that may invoke user code
/// (ToPrimitive, ToNumber/ToString of objects, abstract equality, the addition
/// operator) are methods here; the pure ones live in
/// [org.aspose.pdf.engine.script.js.runtime.Types].
public final class Interpreter {

    private static final Object UNDEF = Undefined.INSTANCE;

    private final Realm realm;
    private final Scope globalScope;
    private final Deque<Object> thisStack = new ArrayDeque<>();

    /// Guards against unbounded user-function recursion. A runaway or non-terminating recursive
    /// script (seen in real XFA forms) would otherwise blow the JVM stack with a fatal
    /// [StackOverflowError], aborting the whole render. Like a real engine we instead throw a
    /// catchable `RangeError` ("Maximum call stack size exceeded"), so the script host reports
    /// the failure and rendering continues. 512 is well below the depth at which the interpreter's
    /// \~7 Java frames per JS call overflow the default thread stack.
    private static final int MAX_CALL_DEPTH = 512;
    private int callDepth;

    /// Execution-step budget: every statement/expression dispatch counts one step, and a
    /// script exceeding the budget is aborted with a [JsExecutionLimitError] (a host-level
    /// error a script-level `try/catch` cannot swallow). Closes the DoS where an untrusted
    /// form's `while(true){}` — or the child-chain absent-node loops noted in the B3.5
    /// findings — hangs the processing thread. The default (50 million steps, well under a
    /// minute of tree-walking) is far above any legitimate form script; override with
    /// `-Dxfa.js.maxSteps=N`. The budget resets per top-level [#run]/[#runWithThis].
    private static final long DEFAULT_MAX_STEPS = 50_000_000L;
    private final long maxSteps = readMaxSteps();
    private long steps;

    private static long readMaxSteps() {
        try {
            return Long.getLong("xfa.js.maxSteps", DEFAULT_MAX_STEPS);
        } catch (SecurityException e) {
            return DEFAULT_MAX_STEPS;
        }
    }

    /// Counts one dispatch step; aborts the script when the budget is exhausted.
    private void countStep() {
        if (++steps > maxSteps) {
            throw new JsExecutionLimitError(
                    "Script exceeded the execution budget of " + maxSteps
                            + " steps (non-terminating script? see -Dxfa.js.maxSteps)");
        }
    }

    /// Creates an interpreter over a realm.
    ///
    /// @param realm the realm to execute against
    public Interpreter(Realm realm) {
        this.realm = realm;
        this.globalScope = new Scope(realm.globalObject, null, false);
        thisStack.push(realm.globalObject);
    }

    /// @return the realm (intrinsics + global object).
    public Realm getRealm() {
        return realm;
    }

    /// @return the global scope.
    public Scope getGlobalScope() {
        return globalScope;
    }

    /// Runs a parsed program in the global scope and returns the completion
    /// value (the value of the last evaluated statement, REPL-style).
    ///
    /// @param program parsed program
    /// @return completion value
    public Object run(Node.Program program) {
        steps = 0; // fresh execution budget per top-level script
        try {
            hoist(program.body, realm.globalObject, globalScope);
            Object last = UNDEF;
            for (Node stmt : program.body) {
                Object v = execStmt(stmt, globalScope);
                if (v != null) {
                    last = v;
                }
            }
            return last;
        } catch (StackOverflowError e) {
            throw hostStackExhausted();
        }
    }

    /// Converts a JVM [StackOverflowError] into the controlled execution
    /// abort. The MAX\_CALL\_DEPTH guard caps _script_ recursion, but a
    /// script that keeps recursing from inside `catch` blocks stacks a
    /// fresh Java frame layer per caught RangeError, so on hosts with a small
    /// thread stack the JVM can overflow before the step budget triggers. At
    /// this point the stack has already unwound to the top-level entry, so
    /// mapping the error to [JsExecutionLimitError] is safe and keeps the
    /// host contract: scripts terminate with a RuntimeException, never an Error.
    ///
    /// @return the limit error to throw (never returns normally)
    private JsExecutionLimitError hostStackExhausted() {
        callDepth = 0; // finally-based unwinding may have been skipped mid-overflow
        return new JsExecutionLimitError(
                "Maximum call stack size exceeded (host thread stack exhausted)");
    }

    /// Runs a program in global scope with an explicit `this` binding, returning the
    /// completion value (REPL-style, like [#run]). Used by host integrations (e.g. XFA
    /// scripting) that execute a script "as a method" of a host node — `this` resolves to the
    /// given object while top-level `var`/`function` declarations and the completion
    /// value behave as in [#run].
    ///
    /// @param program  the parsed program
    /// @param thisVal  the `this` binding (a [JSObject]; falls back to the global object)
    /// @return the completion value
    public Object runWithThis(Node.Program program, Object thisVal) {
        steps = 0; // fresh execution budget per top-level script
        Object binding = thisVal instanceof JSObject ? thisVal : realm.globalObject;
        hoist(program.body, realm.globalObject, globalScope);
        thisStack.push(binding);
        try {
            Object last = UNDEF;
            for (Node stmt : program.body) {
                Object v = execStmt(stmt, globalScope);
                if (v != null) {
                    last = v;
                }
            }
            return last;
        } catch (StackOverflowError e) {
            throw hostStackExhausted();
        } finally {
            thisStack.pop();
        }
    }

    /* ========================== statements ========================== */

    private Object execStmt(Node n, Scope scope) {
        countStep();
        if (n instanceof Node.ExprStmt) {
            return evalExpr(((Node.ExprStmt) n).expression, scope);
        }
        if (n instanceof Node.VarStmt) {
            for (Node.VarDeclarator d : ((Node.VarStmt) n).declarations) {
                if (d.init != null) {
                    Object v = evalExpr(d.init, scope);
                    assignName(d.name, v, scope);
                }
            }
            return null;
        }
        if (n instanceof Node.Block) {
            Object last = null;
            for (Node s : ((Node.Block) n).body) {
                Object v = execStmt(s, scope);
                if (v != null) {
                    last = v;
                }
            }
            return last;
        }
        if (n instanceof Node.EmptyStmt || n instanceof Node.FunctionDecl
                || n instanceof Node.DebuggerStmt) {
            return null;
        }
        if (n instanceof Node.IfStmt) {
            Node.IfStmt s = (Node.IfStmt) n;
            if (Types.toBoolean(evalExpr(s.test, scope))) {
                return execStmt(s.consequent, scope);
            } else if (s.alternate != null) {
                return execStmt(s.alternate, scope);
            }
            return null;
        }
        if (n instanceof Node.WhileStmt) {
            return execWhile((Node.WhileStmt) n, scope, null);
        }
        if (n instanceof Node.DoWhileStmt) {
            return execDoWhile((Node.DoWhileStmt) n, scope, null);
        }
        if (n instanceof Node.ForStmt) {
            return execFor((Node.ForStmt) n, scope, null);
        }
        if (n instanceof Node.ForInStmt) {
            return execForIn((Node.ForInStmt) n, scope, null);
        }
        if (n instanceof Node.ReturnStmt) {
            Node arg = ((Node.ReturnStmt) n).argument;
            throw new ReturnSignal(arg == null ? UNDEF : evalExpr(arg, scope));
        }
        if (n instanceof Node.BreakStmt) {
            throw new BreakSignal(((Node.BreakStmt) n).label);
        }
        if (n instanceof Node.ContinueStmt) {
            throw new ContinueSignal(((Node.ContinueStmt) n).label);
        }
        if (n instanceof Node.ThrowStmt) {
            throw new JSException(evalExpr(((Node.ThrowStmt) n).argument, scope));
        }
        if (n instanceof Node.TryStmt) {
            return execTry((Node.TryStmt) n, scope);
        }
        if (n instanceof Node.SwitchStmt) {
            return execSwitch((Node.SwitchStmt) n, scope);
        }
        if (n instanceof Node.WithStmt) {
            Node.WithStmt s = (Node.WithStmt) n;
            JSObject obj = toObject(evalExpr(s.object, scope));
            Scope w = new Scope(obj, scope, true);
            return execStmt(s.body, w);
        }
        if (n instanceof Node.LabeledStmt) {
            return execLabeled((Node.LabeledStmt) n, scope);
        }
        throw new JSUnsupportedError("statement " + n.getClass().getSimpleName(), n.line, n.column);
    }

    private Object execLabeled(Node.LabeledStmt s, Scope scope) {
        Node body = s.body;
        try {
            if (body instanceof Node.WhileStmt) {
                return execWhile((Node.WhileStmt) body, scope, s.label);
            }
            if (body instanceof Node.DoWhileStmt) {
                return execDoWhile((Node.DoWhileStmt) body, scope, s.label);
            }
            if (body instanceof Node.ForStmt) {
                return execFor((Node.ForStmt) body, scope, s.label);
            }
            if (body instanceof Node.ForInStmt) {
                return execForIn((Node.ForInStmt) body, scope, s.label);
            }
            return execStmt(body, scope);
        } catch (BreakSignal b) {
            if (b.label != null && b.label.equals(s.label)) {
                return null;
            }
            throw b;
        }
    }

    private Object execWhile(Node.WhileStmt s, Scope scope, String label) {
        while (Types.toBoolean(evalExpr(s.test, scope))) {
            try {
                execStmt(s.body, scope);
            } catch (BreakSignal b) {
                if (matchesLoop(b.label, label)) {
                    break;
                }
                throw b;
            } catch (ContinueSignal c) {
                if (matchesLoop(c.label, label)) {
                    continue;
                }
                throw c;
            }
        }
        return null;
    }

    private Object execDoWhile(Node.DoWhileStmt s, Scope scope, String label) {
        do {
            try {
                execStmt(s.body, scope);
            } catch (BreakSignal b) {
                if (matchesLoop(b.label, label)) {
                    break;
                }
                throw b;
            } catch (ContinueSignal c) {
                if (matchesLoop(c.label, label)) {
                    continue;
                }
                throw c;
            }
        } while (Types.toBoolean(evalExpr(s.test, scope)));
        return null;
    }

    private Object execFor(Node.ForStmt s, Scope scope, String label) {
        if (s.init != null) {
            if (s.init instanceof Node.VarStmt) {
                execStmt(s.init, scope);
            } else {
                evalExpr(s.init, scope);
            }
        }
        while (s.test == null || Types.toBoolean(evalExpr(s.test, scope))) {
            try {
                execStmt(s.body, scope);
            } catch (BreakSignal b) {
                if (matchesLoop(b.label, label)) {
                    break;
                }
                throw b;
            } catch (ContinueSignal c) {
                if (!matchesLoop(c.label, label)) {
                    throw c;
                }
            }
            if (s.update != null) {
                evalExpr(s.update, scope);
            }
        }
        return null;
    }

    private Object execForIn(Node.ForInStmt s, Scope scope, String label) {
        Object rightVal = evalExpr(s.right, scope);
        if (rightVal == UNDEF || rightVal == JSNull.NULL) {
            return null;
        }
        JSObject obj = toObject(rightVal);
        for (String key : enumerateKeys(obj)) {
            assignForInTarget(s.left, key, scope);
            try {
                execStmt(s.body, scope);
            } catch (BreakSignal b) {
                if (matchesLoop(b.label, label)) {
                    break;
                }
                throw b;
            } catch (ContinueSignal c) {
                if (matchesLoop(c.label, label)) {
                    continue;
                }
                throw c;
            }
        }
        return null;
    }

    private void assignForInTarget(Node left, String key, Scope scope) {
        if (left instanceof Node.VarStmt) {
            assignName(((Node.VarStmt) left).declarations.get(0).name, key, scope);
        } else {
            Ref ref = resolveRef(left, scope);
            setRef(ref, key);
        }
    }

    private List<String> enumerateKeys(JSObject obj) {
        List<String> out = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        JSObject o = obj;
        while (o != null) {
            for (String k : o.ownEnumerableKeys()) {
                if (seen.add(k)) {
                    out.add(k);
                }
            }
            o = o.getPrototype();
        }
        return out;
    }

    private boolean matchesLoop(String signalLabel, String loopLabel) {
        return signalLabel == null || signalLabel.equals(loopLabel);
    }

    private Object execTry(Node.TryStmt s, Scope scope) {
        try {
            execStmt(s.block, scope);
        } catch (JSException ex) {
            if (s.catchBlock != null) {
                JSObject catchRec = new JSObject(null);
                catchRec.put(s.catchParam, ex.value);
                Scope catchScope = new Scope(catchRec, scope, false);
                try {
                    execStmt(s.catchBlock, catchScope);
                } finally {
                    if (s.finallyBlock != null) {
                        execStmt(s.finallyBlock, scope);
                    }
                }
                return null;
            }
            if (s.finallyBlock != null) {
                execStmt(s.finallyBlock, scope);
            }
            throw ex;
        } catch (ReturnSignal | BreakSignal | ContinueSignal flow) {
            if (s.finallyBlock != null) {
                execStmt(s.finallyBlock, scope);
            }
            throw flow;
        }
        if (s.finallyBlock != null) {
            execStmt(s.finallyBlock, scope);
        }
        return null;
    }

    private Object execSwitch(Node.SwitchStmt s, Scope scope) {
        Object disc = evalExpr(s.discriminant, scope);
        try {
            boolean matched = false;
            int defaultIdx = -1;
            for (int i = 0; i < s.cases.size(); i++) {
                Node.SwitchCase c = s.cases.get(i);
                if (c.test == null) {
                    defaultIdx = i;
                    continue;
                }
                if (!matched && Types.strictEquals(disc, evalExpr(c.test, scope))) {
                    matched = true;
                }
                if (matched) {
                    for (Node st : c.body) {
                        execStmt(st, scope);
                    }
                }
            }
            if (!matched && defaultIdx >= 0) {
                for (int i = defaultIdx; i < s.cases.size(); i++) {
                    for (Node st : s.cases.get(i).body) {
                        execStmt(st, scope);
                    }
                }
            }
        } catch (BreakSignal b) {
            if (b.label != null) {
                throw b;
            }
        }
        return null;
    }

    /* ========================== expressions ========================= */

    /// Evaluates an expression node.
    ///
    /// @param n     expression
    /// @param scope current scope
    /// @return the value
    public Object evalExpr(Node n, Scope scope) {
        countStep();
        if (n instanceof Node.NumberLit) {
            return ((Node.NumberLit) n).value;
        }
        if (n instanceof Node.StringLit) {
            return ((Node.StringLit) n).value;
        }
        if (n instanceof Node.BoolLit) {
            return ((Node.BoolLit) n).value;
        }
        if (n instanceof Node.NullLit) {
            return JSNull.NULL;
        }
        if (n instanceof Node.Ident) {
            return readName(((Node.Ident) n).name, scope, n);
        }
        if (n instanceof Node.ThisExpr) {
            return thisStack.peek();
        }
        if (n instanceof Node.AssignExpr) {
            return evalAssign((Node.AssignExpr) n, scope);
        }
        if (n instanceof Node.BinaryExpr) {
            return evalBinary((Node.BinaryExpr) n, scope);
        }
        if (n instanceof Node.LogicalExpr) {
            Node.LogicalExpr e = (Node.LogicalExpr) n;
            Object l = evalExpr(e.left, scope);
            if (e.operator.equals("&&")) {
                return Types.toBoolean(l) ? evalExpr(e.right, scope) : l;
            }
            return Types.toBoolean(l) ? l : evalExpr(e.right, scope);
        }
        if (n instanceof Node.UnaryExpr) {
            return evalUnary((Node.UnaryExpr) n, scope);
        }
        if (n instanceof Node.UpdateExpr) {
            return evalUpdate((Node.UpdateExpr) n, scope);
        }
        if (n instanceof Node.ConditionalExpr) {
            Node.ConditionalExpr e = (Node.ConditionalExpr) n;
            return Types.toBoolean(evalExpr(e.test, scope))
                    ? evalExpr(e.consequent, scope) : evalExpr(e.alternate, scope);
        }
        if (n instanceof Node.CallExpr) {
            return evalCall((Node.CallExpr) n, scope);
        }
        if (n instanceof Node.NewExpr) {
            return evalNew((Node.NewExpr) n, scope);
        }
        if (n instanceof Node.MemberExpr) {
            Node.MemberExpr e = (Node.MemberExpr) n;
            Object base = evalExpr(e.object, scope);
            String name = memberName(e, scope);
            return getMember(base, name, e);
        }
        if (n instanceof Node.ArrayLit) {
            return evalArrayLit((Node.ArrayLit) n, scope);
        }
        if (n instanceof Node.ObjectLit) {
            return evalObjectLit((Node.ObjectLit) n, scope);
        }
        if (n instanceof Node.FunctionExpr) {
            return makeFunctionExpr((Node.FunctionExpr) n, scope);
        }
        if (n instanceof Node.RegexLit) {
            Node.RegexLit e = (Node.RegexLit) n;
            return realm.regexpConstructor != null
                    ? ((JSFunction) realm.regexpConstructor).construct(this,
                        new Object[]{e.pattern, e.flags})
                    : UNDEF;
        }
        if (n instanceof Node.SequenceExpr) {
            Object v = UNDEF;
            for (Node e : ((Node.SequenceExpr) n).expressions) {
                v = evalExpr(e, scope);
            }
            return v;
        }
        throw new JSUnsupportedError("expression " + n.getClass().getSimpleName(), n.line, n.column);
    }

    private Object evalArrayLit(Node.ArrayLit n, Scope scope) {
        JSArray a = realm.newArray();
        for (int i = 0; i < n.elements.size(); i++) {
            Node el = n.elements.get(i);
            if (el != null) {
                a.put(Integer.toString(i), evalExpr(el, scope));
            }
        }
        a.setLength(n.elements.size());
        return a;
    }

    private Object evalObjectLit(Node.ObjectLit n, Scope scope) {
        JSObject o = realm.newObject();
        for (Node.Property p : n.properties) {
            o.put(p.key, evalExpr(p.value, scope));
        }
        return o;
    }

    private Object makeFunctionExpr(Node.FunctionExpr n, Scope scope) {
        Scope closure = scope;
        if (n.name != null) {
            JSObject nameRec = new JSObject(null);
            closure = new Scope(nameRec, scope, false);
            UserFunction fn = newUserFunction(n.name, n.params, n.body, closure);
            nameRec.define(n.name, fn, false, false, false);
            return fn;
        }
        return newUserFunction("", n.params, n.body, closure);
    }

    private UserFunction newUserFunction(String name, List<String> params, Node.Block body, Scope closure) {
        UserFunction fn = new UserFunction(realm.functionPrototype, name, params, body, closure);
        JSObject proto = realm.newObject();
        proto.defineHidden("constructor", fn);
        fn.defineHidden("prototype", proto);
        return fn;
    }

    private Object evalUnary(Node.UnaryExpr n, Scope scope) {
        String op = n.operator;
        if (op.equals("typeof")) {
            if (n.argument instanceof Node.Ident
                    && scope.resolveBase(((Node.Ident) n.argument).name) == null) {
                return "undefined";
            }
            return Types.typeOf(evalExpr(n.argument, scope));
        }
        if (op.equals("delete")) {
            if (n.argument instanceof Node.MemberExpr) {
                Node.MemberExpr m = (Node.MemberExpr) n.argument;
                Object base = evalExpr(m.object, scope);
                if (base instanceof JSObject) {
                    return ((JSObject) base).delete(memberName(m, scope));
                }
                return true;
            }
            if (n.argument instanceof Node.Ident) {
                JSObject base = scope.resolveBase(((Node.Ident) n.argument).name);
                if (base != null) {
                    return base.delete(((Node.Ident) n.argument).name);
                }
                return true;
            }
            evalExpr(n.argument, scope);
            return true;
        }
        if (op.equals("void")) {
            evalExpr(n.argument, scope);
            return UNDEF;
        }
        Object v = evalExpr(n.argument, scope);
        switch (op) {
            case "!": return !Types.toBoolean(v);
            case "-": return -toNumber(v);
            case "+": return toNumber(v);
            case "~": return (double) (~JSNumber.toInt32(toNumber(v)));
            default:
                throw new JSUnsupportedError("unary " + op, n.line, n.column);
        }
    }

    private Object evalUpdate(Node.UpdateExpr n, Scope scope) {
        Ref ref = resolveRef(n.argument, scope);
        double oldV = toNumber(getRef(ref));
        double newV = n.operator.equals("++") ? oldV + 1 : oldV - 1;
        setRef(ref, newV);
        return n.prefix ? (Object) newV : (Object) oldV;
    }

    private Object evalAssign(Node.AssignExpr n, Scope scope) {
        if (n.operator.equals("=")) {
            Object v = evalExpr(n.value, scope);
            Ref ref = resolveRef(n.target, scope);
            setRef(ref, v);
            return v;
        }
        Ref ref = resolveRef(n.target, scope);
        Object cur = getRef(ref);
        Object rhs = evalExpr(n.value, scope);
        String binOp = n.operator.substring(0, n.operator.length() - 1);
        Object result = applyBinary(binOp, cur, rhs);
        setRef(ref, result);
        return result;
    }

    private Object evalBinary(Node.BinaryExpr n, Scope scope) {
        Object l = evalExpr(n.left, scope);
        Object r = evalExpr(n.right, scope);
        return applyBinary(n.operator, l, r);
    }

    private Object applyBinary(String op, Object l, Object r) {
        switch (op) {
            case "+": return add(l, r);
            case "-": return toNumber(l) - toNumber(r);
            case "*": return toNumber(l) * toNumber(r);
            case "/": return toNumber(l) / toNumber(r);
            case "%": return toNumber(l) % toNumber(r);
            case "==": return looseEquals(l, r);
            case "!=": return !looseEquals(l, r);
            case "===": return Types.strictEquals(l, r);
            case "!==": return !Types.strictEquals(l, r);
            case "<": return lessThan(l, r);
            case ">": return lessThan(r, l);
            case "<=": return !lessThanOrUndefined(r, l);
            case ">=": return !lessThanOrUndefined(l, r);
            case "&": return (double) (JSNumber.toInt32(toNumber(l)) & JSNumber.toInt32(toNumber(r)));
            case "|": return (double) (JSNumber.toInt32(toNumber(l)) | JSNumber.toInt32(toNumber(r)));
            case "^": return (double) (JSNumber.toInt32(toNumber(l)) ^ JSNumber.toInt32(toNumber(r)));
            case "<<": return (double) (JSNumber.toInt32(toNumber(l)) << (JSNumber.toUint32(toNumber(r)) & 31));
            case ">>": return (double) (JSNumber.toInt32(toNumber(l)) >> (JSNumber.toUint32(toNumber(r)) & 31));
            case ">>>": return (double) (JSNumber.toUint32(toNumber(l)) >>> (JSNumber.toUint32(toNumber(r)) & 31));
            case "instanceof": return instanceOf(l, r);
            case "in": return inOperator(l, r);
            default:
                throw new JSException(realm.makeError(realm.typeErrorPrototype,
                        "TypeError", "Unknown operator " + op));
        }
    }

    private boolean instanceOf(Object l, Object r) {
        if (!(r instanceof JSFunction)) {
            throw realm.typeError("instanceof: right operand is not callable");
        }
        Object protoProp = ((JSObject) r).get("prototype");
        if (!(protoProp instanceof JSObject)) {
            throw realm.typeError("instanceof: prototype is not an object");
        }
        if (!(l instanceof JSObject)) {
            return false;
        }
        JSObject p = ((JSObject) l).getPrototype();
        while (p != null) {
            if (p == protoProp) {
                return true;
            }
            p = p.getPrototype();
        }
        return false;
    }

    private boolean inOperator(Object l, Object r) {
        if (!(r instanceof JSObject)) {
            throw realm.typeError("'in' right operand is not an object");
        }
        return ((JSObject) r).hasProperty(toStringJS(l));
    }

    /* ---------------------------- calls ----------------------------- */

    private Object evalCall(Node.CallExpr n, Scope scope) {
        Object thisVal = UNDEF;
        Object fn;
        if (n.callee instanceof Node.MemberExpr) {
            Node.MemberExpr m = (Node.MemberExpr) n.callee;
            Object base = evalExpr(m.object, scope);
            String name = memberName(m, scope);
            thisVal = base;
            fn = getMember(base, name, m);
        } else {
            fn = evalExpr(n.callee, scope);
        }
        Object[] args = evalArgs(n.arguments, scope);
        if (!(fn instanceof JSFunction)) {
            throw realm.typeError(describeCallee(n.callee) + " is not a function");
        }
        return ((JSFunction) fn).call(this, thisVal, args);
    }

    private String describeCallee(Node callee) {
        if (callee instanceof Node.Ident) {
            return ((Node.Ident) callee).name;
        }
        if (callee instanceof Node.MemberExpr) {
            Node.MemberExpr m = (Node.MemberExpr) callee;
            if (!m.computed && m.property instanceof Node.Ident) {
                return ((Node.Ident) m.property).name;
            }
        }
        return "expression";
    }

    private Object evalNew(Node.NewExpr n, Scope scope) {
        Object fn = evalExpr(n.callee, scope);
        Object[] args = evalArgs(n.arguments, scope);
        if (!(fn instanceof JSFunction)) {
            throw realm.typeError(describeCallee(n.callee) + " is not a constructor");
        }
        return ((JSFunction) fn).construct(this, args);
    }

    private Object[] evalArgs(List<Node> argNodes, Scope scope) {
        Object[] args = new Object[argNodes.size()];
        for (int i = 0; i < argNodes.size(); i++) {
            args[i] = evalExpr(argNodes.get(i), scope);
        }
        return args;
    }

    /// Invokes a function value with an explicit `this` and arguments
    /// (used by `call`/`apply` and internal callbacks).
    ///
    /// @param fn      function value
    /// @param thisVal`this` binding
    /// @param args    arguments
    /// @return result
    public Object callFunction(Object fn, Object thisVal, Object[] args) {
        if (!(fn instanceof JSFunction)) {
            throw realm.typeError("value is not a function");
        }
        return ((JSFunction) fn).call(this, thisVal, args);
    }

    /// Executes a user function: builds the activation record, binds parameters
    /// and `arguments`, hoists declarations and runs the body.
    ///
    /// @param fn      the user function
    /// @param thisArg requested `this`
    /// @param args    arguments
    /// @return the return value
    public Object callUserFunction(UserFunction fn, Object thisArg, Object[] args) {
        if (++callDepth > MAX_CALL_DEPTH) {
            callDepth--;
            throw realm.rangeError("Maximum call stack size exceeded");
        }
        try {
            Object thisBinding;
            if (thisArg == UNDEF || thisArg == JSNull.NULL) {
                thisBinding = realm.globalObject;
            } else if (thisArg instanceof JSObject) {
                thisBinding = thisArg;
            } else {
                thisBinding = toObject(thisArg);
            }

            JSObject activation = new JSObject(null);
            Scope scope = new Scope(activation, fn.closure, false);

            for (int i = 0; i < fn.params.size(); i++) {
                activation.put(fn.params.get(i), i < args.length ? args[i] : UNDEF);
            }
            if (!activation.hasOwnProperty("arguments")) {
                activation.defineHidden("arguments", makeArguments(fn, args));
            }
            hoist(fn.body.body, activation, scope);

            thisStack.push(thisBinding);
            try {
                for (Node stmt : fn.body.body) {
                    execStmt(stmt, scope);
                }
                return UNDEF;
            } catch (ReturnSignal rs) {
                return rs.value;
            } finally {
                thisStack.pop();
            }
        } finally {
            callDepth--;
        }
    }

    private JSObject makeArguments(UserFunction fn, Object[] args) {
        JSObject argObj = new JSObject(realm.objectPrototype);
        argObj.setClassName("Arguments");
        for (int i = 0; i < args.length; i++) {
            argObj.put(Integer.toString(i), args[i]);
        }
        argObj.defineHidden("length", (double) args.length);
        argObj.defineHidden("callee", fn);
        return argObj;
    }

    /* ----------------------- hoisting --------------------------- */

    private void hoist(List<Node> body, JSObject record, Scope scope) {
        // Function declarations first (sec 10.1.3), then var names.
        for (Node n : body) {
            if (n instanceof Node.FunctionDecl) {
                Node.FunctionDecl f = (Node.FunctionDecl) n;
                UserFunction fn = newUserFunction(f.name, f.params, f.body, scope);
                record.put(f.name, fn);
            }
        }
        hoistVars(body, record);
    }

    private void hoistVars(List<Node> body, JSObject record) {
        for (Node n : body) {
            collectVars(n, record);
        }
    }

    private void collectVars(Node n, JSObject record) {
        if (n == null) {
            return;
        }
        if (n instanceof Node.VarStmt) {
            for (Node.VarDeclarator d : ((Node.VarStmt) n).declarations) {
                if (!record.hasOwnProperty(d.name)) {
                    record.put(d.name, UNDEF);
                }
            }
        } else if (n instanceof Node.Block) {
            for (Node s : ((Node.Block) n).body) {
                collectVars(s, record);
            }
        } else if (n instanceof Node.IfStmt) {
            collectVars(((Node.IfStmt) n).consequent, record);
            collectVars(((Node.IfStmt) n).alternate, record);
        } else if (n instanceof Node.ForStmt) {
            collectVars(((Node.ForStmt) n).init, record);
            collectVars(((Node.ForStmt) n).body, record);
        } else if (n instanceof Node.ForInStmt) {
            collectVars(((Node.ForInStmt) n).left, record);
            collectVars(((Node.ForInStmt) n).body, record);
        } else if (n instanceof Node.WhileStmt) {
            collectVars(((Node.WhileStmt) n).body, record);
        } else if (n instanceof Node.DoWhileStmt) {
            collectVars(((Node.DoWhileStmt) n).body, record);
        } else if (n instanceof Node.WithStmt) {
            collectVars(((Node.WithStmt) n).body, record);
        } else if (n instanceof Node.LabeledStmt) {
            collectVars(((Node.LabeledStmt) n).body, record);
        } else if (n instanceof Node.TryStmt) {
            Node.TryStmt t = (Node.TryStmt) n;
            collectVars(t.block, record);
            collectVars(t.catchBlock, record);
            collectVars(t.finallyBlock, record);
        } else if (n instanceof Node.SwitchStmt) {
            for (Node.SwitchCase c : ((Node.SwitchStmt) n).cases) {
                for (Node s : c.body) {
                    collectVars(s, record);
                }
            }
        }
        // Do NOT descend into nested function declarations/expressions.
    }

    /* ----------------------- references -------------------------- */

    private static final class Ref {
        Scope scope;     // for identifier refs
        String identName;
        Object base;     // for member refs
        String memberName;
        boolean member;
    }

    private Ref resolveRef(Node target, Scope scope) {
        Ref r = new Ref();
        if (target instanceof Node.Ident) {
            r.identName = ((Node.Ident) target).name;
            r.scope = scope;
            r.member = false;
        } else if (target instanceof Node.MemberExpr) {
            Node.MemberExpr m = (Node.MemberExpr) target;
            r.base = evalExpr(m.object, scope);
            r.memberName = memberName(m, scope);
            r.member = true;
        } else {
            throw realm.referenceError("Invalid assignment target");
        }
        return r;
    }

    private Object getRef(Ref r) {
        if (r.member) {
            return getMember(r.base, r.memberName, null);
        }
        return readName(r.identName, r.scope, null);
    }

    private void setRef(Ref r, Object value) {
        if (r.member) {
            putMember(r.base, r.memberName, value);
        } else {
            assignName(r.identName, value, r.scope);
        }
    }

    private Object readName(String name, Scope scope, Node node) {
        JSObject base = scope.resolveBase(name);
        if (base == null) {
            throw realm.referenceError(name + " is not defined");
        }
        return base.get(name);
    }

    private void assignName(String name, Object value, Scope scope) {
        JSObject base = scope.resolveBase(name);
        if (base == null) {
            scope.globalRecord().put(name, value);
        } else {
            base.put(name, value);
        }
    }

    private String memberName(Node.MemberExpr m, Scope scope) {
        if (m.computed) {
            return toStringJS(evalExpr(m.property, scope));
        }
        return ((Node.Ident) m.property).name;
    }

    /* --------------------- property access ----------------------- */

    /// `[[Get]]` on any value, boxing primitives to their wrapper
    /// prototypes for method/property access.
    ///
    /// @param base value
    /// @param name property name
    /// @param node source node (for diagnostics) or `null`
    /// @return the property value
    public Object getMember(Object base, String name, Node node) {
        if (base instanceof JSObject) {
            return ((JSObject) base).get(name);
        }
        if (base instanceof String) {
            String s = (String) base;
            if (name.equals("length")) {
                return (double) s.length();
            }
            long idx = JSArray.arrayIndex(name);
            if (idx >= 0 && idx < s.length()) {
                return String.valueOf(s.charAt((int) idx));
            }
            return realm.stringPrototype.get(name);
        }
        if (base instanceof Double) {
            return realm.numberPrototype.get(name);
        }
        if (base instanceof Boolean) {
            return realm.booleanPrototype.get(name);
        }
        if (base == UNDEF) {
            throw realm.typeError("Cannot read property '" + name + "' of undefined");
        }
        if (base == JSNull.NULL) {
            throw realm.typeError("Cannot read property '" + name + "' of null");
        }
        throw realm.typeError("Cannot read property '" + name + "'");
    }

    private void putMember(Object base, String name, Object value) {
        if (base instanceof JSObject) {
            ((JSObject) base).put(name, value);
            return;
        }
        if (base == UNDEF || base == JSNull.NULL) {
            throw realm.typeError("Cannot set property '" + name + "' of "
                    + (base == UNDEF ? "undefined" : "null"));
        }
        // Setting a property on a primitive is silently ignored (ES3 non-strict).
    }

    /* --------------------- abstract operations ------------------- */

    /// ToPrimitive (sec 9.1).
    ///
    /// @param v    value
    /// @param hint`"string"` or `"number"` (or `null` for default)
    /// @return a primitive value
    public Object toPrimitive(Object v, String hint) {
        if (!(v instanceof JSObject)) {
            return v;
        }
        JSObject o = (JSObject) v;
        String h = hint;
        if (h == null) {
            h = "Date".equals(o.getClassName()) ? "string" : "number";
        }
        String[] order = h.equals("string")
                ? new String[]{"toString", "valueOf"}
                : new String[]{"valueOf", "toString"};
        for (String m : order) {
            Object fn = o.get(m);
            if (fn instanceof JSFunction) {
                Object res = ((JSFunction) fn).call(this, o, new Object[0]);
                if (!(res instanceof JSObject)) {
                    return res;
                }
            }
        }
        throw realm.typeError("Cannot convert object to primitive value");
    }

    /// ToNumber (sec 9.3).
    public double toNumber(Object v) {
        if (v instanceof JSObject) {
            return Types.toNumberPrimitiveOrNaN(toPrimitive(v, "number"));
        }
        return Types.toNumberPrimitiveOrNaN(v);
    }

    /// ToString (sec 9.8).
    public String toStringJS(Object v) {
        if (v instanceof JSObject) {
            return Types.primitiveToString(toPrimitive(v, "string"));
        }
        return Types.primitiveToString(v);
    }

    /// ToObject (sec 9.9); throws TypeError for `undefined`/`null`.
    public JSObject toObject(Object v) {
        if (v instanceof JSObject) {
            return (JSObject) v;
        }
        if (v instanceof String) {
            return makeStringWrapper((String) v);
        }
        if (v instanceof Double) {
            JSObject o = new JSObject(realm.numberPrototype);
            o.setClassName("Number");
            o.primitiveValue = v;
            return o;
        }
        if (v instanceof Boolean) {
            JSObject o = new JSObject(realm.booleanPrototype);
            o.setClassName("Boolean");
            o.primitiveValue = v;
            return o;
        }
        throw realm.typeError("Cannot convert " + (v == UNDEF ? "undefined" : "null") + " to object");
    }

    private JSObject makeStringWrapper(String s) {
        JSObject o = new JSObject(realm.stringPrototype);
        o.setClassName("String");
        o.primitiveValue = s;
        o.define("length", (double) s.length(), false, false, false);
        return o;
    }

    /// The addition operator (sec 11.6.1): string concat or numeric add.
    public Object add(Object l, Object r) {
        Object lp = toPrimitive(l, null);
        Object rp = toPrimitive(r, null);
        if (lp instanceof String || rp instanceof String) {
            return Types.primitiveToString(lp) + Types.primitiveToString(rp);
        }
        return Types.toNumberPrimitiveOrNaN(lp) + Types.toNumberPrimitiveOrNaN(rp);
    }

    /// Abstract relational comparison (sec 11.8.5); returns Java boolean.
    private boolean lessThan(Object l, Object r) {
        Object lp = toPrimitive(l, "number");
        Object rp = toPrimitive(r, "number");
        if (lp instanceof String && rp instanceof String) {
            return ((String) lp).compareTo((String) rp) < 0;
        }
        double a = Types.toNumberPrimitiveOrNaN(lp);
        double b = Types.toNumberPrimitiveOrNaN(rp);
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return false;
        }
        return a < b;
    }

    /// Relational comparison treating a NaN/undefined result as `true`
    /// so that `<=`/`>=` (which negate the swapped comparison)
    /// behave correctly when an operand is NaN.
    private boolean lessThanOrUndefined(Object l, Object r) {
        Object lp = toPrimitive(l, "number");
        Object rp = toPrimitive(r, "number");
        if (lp instanceof String && rp instanceof String) {
            return ((String) lp).compareTo((String) rp) < 0;
        }
        double a = Types.toNumberPrimitiveOrNaN(lp);
        double b = Types.toNumberPrimitiveOrNaN(rp);
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return true; // undefined comparison -> forces <=/>= to false
        }
        return a < b;
    }

    /// Abstract equality `==` (sec 11.9.3).
    public boolean looseEquals(Object a, Object b) {
        if (sameType(a, b)) {
            return Types.strictEquals(a, b);
        }
        if ((a == JSNull.NULL && b == UNDEF) || (a == UNDEF && b == JSNull.NULL)) {
            return true;
        }
        if (a instanceof Double && b instanceof String) {
            return ((Double) a) == JSNumber.fromString((String) b);
        }
        if (a instanceof String && b instanceof Double) {
            return JSNumber.fromString((String) a) == ((Double) b);
        }
        if (a instanceof Boolean) {
            return looseEquals(((Boolean) a) ? 1.0 : 0.0, b);
        }
        if (b instanceof Boolean) {
            return looseEquals(a, ((Boolean) b) ? 1.0 : 0.0);
        }
        if ((a instanceof Double || a instanceof String) && b instanceof JSObject) {
            return looseEquals(a, toPrimitive(b, null));
        }
        if (a instanceof JSObject && (b instanceof Double || b instanceof String)) {
            return looseEquals(toPrimitive(a, null), b);
        }
        return false;
    }

    private boolean sameType(Object a, Object b) {
        return Types.typeOf(a).equals(Types.typeOf(b))
                && (a == JSNull.NULL) == (b == JSNull.NULL);
    }
}
