package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>locale</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Locale extends XfaNode {

    /** Wraps a backing <code>locale</code> element. */
    public Locale(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>desc</code> attribute, or null. */
    public String getDesc() { return getString("desc"); }
    /** Sets the <code>desc</code> attribute. */
    public void setDesc(String value) { setAttribute("desc", value); }

    /** @return the <code>calendarSymbols</code> child (typed), or null. */
    public CalendarSymbols getCalendarSymbols() { return (CalendarSymbols) getChild("calendarSymbols"); }
    /** Ensures and returns the <code>calendarSymbols</code> child. */
    public CalendarSymbols ensureCalendarSymbols() { return (CalendarSymbols) ensureChild("calendarSymbols"); }

    /** @return the <code>datePatterns</code> child (typed), or null. */
    public DatePatterns getDatePatterns() { return (DatePatterns) getChild("datePatterns"); }
    /** Ensures and returns the <code>datePatterns</code> child. */
    public DatePatterns ensureDatePatterns() { return (DatePatterns) ensureChild("datePatterns"); }

    /** @return the <code>timePatterns</code> child (typed), or null. */
    public TimePatterns getTimePatterns() { return (TimePatterns) getChild("timePatterns"); }
    /** Ensures and returns the <code>timePatterns</code> child. */
    public TimePatterns ensureTimePatterns() { return (TimePatterns) ensureChild("timePatterns"); }

    /** @return the <code>numberPatterns</code> child (typed), or null. */
    public NumberPatterns getNumberPatterns() { return (NumberPatterns) getChild("numberPatterns"); }
    /** Ensures and returns the <code>numberPatterns</code> child. */
    public NumberPatterns ensureNumberPatterns() { return (NumberPatterns) ensureChild("numberPatterns"); }

    /** @return the <code>dateTimeSymbols</code> child (typed), or null. */
    public DateTimeSymbols getDateTimeSymbols() { return (DateTimeSymbols) getChild("dateTimeSymbols"); }
    /** Ensures and returns the <code>dateTimeSymbols</code> child. */
    public DateTimeSymbols ensureDateTimeSymbols() { return (DateTimeSymbols) ensureChild("dateTimeSymbols"); }

    /** @return the <code>numberSymbols</code> child (typed), or null. */
    public NumberSymbols getNumberSymbols() { return (NumberSymbols) getChild("numberSymbols"); }
    /** Ensures and returns the <code>numberSymbols</code> child. */
    public NumberSymbols ensureNumberSymbols() { return (NumberSymbols) ensureChild("numberSymbols"); }

    /** @return the <code>currencySymbols</code> child (typed), or null. */
    public CurrencySymbols getCurrencySymbols() { return (CurrencySymbols) getChild("currencySymbols"); }
    /** Ensures and returns the <code>currencySymbols</code> child. */
    public CurrencySymbols ensureCurrencySymbols() { return (CurrencySymbols) ensureChild("currencySymbols"); }
}
