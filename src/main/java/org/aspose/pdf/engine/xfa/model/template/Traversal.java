package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `traversal`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Traversal extends XfaNode {

    /// Wraps a backing `traversal` element.
    public Traversal(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `traverse` children (typed).
    public java.util.List<Traverse> getTraverseList() {
        java.util.List<Traverse> r = new java.util.ArrayList<Traverse>();
        for (XfaNode n : getChildren("traverse")) { r.add((Traverse) n); }
        return r;
    }
    /// Appends a new `traverse` child.
    public Traverse addTraverse() { return (Traverse) addChild("traverse"); }
}
