package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `dayNames`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class DayNames extends XfaNode {

    /// Wraps a backing `dayNames` element.
    public DayNames(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `abbr` attribute.
    public enum AbbrValue {
        V_0("0"),
        V_1("1");
        private final String v;
        AbbrValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AbbrValue fromValue(String s) {
            for (AbbrValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `abbr` attribute, or null.
    public AbbrValue getAbbr() {
        String v = getAttribute("abbr");
        return v == null ? null : AbbrValue.fromValue(v);
    }
    /// Sets the `abbr` attribute.
    public void setAbbr(AbbrValue value) {
        setAttribute("abbr", value == null ? null : value.value());
    }
    /// @return the raw `abbr` string, or null.
    public String getAbbrRaw() { return getAttribute("abbr"); }

    /// @return the `day` child (typed), or null.
    public Day getDay() { return (Day) getChild("day"); }
    /// Ensures and returns the `day` child.
    public Day ensureDay() { return (Day) ensureChild("day"); }
}
