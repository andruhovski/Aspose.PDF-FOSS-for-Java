package org.aspose.pdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/// Represents a typed XMP metadata value (ISO 16684-1).
///
/// Wraps a string representation with type detection. XMP values can be
/// strings, integers, doubles, dates, arrays, or named value pairs (structures).
///
public class XmpValue {

    private static final Logger LOG = Logger.getLogger(XmpValue.class.getName());

    private final String stringValue;
    private final Object[] arrayValue;
    private final List<Map.Entry<String, XmpValue>> namedValues;

    /// Creates an XmpValue from a string.
    ///
    /// @param value the string value
    public XmpValue(String value) {
        this.stringValue = value != null ? value : "";
        this.arrayValue = null;
        this.namedValues = null;
    }

    /// Creates an XmpValue from an integer.
    ///
    /// @param value the integer value
    public XmpValue(int value) {
        this.stringValue = String.valueOf(value);
        this.arrayValue = null;
        this.namedValues = null;
    }

    /// Creates an XmpValue from a double.
    ///
    /// @param value the double value
    public XmpValue(double value) {
        this.stringValue = String.valueOf(value);
        this.arrayValue = null;
        this.namedValues = null;
    }

    /// Creates an XmpValue from a date (formatted as ISO 8601 / W3C-DTF).
    ///
    /// @param value the date value
    public XmpValue(Date value) {
        if (value != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.stringValue = sdf.format(value);
        } else {
            this.stringValue = "";
        }
        this.arrayValue = null;
        this.namedValues = null;
    }

    /// Creates an XmpValue from an array of objects.
    ///
    /// @param array the array values
    public XmpValue(Object[] array) {
        this.arrayValue = array;
        this.stringValue = array != null && array.length > 0 ? String.valueOf(array[0]) : "";
        this.namedValues = null;
    }

    /// Creates an XmpValue from named value pairs (structure).
    ///
    /// @param entries the named value entries
    public XmpValue(List<Map.Entry<String, XmpValue>> entries) {
        this.namedValues = entries;
        this.stringValue = "";
        this.arrayValue = null;
    }

    /// Returns true if this value is a simple string.
    public boolean isString() {
        return arrayValue == null && namedValues == null && !isInteger() && !isDouble() && !isDateTime();
    }

    /// Returns true if this value can be parsed as an integer.
    public boolean isInteger() {
        if (stringValue == null || stringValue.isEmpty()) return false;
        try {
            Integer.parseInt(stringValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /// Returns true if this value can be parsed as a double.
    public boolean isDouble() {
        if (stringValue == null || stringValue.isEmpty()) return false;
        if (isInteger()) return false; // prefer integer
        try {
            Double.parseDouble(stringValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /// Returns true if this value can be parsed as a date/time.
    public boolean isDateTime() {
        if (stringValue == null || stringValue.isEmpty()) return false;
        // ISO 8601 date pattern check
        return stringValue.matches("\\d{4}-\\d{2}-\\d{2}[T ].*")
                || stringValue.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /// Returns true if this value is an array.
    public boolean isArray() {
        return arrayValue != null;
    }

    /// Returns true if this value is a structure (named value pairs).
    public boolean isNamedValues() {
        return namedValues != null;
    }

    /// Returns the string representation of this value.
    ///
    /// @return the string value
    @Override
    public String toString() {
        return stringValue != null ? stringValue : "";
    }

    /// Alias for toString().
    ///
    /// @return the string value
    public String toStringValue() {
        return toString();
    }

    /// Returns this value as an integer.
    ///
    /// @return the integer value
    /// @throws NumberFormatException if not parseable
    public int toInteger() {
        return Integer.parseInt(stringValue);
    }

    /// Returns this value as a double.
    ///
    /// @return the double value
    /// @throws NumberFormatException if not parseable
    public double toDouble() {
        return Double.parseDouble(stringValue);
    }

    /// Returns this value as a Date (parses ISO 8601 format).
    ///
    /// @return the date, or null if not parseable
    public Date toDateTime() {
        if (stringValue == null || stringValue.isEmpty()) return null;
        // Try multiple ISO 8601 formats
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(stringValue);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /// Returns this value as an array.
    ///
    /// @return the array, or null if not an array
    public Object[] toArray() {
        return arrayValue;
    }

    /// Returns this value as named value pairs (structure).
    ///
    /// @return the entries, or empty list
    public List<Map.Entry<String, XmpValue>> toNamedValues() {
        return namedValues != null ? namedValues : Collections.emptyList();
    }
}
