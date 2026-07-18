package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `submit`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Submit extends XfaNode {

    /// Wraps a backing `submit` element.
    public Submit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `embedPDF` attribute.
    public enum EmbedPDFValue {
        V_0("0"),
        V_1("1");
        private final String v;
        EmbedPDFValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static EmbedPDFValue fromValue(String s) {
            for (EmbedPDFValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `embedPDF` attribute, or null.
    public EmbedPDFValue getEmbedPDF() {
        String v = getAttribute("embedPDF");
        return v == null ? null : EmbedPDFValue.fromValue(v);
    }
    /// Sets the `embedPDF` attribute.
    public void setEmbedPDF(EmbedPDFValue value) {
        setAttribute("embedPDF", value == null ? null : value.value());
    }
    /// @return the raw `embedPDF` string, or null.
    public String getEmbedPDFRaw() { return getAttribute("embedPDF"); }

    /// Allowed values of the `format` attribute.
    public enum FormatValue {
        FORMDATA("formdata"),
        PDF("pdf"),
        URLENCODED("urlencoded"),
        XDP("xdp"),
        XFD("xfd"),
        XML("xml");
        private final String v;
        FormatValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static FormatValue fromValue(String s) {
            for (FormatValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `format` attribute, or null.
    public FormatValue getFormat() {
        String v = getAttribute("format");
        return v == null ? null : FormatValue.fromValue(v);
    }
    /// Sets the `format` attribute.
    public void setFormat(FormatValue value) {
        setAttribute("format", value == null ? null : value.value());
    }
    /// @return the raw `format` string, or null.
    public String getFormatRaw() { return getAttribute("format"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `target` attribute, or null.
    public String getTarget() { return getString("target"); }
    /// Sets the `target` attribute.
    public void setTarget(String value) { setAttribute("target", value); }

    /// @return the typed `textEncoding` attribute, or null.
    public String getTextEncoding() { return getString("textEncoding"); }
    /// Sets the `textEncoding` attribute.
    public void setTextEncoding(String value) { setAttribute("textEncoding", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the typed `xdpContent` attribute, or null.
    public String getXdpContent() { return getString("xdpContent"); }
    /// Sets the `xdpContent` attribute.
    public void setXdpContent(String value) { setAttribute("xdpContent", value); }

    /// @return the `encrypt` child (typed), or null.
    public Encrypt getEncrypt() { return (Encrypt) getChild("encrypt"); }
    /// Ensures and returns the `encrypt` child.
    public Encrypt ensureEncrypt() { return (Encrypt) ensureChild("encrypt"); }

    /// @return the `signData` children (typed).
    public java.util.List<SignData> getSignDataList() {
        java.util.List<SignData> r = new java.util.ArrayList<SignData>();
        for (XfaNode n : getChildren("signData")) { r.add((SignData) n); }
        return r;
    }
    /// Appends a new `signData` child.
    public SignData addSignData() { return (SignData) addChild("signData"); }
}
