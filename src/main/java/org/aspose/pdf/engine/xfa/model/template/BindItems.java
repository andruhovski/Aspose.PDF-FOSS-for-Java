package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `bindItems`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class BindItems extends XfaNode {

    /// Wraps a backing `bindItems` element.
    public BindItems(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `connection` attribute, or null.
    public String getConnection() { return getString("connection"); }
    /// Sets the `connection` attribute.
    public void setConnection(String value) { setAttribute("connection", value); }

    /// @return the typed `labelRef` attribute, or null.
    public String getLabelRef() { return getString("labelRef"); }
    /// Sets the `labelRef` attribute.
    public void setLabelRef(String value) { setAttribute("labelRef", value); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

    /// @return the typed `valueRef` attribute, or null.
    public String getValueRef() { return getString("valueRef"); }
    /// Sets the `valueRef` attribute.
    public void setValueRef(String value) { setAttribute("valueRef", value); }
}
