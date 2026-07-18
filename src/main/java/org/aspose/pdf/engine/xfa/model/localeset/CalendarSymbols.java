package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `calendarSymbols`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class CalendarSymbols extends XfaNode {

    /// Wraps a backing `calendarSymbols` element.
    public CalendarSymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `name` attribute.
    public enum NameValue {
        GREGORIAN("gregorian");
        private final String v;
        NameValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static NameValue fromValue(String s) {
            for (NameValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `name` attribute, or null.
    public NameValue getName2() {
        String v = getAttribute("name");
        return v == null ? null : NameValue.fromValue(v);
    }
    /// Sets the `name` attribute.
    public void setName(NameValue value) {
        setAttribute("name", value == null ? null : value.value());
    }
    /// @return the raw `name` string, or null.
    public String getNameRaw() { return getAttribute("name"); }

    /// @return the `monthNames` child (typed), or null.
    public MonthNames getMonthNames() { return (MonthNames) getChild("monthNames"); }
    /// Ensures and returns the `monthNames` child.
    public MonthNames ensureMonthNames() { return (MonthNames) ensureChild("monthNames"); }

    /// @return the `dayNames` child (typed), or null.
    public DayNames getDayNames() { return (DayNames) getChild("dayNames"); }
    /// Ensures and returns the `dayNames` child.
    public DayNames ensureDayNames() { return (DayNames) ensureChild("dayNames"); }

    /// @return the `eraNames` child (typed), or null.
    public EraNames getEraNames() { return (EraNames) getChild("eraNames"); }
    /// Ensures and returns the `eraNames` child.
    public EraNames ensureEraNames() { return (EraNames) ensureChild("eraNames"); }

    /// @return the `meridiemNames` child (typed), or null.
    public MeridiemNames getMeridiemNames() { return (MeridiemNames) getChild("meridiemNames"); }
    /// Ensures and returns the `meridiemNames` child.
    public MeridiemNames ensureMeridiemNames() { return (MeridiemNames) ensureChild("meridiemNames"); }
}
