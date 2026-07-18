package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `connect`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Connect extends XfaNode {

    /// Wraps a backing `connect` element.
    public Connect(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `delayedOpen` attribute, or null.
    public String getDelayedOpen() { return getString("delayedOpen"); }
    /// Sets the `delayedOpen` attribute.
    public void setDelayedOpen(String value) { setAttribute("delayedOpen", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `timeout` attribute, or null.
    public java.lang.Integer getTimeout() { return getInteger("timeout"); }
    /// Sets the `timeout` attribute.
    public void setTimeout(java.lang.Integer value) { setAttribute("timeout", value == null ? null : value.toString()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `connectString` child (typed), or null.
    public ConnectString getConnectString() { return (ConnectString) getChild("connectString"); }
    /// Ensures and returns the `connectString` child.
    public ConnectString ensureConnectString() { return (ConnectString) ensureChild("connectString"); }

    /// @return the `password` child (typed), or null.
    public Password getPassword() { return (Password) getChild("password"); }
    /// Ensures and returns the `password` child.
    public Password ensurePassword() { return (Password) ensureChild("password"); }

    /// @return the `user` child (typed), or null.
    public User getUser() { return (User) getChild("user"); }
    /// Ensures and returns the `user` child.
    public User ensureUser() { return (User) ensureChild("user"); }

    /// @return the `extras` children (typed).
    public java.util.List<Extras> getExtrasList() {
        java.util.List<Extras> r = new java.util.ArrayList<Extras>();
        for (XfaNode n : getChildren("extras")) { r.add((Extras) n); }
        return r;
    }
    /// Appends a new `extras` child.
    public Extras addExtras() { return (Extras) addChild("extras"); }
}
