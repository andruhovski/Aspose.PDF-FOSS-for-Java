package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;
import org.aspose.pdf.engine.xfa.model.XfaNode;

import java.util.List;

/// The XFA node list a node exposes as `node.nodes` (B3.4). Scripts build structure dynamically
/// with `container.nodes.append(xfa.form.createNode("field","x"))` and prune it with
/// `.remove(child)`; `item(i)` / `length` iterate. Mutations go through
/// [XfaScriptHost] so the canonical Form-DOM tree, fields and named globals stay consistent and
/// the change reaches compute/render.
final class XfaNodeList extends JSObject {

    private final XfaScriptHost host;
    private final XfaNode parent;

    XfaNodeList(XfaScriptHost host, XfaNode parent, JSObject proto) {
        super(proto);
        this.host = host;
        this.parent = parent;
        setClassName("XFANodeList");
        host.realm().method(this, "append", 1, (i, t, a) -> {
            XfaNode c = childArg(a);
            if (c != null) {
                host.appendChild(parent, c);
            }
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "insert", 2, (i, t, a) -> {
            XfaNode c = childArg(a);
            Object refObj = XfaScriptHost.arg(a, 1);
            XfaNode ref = refObj instanceof XfaScriptNode ? ((XfaScriptNode) refObj).node : null;
            if (c != null) {
                host.insertChild(parent, c, ref);
            }
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "remove", 1, (i, t, a) -> {
            XfaNode c = childArg(a);
            if (c != null) {
                host.removeChild(parent, c);
            }
            return Undefined.INSTANCE;
        });
        host.realm().method(this, "item", 1, (i, t, a) -> {
            int idx = (int) i.toNumber(XfaScriptHost.arg(a, 0));
            List<XfaNode> ch = parent.getChildren();
            return idx >= 0 && idx < ch.size() ? host.wrap(ch.get(idx)) : Undefined.INSTANCE;
        });
    }

    private static XfaNode childArg(Object[] a) {
        Object v = XfaScriptHost.arg(a, 0);
        return v instanceof XfaScriptNode ? ((XfaScriptNode) v).node : null;
    }

    @Override
    public Object get(String name) {
        if ("length".equals(name)) {
            return (double) parent.getChildren().size();
        }
        return super.get(name);
    }
}
