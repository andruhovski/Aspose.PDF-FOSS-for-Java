package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.parser.Parser;
import org.aspose.pdf.engine.script.js.runtime.*;

/// Installs the ECMAScript 3 standard library into a [Realm]
/// (ECMA-262 3rd ed., sec 15). Core intrinsics (Object, Function, Boolean) and
/// the global functions live here; the larger libraries are delegated to
/// companion installers ([ArrayBuiltins], [StringBuiltins], etc.).
public final class Builtins {

    static final Object UNDEF = Undefined.INSTANCE;

    private Builtins() { }

    /// Returns `args[i]` or `undefined`.
    public static Object arg(Object[] args, int i) {
        return i < args.length ? args[i] : UNDEF;
    }

    /// Installs all standard built-ins into the realm.
    ///
    /// @param r the realm to populate
    public static void install(Realm r) {
        // --- bootstrap Object.prototype and Function.prototype ---
        JSObject objectProto = new JSObject(null);
        r.objectPrototype = objectProto;

        NativeFunction funcProto = new NativeFunction(null, "", 0, (i, t, a) -> UNDEF);
        funcProto.setPrototype(objectProto);
        r.functionPrototype = funcProto;

        installObject(r);
        installFunction(r);
        installBoolean(r);
        ArrayBuiltins.install(r);
        StringBuiltins.install(r);
        NumberBuiltins.install(r);
        MathBuiltins.install(r);
        ErrorBuiltins.install(r);
        RegExpBuiltins.install(r);
        DateBuiltins.install(r);
        installGlobalFunctions(r);

        // Expose constructors and values on the global object.
        JSObject g = r.globalObject;
        g.setPrototype(objectProto);
        g.defineHidden("Object", r.objectConstructor);
        g.defineHidden("Function", r.functionConstructor);
        g.defineHidden("Array", r.arrayConstructor);
        g.defineHidden("String", r.stringConstructor);
        g.defineHidden("Number", r.numberConstructor);
        g.defineHidden("Boolean", r.booleanConstructor);
        g.defineHidden("Date", r.dateConstructor);
        g.defineHidden("RegExp", r.regexpConstructor);
        g.define("NaN", Double.NaN, false, false, false);
        g.define("Infinity", Double.POSITIVE_INFINITY, false, false, false);
        g.define("undefined", UNDEF, false, false, false);
    }

    /* ----------------------------- Object ----------------------------- */

