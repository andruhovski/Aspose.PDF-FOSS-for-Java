package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

/**
 * Installs the Math object (ECMA-262 3rd ed., sec 15.8): all ES3 constants and
 * functions. Delegates to {@link java.lang.Math}.
 */
final class MathBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private MathBuiltins() { }

    static void install(Realm r) {
        JSObject math = new JSObject(r.objectPrototype);
        math.setClassName("Math");

        math.define("E", Math.E, false, false, false);
        math.define("LN10", Math.log(10), false, false, false);
        math.define("LN2", Math.log(2), false, false, false);
        math.define("LOG2E", 1 / Math.log(2), false, false, false);
        math.define("LOG10E", 1 / Math.log(10), false, false, false);
        math.define("PI", Math.PI, false, false, false);
        math.define("SQRT1_2", Math.sqrt(0.5), false, false, false);
        math.define("SQRT2", Math.sqrt(2), false, false, false);

        r.method(math, "abs", 1, (i, t, a) -> Math.abs(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "acos", 1, (i, t, a) -> Math.acos(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "asin", 1, (i, t, a) -> Math.asin(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "atan", 1, (i, t, a) -> Math.atan(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "atan2", 2, (i, t, a) ->
                Math.atan2(i.toNumber(Builtins.arg(a, 0)), i.toNumber(Builtins.arg(a, 1))));
        r.method(math, "ceil", 1, (i, t, a) -> Math.ceil(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "cos", 1, (i, t, a) -> Math.cos(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "exp", 1, (i, t, a) -> Math.exp(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "floor", 1, (i, t, a) -> Math.floor(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "log", 1, (i, t, a) -> Math.log(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "pow", 2, (i, t, a) ->
                Math.pow(i.toNumber(Builtins.arg(a, 0)), i.toNumber(Builtins.arg(a, 1))));
        r.method(math, "random", 0, (i, t, a) -> Math.random());
        r.method(math, "round", 1, (i, t, a) -> {
            double d = i.toNumber(Builtins.arg(a, 0));
            if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
                return d;
            }
            return Math.floor(d + 0.5);
        });
        r.method(math, "sin", 1, (i, t, a) -> Math.sin(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "sqrt", 1, (i, t, a) -> Math.sqrt(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "tan", 1, (i, t, a) -> Math.tan(i.toNumber(Builtins.arg(a, 0))));
        r.method(math, "max", 2, (i, t, a) -> {
            double m = Double.NEGATIVE_INFINITY;
            for (Object v : a) {
                double d = i.toNumber(v);
                if (Double.isNaN(d)) {
                    return Double.NaN;
                }
                m = Math.max(m, d);
            }
            return m;
        });
        r.method(math, "min", 2, (i, t, a) -> {
            double m = Double.POSITIVE_INFINITY;
            for (Object v : a) {
                double d = i.toNumber(v);
                if (Double.isNaN(d)) {
                    return Double.NaN;
                }
                m = Math.min(m, d);
            }
            return m;
        });

        r.globalObject.defineHidden("Math", math);
    }
}
