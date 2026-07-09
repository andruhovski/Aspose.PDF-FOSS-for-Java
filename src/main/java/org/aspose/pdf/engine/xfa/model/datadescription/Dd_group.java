package org.aspose.pdf.engine.xfa.model.datadescription;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>dd:group</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Dd_group extends XfaNode {

    /** Wraps a backing <code>dd:group</code> element. */
    public Dd_group(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>dd:minOccur</code> attribute, or null. */
    public java.lang.Integer getDd_minOccur() { return getInteger("dd:minOccur"); }
    /** Sets the <code>dd:minOccur</code> attribute. */
    public void setDd_minOccur(java.lang.Integer value) { setAttribute("dd:minOccur", value == null ? null : value.toString()); }

    /** @return the typed <code>dd:maxOccur</code> attribute, or null. */
    public java.lang.Integer getDd_maxOccur() { return getInteger("dd:maxOccur"); }
    /** Sets the <code>dd:maxOccur</code> attribute. */
    public void setDd_maxOccur(java.lang.Integer value) { setAttribute("dd:maxOccur", value == null ? null : value.toString()); }

    /** Allowed values of the <code>dd:model</code> attribute. */
    public enum Dd_modelValue {
        CHOICE("choice"),
        ORDERED("ordered"),
        UNORDERED("unordered");
        private final String v;
        Dd_modelValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static Dd_modelValue fromValue(String s) {
            for (Dd_modelValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>dd:model</code> attribute, or null. */
    public Dd_modelValue getDd_model() {
        String v = getAttribute("dd:model");
        return v == null ? null : Dd_modelValue.fromValue(v);
    }
    /** Sets the <code>dd:model</code> attribute. */
    public void setDd_model(Dd_modelValue value) {
        setAttribute("dd:model", value == null ? null : value.value());
    }
    /** @return the raw <code>dd:model</code> string, or null. */
    public String getDd_modelRaw() { return getAttribute("dd:model"); }

    /** @return the <code>dd:group</code> children (typed). */
    public java.util.List<Dd_group> getDd_groupList() {
        java.util.List<Dd_group> r = new java.util.ArrayList<Dd_group>();
        for (XfaNode n : getChildren("dd:group")) { r.add((Dd_group) n); }
        return r;
    }
    /** Appends a new <code>dd:group</code> child. */
    public Dd_group addDd_group() { return (Dd_group) addChild("dd:group"); }
}
