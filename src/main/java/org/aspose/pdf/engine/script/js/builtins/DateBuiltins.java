package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.runtime.JSNumber;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.NativeFunction;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Installs Date and Date.prototype (ECMA-262 3rd ed., sec 15.9). Provides the
 * time value, component getters/setters (local and UTC), {@code getTime},
 * {@code getTimezoneOffset}, static {@code UTC}/{@code parse} and string
 * formatting. Local-time methods use the host default time zone.
 */
final class DateBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private DateBuiltins() { }

    static void install(Realm r) {
        JSObject proto = new JSObject(r.objectPrototype);
        proto.setClassName("Date");
        proto.primitiveValue = Double.NaN;
        r.datePrototype = proto;

        r.method(proto, "valueOf", 0, (i, t, a) -> time(r, t));
        r.method(proto, "getTime", 0, (i, t, a) -> time(r, t));
        r.method(proto, "setTime", 1, (i, t, a) -> {
            double ms = JSNumber.toInteger(i.toNumber(Builtins.arg(a, 0)));
            ((JSObject) t).primitiveValue = ms;
            return ms;
        });
        r.method(proto, "getTimezoneOffset", 0, (i, t, a) -> {
            double ms = time(r, t);
            if (Double.isNaN(ms)) {
                return Double.NaN;
            }
            return (double) (-TimeZone.getDefault().getOffset((long) ms) / 60000);
        });

        field(r, proto, "getFullYear", Calendar.YEAR, false, 0);
        field(r, proto, "getUTCFullYear", Calendar.YEAR, true, 0);
        field(r, proto, "getMonth", Calendar.MONTH, false, 0);
        field(r, proto, "getUTCMonth", Calendar.MONTH, true, 0);
        field(r, proto, "getDate", Calendar.DAY_OF_MONTH, false, 0);
        field(r, proto, "getUTCDate", Calendar.DAY_OF_MONTH, true, 0);
        field(r, proto, "getDay", Calendar.DAY_OF_WEEK, false, -1);
        field(r, proto, "getUTCDay", Calendar.DAY_OF_WEEK, true, -1);
        field(r, proto, "getHours", Calendar.HOUR_OF_DAY, false, 0);
        field(r, proto, "getUTCHours", Calendar.HOUR_OF_DAY, true, 0);
        field(r, proto, "getMinutes", Calendar.MINUTE, false, 0);
        field(r, proto, "getUTCMinutes", Calendar.MINUTE, true, 0);
        field(r, proto, "getSeconds", Calendar.SECOND, false, 0);
        field(r, proto, "getUTCSeconds", Calendar.SECOND, true, 0);
        field(r, proto, "getMilliseconds", Calendar.MILLISECOND, false, 0);
        field(r, proto, "getUTCMilliseconds", Calendar.MILLISECOND, true, 0);

        setField(r, proto, "setFullYear", Calendar.YEAR, false);
        setField(r, proto, "setUTCFullYear", Calendar.YEAR, true);
        setField(r, proto, "setMonth", Calendar.MONTH, false);
        setField(r, proto, "setUTCMonth", Calendar.MONTH, true);
        setField(r, proto, "setDate", Calendar.DAY_OF_MONTH, false);
        setField(r, proto, "setUTCDate", Calendar.DAY_OF_MONTH, true);
        setField(r, proto, "setHours", Calendar.HOUR_OF_DAY, false);
        setField(r, proto, "setUTCHours", Calendar.HOUR_OF_DAY, true);
        setField(r, proto, "setMinutes", Calendar.MINUTE, false);
        setField(r, proto, "setSeconds", Calendar.SECOND, false);
        setField(r, proto, "setMilliseconds", Calendar.MILLISECOND, false);

        r.method(proto, "toString", 0, (i, t, a) -> formatLocal(r, t));
        r.method(proto, "toUTCString", 0, (i, t, a) -> formatUtc(r, t));
        r.method(proto, "toDateString", 0, (i, t, a) -> formatLocal(r, t));
        r.method(proto, "toTimeString", 0, (i, t, a) -> formatLocal(r, t));

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Date", 7, (i, t, a) ->
                formatUtc(r, makeNow(r))).withConstructor((i, a) -> constructDate(r, i, a));
        Builtins.link(ctor, proto);
        r.method(ctor, "UTC", 7, (i, t, a) -> utcFromArgs(i, a));
        r.method(ctor, "parse", 1, (i, t, a) -> parse(i.toStringJS(Builtins.arg(a, 0))));
        r.dateConstructor = ctor;
    }

    private static Object constructDate(Realm r, Interpreter i, Object[] a) {
        JSObject d = new JSObject(r.datePrototype);
        d.setClassName("Date");
        if (a.length == 0) {
            d.primitiveValue = (double) System.currentTimeMillis();
        } else if (a.length == 1) {
            Object v = i.toPrimitive(a[0], null);
            if (v instanceof String) {
                d.primitiveValue = parse((String) v);
            } else {
                d.primitiveValue = JSNumber.toInteger(i.toNumber(v));
            }
        } else {
            d.primitiveValue = componentsToMs(i, a, false);
        }
        return d;
    }

    private static JSObject makeNow(Realm r) {
        JSObject d = new JSObject(r.datePrototype);
        d.setClassName("Date");
        d.primitiveValue = (double) System.currentTimeMillis();
        return d;
    }

    private static double utcFromArgs(Interpreter i, Object[] a) {
        if (a.length < 2) {
            return Double.NaN;
        }
        return componentsToMs(i, a, true);
    }

    private static double componentsToMs(Interpreter i, Object[] a, boolean utc) {
        double year = JSNumber.toInteger(i.toNumber(a[0]));
        double month = a.length > 1 ? JSNumber.toInteger(i.toNumber(a[1])) : 0;
        double day = a.length > 2 ? JSNumber.toInteger(i.toNumber(a[2])) : 1;
        double hour = a.length > 3 ? JSNumber.toInteger(i.toNumber(a[3])) : 0;
        double min = a.length > 4 ? JSNumber.toInteger(i.toNumber(a[4])) : 0;
        double sec = a.length > 5 ? JSNumber.toInteger(i.toNumber(a[5])) : 0;
        double ms = a.length > 6 ? JSNumber.toInteger(i.toNumber(a[6])) : 0;
        if (Double.isNaN(year) || Double.isNaN(month) || Double.isNaN(day)) {
            return Double.NaN;
        }
        int y = (int) year;
        if (y >= 0 && y <= 99) {
            y += 1900;
        }
        Calendar c = new GregorianCalendar(utc ? UTC : TimeZone.getDefault());
        c.clear();
        c.set((int) y, (int) month, (int) day, (int) hour, (int) min, (int) sec);
        c.set(Calendar.MILLISECOND, (int) ms);
        return (double) c.getTimeInMillis();
    }

    private static void field(Realm r, JSObject proto, String name, int calField,
                              boolean utc, int offset) {
        r.method(proto, name, 0, (i, t, a) -> {
            double ms = time(r, t);
            if (Double.isNaN(ms)) {
                return Double.NaN;
            }
            Calendar c = cal(ms, utc);
            return (double) (c.get(calField) + offset);
        });
    }

    private static void setField(Realm r, JSObject proto, String name, int calField, boolean utc) {
        r.method(proto, name, 1, (i, t, a) -> {
            double ms = time(r, t);
            Calendar c = cal(Double.isNaN(ms) ? 0 : ms, utc);
            c.set(calField, (int) JSNumber.toInteger(i.toNumber(Builtins.arg(a, 0))));
            double res = (double) c.getTimeInMillis();
            ((JSObject) t).primitiveValue = res;
            return res;
        });
    }

    private static Calendar cal(double ms, boolean utc) {
        Calendar c = new GregorianCalendar(utc ? UTC : TimeZone.getDefault());
        c.setTimeInMillis((long) ms);
        return c;
    }

    private static double time(Realm r, Object t) {
        if (t instanceof JSObject && ((JSObject) t).primitiveValue instanceof Double) {
            return (Double) ((JSObject) t).primitiveValue;
        }
        throw r.typeError("Date.prototype method called on incompatible receiver");
    }

    private static String formatUtc(Realm r, Object t) {
        double ms = time(r, t);
        if (Double.isNaN(ms)) {
            return "Invalid Date";
        }
        Calendar c = cal(ms, true);
        return String.format(java.util.Locale.ROOT, "%s, %02d %s %04d %02d:%02d:%02d GMT",
                DAYS[c.get(Calendar.DAY_OF_WEEK) - 1], c.get(Calendar.DAY_OF_MONTH),
                MONTHS[c.get(Calendar.MONTH)], c.get(Calendar.YEAR),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    private static String formatLocal(Realm r, Object t) {
        double ms = time(r, t);
        if (Double.isNaN(ms)) {
            return "Invalid Date";
        }
        Calendar c = cal(ms, false);
        return String.format(java.util.Locale.ROOT, "%s %s %02d %04d %02d:%02d:%02d",
                DAYS[c.get(Calendar.DAY_OF_WEEK) - 1], MONTHS[c.get(Calendar.MONTH)],
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    /**
     * Parses a limited set of date string formats: ISO-8601
     * ({@code YYYY-MM-DD} and {@code YYYY-MM-DDTHH:mm:ss(.sss)(Z)}). Other
     * formats yield {@code NaN} (documented gap).
     */
    private static double parse(String s) {
        s = JSNumber.trimJs(s);
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "^(\\d{4})-(\\d{2})-(\\d{2})(?:T(\\d{2}):(\\d{2})(?::(\\d{2})(?:\\.(\\d{1,3}))?)?(Z)?)?$")
                    .matcher(s);
            if (!m.matches()) {
                return Double.NaN;
            }
            Calendar c = new GregorianCalendar(UTC);
            c.clear();
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2)) - 1;
            int day = Integer.parseInt(m.group(3));
            int hh = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            int mm = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
            int ss = m.group(6) != null ? Integer.parseInt(m.group(6)) : 0;
            int millis = m.group(7) != null
                    ? Integer.parseInt((m.group(7) + "000").substring(0, 3)) : 0;
            c.set(year, month, day, hh, mm, ss);
            c.set(Calendar.MILLISECOND, millis);
            return (double) c.getTimeInMillis();
        } catch (RuntimeException e) {
            return Double.NaN;
        }
    }
}
