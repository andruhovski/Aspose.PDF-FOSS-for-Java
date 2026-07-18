package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `operation`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Operation extends XfaNode {

    /// Wraps a backing `operation` element.
    public Operation(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `input` attribute, or null.
    public String getInput() { return getString("input"); }
    /// Sets the `input` attribute.
    public void setInput(String value) { setAttribute("input", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `output` attribute, or null.
    public String getOutput() { return getString("output"); }
    /// Sets the `output` attribute.
    public void setOutput(String value) { setAttribute("output", value); }

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
