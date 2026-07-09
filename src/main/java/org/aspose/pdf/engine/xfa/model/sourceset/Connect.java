package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>connect</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Connect extends XfaNode {

    /** Wraps a backing <code>connect</code> element. */
    public Connect(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>delayedOpen</code> attribute, or null. */
    public String getDelayedOpen() { return getString("delayedOpen"); }
    /** Sets the <code>delayedOpen</code> attribute. */
    public void setDelayedOpen(String value) { setAttribute("delayedOpen", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>timeout</code> attribute, or null. */
    public java.lang.Integer getTimeout() { return getInteger("timeout"); }
    /** Sets the <code>timeout</code> attribute. */
    public void setTimeout(java.lang.Integer value) { setAttribute("timeout", value == null ? null : value.toString()); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>connectString</code> child (typed), or null. */
    public ConnectString getConnectString() { return (ConnectString) getChild("connectString"); }
    /** Ensures and returns the <code>connectString</code> child. */
    public ConnectString ensureConnectString() { return (ConnectString) ensureChild("connectString"); }

    /** @return the <code>password</code> child (typed), or null. */
    public Password getPassword() { return (Password) getChild("password"); }
    /** Ensures and returns the <code>password</code> child. */
    public Password ensurePassword() { return (Password) ensureChild("password"); }

    /** @return the <code>user</code> child (typed), or null. */
    public User getUser() { return (User) getChild("user"); }
    /** Ensures and returns the <code>user</code> child. */
    public User ensureUser() { return (User) ensureChild("user"); }

    /** @return the <code>extras</code> children (typed). */
    public java.util.List<Extras> getExtrasList() {
        java.util.List<Extras> r = new java.util.ArrayList<Extras>();
        for (XfaNode n : getChildren("extras")) { r.add((Extras) n); }
        return r;
    }
    /** Appends a new <code>extras</code> child. */
    public Extras addExtras() { return (Extras) addChild("extras"); }
}
