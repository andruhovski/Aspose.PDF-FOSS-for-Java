package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/**
 * Registry of generated typed element constructors for this XFA grammar
 * (element local name -> typed node).
 */
public final class LocaleSetElements {

    private LocaleSetElements() { }

    /** The grammar's (version-independent) target namespace. */
    public static final String NAMESPACE = "http://www.xfa.org/schema/xfa-locale-set/";

    /** Number of generated typed element classes. */
    public static final int COUNT = 22;

    /**
     * Registers all typed element constructors.
     * @param reg the factory registry map
     */
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("calendarSymbols", CalendarSymbols::new);
        reg.put("currencySymbol", CurrencySymbol::new);
        reg.put("currencySymbols", CurrencySymbols::new);
        reg.put("datePattern", DatePattern::new);
        reg.put("datePatterns", DatePatterns::new);
        reg.put("dateTimeSymbols", DateTimeSymbols::new);
        reg.put("day", Day::new);
        reg.put("dayNames", DayNames::new);
        reg.put("era", Era::new);
        reg.put("eraNames", EraNames::new);
        reg.put("locale", Locale::new);
        reg.put("localeSet", LocaleSet::new);
        reg.put("meridiem", Meridiem::new);
        reg.put("meridiemNames", MeridiemNames::new);
        reg.put("month", Month::new);
        reg.put("monthNames", MonthNames::new);
        reg.put("numberPattern", NumberPattern::new);
        reg.put("numberPatterns", NumberPatterns::new);
        reg.put("numberSymbol", NumberSymbol::new);
        reg.put("numberSymbols", NumberSymbols::new);
        reg.put("timePattern", TimePattern::new);
        reg.put("timePatterns", TimePatterns::new);
    }
}
