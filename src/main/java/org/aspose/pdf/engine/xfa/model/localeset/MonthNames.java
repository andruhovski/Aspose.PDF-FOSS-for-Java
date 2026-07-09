package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>monthNames</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class MonthNames extends XfaNode {

    /** Wraps a backing <code>monthNames</code> element. */
    public MonthNames(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>abbr</code> attribute. */
    public enum AbbrValue {
        V_1("1"),
        V_0("0");
        private final String v;
        AbbrValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static AbbrValue fromValue(String s) {
            for (AbbrValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>abbr</code> attribute, or null. */
    public AbbrValue getAbbr() {
        String v = getAttribute("abbr");
        return v == null ? null : AbbrValue.fromValue(v);
    }
    /** Sets the <code>abbr</code> attribute. */
    public void setAbbr(AbbrValue value) {
        setAttribute("abbr", value == null ? null : value.value());
    }
    /** @return the raw <code>abbr</code> string, or null. */
    public String getAbbrRaw() { return getAttribute("abbr"); }

    /** @return the <code>month</code> child (typed), or null. */
    public Month getMonth() { return (Month) getChild("month"); }
    /** Ensures and returns the <code>month</code> child. */
    public Month ensureMonth() { return (Month) ensureChild("month"); }
}
