package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `setProperty`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class SetProperty extends XfaNode {

    /// Wraps a backing `setProperty` element.
    public SetProperty(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `connection` attribute, or null.
    public String getConnection() { return getString("connection"); }
    /// Sets the `connection` attribute.
    public void setConnection(String value) { setAttribute("connection", value); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

    /// @return the typed `target` attribute, or null.
    public String getTarget() { return getString("target"); }
    /// Sets the `target` attribute.
    public void setTarget(String value) { setAttribute("target", value); }
}
