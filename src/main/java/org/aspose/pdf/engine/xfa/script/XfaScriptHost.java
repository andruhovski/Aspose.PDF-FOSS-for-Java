package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.Engine;
import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.builtins.Realm;
import org.aspose.pdf.engine.script.js.parser.Parser;
import org.aspose.pdf.engine.script.js.runtime.JSArray;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.binding.som.SomResolver;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;

import java.util.*;

/// The XFA scripting host (Stage B / B3.1 PART A): owns one JS-0 [Engine], injects the XFA host
/// objects the corpus scripts use (`xfa` + `host`/`app`/`util`/`console`/
/// `event`) onto the global object, and bridges `xfa.resolveNode`/`resolveNodes` to
/// the Stage-A [SomResolver] over the merged Form DOM. A script runs with `this` bound to
/// its current node; node value reads/writes go through [FormField] so a calculate/initialize
/// result flows into the render track.
public final class XfaScriptHost {

    private final Engine engine;
    private final Realm realm;
    private final FormDom dom;
    private final SomResolver som = new SomResolver();
    private final Map<Element, FormField> fieldByElement = new IdentityHashMap<>();
    /// Canonical (parent-linked) tree node per element — [FormField#getFormNode] is detached.
    private final Map<Element, XfaNode> treeByElement = new IdentityHashMap<>();
    private final Map<Element, XfaScriptNode> nodeCache = new IdentityHashMap<>();
    private final Template template;
    /// Variable-occurrence container name → {min, max(-1=unbounded)} from the template `<occur>`.
    private final Map<String, int[]> occurByName = new HashMap<>();
    /// Variable-occurrence container name → a template subform element (clone source for a fresh instance).
    private final Map<String, Element> templateByName = new HashMap<>();
    /// instanceManager per (parent element, child container name) — node-scoped so `node._child`
    /// resolves to the manager anchored at THIS node (not an ambiguous global, since a name like
    /// `po` recurs under several parents). Covers both present-instance and zero-instance slots.
    private final Map<Element, Map<String, XfaInstanceManager>> imByParent = new IdentityHashMap<>();
    private JSObject globalRef;

    /// Recorded host side effects (app.alert / console.println / host.messageBox) for diagnostics.
    private final List<String> messages = new ArrayList<>();
    /// Form-level script-object libraries loaded / that failed to evaluate (JS-0 parser/host gaps).
    private int libsLoaded;
    private int libsFailed;
    /// The node whose script is currently executing (the SOM resolution origin for `xfa.resolveNode`).
    private XfaScriptNode currentNode;
    /// Shared absent-node (B3.5.1) returned when a SOM/child step resolves to nothing — lazily built.
    private XfaAbsentNode absentNode;
    /// Re-entrancy depth of `node.execEvent(...)` — bounds a handler that triggers another handler.
    int execEventDepth;
    /// Max synchronous `execEvent` nesting (a handler-runs-handler guard, not the interactive loop).
    static final int MAX_EXEC_DEPTH = 16;

    /// Builds a host over a merged Form DOM (no template → no instanceManager).
    ///
    /// @param dom the merged Form DOM (the script's form tree + field values)
    public XfaScriptHost(FormDom dom) {
        this(dom, null);
    }

