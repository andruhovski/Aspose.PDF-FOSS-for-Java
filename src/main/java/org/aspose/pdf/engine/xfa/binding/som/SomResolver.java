package org.aspose.pdf.engine.xfa.binding.som;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Evaluates a {@link SomExpr} over the typed XFA model (template/data/form
 * trees). Independent of the JavaScript engine: SOM is a structural expression
 * language; only structural and comparison predicates are evaluated, and
 * script-bearing predicates are reported (not run).
 *
 * <p>Name matching is space-agnostic: a step name matches a node's {@code name}
 * attribute (template space) or its element local name (data space).</p>
 */
public final class SomResolver {

    /** Resolution context: the accessor roots plus the current node. */
    public static final class Context {
        public XfaNode template;
        public XfaNode data;
        public XfaNode form;
        public XfaNode record;
        public XfaNode current;
        /**
         * Script-context resolution (SOM-R, additive). When {@code true}, a leading <em>unqualified</em>
         * relative name (no {@code $} root, e.g. {@code price}) is resolved with the XFA SOM
         * relative-reference <em>scope search</em>: the current container's own children, then — if none
         * match — each enclosing container's children (the current node's siblings, then the parent's
         * siblings, &hellip;), nearest container first. This lets {@code xfa.resolveNode("price")} from a
         * calculate script on a sibling field find {@code price}. The binding/flatten/render path leaves
         * this {@code false}, so it keeps the strict child-only relative match (Stage A is unchanged).
         */
        public boolean scriptScope;

        /**
         * @param current the current (relative) node
         * @return a context whose roots default to {@code current}'s tree
         */
        public static Context of(XfaNode current) {
            Context c = new Context();
            c.current = current;
            return c;
        }
    }

    private final Set<String> classNames;
    private final SomParser parser;

    /** Creates a resolver (class-name set seeded from the template element registry). */
    public SomResolver() {
        this.classNames = buildClassNames();
        this.parser = new SomParser(classNames);
    }

    private static Set<String> buildClassNames() {
        Set<String> names = new HashSet<>();
        java.util.Map<String, XfaNodeFactory.Ctor> reg = new java.util.HashMap<>();
        org.aspose.pdf.engine.xfa.model.template.XfaTemplateElements.registerAll(reg);
        names.addAll(reg.keySet());
        names.add("dataGroup");
        names.add("dataValue");
        return names;
    }

    /**
     * Parses a SOM string.
     *
     * @param expr the expression text
     * @return the parsed expression
     */
    public SomExpr parse(String expr) {
        return parser.parse(expr);
    }

    /**
     * Resolves all matching nodes.
     *
     * @param expr SOM text
     * @param ctx  resolution context
     * @return the matching nodes (possibly empty, never {@code null})
     */
    public List<XfaNode> resolveNodes(String expr, Context ctx) {
        return resolveNodes(parser.parse(expr), ctx);
    }

