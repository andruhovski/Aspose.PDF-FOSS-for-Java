package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>submit</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Submit extends XfaNode {

    /** Wraps a backing <code>submit</code> element. */
    public Submit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>embedPDF</code> attribute. */
    public enum EmbedPDFValue {
        V_0("0"),
        V_1("1");
        private final String v;
        EmbedPDFValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static EmbedPDFValue fromValue(String s) {
            for (EmbedPDFValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>embedPDF</code> attribute, or null. */
    public EmbedPDFValue getEmbedPDF() {
        String v = getAttribute("embedPDF");
        return v == null ? null : EmbedPDFValue.fromValue(v);
    }
    /** Sets the <code>embedPDF</code> attribute. */
    public void setEmbedPDF(EmbedPDFValue value) {
        setAttribute("embedPDF", value == null ? null : value.value());
    }
    /** @return the raw <code>embedPDF</code> string, or null. */
    public String getEmbedPDFRaw() { return getAttribute("embedPDF"); }

    /** Allowed values of the <code>format</code> attribute. */
    public enum FormatValue {
        FORMDATA("formdata"),
        PDF("pdf"),
        URLENCODED("urlencoded"),
        XDP("xdp"),
        XFD("xfd"),
        XML("xml");
        private final String v;
        FormatValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static FormatValue fromValue(String s) {
            for (FormatValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>format</code> attribute, or null. */
    public FormatValue getFormat() {
        String v = getAttribute("format");
        return v == null ? null : FormatValue.fromValue(v);
    }
    /** Sets the <code>format</code> attribute. */
    public void setFormat(FormatValue value) {
        setAttribute("format", value == null ? null : value.value());
    }
    /** @return the raw <code>format</code> string, or null. */
    public String getFormatRaw() { return getAttribute("format"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>target</code> attribute, or null. */
    public String getTarget() { return getString("target"); }
    /** Sets the <code>target</code> attribute. */
    public void setTarget(String value) { setAttribute("target", value); }

    /** @return the typed <code>textEncoding</code> attribute, or null. */
    public String getTextEncoding() { return getString("textEncoding"); }
    /** Sets the <code>textEncoding</code> attribute. */
    public void setTextEncoding(String value) { setAttribute("textEncoding", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>xdpContent</code> attribute, or null. */
    public String getXdpContent() { return getString("xdpContent"); }
    /** Sets the <code>xdpContent</code> attribute. */
    public void setXdpContent(String value) { setAttribute("xdpContent", value); }

    /** @return the <code>encrypt</code> child (typed), or null. */
    public Encrypt getEncrypt() { return (Encrypt) getChild("encrypt"); }
    /** Ensures and returns the <code>encrypt</code> child. */
    public Encrypt ensureEncrypt() { return (Encrypt) ensureChild("encrypt"); }

    /** @return the <code>signData</code> children (typed). */
    public java.util.List<SignData> getSignDataList() {
        java.util.List<SignData> r = new java.util.ArrayList<SignData>();
        for (XfaNode n : getChildren("signData")) { r.add((SignData) n); }
        return r;
    }
    /** Appends a new <code>signData</code> child. */
    public SignData addSignData() { return (SignData) addChild("signData"); }
}
