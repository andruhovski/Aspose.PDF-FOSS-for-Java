package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `occur`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Occur extends XfaNode {

    /// Wraps a backing `occur` element.
    public Occur(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `initial` attribute, or null.
    public java.lang.Integer getInitial() { return getInteger("initial"); }
    /// Sets the `initial` attribute.
    public void setInitial(java.lang.Integer value) { setAttribute("initial", value == null ? null : value.toString()); }

    /// @return the typed `max` attribute, or null.
    public java.lang.Integer getMax() { return getInteger("max"); }
    /// Sets the `max` attribute.
    public void setMax(java.lang.Integer value) { setAttribute("max", value == null ? null : value.toString()); }

    /// @return the typed `min` attribute, or null.
    public java.lang.Integer getMin() { return getInteger("min"); }
    /// Sets the `min` attribute.
    public void setMin(java.lang.Integer value) { setAttribute("min", value == null ? null : value.toString()); }

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
}
