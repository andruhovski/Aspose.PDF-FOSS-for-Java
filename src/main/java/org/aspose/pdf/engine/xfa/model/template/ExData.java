package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>exData</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class ExData extends XfaNode {

    /** Wraps a backing <code>exData</code> element. */
    public ExData(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>contentType</code> attribute, or null. */
    public String getContentType() { return getString("contentType"); }
    /** Sets the <code>contentType</code> attribute. */
    public void setContentType(String value) { setAttribute("contentType", value); }

    /** @return the typed <code>href</code> attribute, or null. */
    public String getHref() { return getString("href"); }
    /** Sets the <code>href</code> attribute. */
    public void setHref(String value) { setAttribute("href", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>maxLength</code> attribute, or null. */
    public java.lang.Integer getMaxLength() { return getInteger("maxLength"); }
    /** Sets the <code>maxLength</code> attribute. */
    public void setMaxLength(java.lang.Integer value) { setAttribute("maxLength", value == null ? null : value.toString()); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>rid</code> attribute, or null. */
    public String getRid() { return getString("rid"); }
    /** Sets the <code>rid</code> attribute. */
    public void setRid(String value) { setAttribute("rid", value); }

    /** Allowed values of the <code>transferEncoding</code> attribute. */
    public enum TransferEncodingValue {
        BASE64("base64"),
        NONE("none"),
        PACKAGE("package");
        private final String v;
        TransferEncodingValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static TransferEncodingValue fromValue(String s) {
            for (TransferEncodingValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>transferEncoding</code> attribute, or null. */
    public TransferEncodingValue getTransferEncoding() {
        String v = getAttribute("transferEncoding");
        return v == null ? null : TransferEncodingValue.fromValue(v);
    }
    /** Sets the <code>transferEncoding</code> attribute. */
    public void setTransferEncoding(TransferEncodingValue value) {
        setAttribute("transferEncoding", value == null ? null : value.value());
    }
    /** @return the raw <code>transferEncoding</code> string, or null. */
    public String getTransferEncodingRaw() { return getAttribute("transferEncoding"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return this element's text content. */
    public String getValue() { return getTextContent(); }
    /** Sets this element's text content. */
    public void setValue(String value) { setTextContent(value); }
}
