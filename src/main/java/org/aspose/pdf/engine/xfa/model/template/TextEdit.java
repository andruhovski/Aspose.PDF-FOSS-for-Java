package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>textEdit</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class TextEdit extends XfaNode {

    /** Wraps a backing <code>textEdit</code> element. */
    public TextEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>allowRichText</code> attribute. */
    public enum AllowRichTextValue {
        V_0("0"),
        V_1("1");
        private final String v;
        AllowRichTextValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static AllowRichTextValue fromValue(String s) {
            for (AllowRichTextValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>allowRichText</code> attribute, or null. */
    public AllowRichTextValue getAllowRichText() {
        String v = getAttribute("allowRichText");
        return v == null ? null : AllowRichTextValue.fromValue(v);
    }
    /** Sets the <code>allowRichText</code> attribute. */
    public void setAllowRichText(AllowRichTextValue value) {
        setAttribute("allowRichText", value == null ? null : value.value());
    }
    /** @return the raw <code>allowRichText</code> string, or null. */
    public String getAllowRichTextRaw() { return getAttribute("allowRichText"); }

    /** Allowed values of the <code>hScrollPolicy</code> attribute. */
    public enum HScrollPolicyValue {
        AUTO("auto"),
        OFF("off"),
        ON("on");
        private final String v;
        HScrollPolicyValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static HScrollPolicyValue fromValue(String s) {
            for (HScrollPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>hScrollPolicy</code> attribute, or null. */
    public HScrollPolicyValue getHScrollPolicy() {
        String v = getAttribute("hScrollPolicy");
        return v == null ? null : HScrollPolicyValue.fromValue(v);
    }
    /** Sets the <code>hScrollPolicy</code> attribute. */
    public void setHScrollPolicy(HScrollPolicyValue value) {
        setAttribute("hScrollPolicy", value == null ? null : value.value());
    }
    /** @return the raw <code>hScrollPolicy</code> string, or null. */
    public String getHScrollPolicyRaw() { return getAttribute("hScrollPolicy"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>multiLine</code> attribute. */
    public enum MultiLineValue {
        V_0("0"),
        V_1("1");
        private final String v;
        MultiLineValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static MultiLineValue fromValue(String s) {
            for (MultiLineValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>multiLine</code> attribute, or null. */
    public MultiLineValue getMultiLine() {
        String v = getAttribute("multiLine");
        return v == null ? null : MultiLineValue.fromValue(v);
    }
    /** Sets the <code>multiLine</code> attribute. */
    public void setMultiLine(MultiLineValue value) {
        setAttribute("multiLine", value == null ? null : value.value());
    }
    /** @return the raw <code>multiLine</code> string, or null. */
    public String getMultiLineRaw() { return getAttribute("multiLine"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** Allowed values of the <code>vScrollPolicy</code> attribute. */
    public enum VScrollPolicyValue {
        AUTO("auto"),
        OFF("off"),
        ON("on");
        private final String v;
        VScrollPolicyValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static VScrollPolicyValue fromValue(String s) {
            for (VScrollPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>vScrollPolicy</code> attribute, or null. */
    public VScrollPolicyValue getVScrollPolicy() {
        String v = getAttribute("vScrollPolicy");
        return v == null ? null : VScrollPolicyValue.fromValue(v);
    }
    /** Sets the <code>vScrollPolicy</code> attribute. */
    public void setVScrollPolicy(VScrollPolicyValue value) {
        setAttribute("vScrollPolicy", value == null ? null : value.value());
    }
    /** @return the raw <code>vScrollPolicy</code> string, or null. */
    public String getVScrollPolicyRaw() { return getAttribute("vScrollPolicy"); }

    /** @return the <code>border</code> child (typed), or null. */
    public Border getBorder() { return (Border) getChild("border"); }
    /** Ensures and returns the <code>border</code> child. */
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /** @return the <code>comb</code> child (typed), or null. */
    public Comb getComb() { return (Comb) getChild("comb"); }
    /** Ensures and returns the <code>comb</code> child. */
    public Comb ensureComb() { return (Comb) ensureChild("comb"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
