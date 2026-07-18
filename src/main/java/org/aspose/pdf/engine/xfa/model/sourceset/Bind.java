package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `bind`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Bind extends XfaNode {

    /// Wraps a backing `bind` element.
    public Bind(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `contentType` attribute, or null.
    public String getContentType() { return getString("contentType"); }
    /// Sets the `contentType` attribute.
    public void setContentType(String value) { setAttribute("contentType", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

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
}
