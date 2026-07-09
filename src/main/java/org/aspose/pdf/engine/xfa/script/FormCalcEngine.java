package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.xfa.model.XfaNode;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * FormCalc evaluator (B2.2) — evaluates a {@link FormCalcParser} AST over the merged Form DOM through
 * the same {@link XfaScriptHost} value bridge + SOM resolver the JS path uses. Typeless: values are
 * {@link Double} (numbers) or {@link String} (text), with FormCalc coercion (empty/{@code null} → 0
 * for arithmetic, → "" for text; comparison numeric when both operands are numeric). A calculate's
 * result is the value of its last expression (written back by {@link XfaScripting}); an explicit
 * {@code $ = …} assignment writes the field directly.
 *
 * <p>Builtins implemented are exactly the B2.0 corpus subset + the common aggregates the FormCalc spec
 * worked examples use: {@code Sum/Avg/Min/Max/Count} (node-set aware), {@code Concat/Len}, {@code Abs/
 * Round}, {@code Exists}, and date helpers {@code Num2Date/Date2Num/Date} (reusing the B3.1
 * {@link XfaUtil} date machinery). The remaining ~297 spec builtins are stubbed (return "") and
 * recorded in {@link #unimplemented()} as a tracked gap.</p>
 */
final class FormCalcEngine {

    private final XfaScriptHost host;
    private final TreeSet<String> unimplemented = new TreeSet<>();

    /** Milliseconds of 1900-01-01T00:00:00 UTC — the FormCalc date-number epoch. */
    private static final long EPOCH_1900;

    static {
        java.util.Calendar c = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(1900, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        EPOCH_1900 = c.getTimeInMillis();
    }

    FormCalcEngine(XfaScriptHost host) {
        this.host = host;
    }

    /** @return the FormCalc builtins encountered but not implemented (a tracked gap, returns ""). */
    TreeSet<String> unimplemented() {
        return unimplemented;
    }

    /**
     * Parses + evaluates a FormCalc script.
     *
     * @param src     the FormCalc source
     * @param current the calculate/validate/event carrier node (the {@code $} reference + SOM origin)
     * @return the value of the last expression (Double/String/null)
     * @throws FormCalcError on a lex/parse/eval failure
     */
    Object run(String src, XfaScriptNode current) {
        FormCalcParser.Block program = FormCalcParser.parse(src);
        return evalBlock(program, current);
    }

    /* ------------------------------ evaluation ------------------------------ */

    private Object evalBlock(FormCalcParser.Block b, XfaScriptNode cur) {
        Object last = null;
        for (FormCalcParser.Node s : b.stmts) {
            last = eval(s, cur);
        }
        return last;
    }

    private Object eval(FormCalcParser.Node n, XfaScriptNode cur) {
        if (n instanceof FormCalcParser.Num) {
            return ((FormCalcParser.Num) n).value;
        }
        if (n instanceof FormCalcParser.Str) {
            return ((FormCalcParser.Str) n).value;
        }
        if (n instanceof FormCalcParser.Null) {
            return null;
        }
        if (n instanceof FormCalcParser.Ref) {
            return evalRefScalar((FormCalcParser.Ref) n, cur);
        }
        if (n instanceof FormCalcParser.Unary) {
            return evalUnary((FormCalcParser.Unary) n, cur);
        }
        if (n instanceof FormCalcParser.Bin) {
            return evalBin((FormCalcParser.Bin) n, cur);
        }
        if (n instanceof FormCalcParser.Call) {
            return evalCall((FormCalcParser.Call) n, cur);
        }
        if (n instanceof FormCalcParser.If) {
            return evalIf((FormCalcParser.If) n, cur);
        }
        if (n instanceof FormCalcParser.Block) {
            return evalBlock((FormCalcParser.Block) n, cur);
        }
        if (n instanceof FormCalcParser.Assign) {
            return evalAssign((FormCalcParser.Assign) n, cur);
        }
        throw new FormCalcError("FormCalc: unhandled node " + n.getClass().getSimpleName());
    }

    private Object evalIf(FormCalcParser.If n, XfaScriptNode cur) {
        if (truthy(eval(n.cond, cur))) {
            return evalBlock(n.thenBlock, cur);
        }
        if (n.elseBranch != null) {
            return eval(n.elseBranch, cur);
        }
        return null;
    }

    private Object evalUnary(FormCalcParser.Unary n, XfaScriptNode cur) {
        Object v = eval(n.operand, cur);
        switch (n.op) {
            case "-": return -toNum(v);
            case "+": return toNum(v);
            case "not": return truthy(v) ? 0.0 : 1.0;
            default: throw new FormCalcError("FormCalc: bad unary '" + n.op + "'");
        }
    }

    private Object evalBin(FormCalcParser.Bin n, XfaScriptNode cur) {
        // logical short-circuit
        if (n.op.equals("and")) {
            return truthy(eval(n.left, cur)) && truthy(eval(n.right, cur)) ? 1.0 : 0.0;
        }
        if (n.op.equals("or")) {
            return truthy(eval(n.left, cur)) || truthy(eval(n.right, cur)) ? 1.0 : 0.0;
        }
        Object l = eval(n.left, cur);
        Object r = eval(n.right, cur);
        switch (n.op) {
            case "+": return toNum(l) + toNum(r);
            case "-": return toNum(l) - toNum(r);
            case "*": return toNum(l) * toNum(r);
            case "/": {
                double d = toNum(r);
                return d == 0 ? 0.0 : toNum(l) / d; // FormCalc: guard /0 to a value, not NaN/Inf
            }
            case "&": return toStr(l) + toStr(r); // string concat
            case "==": case "<>": case "<": case ">": case "<=": case ">=":
                return compare(n.op, l, r) ? 1.0 : 0.0;
            default: throw new FormCalcError("FormCalc: bad operator '" + n.op + "'");
        }
    }

    private boolean compare(String op, Object l, Object r) {
        int c;
        if (isNumeric(l) && isNumeric(r)) {
            c = Double.compare(toNum(l), toNum(r));
        } else {
            c = toStr(l).compareTo(toStr(r));
        }
        switch (op) {
            case "==": return c == 0;
            case "<>": return c != 0;
            case "<": return c < 0;
            case ">": return c > 0;
            case "<=": return c <= 0;
            case ">=": return c >= 0;
            default: return false;
        }
    }

    /* ------------------------------ references / assignment ------------------------------ */

    private Object evalRefScalar(FormCalcParser.Ref ref, XfaScriptNode cur) {
        XfaScriptNode n = resolveRef(ref.path, cur);
        if (n == null) {
            return null;
        }
        return n.get("rawValue"); // reuses the JS value bridge (numeric field → Double, else String)
    }

    /** Resolves a reference, treating a bare {@code $} as the current node. */
    private XfaScriptNode resolveRef(String path, XfaScriptNode cur) {
        if (path.equals("$")) {
            return cur;
        }
        return host.resolveOne(path, cur);
    }

    /** Expands a node-set for an aggregate argument (a {@code Ref} → all matching nodes' values). */
    private List<Object> values(FormCalcParser.Node arg, XfaScriptNode cur) {
        List<Object> out = new ArrayList<>();
        if (arg instanceof FormCalcParser.Ref) {
            String path = ((FormCalcParser.Ref) arg).path;
            if (path.equals("$")) {
                out.add(cur == null ? null : cur.get("rawValue"));
                return out;
            }
            for (XfaScriptNode n : host.resolveList(path, cur)) {
                out.add(n.get("rawValue"));
            }
            return out;
        }
        out.add(eval(arg, cur));
        return out;
    }

    private Object evalAssign(FormCalcParser.Assign a, XfaScriptNode cur) {
        Object v = eval(a.value, cur);
        String path = a.target.path;
        String[] segs = path.split("\\.");
        String last = segs.length == 0 ? path : segs[segs.length - 1];
        String base = segs.length <= 1 ? "" : path.substring(0, path.length() - last.length() - 1);
        if (isAttrProp(last) && !base.isEmpty()) {
            XfaScriptNode bn = resolveRef(base, cur);
            if (bn != null) {
                bn.node.setAttribute(last, XfaScriptNode.coerce(v));
            }
        } else if (("rawValue".equals(last) || "value".equals(last) || "formattedValue".equals(last))
                && !base.isEmpty()) {
            XfaScriptNode bn = resolveRef(base, cur);
            if (bn != null) {
                host.writeValue(bn.node, XfaScriptNode.coerce(v));
            }
        } else {
            XfaScriptNode bn = resolveRef(path, cur);
            if (bn != null) {
                host.writeValue(bn.node, XfaScriptNode.coerce(v));
            }
        }
        return v;
    }

    private static boolean isAttrProp(String p) {
        return "presence".equals(p) || "access".equals(p) || "mandatory".equals(p)
                || "border".equals(p) || "font".equals(p);
    }

    /* ------------------------------ builtins ------------------------------ */

    private Object evalCall(FormCalcParser.Call c, XfaScriptNode cur) {
        String fn = c.name.toLowerCase();
        switch (fn) {
            case "sum": return aggregate(c.args, cur, "sum");
            case "avg": return aggregate(c.args, cur, "avg");
            case "min": return aggregate(c.args, cur, "min");
            case "max": return aggregate(c.args, cur, "max");
            case "count": return aggregate(c.args, cur, "count");
            case "concat": {
                StringBuilder sb = new StringBuilder();
                for (FormCalcParser.Node arg : c.args) {
                    sb.append(toStr(eval(arg, cur)));
                }
                return sb.toString();
            }
            case "len": return (double) toStr(eval(arg0(c), cur)).length();
            case "abs": return Math.abs(toNum(eval(arg0(c), cur)));
            case "round": {
                double v = toNum(eval(arg0(c), cur));
                int prec = c.args.size() > 1 ? (int) toNum(eval(c.args.get(1), cur)) : 0;
                double f = Math.pow(10, prec);
                return Math.round(v * f) / f;
            }
            case "exists": {
                if (!c.args.isEmpty() && c.args.get(0) instanceof FormCalcParser.Ref) {
                    String path = ((FormCalcParser.Ref) c.args.get(0)).path;
                    if (path.equals("$")) {
                        return cur != null ? 1.0 : 0.0;
                    }
                    return host.resolveList(path, cur).isEmpty() ? 0.0 : 1.0;
                }
                Object v = c.args.isEmpty() ? null : eval(c.args.get(0), cur);
                return v == null || toStr(v).isEmpty() ? 0.0 : 1.0;
            }
            case "num2date": {
                double dn = toNum(eval(arg0(c), cur));
                String fmt = c.args.size() > 1 ? toStr(eval(c.args.get(1), cur)) : "YYYY-MM-DD";
                return XfaUtil.printd(fmt, EPOCH_1900 + (long) dn * 86400000L);
            }
            case "date2num": {
                String s = toStr(eval(arg0(c), cur));
                String fmt = c.args.size() > 1 ? toStr(eval(c.args.get(1), cur)) : "YYYY-MM-DD";
                Long ms = XfaUtil.scand(fmt, s);
                return ms == null ? 0.0 : (double) ((ms - EPOCH_1900) / 86400000L);
            }
            case "date":
                // current date as a FormCalc day-number (real clock; non-deterministic, not asserted).
                return (double) ((System.currentTimeMillis() - EPOCH_1900) / 86400000L);
            default:
                unimplemented.add(fn); // tracked gap — return "" so the script continues, no crash
                return "";
        }
    }

    private static FormCalcParser.Node arg0(FormCalcParser.Call c) {
        if (c.args.isEmpty()) {
            throw new FormCalcError("FormCalc: " + c.name + "() requires an argument");
        }
        return c.args.get(0);
    }

    /** Sum/Avg/Min/Max/Count over the flattened node-set + scalar arguments. */
    private Object aggregate(List<FormCalcParser.Node> args, XfaScriptNode cur, String kind) {
        List<Double> nums = new ArrayList<>();
        for (FormCalcParser.Node arg : args) {
            for (Object v : values(arg, cur)) {
                if ("count".equals(kind)) {
                    nums.add(1.0); // count counts the members, regardless of value
                } else if (v != null && !(v instanceof String && ((String) v).isEmpty())) {
                    nums.add(toNum(v));
                }
            }
        }
        switch (kind) {
            case "count":
                return (double) nums.size();
            case "sum": {
                double s = 0;
                for (double d : nums) {
                    s += d;
                }
                return s;
            }
            case "avg": {
                if (nums.isEmpty()) {
                    return 0.0;
                }
                double s = 0;
                for (double d : nums) {
                    s += d;
                }
                return s / nums.size();
            }
            case "min": {
                if (nums.isEmpty()) {
                    return 0.0;
                }
                double m = nums.get(0);
                for (double d : nums) {
                    m = Math.min(m, d);
                }
                return m;
            }
            case "max": {
                if (nums.isEmpty()) {
                    return 0.0;
                }
                double m = nums.get(0);
                for (double d : nums) {
                    m = Math.max(m, d);
                }
                return m;
            }
            default:
                return 0.0;
        }
    }

    /* ------------------------------ coercion ------------------------------ */

    static double toNum(Object v) {
        if (v instanceof Double) {
            return (Double) v;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1.0 : 0.0;
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            if (s.isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0.0; // FormCalc coerces a non-numeric string to 0 in arithmetic context
            }
        }
        return 0.0; // null / empty
    }

    static String toStr(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof Double) {
            return XfaScriptNode.coerce(v); // integral doubles lose the trailing .0
        }
        return String.valueOf(v);
    }

    private static boolean truthy(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            if (s.isEmpty()) {
                return false;
            }
            Double d = parse(s);
            return d == null ? !s.isEmpty() : d != 0;
        }
        return toNum(v) != 0;
    }

    private static boolean isNumeric(Object v) {
        if (v instanceof Double) {
            return true;
        }
        if (v instanceof String) {
            return parse(((String) v).trim()) != null;
        }
        return false;
    }

    private static Double parse(String s) {
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
