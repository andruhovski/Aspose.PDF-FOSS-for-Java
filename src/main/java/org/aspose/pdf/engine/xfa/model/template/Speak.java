package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>speak</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Speak extends XfaNode {

    /** Wraps a backing <code>speak</code> element. */
    public Speak(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>disable</code> attribute. */
    public enum DisableValue {
        V_0("0"),
        V_1("1");
        private final String v;
        DisableValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static DisableValue fromValue(String s) {
            for (DisableValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>disable</code> attribute, or null. */
    public DisableValue getDisable() {
        String v = getAttribute("disable");
        return v == null ? null : DisableValue.fromValue(v);
    }
    /** Sets the <code>disable</code> attribute. */
    public void setDisable(DisableValue value) {
        setAttribute("disable", value == null ? null : value.value());
    }
    /** @return the raw <code>disable</code> string, or null. */
    public String getDisableRaw() { return getAttribute("disable"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>priority</code> attribute. */
    public enum PriorityValue {
        CAPTION("caption"),
        CUSTOM("custom"),
        NAME("name"),
        TOOLTIP("toolTip");
        private final String v;
        PriorityValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PriorityValue fromValue(String s) {
            for (PriorityValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>priority</code> attribute, or null. */
    public PriorityValue getPriority() {
        String v = getAttribute("priority");
        return v == null ? null : PriorityValue.fromValue(v);
    }
    /** Sets the <code>priority</code> attribute. */
    public void setPriority(PriorityValue value) {
        setAttribute("priority", value == null ? null : value.value());
    }
    /** @return the raw <code>priority</code> string, or null. */
    public String getPriorityRaw() { return getAttribute("priority"); }

    /** @return the typed <code>rid</code> attribute, or null. */
    public String getRid() { return getString("rid"); }
    /** Sets the <code>rid</code> attribute. */
    public void setRid(String value) { setAttribute("rid", value); }

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
