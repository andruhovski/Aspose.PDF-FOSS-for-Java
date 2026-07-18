package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `mdp`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Mdp extends XfaNode {

    /// Wraps a backing `mdp` element.
    public Mdp(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `permissions` attribute.
    public enum PermissionsValue {
        V_1("1"),
        V_2("2"),
        V_3("3");
        private final String v;
        PermissionsValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PermissionsValue fromValue(String s) {
            for (PermissionsValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `permissions` attribute, or null.
    public PermissionsValue getPermissions() {
        String v = getAttribute("permissions");
        return v == null ? null : PermissionsValue.fromValue(v);
    }
    /// Sets the `permissions` attribute.
    public void setPermissions(PermissionsValue value) {
        setAttribute("permissions", value == null ? null : value.value());
    }
    /// @return the raw `permissions` string, or null.
    public String getPermissionsRaw() { return getAttribute("permissions"); }

    /// Allowed values of the `signatureType` attribute.
    public enum SignatureTypeValue {
        AUTHOR("author"),
        FILLER("filler");
        private final String v;
        SignatureTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static SignatureTypeValue fromValue(String s) {
            for (SignatureTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `signatureType` attribute, or null.
    public SignatureTypeValue getSignatureType() {
        String v = getAttribute("signatureType");
        return v == null ? null : SignatureTypeValue.fromValue(v);
    }
    /// Sets the `signatureType` attribute.
    public void setSignatureType(SignatureTypeValue value) {
        setAttribute("signatureType", value == null ? null : value.value());
    }
    /// @return the raw `signatureType` string, or null.
    public String getSignatureTypeRaw() { return getAttribute("signatureType"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
