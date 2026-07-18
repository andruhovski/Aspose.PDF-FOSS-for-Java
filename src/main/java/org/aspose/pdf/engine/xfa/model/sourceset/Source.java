package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `source`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Source extends XfaNode {

    /// Wraps a backing `source` element.
    public Source(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /// @return the `connect` child (typed), or null.
    public Connect getConnect() { return (Connect) getChild("connect"); }
    /// Ensures and returns the `connect` child.
    public Connect ensureConnect() { return (Connect) ensureChild("connect"); }

    /// @return the `bind` children (typed).
    public java.util.List<Bind> getBindList() {
        java.util.List<Bind> r = new java.util.ArrayList<Bind>();
        for (XfaNode n : getChildren("bind")) { r.add((Bind) n); }
        return r;
    }
    /// Appends a new `bind` child.
    public Bind addBind() { return (Bind) addChild("bind"); }

    /// @return the `command` children (typed).
    public java.util.List<Command> getCommandList() {
        java.util.List<Command> r = new java.util.ArrayList<Command>();
        for (XfaNode n : getChildren("command")) { r.add((Command) n); }
        return r;
    }
    /// Appends a new `command` child.
    public Command addCommand() { return (Command) addChild("command"); }
}
