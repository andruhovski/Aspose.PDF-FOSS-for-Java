package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `manifest`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Manifest extends XfaNode {

    /// Wraps a backing `manifest` element.
    public Manifest(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `action` attribute.
    public enum ActionValue {
        ALL("all"),
        EXCLUDE("exclude"),
        INCLUDE("include");
        private final String v;
        ActionValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ActionValue fromValue(String s) {
            for (ActionValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `action` attribute, or null.
    public ActionValue getAction() {
        String v = getAttribute("action");
        return v == null ? null : ActionValue.fromValue(v);
    }
    /// Sets the `action` attribute.
    public void setAction(ActionValue value) {
        setAttribute("action", value == null ? null : value.value());
    }
    /// @return the raw `action` string, or null.
    public String getActionRaw() { return getAttribute("action"); }

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

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `ref` children (typed).
    public java.util.List<Ref> getRefList() {
        java.util.List<Ref> r = new java.util.ArrayList<Ref>();
        for (XfaNode n : getChildren("ref")) { r.add((Ref) n); }
        return r;
    }
    /// Appends a new `ref` child.
    public Ref addRef() { return (Ref) addChild("ref"); }
}
