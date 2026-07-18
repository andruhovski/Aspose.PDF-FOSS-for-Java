package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/// The XFA **instanceManager** host object (Stage B / B3.2) for a variable-occurrence container —
/// accessed in script as `_<name>` (e.g. `_Detail.addInstance()`). Binding does the
/// static occur-expansion (the `min` occurrences); this object performs the **runtime**
/// add/remove/set against the same Form-DOM instance list, honouring the `<occur min max>`
/// limits. Each instance is a same-named subform sibling under a shared parent; a new instance is a
/// deep clone of the template (or an existing instance), its field values cleared unless merged.
public final class XfaInstanceManager extends JSObject {

    private final XfaScriptHost host;
    private final XfaNode parent;        // the container holding the instance siblings
    private final String name;           // the instance subform name
    private final Element prototype;     // a template instance element to clone for a fresh instance
    private final int min;
    private final int max;               // -1 = unbounded

    XfaInstanceManager(XfaScriptHost host, XfaNode parent, String name, Element prototype,
                       int min, int max, JSObject proto) {
        super(proto);
        this.host = host;
        this.parent = parent;
        this.name = name;
        this.prototype = prototype;
        this.min = Math.max(0, min);
        this.max = max;
        setClassName("XFAObject");
        install();
    }

    private void install() {
        host.realm().method(this, "addInstance", 1, (i, t, a) -> {
            boolean merge = a.length > 0 && org.aspose.pdf.engine.script.js.runtime.Types.toBoolean(a[0]);
            XfaScriptNode n = addInstance(merge);
            return n == null ? Undefined.INSTANCE : n;
        });
        host.realm().method(this, "removeInstance", 1, (i, t, a) -> {
            removeInstance((int) i.toNumber(XfaScriptHost.arg(a, 0)));
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "setInstances", 1, (i, t, a) -> {
            setInstances((int) i.toNumber(XfaScriptHost.arg(a, 0)));
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "insertInstance", 2, (i, t, a) -> {
            boolean merge = a.length > 1 && org.aspose.pdf.engine.script.js.runtime.Types.toBoolean(a[1]);
            XfaScriptNode n = insertInstance((int) i.toNumber(XfaScriptHost.arg(a, 0)), merge);
            return n == null ? Undefined.INSTANCE : n;
        });
        host.realm().method(this, "moveInstance", 2, (i, t, a) -> {
            moveInstance((int) i.toNumber(XfaScriptHost.arg(a, 0)), (int) i.toNumber(XfaScriptHost.arg(a, 1)));
            return Undefined.INSTANCE;
        });
    }

    @Override
    public Object get(String n) {
        if ("count".equals(n)) {
            return (double) count();
        }
        if ("min".equals(n)) {
            return (double) min;
        }
        if ("max".equals(n)) {
            return (double) max;
        }
        return super.get(n);
    }

    @Override
    public void put(String n, Object value) {
        if ("count".equals(n)) {
            setInstances((int) toNum(value));
            return;
        }
        super.put(n, value);
    }

    /* ------------------------------ mutators ------------------------------ */

    /// @return the current instance count.
    public int count() {
        return instanceElements().size();
    }

    /// Adds one instance (clone of the template / last instance), honouring `max`.
    public XfaScriptNode addInstance(boolean merge) {
        int c = count();
        if (max != -1 && c >= max) {
            return null;
        }
        return insertAt(c, merge);
    }

    /// Inserts an instance at `index`, honouring `max`.
    public XfaScriptNode insertInstance(int index, boolean merge) {
        int c = count();
        if (max != -1 && c >= max) {
            return null;
        }
        if (index < 0) {
            index = 0;
        }
        if (index > c) {
            index = c;
        }
        return insertAt(index, merge);
    }

    private XfaScriptNode insertAt(int index, boolean merge) {
        List<Element> existing = instanceElements();
        Element source = !existing.isEmpty()
                ? existing.get(Math.min(index, existing.size() - 1)) : prototype;
        if (source == null) {
            return null;
        }
        Element clone = (Element) parent.getElement().getOwnerDocument().importNode(source, true);
        if (!merge) {
            clearValues(clone);
        }
        Node before = index < existing.size() ? existing.get(index) : refAfterLast(existing);
        if (before != null) {
            parent.getElement().insertBefore(clone, before);
        } else {
            parent.getElement().appendChild(clone);
        }
        host.onInstanceAdded(clone);
        return host.wrap(host.canonical(clone));
    }

    /// The node to insert before when appending after the last instance (keeps trailing siblings put).
    private Node refAfterLast(List<Element> existing) {
        if (existing.isEmpty()) {
            return null;
        }
        return existing.get(existing.size() - 1).getNextSibling();
    }

    /// Removes the instance at `index`, honouring `min`.
    public void removeInstance(int index) {
        List<Element> existing = instanceElements();
        if (existing.size() <= min || index < 0 || index >= existing.size()) {
            return;
        }
        Element e = existing.get(index);
        host.onInstanceRemoved(e);
        e.getParentNode().removeChild(e);
    }

    /// Grows/shrinks to exactly `n`, clamped to `[min, max]`.
    public void setInstances(int n) {
        if (n < min) {
            n = min;
        }
        if (max != -1 && n > max) {
            n = max;
        }
        int guard = 0;
        while (count() < n && guard++ < 100000) {
            if (addInstance(false) == null) {
                break;
            }
        }
        while (count() > n && guard++ < 100000) {
            removeInstance(count() - 1);
        }
    }

    /// Moves the instance from `from` to `to`.
    public void moveInstance(int from, int to) {
        List<Element> existing = instanceElements();
        if (from < 0 || from >= existing.size() || to < 0 || to >= existing.size() || from == to) {
            return;
        }
        Element moving = existing.get(from);
        parent.getElement().removeChild(moving);
        List<Element> after = instanceElements();
        Node ref = to < after.size() ? after.get(to) : refAfterLast(after);
        if (ref != null) {
            parent.getElement().insertBefore(moving, ref);
        } else {
            parent.getElement().appendChild(moving);
        }
    }

    /* ------------------------------ helpers ------------------------------ */

    private List<Element> instanceElements() {
        List<Element> out = new ArrayList<>();
        for (Node n = parent.getElement().getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String ln = e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
                if (("subform".equals(ln) || "subformSet".equals(ln)) && name.equals(e.getAttribute("name"))) {
                    out.add(e);
                }
            }
        }
        return out;
    }

    /// Clears the field DATA values inside a freshly-added instance (XFA addInstance without merge).
    /// A `<caption>` or `<draw>` subtree is skipped — its `<value>` is static label
    /// content (e.g. "Právní řád založení", or the "2D kód dlužníka" headings of the barcode page added
    /// by a load script), not data, and must survive the clear (else the cloned section renders blank).
    private static void clearValues(Element el) {
        String ln = el.getLocalName() != null ? el.getLocalName() : el.getNodeName();
        if ("caption".equals(ln) || "draw".equals(ln)) {
            return; // static label, not data — keep it
        }
        if ("value".equals(ln)) {
            for (Node c = el.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c.getNodeType() == Node.ELEMENT_NODE) {
                    c.setTextContent("");
                }
            }
            return;
        }
        for (Node c = el.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                clearValues((Element) c);
            }
        }
    }

    private static double toNum(Object v) {
        if (v instanceof Double) {
            return (Double) v;
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
