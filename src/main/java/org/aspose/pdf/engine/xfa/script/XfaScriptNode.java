package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;
import org.aspose.pdf.engine.xfa.model.XfaNode;

/**
 * A live JavaScript view of an XFA Form-DOM node (Stage B / B3.1 A.2). Overrides {@code [[Get]]}/
 * {@code [[Put]]} so the value accessors the corpus scripts use — {@code rawValue} / {@code value} /
 * {@code formattedValue} (read AND write), {@code presence}, {@code name}, {@code parent},
 * {@code somExpression} — read and write the backing Form DOM through {@link XfaScriptHost}. Writing
 * {@code rawValue} updates the bound value the render track paints. Any other property behaves as a
 * plain JS object property (scripts may stash state on the node).
 */
public final class XfaScriptNode extends JSObject {

    final XfaNode node;
    private final XfaScriptHost host;

    XfaScriptNode(XfaScriptHost host, XfaNode node, JSObject proto) {
        super(proto);
        this.host = host;
        this.node = node;
        setClassName("XFAObject");
        // resolveNode/resolveNodes are also available on the node itself (this.resolveNode("..")).
        host.realm().method(this, "resolveNode", 1, (i, t, a) -> {
            XfaScriptNode r = host.resolveOne(i.toStringJS(XfaScriptHost.arg(a, 0)), this);
            return r == null ? host.absent() : r;
        });
        host.realm().method(this, "resolveNodes", 1, (i, t, a) ->
                host.resolveMany(i.toStringJS(XfaScriptHost.arg(a, 0)), this));
        // B3.4 node methods (the measured next-blocker surface)
        host.realm().method(this, "isPropertySpecified", 1, (i, t, a) ->
                isPropertySpecified(i.toStringJS(XfaScriptHost.arg(a, 0))));
        host.realm().method(this, "addItem", 2, (i, t, a) -> {
            Object b = XfaScriptHost.arg(a, 1);
            addItem(i.toStringJS(XfaScriptHost.arg(a, 0)), b instanceof Undefined ? null : i.toStringJS(b));
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "clearItems", 0, (i, t, a) -> {
            clearItems();
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "getDisplayItem", 1, (i, t, a) ->
                getItem(false, (int) i.toNumber(XfaScriptHost.arg(a, 0))));
        host.realm().method(this, "getSaveItem", 1, (i, t, a) ->
                getItem(true, (int) i.toNumber(XfaScriptHost.arg(a, 0))));
        host.realm().method(this, "boundItem", 1, (i, t, a) ->
                boundItem(i.toStringJS(XfaScriptHost.arg(a, 0))));
        host.realm().method(this, "execEvent", 1, (i, t, a) ->
                execEvent(i.toStringJS(XfaScriptHost.arg(a, 0))));
    }

    @Override
    public Object get(String name) {
        switch (name) {
            case "rawValue":
            case "value":
            case "formattedValue": {
                String v = host.readValue(node);
                if (v == null || v.isEmpty()) {
                    return "";
                }
                // A numeric-typed field exposes rawValue as a JS number (so X.rawValue + Y sums,
                // not concatenates) — the canonical calculate case. Text fields stay strings.
                if (host.isNumericField(node)) {
                    try {
                        return Double.parseDouble(v.trim());
                    } catch (NumberFormatException ignore) {
                        return v;
                    }
                }
                return v;
            }
            case "name":
                return node.getName() == null ? "" : node.getName();
            case "className":
                return node.getElementName() == null ? "" : node.getElementName();
            case "somExpression":
                return somOf();
            case "presence": {
                String p = node.getAttribute("presence");
                return p == null || p.isEmpty() ? "visible" : p;
            }
            case "access": {
                // field interactivity (open/readOnly/protected/noInteract) — defaults to open.
                String ac = node.getAttribute("access");
                return ac == null || ac.isEmpty() ? "open" : ac;
            }
            case "isNull":
                // XFA absence test on a real node: true when it holds no value (the idiomatic guard).
                return host.readValue(node) == null || host.readValue(node).isEmpty();
            case "parent": {
                XfaNode p = node.getParent();
                return p == null ? Undefined.INSTANCE : host.wrap(p);
            }
            case "index":
                // XFA occurrence index of this object among its same-named siblings (the standard
                // property the corpus uses, e.g. an appendix row's `this.parent.index + 1` numbering).
                // Without it `node.index` fell through to child-by-name → Undefined, so `index + 1`
                // evaluated to NaN and a calculate overwrote the data-bound "1"/"2"/"3" with "".
            case "instanceIndex":
                return (double) instanceIndex();
            case "selectedIndex":
                return (double) selectedIndex();
            case "instanceManager": {
                // node.instanceManager.addInstance(...) — bridge to the _<name> manager (B3.3 IM closure)
                XfaInstanceManager m = host.instanceManagerFor(node);
                return m == null ? Undefined.INSTANCE : m;
            }
            case "count":
                return (double) sameNameSiblingCount();
            case "nodes":
                return new XfaNodeList(host, node, host.realm().objectPrototype);
            case "toolTip":
                return toolTipNode();
            default: {
                // an own / prototype property (a stashed value, resolveNode, …) wins
                if (super.hasProperty(name)) {
                    return super.get(name);
                }
                // node._<child> — the instanceManager of a variable-occurrence child container, anchored
                // at THIS node (e.g. this.parent._po.addInstance()). Resolved before child-by-name so an
                // <occur min="0"> variant with zero current instances is still reachable to addInstance().
                if (name.length() > 1 && name.charAt(0) == '_') {
                    XfaInstanceManager im = host.instanceManagerForChild(node, name.substring(1));
                    if (im != null) {
                        return im;
                    }
                }
                // child-by-name navigation: the dotted SOM path a.b.c the corpus relies on
                // (personnelFedPage.narrativeSubform1.narrativeSubform2…) over the canonical tree.
                // A miss returns Undefined, NOT the absent node. Making a real node's missing child an
                // absent node would let a loop that ASSUMES the child is reachable run unbounded: a
                // throw aborts the script (caught), but an absent node makes `while (cond) { if
                // (node.absentField.rawValue == X) break; }` spin forever (absent.rawValue is always "",
                // so the break never fires). Measured: 6 corpus forms hung. The absent node (B3.5.1) is
                // therefore applied only at the SOM-resolution boundary (resolveNode/resolveNodes/item),
                // where its result is read once, not looped.
                XfaNode child = childByName(name);
                return child == null ? Undefined.INSTANCE : host.wrap(child);
            }
        }
    }

    @Override
    public void put(String name, Object value) {
        switch (name) {
            case "rawValue":
            case "value":
            case "formattedValue":
                host.writeValue(node, coerce(value));
                return;
            case "presence":
                node.setAttribute("presence", coerce(value));
                return;
            case "access":
                node.setAttribute("access", coerce(value));
                return;
            default:
                super.put(name, value);
        }
    }

    /** Resolves a child container/field by name (then by class/element name) — the SOM dot step {@code a.b}. */
    private XfaNode childByName(String name) {
        XfaNode byName = null;
        for (XfaNode c : node.getChildren()) {
            if (name.equals(c.getName())) {
                return c; // exact name match wins (first instance for repeated subforms)
            }
            if (byName == null && name.equals(c.getElementName())) {
                byName = c; // fall back to a child of that class (e.g. assist, caption, ui, value)
            }
        }
        return byName;
    }

    /** Count of same-named siblings (the XFA instance/occur count); 1 for a normal single node. */
    private int sameNameSiblingCount() {
        XfaNode parent = node.getParent();
        String nm = node.getName();
        String ln = node.getElementName();
        if (parent == null || nm == null) {
            return 1;
        }
        int n = 0;
        for (XfaNode sib : parent.getChildren()) {
            if (ln.equals(sib.getElementName()) && nm.equals(sib.getName())) {
                n++;
            }
        }
        return Math.max(1, n);
    }

    /**
     * The {@code assist/toolTip} node (Acrobat exposes {@code field.toolTip} as a shortcut for
     * {@code field.assist.toolTip}). The corpus writes {@code field.toolTip.value = "…"}; returns the
     * existing toolTip node if present, else a benign object so the write does not throw (dynamic
     * tooltip text is not painted — a tracked render gap, not an error).
     */
    private Object toolTipNode() {
        XfaNode assist = childByName("assist");
        if (assist != null) {
            for (XfaNode c : assist.getChildren()) {
                if ("toolTip".equals(c.getName()) || "toolTip".equals(c.getElementName())) {
                    return host.wrap(c);
                }
            }
        }
        return host.realm().newObject();
    }

    /* ------------------------------ B3.4 node methods ------------------------------ */

    /** XFA {@code isPropertySpecified}: was {@code prop} explicitly set (an attribute or a child property). */
    private boolean isPropertySpecified(String prop) {
        org.w3c.dom.Element el = node.getElement();
        if (el == null || prop == null || prop.isEmpty()) {
            return false;
        }
        if (el.hasAttribute(prop)) {
            return true;
        }
        for (XfaNode c : node.getChildren()) {
            if (prop.equals(c.getElementName()) || prop.equals(c.getName())) {
                return true;
            }
        }
        return false;
    }

    /** The field's {@code <items>} element — display (first) or bound ({@code save="1"}/second); created on demand. */
    private org.w3c.dom.Element itemsElement(boolean bound, boolean create) {
        org.w3c.dom.Element el = node.getElement();
        if (el == null) {
            return null;
        }
        java.util.List<org.w3c.dom.Element> items = new java.util.ArrayList<>();
        for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && "items".equals(local((org.w3c.dom.Element) n))) {
                items.add((org.w3c.dom.Element) n);
            }
        }
        if (!bound) {
            if (!items.isEmpty()) {
                return items.get(0);
            }
            return create ? newItems(el, false) : null;
        }
        for (org.w3c.dom.Element it : items) {
            if ("1".equals(it.getAttribute("save"))) {
                return it;
            }
        }
        if (items.size() >= 2) {
            return items.get(1);
        }
        return create ? newItems(el, true) : null;
    }

