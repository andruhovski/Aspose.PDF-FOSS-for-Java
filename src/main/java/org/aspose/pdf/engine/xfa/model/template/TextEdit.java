package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `textEdit`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class TextEdit extends XfaNode {

    /// Wraps a backing `textEdit` element.
    public TextEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `allowRichText` attribute.
    public enum AllowRichTextValue {
        V_0("0"),
        V_1("1");
        private final String v;
        AllowRichTextValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AllowRichTextValue fromValue(String s) {
            for (AllowRichTextValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `allowRichText` attribute, or null.
    public AllowRichTextValue getAllowRichText() {
        String v = getAttribute("allowRichText");
        return v == null ? null : AllowRichTextValue.fromValue(v);
    }
    /// Sets the `allowRichText` attribute.
    public void setAllowRichText(AllowRichTextValue value) {
        setAttribute("allowRichText", value == null ? null : value.value());
    }
    /// @return the raw `allowRichText` string, or null.
    public String getAllowRichTextRaw() { return getAttribute("allowRichText"); }

    /// Allowed values of the `hScrollPolicy` attribute.
    public enum HScrollPolicyValue {
        AUTO("auto"),
        OFF("off"),
        ON("on");
        private final String v;
        HScrollPolicyValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static HScrollPolicyValue fromValue(String s) {
            for (HScrollPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `hScrollPolicy` attribute, or null.
    public HScrollPolicyValue getHScrollPolicy() {
        String v = getAttribute("hScrollPolicy");
        return v == null ? null : HScrollPolicyValue.fromValue(v);
    }
    /// Sets the `hScrollPolicy` attribute.
    public void setHScrollPolicy(HScrollPolicyValue value) {
        setAttribute("hScrollPolicy", value == null ? null : value.value());
    }
    /// @return the raw `hScrollPolicy` string, or null.
    public String getHScrollPolicyRaw() { return getAttribute("hScrollPolicy"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `multiLine` attribute.
    public enum MultiLineValue {
        V_0("0"),
        V_1("1");
        private final String v;
        MultiLineValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static MultiLineValue fromValue(String s) {
            for (MultiLineValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `multiLine` attribute, or null.
    public MultiLineValue getMultiLine() {
        String v = getAttribute("multiLine");
        return v == null ? null : MultiLineValue.fromValue(v);
    }
    /// Sets the `multiLine` attribute.
    public void setMultiLine(MultiLineValue value) {
        setAttribute("multiLine", value == null ? null : value.value());
    }
    /// @return the raw `multiLine` string, or null.
    public String getMultiLineRaw() { return getAttribute("multiLine"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// Allowed values of the `vScrollPolicy` attribute.
    public enum VScrollPolicyValue {
        AUTO("auto"),
        OFF("off"),
        ON("on");
        private final String v;
        VScrollPolicyValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static VScrollPolicyValue fromValue(String s) {
            for (VScrollPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `vScrollPolicy` attribute, or null.
    public VScrollPolicyValue getVScrollPolicy() {
        String v = getAttribute("vScrollPolicy");
        return v == null ? null : VScrollPolicyValue.fromValue(v);
    }
    /// Sets the `vScrollPolicy` attribute.
    public void setVScrollPolicy(VScrollPolicyValue value) {
        setAttribute("vScrollPolicy", value == null ? null : value.value());
    }
    /// @return the raw `vScrollPolicy` string, or null.
    public String getVScrollPolicyRaw() { return getAttribute("vScrollPolicy"); }

    /// @return the `border` child (typed), or null.
    public Border getBorder() { return (Border) getChild("border"); }
    /// Ensures and returns the `border` child.
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /// @return the `comb` child (typed), or null.
    public Comb getComb() { return (Comb) getChild("comb"); }
    /// Ensures and returns the `comb` child.
    public Comb ensureComb() { return (Comb) ensureChild("comb"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
