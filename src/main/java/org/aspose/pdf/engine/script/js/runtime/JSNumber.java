package org.aspose.pdf.engine.script.js.runtime;

import java.math.BigInteger;

/**
 * Numeric abstract operations: Number-to-String (sec 9.8.1), String-to-Number
 * (sec 9.3.1), ToInteger/ToInt32/ToUint32 (sec 9.4-9.6) and radix conversion
 * for {@code Number.prototype.toString}.
 */
public final class JSNumber {

    private JSNumber() { }

    /**
     * ECMAScript Number-to-String (sec 9.8.1) for radix 10. Produces the
     * shortest decimal that round-trips, formatted with JavaScript's rules
     * (no trailing {@code .0}, exponential only outside 1e-6..1e21).
     *
     * @param d the number
     * @return its canonical string form
     */
    public static String toStr(double d) {
        if (Double.isNaN(d)) {
            return "NaN";
        }
        if (d == 0.0) {
            return "0";
        }
        if (d < 0) {
            return "-" + toStr(-d);
        }
        if (Double.isInfinite(d)) {
            return "Infinity";
        }

        String t = Double.toString(d); // shortest round-trip, e.g. "1.5", "1.0E20"
        int ePos = t.indexOf('E');
        int exp = 0;
        String mant = t;
        if (ePos >= 0) {
            exp = Integer.parseInt(t.substring(ePos + 1));
            mant = t.substring(0, ePos);
        }
        int dot = mant.indexOf('.');
        String intg = dot >= 0 ? mant.substring(0, dot) : mant;
        String frac = dot >= 0 ? mant.substring(dot + 1) : "";
        String digits = intg + frac;
        int pointPos = intg.length() + exp;

        int lead = 0;
        while (lead < digits.length() - 1 && digits.charAt(lead) == '0') {
            lead++;
            pointPos--;
        }
        digits = digits.substring(lead);
        int end = digits.length();
        while (end > 1 && digits.charAt(end - 1) == '0') {
            end--;
        }
        digits = digits.substring(0, end);

        int k = digits.length();
        int n = pointPos;

        StringBuilder sb = new StringBuilder();
        if (k <= n && n <= 21) {
            sb.append(digits);
            for (int z = 0; z < n - k; z++) {
                sb.append('0');
            }
        } else if (0 < n && n <= 21) {
            sb.append(digits, 0, n).append('.').append(digits.substring(n));
        } else if (-6 < n && n <= 0) {
            sb.append("0.");
            for (int z = 0; z < -n; z++) {
                sb.append('0');
            }
            sb.append(digits);
        } else {
            sb.append(digits.charAt(0));
            if (k > 1) {
                sb.append('.').append(digits.substring(1));
            }
            sb.append('e');
            int e = n - 1;
            sb.append(e >= 0 ? '+' : '-');
            sb.append(Math.abs(e));
        }
        return sb.toString();
    }

    /**
     * Number-to-String in an arbitrary radix 2..36 (sec 15.7.4.2).
     *
     * @param d     number
     * @param radix radix (2..36)
     * @return string representation
     */
    public static String toStringRadix(double d, int radix) {
        if (radix == 10) {
            return toStr(d);
        }
        if (Double.isNaN(d)) {
            return "NaN";
        }
        if (Double.isInfinite(d)) {
            return d > 0 ? "Infinity" : "-Infinity";
        }
        if (d == 0.0) {
            return "0";
        }
        boolean neg = d < 0;
        d = Math.abs(d);

        double intPart = Math.floor(d);
        double fracPart = d - intPart;

        StringBuilder sb = new StringBuilder();
        if (intPart <= 9.007199254740992E15) {
            sb.append(Long.toString((long) intPart, radix));
        } else {
            sb.append(new BigInteger(toStr(intPart)).toString(radix));
        }

        if (fracPart > 0) {
            sb.append('.');
            int guard = 0;
            while (fracPart > 0 && guard < 52) {
                fracPart *= radix;
                int digit = (int) Math.floor(fracPart);
                sb.append(Character.forDigit(digit, radix));
                fracPart -= digit;
                guard++;
            }
        }
        return (neg ? "-" : "") + sb;
    }

    /**
     * ECMAScript String-to-Number (sec 9.3.1): trims whitespace, accepts
     * decimal/hex/Infinity, empty string is {@code 0}, otherwise {@code NaN}.
     *
     * @param s the string
     * @return the numeric value
     */
    public static double fromString(String s) {
        String t = trimJs(s);
        if (t.isEmpty()) {
            return 0;
        }
        try {
            if (t.equals("Infinity") || t.equals("+Infinity")) {
                return Double.POSITIVE_INFINITY;
            }
            if (t.equals("-Infinity")) {
                return Double.NEGATIVE_INFINITY;
            }
            if (t.length() > 2 && t.charAt(0) == '0' && (t.charAt(1) == 'x' || t.charAt(1) == 'X')) {
                return new BigInteger(t.substring(2), 16).doubleValue();
            }
            // Reject Java-only forms (trailing d/f, leading/trailing junk) by a strict regex.
            if (!t.matches("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?")) {
                return Double.NaN;
            }
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** ToInteger (sec 9.4). */
    public static double toInteger(double d) {
        if (Double.isNaN(d)) {
            return 0;
        }
        if (d == 0 || Double.isInfinite(d)) {
            return d;
        }
        return (d < 0 ? Math.ceil(d) : Math.floor(d));
    }

    /** ToInt32 (sec 9.5). */
    public static int toInt32(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
            return 0;
        }
        double posInt = (d < 0 ? Math.ceil(d) : Math.floor(d));
        double m = mod(posInt, 4294967296.0);
        if (m >= 2147483648.0) {
            m -= 4294967296.0;
        }
        return (int) (long) m;
    }

    /** ToUint32 (sec 9.6). */
    public static long toUint32(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
            return 0;
        }
        double posInt = (d < 0 ? Math.ceil(d) : Math.floor(d));
        double m = mod(posInt, 4294967296.0);
        return (long) m;
    }

    private static double mod(double a, double b) {
        double r = a % b;
        return r < 0 ? r + b : r;
    }

    /** Trims ECMAScript whitespace and line terminators from both ends. */
    public static String trimJs(String s) {
        int a = 0;
        int b = s.length();
        while (a < b && isWs(s.charAt(a))) {
            a++;
        }
        while (b > a && isWs(s.charAt(b - 1))) {
            b--;
        }
        return s.substring(a, b);
    }

    private static boolean isWs(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f'
                || c == 0x0B || c == 0xA0 || c == 0xFEFF || c == 0x2028 || c == 0x2029
                || Character.getType(c) == Character.SPACE_SEPARATOR;
    }
}
