package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `locale`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Locale extends XfaNode {

    /// Wraps a backing `locale` element.
    public Locale(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `desc` attribute, or null.
    public String getDesc() { return getString("desc"); }
    /// Sets the `desc` attribute.
    public void setDesc(String value) { setAttribute("desc", value); }

    /// @return the `calendarSymbols` child (typed), or null.
    public CalendarSymbols getCalendarSymbols() { return (CalendarSymbols) getChild("calendarSymbols"); }
    /// Ensures and returns the `calendarSymbols` child.
    public CalendarSymbols ensureCalendarSymbols() { return (CalendarSymbols) ensureChild("calendarSymbols"); }

    /// @return the `datePatterns` child (typed), or null.
    public DatePatterns getDatePatterns() { return (DatePatterns) getChild("datePatterns"); }
    /// Ensures and returns the `datePatterns` child.
    public DatePatterns ensureDatePatterns() { return (DatePatterns) ensureChild("datePatterns"); }

    /// @return the `timePatterns` child (typed), or null.
    public TimePatterns getTimePatterns() { return (TimePatterns) getChild("timePatterns"); }
    /// Ensures and returns the `timePatterns` child.
    public TimePatterns ensureTimePatterns() { return (TimePatterns) ensureChild("timePatterns"); }

    /// @return the `numberPatterns` child (typed), or null.
    public NumberPatterns getNumberPatterns() { return (NumberPatterns) getChild("numberPatterns"); }
    /// Ensures and returns the `numberPatterns` child.
    public NumberPatterns ensureNumberPatterns() { return (NumberPatterns) ensureChild("numberPatterns"); }

    /// @return the `dateTimeSymbols` child (typed), or null.
    public DateTimeSymbols getDateTimeSymbols() { return (DateTimeSymbols) getChild("dateTimeSymbols"); }
    /// Ensures and returns the `dateTimeSymbols` child.
    public DateTimeSymbols ensureDateTimeSymbols() { return (DateTimeSymbols) ensureChild("dateTimeSymbols"); }

    /// @return the `numberSymbols` child (typed), or null.
    public NumberSymbols getNumberSymbols() { return (NumberSymbols) getChild("numberSymbols"); }
    /// Ensures and returns the `numberSymbols` child.
    public NumberSymbols ensureNumberSymbols() { return (NumberSymbols) ensureChild("numberSymbols"); }

    /// @return the `currencySymbols` child (typed), or null.
    public CurrencySymbols getCurrencySymbols() { return (CurrencySymbols) getChild("currencySymbols"); }
    /// Ensures and returns the `currencySymbols` child.
    public CurrencySymbols ensureCurrencySymbols() { return (CurrencySymbols) ensureChild("currencySymbols"); }
}