    /// Builds a host over a merged Form DOM with the template (for instanceManager occur limits).
    ///
    /// @param dom the merged Form DOM
    /// @param tpl the template (variable-occurrence limits + clone prototypes), or `null`
    public XfaScriptHost(FormDom dom, Template tpl) {
        this.dom = dom;
        this.template = tpl;
        this.engine = new Engine();
        this.realm = engine.getRealm();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null && f.getFormNode().getElement() != null) {
                fieldByElement.put(f.getFormNode().getElement(), f);
            }
        }
        if (dom.getRoot() != null) {
            indexTree(dom.getRoot());
        }
        if (tpl != null) {
            indexOccur(tpl.getElement());
        }
        installHostObjects();
        this.globalRef = engine.getGlobalObject();
        injectNamedGlobals();
        injectInstanceManagers();
        loadScriptObjects();
    }

    /// Records each template container's `<occur>` limits + a clone prototype, by name.
    private void indexOccur(Element el) {
        String ln = local(el);
        if ("subform".equals(ln) || "subformSet".equals(ln)) {
            Element occur = firstChild(el, "occur");
            if (occur != null) {
                int min = parseOccur(occur.getAttribute("min"), 1);
                int max = parseOccur(occur.getAttribute("max"), 1);
                String nm = el.getAttribute("name");
                if (nm != null && !nm.isEmpty() && (max == -1 || max > min)) {
                    occurByName.putIfAbsent(nm, new int[]{min, max});
                    templateByName.putIfAbsent(nm, el);
                }
            }
        }
        for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                indexOccur((Element) n);
            }
        }
    }

    /// Exposes `_<name>` for each variable-occurrence container as its instanceManager. The
    /// manager is bound to the container's parent (where the same-named instance siblings live) and
    /// the template occur limits. A form-DOM parent may hold several distinct variable-occurrence
    /// names; each gets its own `_name`.
    private void injectInstanceManagers() {
        if (template == null || occurByName.isEmpty() || dom.getRoot() == null) {
            return;
        }
        // group form-DOM instance containers by (parent element, name)
        injectManagersUnder(dom.getRoot());
        // Zero-instance variable-occurrence slots (an <occur min="0"> subform whose data was absent):
        // no form-DOM instance exists to walk to, so anchor the manager at the recorded parent element
        // (BindingEngine.addZeroOccurSlot) — a script can then addInstance() it (clone from prototype).
        for (Element[] slot : dom.getZeroOccurSlots()) {
            Element parentEl = slot[0];
            Element tplEl = slot[1];
            String nm = tplEl.getAttribute("name");
            if (nm == null || nm.isEmpty() || !occurByName.containsKey(nm)) {
                continue;
            }
            if (imFor(parentEl, nm) != null) {
                continue; // a present instance under the same parent already registered it
            }
            int[] mm = occurByName.get(nm);
            Element proto = templateByName.getOrDefault(nm, tplEl);
            XfaInstanceManager mgr = new XfaInstanceManager(this, XfaNodeFactory.wrap(parentEl, null), nm,
                    importProto(proto), mm[0], mm[1], realm.objectPrototype);
            register(parentEl, nm, mgr);
        }
    }

    private void injectManagersUnder(XfaNode node) {
        for (XfaNode child : node.getChildren()) {
            String ln = child.getElementName();
            String nm = child.getName();
            if (("subform".equals(ln) || "subformSet".equals(ln)) && nm != null && occurByName.containsKey(nm)
                    && imFor(node.getElement(), nm) == null) {
                int[] mm = occurByName.get(nm);
                Element proto = templateByName.get(nm);
                XfaInstanceManager mgr = new XfaInstanceManager(this, node, nm,
                        importProto(proto), mm[0], mm[1], realm.objectPrototype);
                register(node.getElement(), nm, mgr);
            }
            injectManagersUnder(child);
        }
    }

    /// Registers an instanceManager in both the parent-scoped registry and the legacy `_name` global.
    private void register(Element parentEl, String name, XfaInstanceManager mgr) {
        imByParent.computeIfAbsent(parentEl, k -> new HashMap<>()).put(name, mgr);
        globalRef.defineHidden("_" + name, mgr); // legacy global (node.instanceManager bridge)
    }

    /// @return the instanceManager registered for (`parentEl`, `name`), or null.
    private XfaInstanceManager imFor(Element parentEl, String name) {
        Map<String, XfaInstanceManager> m = parentEl == null ? null : imByParent.get(parentEl);
        return m == null ? null : m.get(name);
    }

    /// Resolves the instanceManager for a variable-occurrence child container of `parent` named
    /// `name` — backing the `node._<name>` accessor (e.g. `this.parent._po`). Anchored
    /// at the specific parent, so a name shared by several parents resolves to the right manager.
    ///
    /// @return the child's instanceManager, or `null` if `parent` has no such occur child
    XfaInstanceManager instanceManagerForChild(XfaNode parent, String name) {
        return parent == null ? null : imFor(parent.getElement(), name);
    }

    /// Imports a template subform prototype into the form document so a fresh instance can be cloned.
    private Element importProto(Element templateSubform) {
        if (templateSubform == null || dom.getDocument() == null) {
            return null;
        }
        try {
            return (Element) dom.getDocument().importNode(templateSubform, true);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static int parseOccur(String v, int dflt) {
        if (v == null || v.isEmpty()) {
            return dflt;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    /// A form-level script-object library: its JS source plus the `<script name>` (may be empty).
    private static final class Lib {
        final String name;
        final String source;
        Lib(String name, String source) {
            this.name = name;
            this.source = source;
        }
    }

    /// Pre-loads the form's script-object function libraries: XFA forms define reusable helpers in
    /// `<variables><script name="N">` blocks that per-field calculate/event scripts call as
    /// `N.helper(...)` (qualified, the dominant corpus form) or `helper(...)` (bare). Each
    /// JS block is evaluated once in the shared engine global so its `function` declarations are
    /// in scope (bare calls), and the functions it defines are additionally bound to an object
    /// `N` so the qualified calls resolve too (B3.3 — the measured dominant unlock). Because
    /// function declarations are hoisted before any top-level statement runs, a library that throws at
    /// load (e.g. a top-level navigation) still exposes its hoisted helpers. A failed block is tracked
    /// (not fatal).
    private void loadScriptObjects() {
        if (dom.getRoot() == null) {
            return;
        }
        List<Lib> libs = new ArrayList<>();
        collectVariableScripts(dom.getRoot(), libs);
        JSObject global = engine.getGlobalObject();
        for (Lib lib : libs) {
            java.util.Set<String> before = new java.util.HashSet<>(global.ownKeys());
            try {
                engine.evaluate(lib.source);
                libsLoaded++;
            } catch (RuntimeException ignore) {
                libsFailed++; // a malformed/host-dependent library block — hoisted helpers still exposed below
            }
            if (lib.name != null && !lib.name.isEmpty()) {
                exposeScriptObject(global, lib.name, before);
            }
        }
    }

    /// Binds the functions a just-loaded library added to the global scope onto an object named after
    /// the script object (`<script name="N">`), so a qualified call `N.helper()` resolves
    /// (the bare globals the eval already created keep `helper()` working). The object shadows any
    /// node-wrapper global of the same name (a `<script name="N">` node is otherwise injected as a
    /// value-less node wrapper, which is exactly why `N.helper` returned undefined before B3.3).
    ///
    /// @param global the engine global object
    /// @param name   the script-object name `N`
    /// @param before the global's own keys captured immediately before this library was evaluated
    private void exposeScriptObject(JSObject global, String name, java.util.Set<String> before) {
        Object existing = global.hasOwnProperty(name) ? global.get(name) : null;
        JSObject obj;
        if (existing instanceof JSObject && !(existing instanceof XfaScriptNode)
                && !(existing instanceof XfaInstanceManager)
                && !(existing instanceof org.aspose.pdf.engine.script.js.runtime.JSFunction)) {
            obj = (JSObject) existing; // reuse a prior same-named script object (split libraries merge)
        } else {
            obj = realm.newObject();
        }
        for (String k : global.ownKeys()) {
            if (before.contains(k)) {
                continue; // only the keys this library introduced
            }
            Object v = global.get(k);
            if (v instanceof org.aspose.pdf.engine.script.js.runtime.JSFunction) {
                obj.defineHidden(k, v);
            }
        }
        global.defineHidden(name, obj);
    }

    /// @return form-level script-object libraries successfully evaluated.
    public int getLibsLoaded() {
        return libsLoaded;
    }

    /// @return script-object libraries that failed to evaluate (JS-0 parser / host gaps).
    public int getLibsFailed() {
        return libsFailed;
    }

    private void collectVariableScripts(XfaNode node, List<Lib> out) {
        if ("variables".equals(node.getElementName())) {
            for (XfaNode c : node.getChildren()) {
                if ("script".equals(c.getElementName())) {
                    String ct = c.getAttribute("contentType");
                    boolean js = ct == null || ct.isEmpty() || ct.toLowerCase().contains("javascript");
                    String src = c.getTextContent();
                    if (js && src != null && !src.trim().isEmpty()) {
                        out.add(new Lib(c.getName(), src));
                    }
                }
            }
        }
        for (XfaNode c : node.getChildren()) {
            collectVariableScripts(c, out);
        }
    }

    /// Builds the canonical parent-linked node per element (walking the root tree, whose children carry parents).
    private void indexTree(XfaNode node) {
        treeByElement.putIfAbsent(node.getElement(), node);
        for (XfaNode c : node.getChildren()) {
            indexTree(c);
        }
    }

    /* ------------------------------ instance mutation ------------------------------ */

    /// @return the canonical parent-linked node for an element (registering it + descendants if new).
    XfaNode canonical(Element e) {
        XfaNode n = treeByElement.get(e);
        if (n != null) {
            return n;
        }
        Element parentEl = e.getParentNode() instanceof Element ? (Element) e.getParentNode() : null;
        XfaNode parentNode = parentEl != null ? treeByElement.get(parentEl) : null;
        XfaNode node = XfaNodeFactory.wrap(e, parentNode);
        indexTree(node);
        return node;
    }

    /// Called after a new instance subform is inserted: register its node tree + fields + named globals.
    void onInstanceAdded(Element instance) {
        XfaNode node = canonical(instance);
        registerFieldsUnder(node);
        injectNamedGlobals();
    }

    /// Called before an instance is removed: drop its fields + cached wrappers + tree entries.
    void onInstanceRemoved(Element instance) {
        List<Element> sub = new ArrayList<>();
        collectElements(instance, sub);
        for (Element e : sub) {
            FormField f = fieldByElement.remove(e);
            if (f != null) {
                dom.removeField(f);
            }
            treeByElement.remove(e);
            nodeCache.remove(e);
        }
    }

    /// Registers a [FormField] for every leaf `<field>`/`<exclGroup>` under a new instance.
    private void registerFieldsUnder(XfaNode node) {
        String ln = node.getElementName();
        if ("field".equals(ln) || "exclGroup".equals(ln)) {
            if (!fieldByElement.containsKey(node.getElement())) {
                FormField f = dom.registerField(node.getName(), somPath(node), readValue(node),
                        items(node), uiType(node), node);
                fieldByElement.put(node.getElement(), f);
            }
            return;
        }
        for (XfaNode c : node.getChildren()) {
            registerFieldsUnder(c);
        }
    }

    private String somPath(XfaNode node) {
        StringBuilder sb = new StringBuilder();
        for (XfaNode n = node; n != null && n.getParent() != null; n = n.getParent()) {
            String nm = n.getName();
            if (nm == null || nm.isEmpty()) {
                nm = n.getElementName();
            }
            sb.insert(0, "." + nm);
        }
        return sb.length() == 0 ? "" : sb.substring(1);
    }

    private static List<String> items(XfaNode field) {
        List<String> out = new ArrayList<>();
        XfaNode items = field.getChild("items");
        if (items != null) {
            for (XfaNode c : items.getChildren()) {
                out.add(c.getTextContent());
            }
        }
        return out;
    }

    private static String uiType(XfaNode field) {
        XfaNode ui = field.getChild("ui");
        if (ui != null) {
            for (XfaNode c : ui.getChildren()) {
                return c.getElementName();
            }
        }
        return null;
    }

    private static void collectElements(Element el, List<Element> out) {
        out.add(el);
        for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                collectElements((Element) n, out);
            }
        }
    }

    private static String local(org.w3c.dom.Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    private static Element firstChild(Element el, String localName) {
        for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                String ln = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
                if (localName.equals(ln)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }

    /// Injects each named node as a global so an unqualified SOM name in a script (`Qty.rawValue`)
    /// resolves to its node — XFA scripts run in a scope where sibling names are visible. Builtins are
    /// never shadowed (a field named `Date` stays reachable via `resolveNode`); duplicate
    /// names resolve last-wins (a flat-scope approximation — nested same-name fields are a tracked gap).
    private void injectNamedGlobals() {
        JSObject global = engine.getGlobalObject();
        for (Map.Entry<Element, XfaNode> e : treeByElement.entrySet()) {
            String nm = e.getValue().getName();
            if (nm == null || nm.isEmpty() || global.hasOwnProperty(nm)) {
                continue;
            }
            global.defineHidden(nm, wrap(e.getValue()));
        }
    }

    /* ------------------------------ execution ------------------------------ */

    /// Runs a script source with `this` bound to `current`.
    ///
    /// @param source  the JavaScript source
    /// @param current the current node (the script's `this` and SOM origin)
    /// @return the completion value (the calculate result), or `null` on a script error
    /// @throws XfaScriptError if the script throws or fails to parse
    public Object run(String source, XfaScriptNode current) {
        XfaScriptNode prev = currentNode;
        currentNode = current;
        try {
            Node.Program prog = Parser.parse(source);
            return engine.getInterpreter().runWithThis(prog, current);
        } catch (XfaScriptError e) {
            throw e;
        } catch (org.aspose.pdf.engine.script.js.interp.JSException e) {
            throw new XfaScriptError(describeThrown(e.value), e);
        } catch (RuntimeException e) {
            throw new XfaScriptError(e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + e.getMessage()), e);
        } finally {
            currentNode = prev;
        }
    }

    /// Lazily-built FormCalc evaluator (B2) — shares this host's SOM resolver + value bridge.
    private FormCalcEngine formCalc;

    /// Runs a FormCalc script with `current` as the `$` reference + SOM origin (B2). A
    /// sibling to [#run] for the JS path; the carrier's value flows back through the same
    /// [FormField] bridge so a FormCalc calculate reaches the render track.
    ///
    /// @param source  the FormCalc source
    /// @param current the carrier node
    /// @return the computed value (Double/String/null)
    /// @throws FormCalcError on a lex/parse/eval failure
    public Object runFormCalc(String source, XfaScriptNode current) {
        if (formCalc == null) {
            formCalc = new FormCalcEngine(this);
        }
        XfaScriptNode prev = currentNode;
        currentNode = current;
        try {
            return formCalc.run(source, current);
        } finally {
            currentNode = prev;
        }
    }

    /// @return the FormCalc builtins encountered but not yet implemented (a tracked B2 gap).
    public java.util.Set<String> getFormCalcUnimplemented() {
        return formCalc == null ? java.util.Collections.emptySet() : formCalc.unimplemented();
    }

    /// Builds a readable "Name: message" from a thrown JS Error value (else its string form).
    private static String describeThrown(Object thrown) {
        if (thrown instanceof JSObject) {
            JSObject o = (JSObject) thrown;
            Object name = o.get("name");
            Object msg = o.get("message");
            String n = name instanceof String ? (String) name : "Error";
            String m = msg instanceof String ? (String) msg : "";
            return m.isEmpty() ? n : n + ": " + m;
        }
        return String.valueOf(thrown);
    }

    /* ------------------------------ node wrappers ------------------------------ */

    /// @return the shared absent node (B3.5.1) — the null-object returned when a SOM expression or a
    /// dotted child step finds nothing, so the next property read is a benign empty, not a throw.
    XfaAbsentNode absent() {
        if (absentNode == null) {
            absentNode = new XfaAbsentNode(this, realm.objectPrototype);
        }
        return absentNode;
    }

    /// @return the script node wrapping `node` (cached).
    public XfaScriptNode wrap(XfaNode node) {
        if (node == null) {
            return null;
        }
        Element e = node.getElement();
        XfaScriptNode w = nodeCache.get(e);
        if (w == null) {
            // use the canonical parent-linked node (FormField.getFormNode() is detached, parent=null)
            XfaNode canonical = treeByElement.getOrDefault(e, node);
            w = new XfaScriptNode(this, canonical, realm.objectPrototype);
            nodeCache.put(e, w);
        }
        return w;
    }

    /// @return the form-DOM root as a script node.
    public XfaScriptNode formRoot() {
        return dom.getRoot() == null ? null : wrap(dom.getRoot());
    }

    /* ------------------------------ value bridge ------------------------------ */

    /// Reads a node's value (the field's bound/computed value), or `null` if none.
    String readValue(XfaNode node) {
        FormField f = fieldByElement.get(node.getElement());
        if (f != null) {
            return f.getValue();
        }
        XfaNode value = node.getChild("value");
        if (value == null) {
            return null;
        }
        for (XfaNode c : value.getChildren()) {
            return c.getTextContent();
        }
        return null;
    }

    /// Writes a node's value through the [FormField] (so the render track sees it).
    void writeValue(XfaNode node, String v) {
        FormField f = fieldByElement.get(node.getElement());
        if (f != null) {
            f.setValue(v);
        }
        // else: a non-field node (container) — XFA scripts rarely write these; left as a tracked gap.
    }

    /// @return the FormField for a node, or `null`.
    FormField fieldFor(XfaNode node) {
        return fieldByElement.get(node.getElement());
    }

    /// The XFA datasets (data) packet namespace — the class of nodes `xfa.datasets.createNode` builds.
    static final String DATASETS_NS = "http://www.xfa.org/schema/xfa-data/1.0/";

    /// Creates a new, detached node of the given class (B3.4 — `xfa.form.createNode("field","x")`
    /// / `xfa.datasets.createNode("dataValue","x")`). The node is backed by a fresh element in the
    /// Form-DOM document so it can be attached via [#appendChild] / [#insertChild] and then
    /// resolved, computed and painted.
    ///
    /// @param className the XFA class (element local name): `subform`/`field`/`dataValue`/…
    /// @param name      the node name (may be empty)
    /// @param dataNs`true` for a datasets (data) node, `false` for a template/form node
    /// @return the detached node, or `null` if it cannot be created
    XfaScriptNode createNode(String className, String name, boolean dataNs) {
        if (className == null || className.isEmpty() || dom.getDocument() == null) {
            return null;
        }
        String ns = dataNs ? DATASETS_NS : XfaNode.TEMPLATE_NS;
        Element e = dom.getDocument().createElementNS(ns, className);
        if (name != null && !name.isEmpty()) {
            e.setAttribute("name", name);
        }
        return wrap(XfaNodeFactory.wrap(e, null));
    }

    /// Appends `child` under `parent` (the `node.nodes.append(createNode(...))` pattern).
    void appendChild(XfaNode parent, XfaNode child) {
        insertChild(parent, child, null);
    }

    /// Inserts `child` under `parent` before `ref` (or appends when `ref` is null).
    void insertChild(XfaNode parent, XfaNode child, XfaNode ref) {
        if (parent == null || child == null || parent.getElement() == null || child.getElement() == null) {
            return;
        }
        Element pe = parent.getElement();
        Element ce = child.getElement();
        if (ce.getOwnerDocument() != pe.getOwnerDocument()) {
            ce = (Element) pe.getOwnerDocument().importNode(ce, true);
        }
        org.w3c.dom.Node before = ref != null && ref.getElement() != null
                && ref.getElement().getParentNode() == pe ? ref.getElement() : null;
        if (before != null) {
            pe.insertBefore(ce, before);
        } else {
            pe.appendChild(ce);
        }
        // re-canonicalize with the real parent (a created node was wrapped detached, parent=null)
        treeByElement.remove(ce);
        nodeCache.remove(ce);
        onInstanceAdded(ce);
    }

    /// Detaches `child` from `parent` (the `node.nodes.remove(child)` pattern).
    void removeChild(XfaNode parent, XfaNode child) {
        if (child == null || child.getElement() == null || child.getElement().getParentNode() == null) {
            return;
        }
        onInstanceRemoved(child.getElement());
        child.getElement().getParentNode().removeChild(child.getElement());
    }

    /// Resolves the instanceManager exposed for a node's variable-occurrence name (the `_<name>`
    /// global from [#injectInstanceManagers()]), backing the `node.instanceManager`
    /// accessor scripts use (`subform.instanceManager.addInstance(...)`) — B3.3 IM closure.
    ///
    /// @param node the (instance) subform node
    /// @return its instanceManager, or `null` if the container is not variable-occurrence
    XfaInstanceManager instanceManagerFor(XfaNode node) {
        if (node == null || node.getName() == null || node.getName().isEmpty()) {
            return null;
        }
        Object m = engine.getGlobalObject().get("_" + node.getName());
        return m instanceof XfaInstanceManager ? (XfaInstanceManager) m : null;
    }

    /// Whether a field is numeric-typed (numericEdit UI, or a decimal/float/integer value) — its rawValue is a number.
    boolean isNumericField(XfaNode node) {
        XfaNode ui = node.getChild("ui");
        if (ui != null) {
            for (XfaNode c : ui.getChildren()) {
                String n = c.getElementName();
                if ("numericEdit".equals(n)) {
                    return true;
                }
                if ("textEdit".equals(n) || "dateTimeEdit".equals(n) || "choiceList".equals(n)
                        || "checkButton".equals(n) || "barcode".equals(n) || "imageEdit".equals(n)) {
                    return false;
                }
            }
        }
        XfaNode value = node.getChild("value");
        if (value != null) {
            for (XfaNode c : value.getChildren()) {
                String n = c.getElementName();
                if ("decimal".equals(n) || "float".equals(n) || "integer".equals(n)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ------------------------------ SOM bridge ------------------------------ */

    /// Resolves a single node for a SOM expression, relative to `from` (the current node).
    ///
    /// @return the wrapped node, or `null` if nothing matches
    XfaScriptNode resolveOne(String expr, XfaScriptNode from) {
        XfaNode n = som.resolveNode(expr, contextFor(from));
        return n == null ? null : wrap(n);
    }

    /// Resolves all nodes for a SOM expression as a plain list of script nodes (the FormCalc node-set).
    List<XfaScriptNode> resolveList(String expr, XfaScriptNode from) {
        List<XfaNode> ns = som.resolveNodes(expr, contextFor(from));
        List<XfaScriptNode> out = new ArrayList<>(ns.size());
        for (XfaNode n : ns) {
            out.add(wrap(n));
        }
        return out;
    }

    /// Resolves all nodes for a SOM expression as a JS array of script nodes.
    JSArray resolveMany(String expr, XfaScriptNode from) {
        List<XfaNode> ns = som.resolveNodes(expr, contextFor(from));
        JSArray a = realm.newArray();
        int i = 0;
        for (XfaNode n : ns) {
            a.put(Integer.toString(i++), wrap(n));
        }
        a.setLength(ns.size());
        // XFA list result exposes item(i) (the corpus uses rows.item(i) alongside rows[i])
        realm.method(a, "item", 1, (in, t, ar) -> {
            int idx = (int) in.toNumber(arg(ar, 0));
            return idx >= 0 && idx < a.length() ? a.get(Integer.toString(idx)) : absent();
        });
        return a;
    }

    private SomResolver.Context contextFor(XfaScriptNode from) {
        SomResolver.Context c = new SomResolver.Context();
        c.current = from != null ? from.node : (dom.getRoot());
        c.form = dom.getRoot();
        // SOM-R: the script path resolves a leading unqualified name by the XFA scope search
        // (self → ancestors), so resolveNode("price") from a calculate script on a sibling field finds
        // it. Additive — the binding/flatten/render resolver leaves scriptScope false (Stage A neutral).
        c.scriptScope = true;
        return c;
    }

    /* ------------------------------ host objects ------------------------------ */

    Realm realm() {
        return realm;
    }

    /// @return the recorded host side effects (alerts / messages).
    public List<String> getMessages() {
        return messages;
    }

    private void installHostObjects() {
        JSObject global = engine.getGlobalObject();

        JSObject util = realm.newObject();
        XfaUtil.install(realm, util);
        global.defineHidden("util", util);

        JSObject host = realm.newObject();
        realm.method(host, "messageBox", 4, (i, t, a) -> {
            messages.add("messageBox:" + i.toStringJS(arg(a, 0)));
            return 0.0;
        });
        host.defineHidden("name", "Aspose.PDF.FOSS");
        host.defineHidden("version", "1.0");
        realm.method(host, "setFocus", 1, (i, t, a) -> Undefined.INSTANCE);
        realm.method(host, "resetData", 0, (i, t, a) -> Undefined.INSTANCE);
        global.defineHidden("host", host);

        JSObject app = realm.newObject();
        realm.method(app, "alert", 4, (i, t, a) -> {
            messages.add("alert:" + i.toStringJS(arg(a, 0)));
            return 0.0;
        });
        realm.method(app, "beep", 1, (i, t, a) -> Undefined.INSTANCE);
        app.defineHidden("viewerVersion", 11.0);
        global.defineHidden("app", app);

        JSObject console = realm.newObject();
        realm.method(console, "println", 1, (i, t, a) -> {
            messages.add("console:" + i.toStringJS(arg(a, 0)));
            return Undefined.INSTANCE;
        });
        realm.method(console, "show", 0, (i, t, a) -> Undefined.INSTANCE);
        realm.method(console, "clear", 0, (i, t, a) -> Undefined.INSTANCE);
        global.defineHidden("console", console);

        JSObject event = realm.newObject();
        event.defineHidden("name", "");
        event.defineHidden("change", "");
        global.defineHidden("event", event);

        JSObject xfa = realm.newObject();
        realm.method(xfa, "resolveNode", 1, (i, t, a) -> {
            XfaScriptNode n = resolveOne(i.toStringJS(arg(a, 0)), currentNode);
            return n == null ? absent() : n;
        });
        realm.method(xfa, "resolveNodes", 1, (i, t, a) -> resolveMany(i.toStringJS(arg(a, 0)), currentNode));
        // xfa.form / xfa.host / xfa.event / xfa.layout (stub) / xfa.datasets (stub)
        xfa.defineHidden("host", host);
        xfa.defineHidden("event", event);
        JSObject form = realm.newObject();
        realm.method(form, "resolveNode", 1, (i, t, a) -> {
            XfaScriptNode n = resolveOne(i.toStringJS(arg(a, 0)), formRoot());
            return n == null ? absent() : n;
        });
        realm.method(form, "resolveNodes", 1, (i, t, a) -> resolveMany(i.toStringJS(arg(a, 0)), formRoot()));
        realm.method(form, "recalculate", 0, (i, t, a) -> Undefined.INSTANCE);
        // xfa.form.createNode(className, name[, ns]) — a template/form-class node
        realm.method(form, "createNode", 3, (i, t, a) -> {
            XfaScriptNode n = createNode(i.toStringJS(arg(a, 0)), nameArg(i, a), false);
            return n == null ? Undefined.INSTANCE : n;
        });
        xfa.defineHidden("form", form);
        JSObject layout = realm.newObject();
        realm.method(layout, "pageCount", 0, (i, t, a) -> 1.0);
        realm.method(layout, "page", 1, (i, t, a) -> 1.0);
        realm.method(layout, "absPage", 1, (i, t, a) -> 1.0);
        // layout geometry queries (h = laid-out height of a node) — stubbed numeric (no live layout here)
        realm.method(layout, "h", 2, (i, t, a) -> 0.0);
        realm.method(layout, "w", 2, (i, t, a) -> 0.0);
        realm.method(layout, "x", 2, (i, t, a) -> 0.0);
        realm.method(layout, "y", 2, (i, t, a) -> 0.0);
        realm.method(layout, "pageContent", 3, (i, t, a) -> Undefined.INSTANCE);
        xfa.defineHidden("layout", layout);
        JSObject datasets = realm.newObject();
        // xfa.datasets.createNode(className, name[, ns]) — a data-class node
        realm.method(datasets, "createNode", 3, (i, t, a) -> {
            XfaScriptNode n = createNode(i.toStringJS(arg(a, 0)), nameArg(i, a), true);
            return n == null ? Undefined.INSTANCE : n;
        });
        realm.method(datasets, "resolveNode", 1, (i, t, a) -> {
            XfaScriptNode n = resolveOne(i.toStringJS(arg(a, 0)), formRoot());
            return n == null ? absent() : n;
        });
        xfa.defineHidden("datasets", datasets);
        global.defineHidden("xfa", xfa);
    }

    static Object arg(Object[] a, int i) {
        return i < a.length ? a[i] : Undefined.INSTANCE;
    }

    /// The optional name argument (index 1) as a string, or `""` when absent/undefined.
    private static String nameArg(org.aspose.pdf.engine.script.js.interp.Interpreter i, Object[] a) {
        Object v = arg(a, 1);
        return v instanceof Undefined ? "" : i.toStringJS(v);
    }
}
