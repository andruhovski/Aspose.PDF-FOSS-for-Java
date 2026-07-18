package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `margin`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Margin extends XfaNode {

    /// Wraps a backing `margin` element.
    public Margin(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `bottomInset` attribute, or null.
    public XfaMeasurement getBottomInset() { return getMeasurement("bottomInset"); }
    /// Sets the `bottomInset` attribute.
    public void setBottomInset(XfaMeasurement value) { setAttribute("bottomInset", value == null ? null : value.format()); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `leftInset` attribute, or null.
    public XfaMeasurement getLeftInset() { return getMeasurement("leftInset"); }
    /// Sets the `leftInset` attribute.
    public void setLeftInset(XfaMeasurement value) { setAttribute("leftInset", value == null ? null : value.format()); }

    /// @return the typed `rightInset` attribute, or null.
    public XfaMeasurement getRightInset() { return getMeasurement("rightInset"); }
    /// Sets the `rightInset` attribute.
    public void setRightInset(XfaMeasurement value) { setAttribute("rightInset", value == null ? null : value.format()); }

    /// @return the typed `topInset` attribute, or null.
    public XfaMeasurement getTopInset() { return getMeasurement("topInset"); }
    /// Sets the `topInset` attribute.
    public void setTopInset(XfaMeasurement value) { setAttribute("topInset", value == null ? null : value.format()); }

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
