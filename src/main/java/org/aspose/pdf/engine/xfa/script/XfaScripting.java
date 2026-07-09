package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stage B / B3.1 PART B — executes the load-time XFA JavaScript over a merged Form DOM:
 * <b>initialize</b> events (seed) → <b>calculate</b> (derive values, in SOM-dependency topological
 * order with cycle detection) → <b>ready</b> events. {@code validate} scripts are evaluated
 * report-only (a false result flags the field, it does not block). Computed/seeded values are
 * written back through {@link org.aspose.pdf.engine.xfa.binding.FormField} so they flow into the
 * render track (L1–L5). FormCalc scripts (contentType formcalc) are counted and deferred (B2);
 * interactive events (click/change/exit/enter) are out of scope.
 */
public final class XfaScripting {

    private static final Pattern RESOLVE_REF =
            Pattern.compile("resolveNodes?\\s*\\(\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern FIELD_ACCESS =
            Pattern.compile("([A-Za-z_][\\w]*)\\s*\\.\\s*(rawValue|value|formattedValue)");
    /**
     * JavaScript-only shape markers. An untyped {@code <script>} (XFA spec default is FormCalc) is
     * routed to FormCalc UNLESS it carries one of these JS-only tokens — so the corpus's untyped-but-JS
     * scripts keep running on JS-0 (no mis-route) while genuine FormCalc stops being mis-run as JS
     * (the B3-DIAG "FormCalc-as-JS SyntaxError"). {@code .rawValue}/{@code ==} are deliberately absent
     * (FormCalc shares them).
     */
    private static final Pattern JS_MARK = Pattern.compile(
            "(?m)\\b(var|function|return|this|new|typeof|instanceof|void)\\b|===|!==|&&|\\|\\||=>|\\?|\\bxfa\\.");
    /** FormCalc keywords + implemented/known builtins — excluded from FormCalc dependency-ref scan. */
    private static final java.util.Set<String> FC_STOP = new java.util.HashSet<>(java.util.Arrays.asList(
            "if", "then", "elseif", "else", "endif", "and", "or", "not", "eq", "ne", "lt", "le", "gt",
            "ge", "null", "for", "foreach", "do", "endfor", "while", "endwhile", "func", "var", "return",
            "sum", "avg", "min", "max", "count", "concat", "len", "abs", "round", "exists", "num2date",
            "date2num", "date", "rawValue", "value", "formattedValue", "presence", "access"));
    private static final Pattern FC_IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * Whether a script is FormCalc (vs JavaScript), by contentType then untyped shape.
     *
     * @param ct  the {@code contentType} attribute (may be null/empty)
     * @param src the script source
     * @return {@code true} for FormCalc, {@code false} for JavaScript
     */
    static boolean isFormCalc(String ct, String src) {
        if (ct != null && ct.toLowerCase().contains("formcalc")) {
            return true;
        }
        if (ct != null && ct.toLowerCase().contains("javascript")) {
            return false;
        }
        // untyped: XFA spec default is FormCalc — unless it clearly looks like JavaScript.
        return src == null || !JS_MARK.matcher(src).find();
    }

    private XfaScripting() {
    }

    /** Outcome of a load-time scripting pass. */
    public static final class Result {
        /** calculate scripts seen / executed without error / failed. */
        public int calculates, calculatesOk, calculatesFailed;
        /** validate scripts evaluated / that flagged the field invalid. */
        public int validates, invalid;
        /** initialize+ready event scripts executed / failed. */
        public int events, eventsOk, eventsFailed;
        /** FormCalc scripts skipped (deferred) — B2 runs them, so this stays 0 for an untouched gap. */
        public int formCalcDeferred;
        /** FormCalc scripts run / succeeded / failed (B2 routing). */
        public int formCalc, formCalcOk, formCalcFailed;
        /** FormCalc builtins encountered but not implemented (a tracked gap; the script still ran). */
        public final java.util.TreeSet<String> formCalcUnimplemented = new java.util.TreeSet<>();
        /** Form-level script-object libraries loaded / that failed to evaluate (a JS-0 parser/host gap). */
        public int scriptLibs, scriptLibsFailed;
        /** Fields whose value was produced/changed by a script. */
        public int valuesProduced;
        /** Imported data-bound values re-asserted after a script degenerated them to empty/zero. */
        public int valuesRestored;
        /** SOM paths of fields a validate marked invalid (report-only). */
        public final List<String> invalidFields = new ArrayList<>();
        /** Detected calculate dependency cycles (field names). */
        public final List<String> cycles = new ArrayList<>();
        /** Categorised per-script failures ("phase:field:cause"). */
        public final List<String> errors = new ArrayList<>();
    }

    /** A script attached to a node, with its language and carrier kind. */
    private static final class Script {
        final XfaNode carrier;
        final String source;
        final boolean javaScript;
        final boolean formCalc;
        final String activity; // for events; null otherwise
        Script(XfaNode carrier, String source, boolean js, boolean formCalc, String activity) {
            this.carrier = carrier;
            this.source = source;
            this.javaScript = js;
            this.formCalc = formCalc;
            this.activity = activity;
        }
    }

    /** Dispatches a script to the FormCalc engine or JS-0 by its language. */
    private static Object runScript(XfaScriptHost host, Script s, XfaScriptNode node) {
        return s.formCalc ? host.runFormCalc(s.source, node) : host.run(s.source, node);
    }

    /**
     * Executes the load-time scripts of {@code dom} and writes computed values back into it.
     *
     * @param dom the merged Form DOM (mutated in place: calculated/seeded values are written)
     * @return the execution result (counts, invalid fields, cycles, categorised errors)
     */
    public static Result execute(FormDom dom) {
        return execute(dom, null);
    }

    /**
     * Executes the load-time scripts with the template available, enabling instanceManager (dynamic
     * subform add/remove). initialize events run first (they may {@code addInstance}); calculate /
     * validate / ready are then collected from the <b>mutated</b> tree so a new instance's own scripts
     * participate, and computed values are written back into {@code dom}.
     *
     * @param dom the merged Form DOM (mutated in place)
     * @param tpl the template (instanceManager occur limits + clone prototypes), or {@code null}
     * @return the execution result
     */
    public static Result execute(FormDom dom, org.aspose.pdf.engine.xfa.model.template.Template tpl) {
        Result r = new Result();
        if (dom == null || dom.getRoot() == null) {
            return r;
        }
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        r.scriptLibs = host.getLibsLoaded();
        r.scriptLibsFailed = host.getLibsFailed();

        // Snapshot the imported, data-bound, non-degenerate field values. In Adobe's import-then-print
        // flow the data is authoritative for bound fields, but our engine merges data and THEN runs the
        // load scripts, so a per-row `initialize` like `this.rawValue = 0` (fired after merge) or a row
        // calculate that degenerates to 0 when an input cell is absent would destroy the imported amount
        // (11902's personnel Cost/FEDERAL TOTAL). Restored after all scripts where a script left the
        // field empty/zero, so the authored data survives without suppressing genuine computed values.
        java.util.Map<org.aspose.pdf.engine.xfa.binding.FormField, String> boundSnapshot =
                new java.util.IdentityHashMap<>();
        for (org.aspose.pdf.engine.xfa.binding.FormField ff : dom.getFields()) {
            if (isDataBound(ff) && isNonDegenerate(ff.getValue())) {
                boundSnapshot.put(ff, ff.getValue());
            }
        }

        // 1) initialize first (may add/remove instances via instanceManager)
        List<Script> initialize = new ArrayList<>();
        collectEvents(dom.getRoot(), initialize, null, r);
        for (Script s : initialize) {
            runEvent(host, s, r);
        }

        // 2) collect calculate/validate/ready from the (possibly mutated) tree
        List<Script> ready = new ArrayList<>();
        List<Script> calculate = new ArrayList<>();
        List<Script> validate = new ArrayList<>();
        collectComputeReadyValidate(dom.getRoot(), ready, calculate, validate, r);

        // 3) calculate in SOM-dependency topological order (cycles detected, not looped)
        for (Script s : order(calculate, r)) {
            runCalculate(host, s, r);
        }
        // 4) ready (post-calculation load events)
        for (Script s : ready) {
            runEvent(host, s, r);
        }
        // 5) validate (report-only)
        for (Script s : validate) {
            runValidate(host, s, r);
        }
        // Data-authoritative restore: re-assert any bound value a script degenerated to empty/zero.
        for (java.util.Map.Entry<org.aspose.pdf.engine.xfa.binding.FormField, String> e
                : boundSnapshot.entrySet()) {
            if (!isNonDegenerate(e.getKey().getValue())) {
                e.getKey().setValue(e.getValue());
                r.valuesRestored++;
            }
        }

        r.formCalcUnimplemented.addAll(host.getFormCalcUnimplemented());
        return r;
    }

    /** Whether {@code ff} carries an imported data value (a real binding, not none/unbound). */
    private static boolean isDataBound(org.aspose.pdf.engine.xfa.binding.FormField ff) {
        org.aspose.pdf.engine.xfa.binding.FormField.BindingKind k = ff.getKind();
        return k == org.aspose.pdf.engine.xfa.binding.FormField.BindingKind.DATAREF
                || k == org.aspose.pdf.engine.xfa.binding.FormField.BindingKind.ONCE
                || k == org.aspose.pdf.engine.xfa.binding.FormField.BindingKind.GLOBAL;
    }

    /** Whether {@code s} is a meaningful value — non-null, non-empty, and not numeric zero. */
    private static boolean isNonDegenerate(String s) {
        return s != null && !s.isEmpty() && !isZeroNumeric(s);
    }

    /** Collects {@code initialize} (and, when {@code ready} non-null, ready) event scripts. */
    private static void collectEvents(XfaNode node, List<Script> initialize, List<Script> ready, Result r) {
        for (XfaNode c : node.getChildren()) {
            if ("event".equals(c.getElementName())) {
                String activity = c.getAttribute("activity");
                Script s = scriptOf(node, c, activity, r, false);
                if (s != null) {
                    if ("initialize".equals(activity)) {
                        initialize.add(s);
                    } else if (ready != null && isReady(activity)) {
                        ready.add(s);
                    }
                }
            }
        }
        for (XfaNode c : node.getChildren()) {
            collectEvents(c, initialize, ready, r);
        }
    }

    /** Collects calculate, validate and ready scripts (post-initialize, over the mutated tree). */
    private static void collectComputeReadyValidate(XfaNode node, List<Script> ready,
                                                    List<Script> calculate, List<Script> validate, Result r) {
        XfaNode calc = node.getChild("calculate");
        if (calc != null) {
            Script s = scriptOf(node, calc, null, r, true);
            if (s != null) {
                calculate.add(s);
            }
        }
        XfaNode val = node.getChild("validate");
        if (val != null) {
            Script s = scriptOf(node, val, null, r, false);
            if (s != null) {
                validate.add(s);
            }
        }
        for (XfaNode c : node.getChildren()) {
            if ("event".equals(c.getElementName()) && isReady(c.getAttribute("activity"))) {
                Script s = scriptOf(node, c, c.getAttribute("activity"), r, false);
                if (s != null) {
                    ready.add(s);
                }
            }
        }
        for (XfaNode c : node.getChildren()) {
            collectComputeReadyValidate(c, ready, calculate, validate, r);
        }
    }

    private static boolean isReady(String activity) {
        return "ready".equals(activity) || "formReady".equals(activity)
                || "layoutReady".equals(activity) || "indexChange".equals(activity);
    }

    /* ------------------------------ collection ------------------------------ */

    private static Script scriptOf(XfaNode carrier, XfaNode holder, String activity, Result r,
                                   boolean isCalculate) {
        XfaNode script = holder.getChild("script");
        if (script == null) {
            return null;
        }
        String src = script.getTextContent();
        if (src == null || src.trim().isEmpty()) {
            return null;
        }
        String ct = script.getAttribute("contentType");
        boolean formCalc = isFormCalc(ct, src);
        return new Script(carrier, src, !formCalc, formCalc, activity);
    }

    /* ------------------------------ ordering ------------------------------ */

    /**
     * Orders calculate scripts so a field is computed after the fields it references (SOM
     * dependency topological sort, Kahn's algorithm). A dependency cycle is detected and reported,
     * and its members fall back to document order — the load pass never loops.
     */
    private static List<Script> order(List<Script> calcs, Result r) {
        // index calculate carriers by name
        Map<String, Integer> indexByName = new HashMap<>();
        for (int i = 0; i < calcs.size(); i++) {
            String nm = nameOf(calcs.get(i).carrier);
            if (nm != null) {
                indexByName.putIfAbsent(nm, i);
            }
        }
        int n = calcs.size();
        List<List<Integer>> adj = new ArrayList<>();   // dep -> dependents
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            String self = nameOf(calcs.get(i).carrier);
            for (String ref : refs(calcs.get(i))) {
                if (ref.equals(self)) {
                    continue;
                }
                Integer dep = indexByName.get(ref);
                if (dep != null && dep != i) {
                    adj.get(dep).add(i);
                    indeg[i]++;
                }
            }
        }
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
            }
        }
        List<Script> out = new ArrayList<>();
        boolean[] placed = new boolean[n];
        while (!q.isEmpty()) {
            int u = q.poll();
            out.add(calcs.get(u));
            placed[u] = true;
            for (int v : adj.get(u)) {
                if (--indeg[v] == 0) {
                    q.add(v);
                }
            }
        }
        if (out.size() < n) {
            // cycle: append the unplaced members in document order, and report them.
            for (int i = 0; i < n; i++) {
                if (!placed[i]) {
                    out.add(calcs.get(i));
                    String nm = nameOf(calcs.get(i).carrier);
                    if (nm != null && !r.cycles.contains(nm)) {
                        r.cycles.add(nm);
                    }
                }
            }
        }
        return out;
    }

    /**
     * Referenced field names from a calculate script (for dependency ordering): {@code resolveNode("…")}
     * targets + {@code X.rawValue} for JS; additionally every bare SOM identifier for FormCalc (which
     * references siblings by bare name, e.g. {@code numTotal + numStateTax}), excluding FormCalc
     * keywords/builtins. Only names that are themselves calculate carriers create an edge, so the extra
     * FormCalc identifiers cannot spuriously connect non-calculated fields.
     */
    private static java.util.Set<String> refs(Script s) {
        String src = s.source;
        java.util.Set<String> out = new java.util.LinkedHashSet<>();
        Matcher m = RESOLVE_REF.matcher(src);
        while (m.find()) {
            String expr = m.group(1);
            String[] seg = expr.split("[.\\[]");
            String last = seg.length == 0 ? expr : seg[seg.length - 1];
            last = last.replaceAll("[^A-Za-z0-9_]", "");
            if (!last.isEmpty()) {
                out.add(last);
            }
        }
        m = FIELD_ACCESS.matcher(src);
        while (m.find()) {
            out.add(m.group(1));
        }
        if (s.formCalc) {
            Matcher fm = FC_IDENT.matcher(src);
            while (fm.find()) {
                String id = fm.group();
                if (!FC_STOP.contains(id.toLowerCase())) {
                    out.add(id);
                }
            }
        }
        return out;
    }

    /* ------------------------------ run one script ------------------------------ */

    private static void runCalculate(XfaScriptHost host, Script s, Result r) {
        r.calculates++;
        if (s.formCalc) {
            r.formCalc++;
        }
        XfaScriptNode node = host.wrap(s.carrier);
        // override="ignore": keep a value already present (data-bound / user) — don't recompute.
        XfaNode calc = s.carrier.getChild("calculate");
        String override = calc == null ? null : calc.getAttribute("override");
        String existing = host.readValue(s.carrier);
        if ("ignore".equals(override) && existing != null && !existing.isEmpty()) {
            r.calculatesOk++;
            if (s.formCalc) {
                r.formCalcOk++;
            }
            return;
        }
        try {
            Object result = runScript(host, s, node);
            if (result != null && !(result instanceof org.aspose.pdf.engine.script.js.runtime.Undefined)) {
                String v = XfaScriptNode.coerce(result);
                String before = host.readValue(s.carrier);
                node.put("rawValue", v);
                if (before == null ? !v.isEmpty() : !before.equals(v)) {
                    r.valuesProduced++;
                }
            }
            r.calculatesOk++;
            if (s.formCalc) {
                r.formCalcOk++;
            }
        } catch (RuntimeException e) {
            r.calculatesFailed++;
            if (s.formCalc) {
                r.formCalcFailed++;
            }
            r.errors.add("calculate:" + nameOf(s.carrier) + ":" + cause(e));
        }
    }

    /** Whether {@code s} is a number that parses to exactly zero ("0", "0.00", "0.0", …). */
    private static boolean isZeroNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            return Double.parseDouble(s.trim()) == 0.0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static void runValidate(XfaScriptHost host, Script s, Result r) {
        r.validates++;
        if (s.formCalc) {
            r.formCalc++;
        }
        try {
            Object result = runScript(host, s, host.wrap(s.carrier));
            if (!truthy(result)) {
                r.invalid++;
                r.invalidFields.add(somPath(host, s.carrier));
            }
        } catch (RuntimeException e) {
            if (s.formCalc) {
                r.formCalcFailed++;
            }
            r.errors.add("validate:" + nameOf(s.carrier) + ":" + cause(e));
        }
    }

    private static void runEvent(XfaScriptHost host, Script s, Result r) {
        r.events++;
        if (s.formCalc) {
            r.formCalc++;
        }
        try {
            int before = r.valuesProduced;
            runScript(host, s, host.wrap(s.carrier));
            r.eventsOk++;
            // (value changes inside events are written via this.rawValue / resolveNode assignments)
            if (r.valuesProduced == before) {
                // no-op marker; events may set other nodes' values directly
            }
        } catch (RuntimeException e) {
            r.eventsFailed++;
            if (s.formCalc) {
                r.formCalcFailed++;
            }
            r.errors.add((s.activity == null ? "event" : s.activity) + ":" + nameOf(s.carrier) + ":" + cause(e));
        }
    }

    /* ------------------------------ helpers ------------------------------ */

    private static boolean truthy(Object v) {
        if (v == null || v instanceof org.aspose.pdf.engine.script.js.runtime.Undefined
                || v instanceof org.aspose.pdf.engine.script.js.runtime.JSNull) {
            return false;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Double) {
            double d = (Double) v;
            return d != 0 && !Double.isNaN(d);
        }
        if (v instanceof String) {
            return !((String) v).isEmpty();
        }
        return true;
    }

    private static String nameOf(XfaNode n) {
        String nm = n.getName();
        return nm == null || nm.isEmpty() ? n.getElementName() : nm;
    }

    private static String somPath(XfaScriptHost host, XfaNode n) {
        return (String) host.wrap(n).get("somExpression");
    }

    private static String cause(RuntimeException e) {
        String m = e.getMessage();
        return m == null ? e.getClass().getSimpleName() : m.replace('\n', ' ');
    }
}
