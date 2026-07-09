package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>keyUsage</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class KeyUsage extends XfaNode {

    /** Wraps a backing <code>keyUsage</code> element. */
    public KeyUsage(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>crlSign</code> attribute, or null. */
    public String getCrlSign() { return getString("crlSign"); }
    /** Sets the <code>crlSign</code> attribute. */
    public void setCrlSign(String value) { setAttribute("crlSign", value); }

    /** @return the typed <code>dataEncipherment</code> attribute, or null. */
    public String getDataEncipherment() { return getString("dataEncipherment"); }
    /** Sets the <code>dataEncipherment</code> attribute. */
    public void setDataEncipherment(String value) { setAttribute("dataEncipherment", value); }

    /** @return the typed <code>decipherOnly</code> attribute, or null. */
    public String getDecipherOnly() { return getString("decipherOnly"); }
    /** Sets the <code>decipherOnly</code> attribute. */
    public void setDecipherOnly(String value) { setAttribute("decipherOnly", value); }

    /** @return the typed <code>digitalSignature</code> attribute, or null. */
    public String getDigitalSignature() { return getString("digitalSignature"); }
    /** Sets the <code>digitalSignature</code> attribute. */
    public void setDigitalSignature(String value) { setAttribute("digitalSignature", value); }

    /** @return the typed <code>encipherOnly</code> attribute, or null. */
    public String getEncipherOnly() { return getString("encipherOnly"); }
    /** Sets the <code>encipherOnly</code> attribute. */
    public void setEncipherOnly(String value) { setAttribute("encipherOnly", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>keyAgreement</code> attribute, or null. */
    public String getKeyAgreement() { return getString("keyAgreement"); }
    /** Sets the <code>keyAgreement</code> attribute. */
    public void setKeyAgreement(String value) { setAttribute("keyAgreement", value); }

    /** @return the typed <code>keyCertSign</code> attribute, or null. */
    public String getKeyCertSign() { return getString("keyCertSign"); }
    /** Sets the <code>keyCertSign</code> attribute. */
    public void setKeyCertSign(String value) { setAttribute("keyCertSign", value); }

    /** @return the typed <code>keyEncipherment</code> attribute, or null. */
    public String getKeyEncipherment() { return getString("keyEncipherment"); }
    /** Sets the <code>keyEncipherment</code> attribute. */
    public void setKeyEncipherment(String value) { setAttribute("keyEncipherment", value); }

    /** @return the typed <code>nonRepudiation</code> attribute, or null. */
    public String getNonRepudiation() { return getString("nonRepudiation"); }
    /** Sets the <code>nonRepudiation</code> attribute. */
    public void setNonRepudiation(String value) { setAttribute("nonRepudiation", value); }

    /** Allowed values of the <code>type</code> attribute. */
    public enum TypeValue {
        OPTIONAL("optional"),
        REQUIRED("required");
        private final String v;
        TypeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static TypeValue fromValue(String s) {
            for (TypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>type</code> attribute, or null. */
    public TypeValue getType() {
        String v = getAttribute("type");
        return v == null ? null : TypeValue.fromValue(v);
    }
    /** Sets the <code>type</code> attribute. */
    public void setType(TypeValue value) {
        setAttribute("type", value == null ? null : value.value());
    }
    /** @return the raw <code>type</code> string, or null. */
    public String getTypeRaw() { return getAttribute("type"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
