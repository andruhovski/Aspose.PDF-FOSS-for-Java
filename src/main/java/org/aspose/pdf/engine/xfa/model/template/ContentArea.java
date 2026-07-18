package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `contentArea`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class ContentArea extends XfaNode {

    /// Wraps a backing `contentArea` element.
    public ContentArea(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `h` attribute, or null.
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /// Sets the `h` attribute.
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `relevant` attribute, or null.
    public String getRelevant() { return getString("relevant"); }
    /// Sets the `relevant` attribute.
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the typed `w` attribute, or null.
    public XfaMeasurement getW() { return getMeasurement("w"); }
    /// Sets the `w` attribute.
    public void setW(XfaMeasurement value) { setAttribute("w", value == null ? null : value.format()); }

    /// @return the typed `x` attribute, or null.
    public XfaMeasurement getX() { return getMeasurement("x"); }
    /// Sets the `x` attribute.
    public void setX(XfaMeasurement value) { setAttribute("x", value == null ? null : value.format()); }

    /// @return the typed `y` attribute, or null.
    public XfaMeasurement getY() { return getMeasurement("y"); }
    /// Sets the `y` attribute.
    public void setY(XfaMeasurement value) { setAttribute("y", value == null ? null : value.format()); }

    /// @return the `desc` child (typed), or null.
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /// Ensures and returns the `desc` child.
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