    private org.w3c.dom.Element newItems(org.w3c.dom.Element field, boolean bound) {
        org.w3c.dom.Element it = field.getOwnerDocument().createElementNS(XfaNode.TEMPLATE_NS, "items");
        if (bound) {
            it.setAttribute("save", "1");
            it.setAttribute("presence", "hidden");
        }
        field.appendChild(it);
        return it;
    }

    /** XFA {@code addItem(display[, bound])}: appends an option (display + optional bound value) to a list. */
    private void addItem(String display, String bound) {
        appendText(itemsElement(false, true), display == null ? "" : display);
        if (bound != null) {
            appendText(itemsElement(true, true), bound);
        }
    }

    private void appendText(org.w3c.dom.Element items, String value) {
        if (items == null) {
            return;
        }
        org.w3c.dom.Element text = items.getOwnerDocument().createElementNS(XfaNode.TEMPLATE_NS, "text");
        text.setTextContent(value);
        items.appendChild(text);
    }

    /** XFA {@code clearItems()}: removes every option from the display and bound item lists. */
    private void clearItems() {
        for (boolean bound : new boolean[]{false, true}) {
            org.w3c.dom.Element items = itemsElement(bound, false);
            if (items == null) {
                continue;
            }
            for (org.w3c.dom.Node c = items.getFirstChild(); c != null; ) {
                org.w3c.dom.Node next = c.getNextSibling();
                if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    items.removeChild(c);
                }
                c = next;
            }
        }
    }

    /** The option text at {@code index} in the display (or bound) list, or {@code ""}. */
    private String getItem(boolean bound, int index) {
        java.util.List<String> list = itemTexts(bound);
        return index >= 0 && index < list.size() ? list.get(index) : "";
    }

    /** XFA {@code boundItem(display)}: the bound value paired with a display value (else the display value). */
    private String boundItem(String display) {
        java.util.List<String> disp = itemTexts(false);
        java.util.List<String> save = itemTexts(true);
        int idx = disp.indexOf(display);
        if (idx >= 0 && idx < save.size()) {
            return save.get(idx);
        }
        return display;
    }

    private java.util.List<String> itemTexts(boolean bound) {
        java.util.List<String> out = new java.util.ArrayList<>();
        org.w3c.dom.Element items = itemsElement(bound, false);
        if (items == null) {
            return out;
        }
        for (org.w3c.dom.Node n = items.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                out.add(n.getTextContent() == null ? "" : n.getTextContent());
            }
        }
        return out;
    }

    /** The 0-based index of the field's current value among its bound (then display) items, or -1. */
    private int selectedIndex() {
        String v = host.readValue(node);
        if (v == null) {
            return -1;
        }
        int i = itemTexts(true).indexOf(v);
        if (i >= 0) {
            return i;
        }
        return itemTexts(false).indexOf(v);
    }

    /**
     * XFA {@code execEvent(name)}: runs the node's named event handler (or calculate/validate) script
     * synchronously and returns whether it ran. An inactive node, a missing handler or a thrown handler
     * fails cleanly (returns {@code false}) — this is the script-driven synchronous call, not the
     * interactive event loop.
     */
    private Object execEvent(String eventName) {
        if ("inactive".equals(node.getAttribute("presence")) || eventName == null || eventName.isEmpty()) {
            return Boolean.FALSE;
        }
        XfaNode holder = null;
        if ("calculate".equals(eventName) || "validate".equals(eventName)) {
            holder = node.getChild(eventName);
        } else {
            for (XfaNode c : node.getChildren()) {
                if ("event".equals(c.getElementName()) && eventName.equals(c.getAttribute("activity"))) {
                    holder = c;
                    break;
                }
            }
        }
        XfaNode script = holder == null ? null : holder.getChild("script");
        String src = script == null ? null : script.getTextContent();
        if (src == null || src.trim().isEmpty() || host.execEventDepth >= XfaScriptHost.MAX_EXEC_DEPTH) {
            return Boolean.FALSE;
        }
        host.execEventDepth++;
        try {
            host.run(src, this);
            return Boolean.TRUE;
        } catch (RuntimeException e) {
            return Boolean.FALSE;
        } finally {
            host.execEventDepth--;
        }
    }

    private static String local(org.w3c.dom.Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
    }

    /** This instance's 0-based index among its same-named subform siblings (0 if not an instance). */
    private int instanceIndex() {
        XfaNode parent = node.getParent();
        if (parent == null) {
            return 0;
        }
        String nm = node.getName();
        String ln = node.getElementName();
        int idx = 0;
        for (XfaNode sib : parent.getChildren()) {
            if (sib.getElement() == node.getElement()) {
                return idx;
            }
            if (ln.equals(sib.getElementName()) && nm != null && nm.equals(sib.getName())) {
                idx++;
            }
        }
        return idx;
    }

    private String somOf() {
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

    /** Coerces a JS value to the string an XFA {@code rawValue} stores (integral doubles lose the {@code .0}). */
    static String coerce(Object v) {
        if (v == null || v instanceof Undefined) {
            return "";
        }
        if (v instanceof Double) {
            double d = (Double) v;
            if (Double.isNaN(d)) {
                return "";
            }
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return Long.toString((long) d);
            }
            return Double.toString(d);
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? "true" : "false";
        }
        return String.valueOf(v);
    }
}
