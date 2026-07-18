package org.aspose.pdf.engine.xfa.model;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// An XFA measurement: a numeric value plus a unit (XFA 3.0 measurement
/// datatype). Units supported: `pt`, `mm`, `cm`, `in`,
/// `em`, `px` and `%`/`percent`. A bare number parses to
/// a unitless measurement.
///
/// Immutable value type; [#format()] round-trips the parsed text in
/// canonical form (shortest number + unit), e.g. `"2.5mm"`.
public final class XfaMeasurement {

    private static final Pattern P = Pattern.compile(
            "^\\s*([+-]?(?:\\d+\\.?\\d*|\\.\\d+))\\s*(pt|mm|cm|in|em|px|percent|%)?\\s*$");

    private final double value;
    private final String unit;

    /// Creates a measurement.
    ///
    /// @param value numeric value
    /// @param unit  unit string (normalised; `""` for unitless)
    public XfaMeasurement(double value, String unit) {
        this.value = value;
        this.unit = unit == null ? "" : ("percent".equals(unit) ? "%" : unit);
    }

    /// Parses an XFA measurement string.
    ///
    /// @param s the text (e.g. `"10pt"`, `"-2.5mm"`, `"50%"`)
    /// @return the measurement, or `null` if `s` is null/blank/invalid
    public static XfaMeasurement parse(String s) {
        if (s == null) {
            return null;
        }
        Matcher m = P.matcher(s);
        if (!m.matches()) {
            return null;
        }
        double v;
        try {
            v = Double.parseDouble(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
        String u = m.group(2);
        return new XfaMeasurement(v, u == null ? "" : u);
    }

    /// @return the numeric value.
    public double getValue() {
        return value;
    }

    /// @return the unit (`""` when unitless; `"%"` for percent).
    public String getUnit() {
        return unit;
    }

    /// Formats this measurement in canonical XFA form (shortest number + unit).
    ///
    /// @return the formatted string
    public String format() {
        String num;
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            num = Long.toString((long) value);
        } else {
            num = trimZeros(String.format(Locale.ROOT, "%.6f", value));
        }
        return num + unit;
    }

    private static String trimZeros(String s) {
        if (s.indexOf('.') < 0) {
            return s;
        }
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && s.charAt(end - 1) == '.') {
            end--;
        }
        return s.substring(0, end);
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof XfaMeasurement)) {
            return false;
        }
        XfaMeasurement m = (XfaMeasurement) o;
        return Double.compare(m.value, value) == 0 && unit.equals(m.unit);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value) * 31 + unit.hashCode();
    }
}
