package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `map`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Map extends XfaNode {

    /// Wraps a backing `map` element.
    public Map(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `bind` attribute, or null.
    public String getBind() { return getString("bind"); }
    /// Sets the `bind` attribute.
    public void setBind(String value) { setAttribute("bind", value); }

    /// @return the typed `from` attribute, or null.
    public String getFrom() { return getString("from"); }
    /// Sets the `from` attribute.
    public void setFrom(String value) { setAttribute("from", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

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
}
