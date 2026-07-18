package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `overflow`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Overflow extends XfaNode {

    /// Wraps a backing `overflow` element.
    public Overflow(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `leader` attribute, or null.
    public String getLeader() { return getString("leader"); }
    /// Sets the `leader` attribute.
    public void setLeader(String value) { setAttribute("leader", value); }

    /// @return the typed `target` attribute, or null.
    public String getTarget() { return getString("target"); }
    /// Sets the `target` attribute.
    public void setTarget(String value) { setAttribute("target", value); }

    /// @return the typed `trailer` attribute, or null.
    public String getTrailer() { return getString("trailer"); }
    /// Sets the `trailer` attribute.
    public void setTrailer(String value) { setAttribute("trailer", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
