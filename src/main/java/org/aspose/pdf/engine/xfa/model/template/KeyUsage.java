package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `keyUsage`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class KeyUsage extends XfaNode {

    /// Wraps a backing `keyUsage` element.
    public KeyUsage(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `crlSign` attribute, or null.
    public String getCrlSign() { return getString("crlSign"); }
    /// Sets the `crlSign` attribute.
    public void setCrlSign(String value) { setAttribute("crlSign", value); }

    /// @return the typed `dataEncipherment` attribute, or null.
    public String getDataEncipherment() { return getString("dataEncipherment"); }
    /// Sets the `dataEncipherment` attribute.
    public void setDataEncipherment(String value) { setAttribute("dataEncipherment", value); }

    /// @return the typed `decipherOnly` attribute, or null.
    public String getDecipherOnly() { return getString("decipherOnly"); }
    /// Sets the `decipherOnly` attribute.
    public void setDecipherOnly(String value) { setAttribute("decipherOnly", value); }

    /// @return the typed `digitalSignature` attribute, or null.
    public String getDigitalSignature() { return getString("digitalSignature"); }
    /// Sets the `digitalSignature` attribute.
    public void setDigitalSignature(String value) { setAttribute("digitalSignature", value); }

    /// @return the typed `encipherOnly` attribute, or null.
    public String getEncipherOnly() { return getString("encipherOnly"); }
    /// Sets the `encipherOnly` attribute.
    public void setEncipherOnly(String value) { setAttribute("encipherOnly", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `keyAgreement` attribute, or null.
    public String getKeyAgreement() { return getString("keyAgreement"); }
    /// Sets the `keyAgreement` attribute.
    public void setKeyAgreement(String value) { setAttribute("keyAgreement", value); }

    /// @return the typed `keyCertSign` attribute, or null.
    public String getKeyCertSign() { return getString("keyCertSign"); }
    /// Sets the `keyCertSign` attribute.
    public void setKeyCertSign(String value) { setAttribute("keyCertSign", value); }

    /// @return the typed `keyEncipherment` attribute, or null.
    public String getKeyEncipherment() { return getString("keyEncipherment"); }
    /// Sets the `keyEncipherment` attribute.
    public void setKeyEncipherment(String value) { setAttribute("keyEncipherment", value); }

    /// @return the typed `nonRepudiation` attribute, or null.
    public String getNonRepudiation() { return getString("nonRepudiation"); }
    /// Sets the `nonRepudiation` attribute.
    public void setNonRepudiation(String value) { setAttribute("nonRepudiation", value); }

    /// Allowed values of the `type` attribute.
    public enum TypeValue {
        OPTIONAL("optional"),
        REQUIRED("required");
        private final String v;
        TypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TypeValue fromValue(String s) {
            for (TypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `type` attribute, or null.
    public TypeValue getType() {
        String v = getAttribute("type");
        return v == null ? null : TypeValue.fromValue(v);
    }
    /// Sets the `type` attribute.
    public void setType(TypeValue value) {
        setAttribute("type", value == null ? null : value.value());
    }
    /// @return the raw `type` string, or null.
    public String getTypeRaw() { return getAttribute("type"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
