package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `decimal`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Decimal extends XfaNode {

    /// Wraps a backing `decimal` element.
    public Decimal(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `fracDigits` attribute, or null.
    public java.lang.Integer getFracDigits() { return getInteger("fracDigits"); }
    /// Sets the `fracDigits` attribute.
    public void setFracDigits(java.lang.Integer value) { setAttribute("fracDigits", value == null ? null : value.toString()); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `leadDigits` attribute, or null.
    public java.lang.Integer getLeadDigits() { return getInteger("leadDigits"); }
    /// Sets the `leadDigits` attribute.
    public void setLeadDigits(java.lang.Integer value) { setAttribute("leadDigits", value == null ? null : value.toString()); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return this element's text content.
    public String getValue() { return getTextContent(); }
    /// Sets this element's text content.
    public void setValue(String value) { setTextContent(value); }
}
