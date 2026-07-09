package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>calendarSymbols</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class CalendarSymbols extends XfaNode {

    /** Wraps a backing <code>calendarSymbols</code> element. */
    public CalendarSymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>name</code> attribute. */
    public enum NameValue {
        GREGORIAN("gregorian");
        private final String v;
        NameValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static NameValue fromValue(String s) {
            for (NameValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>name</code> attribute, or null. */
    public NameValue getName2() {
        String v = getAttribute("name");
        return v == null ? null : NameValue.fromValue(v);
    }
    /** Sets the <code>name</code> attribute. */
    public void setName(NameValue value) {
        setAttribute("name", value == null ? null : value.value());
    }
    /** @return the raw <code>name</code> string, or null. */
    public String getNameRaw() { return getAttribute("name"); }

    /** @return the <code>monthNames</code> child (typed), or null. */
    public MonthNames getMonthNames() { return (MonthNames) getChild("monthNames"); }
    /** Ensures and returns the <code>monthNames</code> child. */
    public MonthNames ensureMonthNames() { return (MonthNames) ensureChild("monthNames"); }

    /** @return the <code>dayNames</code> child (typed), or null. */
    public DayNames getDayNames() { return (DayNames) getChild("dayNames"); }
    /** Ensures and returns the <code>dayNames</code> child. */
    public DayNames ensureDayNames() { return (DayNames) ensureChild("dayNames"); }

    /** @return the <code>eraNames</code> child (typed), or null. */
    public EraNames getEraNames() { return (EraNames) getChild("eraNames"); }
    /** Ensures and returns the <code>eraNames</code> child. */
    public EraNames ensureEraNames() { return (EraNames) ensureChild("eraNames"); }

    /** @return the <code>meridiemNames</code> child (typed), or null. */
    public MeridiemNames getMeridiemNames() { return (MeridiemNames) getChild("meridiemNames"); }
    /** Ensures and returns the <code>meridiemNames</code> child. */
    public MeridiemNames ensureMeridiemNames() { return (MeridiemNames) ensureChild("meridiemNames"); }
}
