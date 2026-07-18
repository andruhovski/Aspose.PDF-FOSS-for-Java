package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `connect`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Connect extends XfaNode {

    /// Wraps a backing `connect` element.
    public Connect(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `connection` attribute, or null.
    public String getConnection() { return getString("connection"); }
    /// Sets the `connection` attribute.
    public void setConnection(String value) { setAttribute("connection", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

    /// Allowed values of the `usage` attribute.
    public enum UsageValue {
        EXPORTANDIMPORT("exportAndImport"),
        EXPORTONLY("exportOnly"),
        IMPORTONLY("importOnly");
        private final String v;
        UsageValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static UsageValue fromValue(String s) {
            for (UsageValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `usage` attribute, or null.
    public UsageValue getUsage() {
        String v = getAttribute("usage");
        return v == null ? null : UsageValue.fromValue(v);
    }
    /// Sets the `usage` attribute.
    public void setUsage(UsageValue value) {
        setAttribute("usage", value == null ? null : value.value());
    }
    /// @return the raw `usage` string, or null.
    public String getUsageRaw() { return getAttribute("usage"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `picture` child (typed), or null.
    public Picture getPicture() { return (Picture) getChild("picture"); }
    /// Ensures and returns the `picture` child.
    public Picture ensurePicture() { return (Picture) ensureChild("picture"); }
}
