package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `dateTimeEdit`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class DateTimeEdit extends XfaNode {

    /// Wraps a backing `dateTimeEdit` element.
    public DateTimeEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /// Allowed values of the `picker` attribute.
    public enum PickerValue {
        HOST("host"),
        NONE("none");
        private final String v;
        PickerValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PickerValue fromValue(String s) {
            for (PickerValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `picker` attribute, or null.
    public PickerValue getPicker() {
        String v = getAttribute("picker");
        return v == null ? null : PickerValue.fromValue(v);
    }
    /// Sets the `picker` attribute.
    public void setPicker(PickerValue value) {
        setAttribute("picker", value == null ? null : value.value());
    }
    /// @return the raw `picker` string, or null.
    public String getPickerRaw() { return getAttribute("picker"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

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
