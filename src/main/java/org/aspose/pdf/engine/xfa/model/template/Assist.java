package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `assist`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Assist extends XfaNode {

    /// Wraps a backing `assist` element.
    public Assist(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `role` attribute, or null.
    public String getRole() { return getString("role"); }
    /// Sets the `role` attribute.
    public void setRole(String value) { setAttribute("role", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `speak` child (typed), or null.
    public Speak getSpeak() { return (Speak) getChild("speak"); }
    /// Ensures and returns the `speak` child.
    public Speak ensureSpeak() { return (Speak) ensureChild("speak"); }

    /// @return the `toolTip` child (typed), or null.
    public ToolTip getToolTip() { return (ToolTip) getChild("toolTip"); }
    /// Ensures and returns the `toolTip` child.
    public ToolTip ensureToolTip() { return (ToolTip) ensureChild("toolTip"); }
}
