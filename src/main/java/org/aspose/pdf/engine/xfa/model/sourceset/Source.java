package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>source</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Source extends XfaNode {

    /** Wraps a backing <code>source</code> element. */
    public Source(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>connect</code> child (typed), or null. */
    public Connect getConnect() { return (Connect) getChild("connect"); }
    /** Ensures and returns the <code>connect</code> child. */
    public Connect ensureConnect() { return (Connect) ensureChild("connect"); }

    /** @return the <code>bind</code> children (typed). */
    public java.util.List<Bind> getBindList() {
        java.util.List<Bind> r = new java.util.ArrayList<Bind>();
        for (XfaNode n : getChildren("bind")) { r.add((Bind) n); }
        return r;
    }
    /** Appends a new <code>bind</code> child. */
    public Bind addBind() { return (Bind) addChild("bind"); }

    /** @return the <code>command</code> children (typed). */
    public java.util.List<Command> getCommandList() {
        java.util.List<Command> r = new java.util.ArrayList<Command>();
        for (XfaNode n : getChildren("command")) { r.add((Command) n); }
        return r;
    }
    /** Appends a new <code>command</code> child. */
    public Command addCommand() { return (Command) addChild("command"); }
}
