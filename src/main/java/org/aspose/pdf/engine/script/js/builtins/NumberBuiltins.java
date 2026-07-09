package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.runtime.JSNumber;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.NativeFunction;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Installs Number and Number.prototype (ECMA-262 3rd ed., sec 15.7):
 * {@code toString(radix)}, {@code toFixed}, {@code toExponential},
 * {@code toPrecision}, {@code valueOf} plus the numeric constants.
 */
final class NumberBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private NumberBuiltins() { }

    static void install(Realm r) {
        JSObject proto = new JSObject(r.objectPrototype);
        proto.setClassName("Number");
        proto.primitiveValue = 0.0;
        r.numberPrototype = proto;

        r.method(proto, "valueOf", 0, (i, t, a) -> thisNum(r, t));
        r.method(proto, "toString", 1, (i, t, a) -> {
            double d = thisNum(r, t);
            Object radixArg = Builtins.arg(a, 0);
            int radix = radixArg == UNDEF ? 10 : (int) i.toNumber(radixArg);
            if (radix < 2 || radix > 36) {
                throw r.rangeError("toString() radix must be between 2 and 36");
            }
            return JSNumber.toStringRadix(d, radix);
        });
        r.method(proto, "toLocaleString", 0, (i, t, a) -> JSNumber.toStr(thisNum(r, t)));
        r.method(proto, "toFixed", 1, (i, t, a) -> {
            double d = thisNum(r, t);
            int f = (int) JSNumber.toInteger(i.toNumber(Builtins.arg(a, 0)));
            if (f < 0 || f > 20) {
                throw r.rangeError("toFixed() digits argument must be between 0 and 20");
            }
            if (Double.isNaN(d)) {
                return "NaN";
            }
            if (Double.isInfinite(d) || Math.abs(d) >= 1e21) {
                return JSNumber.toStr(d);
            }
            return BigDecimal.valueOf(d).setScale(f, RoundingMode.HALF_UP).toPlainString();
        });
        r.method(proto, "toExponential", 1, (i, t, a) -> {
            double d = thisNum(r, t);
            if (Double.isNaN(d)) {
                return "NaN";
            }
            if (Double.isInfinite(d)) {
                return d > 0 ? "Infinity" : "-Infinity";
            }
            Object fa = Builtins.arg(a, 0);
            int f = fa == UNDEF ? -1 : (int) JSNumber.toInteger(i.toNumber(fa));
            return toExponential(d, f);
        });
        r.method(proto, "toPrecision", 1, (i, t, a) -> {
            double d = thisNum(r, t);
            Object pa = Builtins.arg(a, 0);
            if (pa == UNDEF) {
                return JSNumber.toStr(d);
            }
            if (Double.isNaN(d)) {
                return "NaN";
            }
            if (Double.isInfinite(d)) {
                return d > 0 ? "Infinity" : "-Infinity";
            }
            int p = (int) JSNumber.toInteger(i.toNumber(pa));
            if (p < 1 || p > 21) {
                throw r.rangeError("toPrecision() argument must be between 1 and 21");
            }
            return toPrecision(d, p);
        });

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Number", 1, (i, t, a) ->
                a.length == 0 ? 0.0 : i.toNumber(a[0])).withConstructor((i, a) -> {
            JSObject o = new JSObject(proto);
            o.setClassName("Number");
            o.primitiveValue = a.length == 0 ? 0.0 : i.toNumber(a[0]);
            return o;
        });
        Builtins.link(ctor, proto);
        ctor.define("MAX_VALUE", Double.MAX_VALUE, false, false, false);
        ctor.define("MIN_VALUE", Double.MIN_VALUE, false, false, false);
        ctor.define("NaN", Double.NaN, false, false, false);
        ctor.define("POSITIVE_INFINITY", Double.POSITIVE_INFINITY, false, false, false);
        ctor.define("NEGATIVE_INFINITY", Double.NEGATIVE_INFINITY, false, false, false);
        r.numberConstructor = ctor;
    }

    private static double thisNum(Realm r, Object t) {
        if (t instanceof Double) {
            return (Double) t;
        }
        if (t instanceof JSObject && ((JSObject) t).primitiveValue instanceof Double) {
            return (Double) ((JSObject) t).primitiveValue;
        }
        throw r.typeError("Number.prototype method called on incompatible receiver");
    }

    private static String toExponential(double d, int f) {
        BigDecimal bd = BigDecimal.valueOf(d);
        String s;
        if (f < 0) {
            s = String.format(java.util.Locale.ROOT, "%e", d);
            // strip trailing zeros in the mantissa for the "as short as needed" case
            s = shortestExponential(d);
        } else {
            s = String.format(java.util.Locale.ROOT, "%." + f + "e", d);
        }
        return normalizeExp(s);
    }

    private static String shortestExponential(double d) {
        if (d == 0) {
            return "0e+0";
        }
        String plain = JSNumber.toStr(d);
        // reuse Java formatting then trim mantissa zeros
        String s = String.format(java.util.Locale.ROOT, "%.20e", d);
        String norm = normalizeExp(s);
        int e = norm.indexOf('e');
        String mant = norm.substring(0, e);
        String exp = norm.substring(e);
        if (mant.contains(".")) {
            int end = mant.length();
            while (end > 0 && mant.charAt(end - 1) == '0') {
                end--;
            }
            if (end > 0 && mant.charAt(end - 1) == '.') {
                end--;
            }
            mant = mant.substring(0, end);
        }
        return mant + exp;
    }

    private static String normalizeExp(String s) {
        // Java: "1.230000e+02" -> JS: "1.23e+2"
        int e = s.indexOf('e');
        if (e < 0) {
            e = s.indexOf('E');
        }
        String mant = s.substring(0, e);
        String expPart = s.substring(e + 1);
        char sign = '+';
        if (expPart.startsWith("+") || expPart.startsWith("-")) {
            sign = expPart.charAt(0);
            expPart = expPart.substring(1);
        }
        expPart = expPart.replaceFirst("^0+(?=\\d)", "");
        return mant + "e" + sign + expPart;
    }

    private static String toPrecision(double d, int p) {
        if (d == 0) {
            if (p == 1) {
                return "0";
            }
            StringBuilder sb = new StringBuilder("0.");
            for (int k = 1; k < p; k++) {
                sb.append('0');
            }
            return sb.toString();
        }
        BigDecimal bd = new BigDecimal(d).round(new java.math.MathContext(p, RoundingMode.HALF_UP));
        int e = bd.precision() - bd.scale() - 1;
        if (e < -6 || e >= p) {
            return normalizeExp(String.format(java.util.Locale.ROOT, "%." + (p - 1) + "e", d));
        }
        return bd.toPlainString();
    }
}
