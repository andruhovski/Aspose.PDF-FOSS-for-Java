package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `exData`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class ExData extends XfaNode {

    /// Wraps a backing `exData` element.
    public ExData(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `contentType` attribute, or null.
    public String getContentType() { return getString("contentType"); }
    /// Sets the `contentType` attribute.
    public void setContentType(String value) { setAttribute("contentType", value); }

    /// @return the typed `href` attribute, or null.
    public String getHref() { return getString("href"); }
    /// Sets the `href` attribute.
    public void setHref(String value) { setAttribute("href", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `maxLength` attribute, or null.
    public java.lang.Integer getMaxLength() { return getInteger("maxLength"); }
    /// Sets the `maxLength` attribute.
    public void setMaxLength(java.lang.Integer value) { setAttribute("maxLength", value == null ? null : value.toString()); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `rid` attribute, or null.
    public String getRid() { return getString("rid"); }
    /// Sets the `rid` attribute.
    public void setRid(String value) { setAttribute("rid", value); }

    /// Allowed values of the `transferEncoding` attribute.
    public enum TransferEncodingValue {
        BASE64("base64"),
        NONE("none"),
        PACKAGE("package");
        private final String v;
        TransferEncodingValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TransferEncodingValue fromValue(String s) {
            for (TransferEncodingValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `transferEncoding` attribute, or null.
    public TransferEncodingValue getTransferEncoding() {
        String v = getAttribute("transferEncoding");
        return v == null ? null : TransferEncodingValue.fromValue(v);
    }
    /// Sets the `transferEncoding` attribute.
    public void setTransferEncoding(TransferEncodingValue value) {
        setAttribute("transferEncoding", value == null ? null : value.value());
    }
    /// @return the raw `transferEncoding` string, or null.
    public String getTransferEncodingRaw() { return getAttribute("transferEncoding"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return this element's text content.
    public String getValue() { return getTextContent(); }
    /// Sets this element's text content.
    public void setValue(String value) { setTextContent(value); }
}
