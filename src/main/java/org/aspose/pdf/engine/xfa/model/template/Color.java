package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `color`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Color extends XfaNode {

    /// Wraps a backing `color` element.
    public Color(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `cSpace` attribute, or null.
    public String getCSpace() { return getString("cSpace"); }
    /// Sets the `cSpace` attribute.
    public void setCSpace(String value) { setAttribute("cSpace", value); }

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

    /// @return the typed `value` attribute, or null.
    public String getValue() { return getString("value"); }
    /// Sets the `value` attribute.
    public void setValue(String value) { setAttribute("value", value); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