    private static void installObject(Realm r) {
        JSObject proto = r.objectPrototype;

        r.method(proto, "toString", 0, (i, t, a) -> {
            if (t == UNDEF) {
                return "[object Undefined]";
            }
            if (t == JSNull.NULL) {
                return "[object Null]";
            }
            JSObject o = i.toObject(t);
            return "[object " + o.getClassName() + "]";
        });
        r.method(proto, "toLocaleString", 0, (i, t, a) ->
                i.callFunction(i.toObject(t).get("toString"), t, new Object[0]));
        r.method(proto, "valueOf", 0, (i, t, a) -> i.toObject(t));
        r.method(proto, "hasOwnProperty", 1, (i, t, a) ->
                i.toObject(t).hasOwnProperty(i.toStringJS(arg(a, 0))));
        r.method(proto, "isPrototypeOf", 1, (i, t, a) -> {
            Object v = arg(a, 0);
            if (!(v instanceof JSObject)) {
                return false;
            }
            JSObject self = i.toObject(t);
            JSObject p = ((JSObject) v).getPrototype();
            while (p != null) {
                if (p == self) {
                    return true;
                }
                p = p.getPrototype();
            }
            return false;
        });
        r.method(proto, "propertyIsEnumerable", 1, (i, t, a) -> {
            JSObject o = i.toObject(t);
            JSObject.Property p = o.getOwnProperty(i.toStringJS(arg(a, 0)));
            return p != null && p.enumerable;
        });

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Object", 1, (i, t, a) -> {
            Object v = arg(a, 0);
            if (v == UNDEF || v == JSNull.NULL) {
                return r.newObject();
            }
            return i.toObject(v);
        }).withConstructor((i, a) -> {
            Object v = arg(a, 0);
            if (v instanceof JSObject) {
                return v;
            }
            if (v == UNDEF || v == JSNull.NULL) {
                return r.newObject();
            }
            return i.toObject(v);
        });
        link(ctor, proto);
        r.objectConstructor = ctor;
    }

    /* ---------------------------- Function ---------------------------- */

    private static void installFunction(Realm r) {
        JSObject proto = r.functionPrototype;

        r.method(proto, "toString", 0, (i, t, a) -> {
            if (t instanceof JSFunction) {
                Object name = ((JSObject) t).get("name");
                return "function " + (name instanceof String ? name : "")
                        + "() { [native code] }";
            }
            throw r.typeError("Function.prototype.toString is not generic");
        });
        r.method(proto, "call", 1, (i, t, a) -> {
            Object thisArg = arg(a, 0);
            Object[] rest = sliceArgs(a, 1);
            return i.callFunction(t, thisArg, rest);
        });
        r.method(proto, "apply", 2, (i, t, a) -> {
            Object thisArg = arg(a, 0);
            Object argArray = arg(a, 1);
            Object[] callArgs;
            if (argArray == UNDEF || argArray == JSNull.NULL) {
                callArgs = new Object[0];
            } else if (argArray instanceof JSObject) {
                JSObject ao = (JSObject) argArray;
                int len = (int) i.toNumber(ao.get("length"));
                callArgs = new Object[Math.max(0, len)];
                for (int k = 0; k < callArgs.length; k++) {
                    callArgs[k] = ao.get(Integer.toString(k));
                }
            } else {
                throw r.typeError("apply: second argument must be an array");
            }
            return i.callFunction(t, thisArg, callArgs);
        });

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Function", 1, (i, t, a) ->
                buildFunction(r, i, a)).withConstructor((i, a) -> buildFunction(r, i, a));
        link(ctor, proto);
        r.functionConstructor = ctor;
    }

    private static Object buildFunction(Realm r, Interpreter i, Object[] a) {
        StringBuilder params = new StringBuilder();
        String body = "";
        if (a.length > 0) {
            for (int k = 0; k < a.length - 1; k++) {
                if (k > 0) {
                    params.append(',');
                }
                params.append(i.toStringJS(a[k]));
            }
            body = i.toStringJS(a[a.length - 1]);
        }
        String src = "(function anonymous(" + params + "){" + body + "})";
        Node.Program prog = Parser.parse(src);
        Node.ExprStmt es = (Node.ExprStmt) prog.body.get(0);
        return i.evalExpr(es.expression, i.getGlobalScope());
    }

    /* ---------------------------- Boolean ----------------------------- */

    private static void installBoolean(Realm r) {
        JSObject proto = new JSObject(r.objectPrototype);
        proto.setClassName("Boolean");
        proto.primitiveValue = Boolean.FALSE;
        r.booleanPrototype = proto;

        r.method(proto, "toString", 0, (i, t, a) -> String.valueOf(thisBoolean(r, t)));
        r.method(proto, "valueOf", 0, (i, t, a) -> thisBoolean(r, t));

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "Boolean", 1, (i, t, a) ->
                Types.toBoolean(arg(a, 0))).withConstructor((i, a) -> {
            JSObject o = new JSObject(proto);
            o.setClassName("Boolean");
            o.primitiveValue = Types.toBoolean(arg(a, 0));
            return o;
        });
        link(ctor, proto);
        r.booleanConstructor = ctor;
    }

    private static boolean thisBoolean(Realm r, Object t) {
        if (t instanceof Boolean) {
            return (Boolean) t;
        }
        if (t instanceof JSObject && ((JSObject) t).primitiveValue instanceof Boolean) {
            return (Boolean) ((JSObject) t).primitiveValue;
        }
        throw r.typeError("Boolean.prototype method called on incompatible receiver");
    }

    /* ------------------------- global functions ----------------------- */

    private static void installGlobalFunctions(Realm r) {
        JSObject g = r.globalObject;

        r.method(g, "parseInt", 2, (i, t, a) -> parseInt(i.toStringJS(arg(a, 0)),
                arg(a, 1) == UNDEF ? 0 : (int) i.toNumber(arg(a, 1))));
        r.method(g, "parseFloat", 1, (i, t, a) -> parseFloat(i.toStringJS(arg(a, 0))));
        r.method(g, "isNaN", 1, (i, t, a) -> Double.isNaN(i.toNumber(arg(a, 0))));
        r.method(g, "isFinite", 1, (i, t, a) -> {
            double d = i.toNumber(arg(a, 0));
            return !Double.isNaN(d) && !Double.isInfinite(d);
        });
        r.method(g, "eval", 1, (i, t, a) -> {
            Object src = arg(a, 0);
            if (!(src instanceof String)) {
                return src;
            }
            Node.Program prog = Parser.parse((String) src);
            return i.run(prog);
        });

        r.method(g, "encodeURI", 1, (i, t, a) ->
                UriCoding.encode(i.toStringJS(arg(a, 0)), UriCoding.URI_UNRESERVED_RESERVED));
        r.method(g, "encodeURIComponent", 1, (i, t, a) ->
                UriCoding.encode(i.toStringJS(arg(a, 0)), UriCoding.COMPONENT_UNRESERVED));
        r.method(g, "decodeURI", 1, (i, t, a) ->
                UriCoding.decode(r, i.toStringJS(arg(a, 0)), UriCoding.URI_RESERVED_HASH));
        r.method(g, "decodeURIComponent", 1, (i, t, a) ->
                UriCoding.decode(r, i.toStringJS(arg(a, 0)), ""));
    }

    private static Object parseInt(String s, int radix) {
        s = JSNumber.trimJs(s);
        int idx = 0;
        int sign = 1;
        if (idx < s.length() && (s.charAt(idx) == '+' || s.charAt(idx) == '-')) {
            if (s.charAt(idx) == '-') {
                sign = -1;
            }
            idx++;
        }
        if (radix == 0) {
            if (idx + 1 < s.length() && s.charAt(idx) == '0'
                    && (s.charAt(idx + 1) == 'x' || s.charAt(idx + 1) == 'X')) {
                idx += 2;
                radix = 16;
            } else {
                radix = 10;
            }
        } else if (radix == 16) {
            if (idx + 1 < s.length() && s.charAt(idx) == '0'
                    && (s.charAt(idx + 1) == 'x' || s.charAt(idx + 1) == 'X')) {
                idx += 2;
            }
        }
        if (radix < 2 || radix > 36) {
            return Double.NaN;
        }
        int start = idx;
        double value = 0;
        while (idx < s.length()) {
            int d = Character.digit(s.charAt(idx), radix);
            if (d < 0) {
                break;
            }
            value = value * radix + d;
            idx++;
        }
        if (idx == start) {
            return Double.NaN;
        }
        return sign * value;
    }

    private static Object parseFloat(String s) {
        s = JSNumber.trimJs(s);
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^[+-]?(Infinity|(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?)")
                .matcher(s);
        if (!m.find()) {
            return Double.NaN;
        }
        String g = m.group();
        if (g.endsWith("Infinity")) {
            return g.startsWith("-") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        try {
            return Double.parseDouble(g);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /* ------------------------------ helpers --------------------------- */

    /// Links a constructor and prototype: ctor.prototype = proto, proto.constructor = ctor.
    static void link(JSFunction ctor, JSObject proto) {
        ctor.define("prototype", proto, false, false, false);
        proto.defineHidden("constructor", ctor);
    }

    /// Returns a copy of `args` starting at index `from`.
    static Object[] sliceArgs(Object[] args, int from) {
        if (from >= args.length) {
            return new Object[0];
        }
        Object[] out = new Object[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    /// Coerces a `this` value carrying a string primitive (own or wrapped).
    static JSArray asArray(Object t) {
        return t instanceof JSArray ? (JSArray) t : null;
    }
}