    /**
     * Resolves to a single node ({@code $xfa.resolveNode} semantics).
     *
     * @param expr SOM text
     * @param ctx  resolution context
     * @return the first matching node, or {@code null}
     */
    public XfaNode resolveNode(String expr, Context ctx) {
        List<XfaNode> r = resolveNodes(expr, ctx);
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * Resolves a value: the property value when the expression ends in a property
     * accessor ({@code .#name}/{@code .#x}), otherwise the resolved node's text.
     *
     * @param expr SOM text
     * @param ctx  resolution context
     * @return the value string, or {@code null}
     */
    public String resolveValue(String expr, Context ctx) {
        SomExpr e = parser.parse(expr);
        if (e.endsWithProperty()) {
            SomExpr.PropertyStep prop = (SomExpr.PropertyStep) e.getSteps().get(e.getSteps().size() - 1);
            List<XfaNode> owners = evaluate(e, ctx, true);
            if (owners.isEmpty()) {
                return null;
            }
            return property(owners.get(0), prop.property);
        }
        XfaNode n = resolveNode(expr, ctx);
        return n == null ? null : n.getTextContent();
    }

    /** Resolves the nodes for a parsed expression. */
    public List<XfaNode> resolveNodes(SomExpr e, Context ctx) {
        return evaluate(e, ctx, false);
    }

    /**
     * Automatic-binding scope search (XFA 3.0, "Basic Data Binding to Produce the
     * XFA Form DOM", pp.180-183). Given the data node that is the nearest bound
     * ancestor's data context ({@code start}), returns the data nodes named
     * {@code name} in binding-precedence order:
     *
     * <ol>
     *   <li><b>direct / ancestor match</b> &mdash; the children of {@code start}
     *       (when intervening template subforms are unbound, {@code start} is an
     *       ancestor of the would-be direct node, so a child of {@code start} is an
     *       <em>ancestor match</em>; spec: "a scope match involving only direct
     *       ancestors&hellip; is preferable");</li>
     *   <li><b>sibling match</b> &mdash; ascending the data tree from {@code start},
     *       at each ancestor the siblings of the node just left (spec: "a scope
     *       match involving sibling(s) of ancestor(s)"), <em>nearest ancestor
     *       first</em> so that "fewer generations ascended" wins.</li>
     * </ol>
     *
     * <p>This never creates data nodes &mdash; it only locates existing ones. Type
     * compatibility (field&rarr;data value, subform&rarr;data group) and
     * single-binding (a consumed node binds at most one field) are applied by the
     * caller. Returning <em>all</em> candidates in order lets the caller skip
     * already-consumed nodes and still honour precedence and index inferral.</p>
     *
     * @param name  the container/field name to match (template name or data local name)
     * @param start the nearest bound ancestor's data node (may be {@code null})
     * @return the matching data nodes, highest binding-precedence first
     */
    public List<XfaNode> scopeMatch(String name, XfaNode start) {
        List<XfaNode> out = new ArrayList<>();
        if (start == null) {
            return out;
        }
        // Phase 1 — direct / ancestor match: children of the nearest bound data node.
        for (XfaNode c : start.getChildren()) {
            if (matchName(c, name)) {
                out.add(c);
            }
        }
        // Phase 2 — sibling match: ascend the data ancestry, nearest first; at each
        // level inspect the siblings of the node just left (its subtree was Phase 1).
        XfaNode node = start;
        XfaNode parent = node.getParent();
        while (parent != null) {
            for (XfaNode c : parent.getChildren()) {
                if (sameNode(c, node)) {
                    continue; // already covered by the deeper phase
                }
                if (matchName(c, name)) {
                    out.add(c);
                }
            }
            node = parent;
            parent = parent.getParent();
        }
        return out;
    }

    private static boolean sameNode(XfaNode a, XfaNode b) {
        return a.getElement() == b.getElement();
    }

    /* --------------------------- evaluation ------------------------- */

    private List<XfaNode> evaluate(SomExpr e, Context ctx, boolean dropTrailingProperty) {
        List<SomExpr.Step> steps = new ArrayList<>(e.getSteps());
        List<XfaNode> set = new ArrayList<>();

        SomExpr.Root root = e.getRoot();
        if (root == SomExpr.Root.XFA && !steps.isEmpty() && steps.get(0) instanceof SomExpr.NameStep) {
            String n = ((SomExpr.NameStep) steps.get(0)).name;
            XfaNode r = xfaChild(ctx, n);
            if (r != null) {
                set.add(r);
                steps.remove(0);
            }
        } else if (ctx.scriptScope && (root == SomExpr.Root.NONE || root == SomExpr.Root.CURRENT)
                && !steps.isEmpty() && steps.get(0) instanceof SomExpr.NameStep && ctx.current != null) {
            // Script-context scope search for a leading unqualified name (SOM-R, additive). Binding
            // never sets scriptScope, so its strict child-only relative match is untouched.
            List<XfaNode> scoped = scopeResolveLeading((SomExpr.NameStep) steps.get(0), ctx);
            if (!scoped.isEmpty()) {
                set = scoped;
                steps.remove(0);
            } else {
                XfaNode start = rootNode(root, ctx);
                if (start != null) {
                    set.add(start); // no scope hit: fall back to the relative start (yields empty/strict)
                }
            }
        } else {
            XfaNode start = rootNode(root, ctx);
            if (start != null) {
                set.add(start);
            }
        }

        for (SomExpr.Step step : steps) {
            if (step instanceof SomExpr.PropertyStep) {
                if (dropTrailingProperty) {
                    break; // owners are the current set
                }
                return new ArrayList<>(); // property in node context -> no nodes
            }
            set = applyStep(step, set, ctx);
            if (set.isEmpty()) {
                break;
            }
        }
        return set;
    }

    /**
     * Resolves the leading unqualified name of a relative script expression by the XFA SOM scope
     * search (script path only — see {@link Context#scriptScope}). Starting at {@code ctx.current},
     * it matches the name among that container's children; if none match it ascends to the enclosing
     * container and matches there (the current node's siblings), continuing up the tree. The
     * <em>nearest</em> container with any match wins (XFA scope precedence), and every same-named node
     * at that level is returned (so {@code resolveNodes} sees all occurrences and {@code resolveNode}
     * takes the first). Returns empty when the name is nowhere in scope (a genuinely absent node).
     *
     * @param ns  the leading name step (with its optional index)
     * @param ctx the resolution context (its {@code current} is the script's container)
     * @return the matching nodes at the nearest scope, or empty
     */
    private List<XfaNode> scopeResolveLeading(SomExpr.NameStep ns, Context ctx) {
        for (XfaNode scope = ctx.current; scope != null; scope = scope.getParent()) {
            List<XfaNode> hits = new ArrayList<>();
            for (XfaNode c : scope.getChildren()) {
                if (matchName(c, ns.name)) {
                    hits.add(c);
                }
            }
            List<XfaNode> indexed = applyIndex(hits, ns.index, ctx);
            if (!indexed.isEmpty()) {
                return indexed; // nearest container with a match wins
            }
        }
        return new ArrayList<>();
    }

    private XfaNode rootNode(SomExpr.Root root, Context ctx) {
        switch (root) {
            case TEMPLATE: return ctx.template;
            case DATA: case DATAWINDOW: return ctx.data;
            case FORM: return ctx.form;
            case RECORD: return ctx.record != null ? ctx.record : ctx.data;
            case CURRENT: case NONE: return ctx.current;
            case XFA: return ctx.template != null ? ctx.template : ctx.current;
            default: return ctx.current;
        }
    }

    private XfaNode xfaChild(Context ctx, String name) {
        switch (name) {
            case "template": return ctx.template;
            case "data": case "datasets": case "dataWindow": return ctx.data;
            case "form": return ctx.form;
            case "record": return ctx.record != null ? ctx.record : ctx.data;
            default: return null;
        }
    }

    private List<XfaNode> applyStep(SomExpr.Step step, List<XfaNode> set, Context ctx) {
        List<XfaNode> candidates = new ArrayList<>();
        if (step instanceof SomExpr.ParentStep) {
            for (XfaNode n : set) {
                if (n.getParent() != null) {
                    candidates.add(n.getParent());
                }
            }
            return candidates;
        }
        if (step instanceof SomExpr.NameStep) {
            SomExpr.NameStep ns = (SomExpr.NameStep) step;
            for (XfaNode n : set) {
                for (XfaNode c : n.getChildren()) {
                    if (matchName(c, ns.name)) {
                        candidates.add(c);
                    }
                }
            }
            return applyIndex(candidates, ns.index, ctx);
        }
        if (step instanceof SomExpr.ClassStep) {
            SomExpr.ClassStep cs = (SomExpr.ClassStep) step;
            for (XfaNode n : set) {
                for (XfaNode c : n.getChildren()) {
                    if (matchClass(c, cs.className)) {
                        candidates.add(c);
                    }
                }
            }
            return applyIndex(candidates, cs.index, ctx);
        }
        return candidates;
    }

    private List<XfaNode> applyIndex(List<XfaNode> candidates, SomExpr.Index idx, Context ctx) {
        if (idx == null || idx.kind == SomExpr.Index.Kind.NONE || idx.kind == SomExpr.Index.Kind.ALL) {
            return candidates;
        }
        if (idx.kind == SomExpr.Index.Kind.NUM) {
            List<XfaNode> r = new ArrayList<>();
            if (idx.n >= 0 && idx.n < candidates.size()) {
                r.add(candidates.get(idx.n));
            }
            return r;
        }
        // PRED
        SomExpr.Predicate p = idx.predicate;
        if (p == null || p.script) {
            return candidates; // script predicate: not evaluated (flagged elsewhere)
        }
        List<XfaNode> r = new ArrayList<>();
        for (XfaNode c : candidates) {
            if (evalPredicate(c, p, ctx)) {
                r.add(c);
            }
        }
        return r;
    }

    private boolean evalPredicate(XfaNode candidate, SomExpr.Predicate p, Context ctx) {
        Context sub = Context.of(candidate);
        sub.template = ctx.template;
        sub.data = ctx.data;
        sub.form = ctx.form;
        sub.record = ctx.record;
        String left = resolveValue(p.path, sub);
        if (p.op == SomExpr.Predicate.Op.NONE) {
            return left != null && !left.isEmpty();
        }
        if (left == null) {
            return false;
        }
        Double ln = num(left);
        Double rn = num(p.value);
        int cmp;
        if (ln != null && rn != null) {
            cmp = Double.compare(ln, rn);
        } else {
            cmp = left.compareTo(p.value == null ? "" : p.value);
        }
        switch (p.op) {
            case EQ: return cmp == 0;
            case NE: return cmp != 0;
            case LT: return cmp < 0;
            case GT: return cmp > 0;
            case LE: return cmp <= 0;
            case GE: return cmp >= 0;
            default: return false;
        }
    }

    private static Double num(String s) {
        try {
            return Double.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean matchName(XfaNode node, String name) {
        if ("*".equals(name)) {
            return true;
        }
        String nm = node.getName();
        if (nm != null && nm.equals(name)) {
            return true;
        }
        return name.equals(node.getElementName());
    }

    private boolean matchClass(XfaNode node, String cls) {
        if ("dataValue".equals(cls)) {
            return !hasElementChild(node);
        }
        if ("dataGroup".equals(cls)) {
            return hasElementChild(node);
        }
        return cls.equals(node.getElementName());
    }

    private static boolean hasElementChild(XfaNode node) {
        return !node.getChildren().isEmpty();
    }

    private static String property(XfaNode node, String prop) {
        if ("name".equals(prop)) {
            return node.getName();
        }
        if ("class".equals(prop)) {
            return node.getElementName();
        }
        return node.getAttribute(prop);
    }
}
