package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>dateTimeEdit</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class DateTimeEdit extends XfaNode {

    /** Wraps a backing <code>dateTimeEdit</code> element. */
    public DateTimeEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /** Allowed values of the <code>picker</code> attribute. */
    public enum PickerValue {
        HOST("host"),
        NONE("none");
        private final String v;
        PickerValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PickerValue fromValue(String s) {
            for (PickerValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>picker</code> attribute, or null. */
    public PickerValue getPicker() {
        String v = getAttribute("picker");
        return v == null ? null : PickerValue.fromValue(v);
    }
    /** Sets the <code>picker</code> attribute. */
    public void setPicker(PickerValue value) {
        setAttribute("picker", value == null ? null : value.value());
    }
    /** @return the raw <code>picker</code> string, or null. */
    public String getPickerRaw() { return getAttribute("picker"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

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
