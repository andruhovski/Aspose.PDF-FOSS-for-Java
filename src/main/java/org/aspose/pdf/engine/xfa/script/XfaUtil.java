package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.builtins.Realm;
import org.aspose.pdf.engine.script.js.runtime.JSFunction;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code util} host object (Stage B / B3.1 A.3) — the XFA date helpers the corpus's "Date" demand
 * actually used (this is where it lived, not the JS {@code Date} engine): {@code util.printd(picture,
 * date)} formats a JS {@code Date} by an XFA/Acrobat date picture, {@code util.scand(picture, str)}
 * parses a string back to a {@code Date}. Supports the common picture tokens (YYYY/YY, MMMM/MMM/MM/M,
 * DD/D, HH/hh/mm/ss) in the host default locale; non-English month names / exotic pictures are a
 * tracked gap. Times are computed in UTC for determinism (headless).
 */
final class XfaUtil {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final String[] MON_SHORT = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final String[] MON_FULL = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private XfaUtil() {
    }

    static void install(Realm realm, JSObject util) {
        realm.method(util, "printd", 2, (i, t, a) -> {
            String picture = i.toStringJS(XfaScriptHost.arg(a, 0));
            Object d = XfaScriptHost.arg(a, 1);
            Double ms = msOf(d);
            if (ms == null || Double.isNaN(ms)) {
                return "";
            }
            return printd(picture, ms.longValue());
        });
        realm.method(util, "scand", 2, (i, t, a) -> {
            String picture = i.toStringJS(XfaScriptHost.arg(a, 0));
            String s = i.toStringJS(XfaScriptHost.arg(a, 1));
            Long ms = scand(picture, s);
            if (ms == null) {
                return Undefined.INSTANCE;
            }
            return ((JSFunction) realm.dateConstructor).construct(i, new Object[]{(double) ms});
        });
        // common string helpers the corpus touches
        realm.method(util, "formatString", 2, (i, t, a) -> i.toStringJS(XfaScriptHost.arg(a, 0)));
        realm.method(util, "stringFromStream", 1, (i, t, a) -> "");
    }

    /* ------------------------------ printd ------------------------------ */

    static String printd(String picture, long ms) {
        Calendar c = new GregorianCalendar(UTC);
        c.setTimeInMillis(ms);
        StringBuilder out = new StringBuilder();
        int i = 0;
        int n = picture.length();
        while (i < n) {
            char ch = picture.charAt(i);
            if (isToken(ch)) {
                int j = i;
                while (j < n && picture.charAt(j) == ch) {
                    j++;
                }
                out.append(field(ch, j - i, c));
                i = j;
            } else {
                out.append(ch);
                i++;
            }
        }
        return out.toString();
    }

    private static String field(char tok, int count, Calendar c) {
        switch (tok) {
            case 'Y':
                int y = c.get(Calendar.YEAR);
                return count <= 2 ? pad(y % 100, 2) : pad(y, 4);
            case 'M':
                int m = c.get(Calendar.MONTH); // 0-based
                if (count >= 4) {
                    return MON_FULL[m];
                }
                if (count == 3) {
                    return MON_SHORT[m];
                }
                return count == 2 ? pad(m + 1, 2) : Integer.toString(m + 1);
            case 'D':
                int d = c.get(Calendar.DAY_OF_MONTH);
                return count >= 2 ? pad(d, 2) : Integer.toString(d);
            case 'H':
                int h = c.get(Calendar.HOUR_OF_DAY);
                return count >= 2 ? pad(h, 2) : Integer.toString(h);
            case 'h':
                int h12 = c.get(Calendar.HOUR);
                if (h12 == 0) {
                    h12 = 12;
                }
                return count >= 2 ? pad(h12, 2) : Integer.toString(h12);
            case 'm':
                return pad(c.get(Calendar.MINUTE), count >= 2 ? 2 : 1);
            case 's':
                return pad(c.get(Calendar.SECOND), count >= 2 ? 2 : 1);
            default:
                return "";
        }
    }

    /* ------------------------------ scand ------------------------------ */

    static Long scand(String picture, String s) {
        StringBuilder regex = new StringBuilder();
        java.util.List<Character> order = new java.util.ArrayList<>();
        int i = 0;
        int n = picture.length();
        while (i < n) {
            char ch = picture.charAt(i);
            if (isToken(ch)) {
                int j = i;
                while (j < n && picture.charAt(j) == ch) {
                    j++;
                }
                int count = j - i;
                if (ch == 'M' && count >= 3) {
                    regex.append("([A-Za-z]+)");
                } else {
                    regex.append("(\\d{1,").append(ch == 'Y' ? 4 : 2).append("})");
                }
                order.add(ch);
                i = j;
            } else {
                regex.append(Pattern.quote(String.valueOf(ch)));
                i++;
            }
        }
        Matcher mt = Pattern.compile(regex.toString()).matcher(s.trim());
        if (!mt.matches()) {
            return null;
        }
        Calendar c = new GregorianCalendar(UTC);
        c.clear();
        c.set(Calendar.YEAR, 1900);
        c.set(Calendar.DAY_OF_MONTH, 1);
        try {
            for (int g = 0; g < order.size(); g++) {
                String v = mt.group(g + 1);
                switch (order.get(g)) {
                    case 'Y':
                        int y = Integer.parseInt(v);
                        c.set(Calendar.YEAR, v.length() <= 2 ? 2000 + y : y);
                        break;
                    case 'M':
                        c.set(Calendar.MONTH, v.matches("\\d+") ? Integer.parseInt(v) - 1 : monthName(v));
                        break;
                    case 'D':
                        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(v));
                        break;
                    case 'H':
                    case 'h':
                        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(v));
                        break;
                    case 'm':
                        c.set(Calendar.MINUTE, Integer.parseInt(v));
                        break;
                    case 's':
                        c.set(Calendar.SECOND, Integer.parseInt(v));
                        break;
                    default:
                        break;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return c.getTimeInMillis();
    }

    private static int monthName(String v) {
        for (int k = 0; k < 12; k++) {
            if (MON_FULL[k].equalsIgnoreCase(v) || MON_SHORT[k].equalsIgnoreCase(v)) {
                return k;
            }
        }
        return 0;
    }

    /* ------------------------------ helpers ------------------------------ */

    private static Double msOf(Object date) {
        if (date instanceof JSObject && ((JSObject) date).primitiveValue instanceof Double) {
            return (Double) ((JSObject) date).primitiveValue;
        }
        return null;
    }

    private static boolean isToken(char c) {
        return c == 'Y' || c == 'M' || c == 'D' || c == 'H' || c == 'h' || c == 'm' || c == 's';
    }

    private static String pad(int v, int width) {
        return String.format(Locale.ROOT, "%0" + width + "d", v);
    }
}
