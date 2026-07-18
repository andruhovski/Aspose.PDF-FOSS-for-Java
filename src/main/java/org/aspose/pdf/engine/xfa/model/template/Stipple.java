package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `stipple`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Stipple extends XfaNode {

    /// Wraps a backing `stipple` element.
    public Stipple(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `rate` attribute, or null.
    public java.lang.Integer getRate() { return getInteger("rate"); }
    /// Sets the `rate` attribute.
    public void setRate(java.lang.Integer value) { setAttribute("rate", value == null ? null : value.toString()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `color` child (typed), or null.
    public Color getColor() { return (Color) getChild("color"); }
    /// Ensures and returns the `color` child.
    public Color ensureColor() { return (Color) ensureChild("color"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
