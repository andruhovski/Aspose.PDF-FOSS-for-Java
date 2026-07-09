package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * PDF real number object (§7.3.3, ISO 32000-1:2008).
 * <p>
 * Represents a floating-point number backed by {@code double}. Serialization follows
 * PDF rules: no exponential notation, trailing zeros removed, maximum 5 decimal places.
 * </p>
 */
public final class PdfFloat extends PdfBase {

    private static final Logger LOG = Logger.getLogger(PdfFloat.class.getName());

    private final double value;
    private transient byte[] serialized;

    /**
     * Creates a PdfFloat from a double value.
     *
     * @param value the numeric value
     * @throws IllegalArgumentException if the value is NaN or infinite
     */
    public PdfFloat(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("NaN is not a valid PDF number");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("Infinity is not a valid PDF number");
        }
        this.value = value;
    }

    /**
     * Creates a PdfFloat by parsing a PDF numeric token.
     *
     * @param textValue the string representation
     * @throws IllegalArgumentException if the string cannot be parsed or represents NaN/Infinity
     */
    public PdfFloat(String textValue) {
        if (textValue == null || textValue.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty string as float");
        }
        double parsed = Double.parseDouble(textValue);
        if (Double.isNaN(parsed) || Double.isInfinite(parsed)) {
            throw new IllegalArgumentException("Invalid PDF number: " + textValue);
        }
        this.value = parsed;
    }

    /**
     * Returns the value as a {@code double}.
     *
     * @return the double value
     */
    public double doubleValue() {
        return value;
    }

    /**
     * Returns the value as a {@code float}.
     *
     * @return the float value
     */
    public float floatValue() {
        return (float) value;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        if (serialized == null) {
            serialized = formatValue().getBytes(StandardCharsets.US_ASCII);
        }
        os.write(serialized);
    }

    /**
     * Formats the value according to PDF rules.
     */
    private String formatValue() {
        // If value has no fractional part and fits in long range, format as integer
        if (value == Math.floor(value) && !Double.isInfinite(value)
                && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            long longVal = (long) value;
            // Handle -0 case
            if (longVal == 0) {
                return "0";
            }
            return Long.toString(longVal);
        }

        // Adobe implementation limit (ISO 32000-1:2008 Annex C, Table C.1): the largest
        // real number a conforming reader is guaranteed to represent is ±32767.0, and reals
        // beyond that magnitude cannot carry meaningful fractional precision. Emitting the
        // fractional digits of such a value (e.g. a −671088.625 clip coordinate) is both
        // non-portable and rejected by strict validators, so round to the nearest integer.
        if (!Double.isInfinite(value) && Math.abs(value) >= 32767.0) {
            return Long.toString(Math.round(value));
        }

        // Format with 10 decimal places for precision
        String formatted = String.format(java.util.Locale.US, "%.10f", value);

        // Remove trailing zeros after decimal point
        if (formatted.contains(".")) {
            int i = formatted.length() - 1;
            while (i > 0 && formatted.charAt(i) == '0') {
                i--;
            }
            if (formatted.charAt(i) == '.') {
                i--; // remove the dot too
            }
            formatted = formatted.substring(0, i + 1);
        }

        // Handle "-0" → "0"
        if ("-0".equals(formatted)) {
            return "0";
        }

        return formatted;
    }

    @Override
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitFloat(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfFloat)) return false;
        return Double.compare(this.value, ((PdfFloat) o).value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return "PdfFloat{" + value + "}";
    }
}
