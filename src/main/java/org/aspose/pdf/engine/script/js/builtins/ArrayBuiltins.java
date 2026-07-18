package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.runtime.*;

import java.util.ArrayList;
import java.util.List;

/// Installs Array and Array.prototype (ECMA-262 3rd ed., sec 15.4). Only the
/// ES3 method set is provided: `toString`, `join`, `concat`,
/// `pop`, `push`, `reverse`, `shift`, `slice`,
/// `sort`, `splice` and `unshift`. ES5 iterators
/// (`indexOf`, `forEach`, `map`, ...) are intentionally
/// excluded.
final class ArrayBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private ArrayBuiltins() { }

    static void install(Realm r) {
        JSArray proto = new JSArray(r.objectPrototype);
        r.arrayPrototype = proto;

        r.method(proto, "toString", 0, (i, t, a) -> join(i, i.toObject(t), ","));
        r.method(proto, "join", 1, (i, t, a) -> {
            String sep = Builtins.arg(a, 0) == UNDEF ? "," : i.toStringJS(a[0]);
            return join(i, i.toObject(t), sep);
        });
        r.method(proto, "push", 1, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            for (Object v : a) {
                o.put(Long.toString(len++), v);
            }
            o.put("length", (double) len);
            return (double) len;
        });
        r.method(proto, "pop", 0, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            if (len == 0) {
                o.put("length", 0.0);
                return UNDEF;
            }
            String idx = Long.toString(len - 1);
            Object v = o.get(idx);
            o.delete(idx);
            o.put("length", (double) (len - 1));
            return v;
        });
        r.method(proto, "shift", 0, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            if (len == 0) {
                o.put("length", 0.0);
                return UNDEF;
            }
            Object first = o.get("0");
            for (long k = 1; k < len; k++) {
                String from = Long.toString(k);
                String to = Long.toString(k - 1);
                if (o.hasProperty(from)) {
                    o.put(to, o.get(from));
                } else {
                    o.delete(to);
                }
            }
            o.delete(Long.toString(len - 1));
            o.put("length", (double) (len - 1));
            return first;
        });
        r.method(proto, "unshift", 1, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            for (long k = len; k > 0; k--) {
                String from = Long.toString(k - 1);
                String to = Long.toString(k - 1 + a.length);
                if (o.hasProperty(from)) {
                    o.put(to, o.get(from));
                } else {
                    o.delete(to);
                }
            }
            for (int j = 0; j < a.length; j++) {
                o.put(Integer.toString(j), a[j]);
            }
            long newLen = len + a.length;
            o.put("length", (double) newLen);
            return (double) newLen;
        });
        r.method(proto, "reverse", 0, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            for (long k = 0; k < len / 2; k++) {
                String lo = Long.toString(k);
                String hi = Long.toString(len - 1 - k);
                Object a1 = o.get(lo);
                Object a2 = o.get(hi);
                o.put(lo, a2);
                o.put(hi, a1);
            }
            return o;
        });
        r.method(proto, "slice", 2, (i, t, a) -> {
            JSObject o = i.toObject(t);
            long len = len(i, o);
            long start = clampIndex(i, Builtins.arg(a, 0), len, 0);
            long end = Builtins.arg(a, 1) == UNDEF ? len : clampIndex(i, a[1], len, len);
            JSArray out = r.newArray();
            long n = 0;
            for (long k = start; k < end; k++) {
                String idx = Long.toString(k);
                if (o.hasProperty(idx)) {
                    out.put(Long.toString(n), o.get(idx));
                }
                n++;
            }
            out.setLength(n);
            return out;
        });
        r.method(proto, "concat", 1, (i, t, a) -> {
            JSArray out = r.newArray();
            long n = 0;
            n = concatInto(i, out, i.toObject(t), n);
            for (Object v : a) {
                if (v instanceof JSArray) {
                    n = concatInto(i, out, (JSObject) v, n);
                } else {
                    out.put(Long.toString(n++), v);
                }
            }
            out.setLength(n);
            return out;
        });
        r.method(proto, "splice", 2, (i, t, a) -> splice(r, i, i.toObject(t), a));
        r.method(proto, "sort", 1, (i, t, a) -> sort(r, i, i.toObject(t), Builtins.arg(a, 0)));

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Array", 1, (i, t, a) ->
                construct(r, a)).withConstructor((i, a) -> construct(r, a));
        Builtins.link(ctor, proto);
        r.method(ctor, "isArray", 1, (i, t, a) -> Builtins.arg(a, 0) instanceof JSArray);
        r.arrayConstructor = ctor;
    }

    private static Object construct(Realm r, Object[] a) {
        JSArray arr = r.newArray();
        if (a.length == 1 && a[0] instanceof Double) {
            double d = (Double) a[0];
            long len = (long) d;
            if (len != d || len < 0 || len > 4294967295L) {
                throw r.rangeError("Invalid array length");
            }
            arr.setLength(len);
        } else {
            for (int k = 0; k < a.length; k++) {
                arr.put(Integer.toString(k), a[k]);
            }
            arr.setLength(a.length);
        }
        return arr;
    }

    private static long len(Interpreter i, JSObject o) {
        return (long) JsLen(i.toNumber(o.get("length")));
    }

    private static double JsLen(double d) {
        if (Double.isNaN(d) || d < 0) {
            return 0;
        }
        return Math.floor(d);
    }

    private static long clampIndex(Interpreter i, Object v, long len, long dflt) {
        if (v == UNDEF) {
            return dflt;
        }
        double d = i.toNumber(v);
        if (Double.isNaN(d)) {
            return 0;
        }
        long rel = (long) (d < 0 ? Math.ceil(d) : Math.floor(d));
        if (rel < 0) {
            return Math.max(len + rel, 0);
        }
        return Math.min(rel, len);
    }

    private static String join(Interpreter i, JSObject o, String sep) {
        long len = len(i, o);
        StringBuilder sb = new StringBuilder();
        for (long k = 0; k < len; k++) {
            if (k > 0) {
                sb.append(sep);
            }
            Object v = o.get(Long.toString(k));
            if (v != UNDEF && v != JSNull.NULL) {
                sb.append(i.toStringJS(v));
            }
        }
        return sb.toString();
    }

    private static long concatInto(Interpreter i, JSArray out, JSObject src, long n) {
        long len = len(i, src);
        for (long k = 0; k < len; k++) {
            String idx = Long.toString(k);
            if (src.hasProperty(idx)) {
                out.put(Long.toString(n), src.get(idx));
            }
            n++;
        }
        return n;
    }

    private static Object splice(Realm r, Interpreter i, JSObject o, Object[] a) {
        long len = len(i, o);
        long start = clampIndex(i, Builtins.arg(a, 0), len, 0);
        long delCount = a.length < 2 ? (len - start)
                : Math.max(0, Math.min((long) i.toNumber(a[1]), len - start));
        JSArray removed = r.newArray();
        for (long k = 0; k < delCount; k++) {
            String idx = Long.toString(start + k);
            if (o.hasProperty(idx)) {
                removed.put(Long.toString(k), o.get(idx));
            }
        }
        removed.setLength(delCount);

        int insertCount = Math.max(0, a.length - 2);
        if (insertCount < delCount) {
            for (long k = start; k < len - delCount; k++) {
                String from = Long.toString(k + delCount);
                String to = Long.toString(k + insertCount);
                if (o.hasProperty(from)) {
                    o.put(to, o.get(from));
                } else {
                    o.delete(to);
                }
            }
            for (long k = len; k > len - delCount + insertCount; k--) {
                o.delete(Long.toString(k - 1));
            }
        } else if (insertCount > delCount) {
            for (long k = len - delCount; k > start; k--) {
                String from = Long.toString(k + delCount - 1);
                String to = Long.toString(k + insertCount - 1);
                if (o.hasProperty(from)) {
                    o.put(to, o.get(from));
                } else {
                    o.delete(to);
                }
            }
        }
        for (int k = 0; k < insertCount; k++) {
            o.put(Long.toString(start + k), a[k + 2]);
        }
        o.put("length", (double) (len - delCount + insertCount));
        return removed;
    }

    private static Object sort(Realm r, Interpreter i, JSObject o, Object cmp) {
        long len = len(i, o);
        List<Object> items = new ArrayList<>();
        List<Boolean> present = new ArrayList<>();
        for (long k = 0; k < len; k++) {
            String idx = Long.toString(k);
            present.add(o.hasProperty(idx));
            items.add(o.get(idx));
        }
        final JSFunction comparator = cmp instanceof JSFunction ? (JSFunction) cmp : null;
        // Collect defined (non-hole, non-undefined) values, sort, then holes/undefined trail.
        List<Object> defined = new ArrayList<>();
        int undefinedCount = 0;
        int holeCount = 0;
        for (int k = 0; k < items.size(); k++) {
            if (!present.get(k)) {
                holeCount++;
            } else if (items.get(k) == UNDEF) {
                undefinedCount++;
            } else {
                defined.add(items.get(k));
            }
        }
        defined.sort((x, y) -> {
            if (comparator != null) {
                double d = i.toNumber(comparator.call(i, UNDEF, new Object[]{x, y}));
                return d < 0 ? -1 : (d > 0 ? 1 : 0);
            }
            return i.toStringJS(x).compareTo(i.toStringJS(y));
        });
        long w = 0;
        for (Object v : defined) {
            o.put(Long.toString(w++), v);
        }
        for (int k = 0; k < undefinedCount; k++) {
            o.put(Long.toString(w++), UNDEF);
        }
        for (int k = 0; k < holeCount; k++) {
            o.delete(Long.toString(w++));
        }
        return o;
    }
}
