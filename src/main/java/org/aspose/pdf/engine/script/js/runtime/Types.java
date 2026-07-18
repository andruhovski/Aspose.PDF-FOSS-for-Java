package org.aspose.pdf.engine.script.js.runtime;

/// Pure ECMAScript abstract operations that do not require calling user code:
/// `typeof`, ToBoolean, primitive coercions and strict equality
/// (ECMA-262 3rd ed., sec 9, 11.9.6). Operations that may invoke
/// `valueOf`/`toString` (ToPrimitive, ToNumber/ToString of objects,
/// abstract `==`) live on the interpreter.
public final class Types {

    private Types() { }

    /// The `typeof` operator (sec 11.4.3).
    public static String typeOf(Object v) {
        if (v == Undefined.INSTANCE) {
            return "undefined";
        }
        if (v == JSNull.NULL) {
            return "object";
        }
        if (v instanceof Boolean) {
            return "boolean";
        }
        if (v instanceof Double) {
            return "number";
        }
        if (v instanceof String) {
            return "string";
        }
        if (v instanceof JSFunction) {
            return "function";
        }
        return "object";
    }

    /// ToBoolean (sec 9.2).
    public static boolean toBoolean(Object v) {
        if (v == Undefined.INSTANCE || v == JSNull.NULL) {
            return false;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Double) {
            double d = (Double) v;
            return d != 0 && !Double.isNaN(d);
        }
        if (v instanceof String) {
            return !((String) v).isEmpty();
        }
        // objects are always truthy (§9.2) — except a host null-object that opts out via isFalsy()
        // (the XFA absent node, so `if (node)` / `while (node.child)` guards terminate). Inert for all
        // standard objects (default isFalsy() == false).
        if (v instanceof JSObject && ((JSObject) v).isFalsy()) {
            return false;
        }
        return true;
    }

    /// @return `true` if the value is a callable function object.
    public static boolean isCallable(Object v) {
        return v instanceof JSFunction;
    }

    /// Primitive-only ToString (no object coercion).
    public static String primitiveToString(Object v) {
        if (v == Undefined.INSTANCE) {
            return "undefined";
        }
        if (v == JSNull.NULL) {
            return "null";
        }
        if (v instanceof Boolean) {
            return v.toString();
        }
        if (v instanceof Double) {
            return JSNumber.toStr((Double) v);
        }
        if (v instanceof String) {
            return (String) v;
        }
        return v.toString();
    }

    /// Primitive-only ToNumber; objects yield `NaN` (caller coerces).
    public static double toNumberPrimitiveOrNaN(Object v) {
        if (v == Undefined.INSTANCE) {
            return Double.NaN;
        }
        if (v == JSNull.NULL) {
            return 0;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1 : 0;
        }
        if (v instanceof Double) {
            return (Double) v;
        }
        if (v instanceof String) {
            return JSNumber.fromString((String) v);
        }
        return Double.NaN;
    }

    /// Strict equality `===` (sec 11.9.6).
    public static boolean strictEquals(Object a, Object b) {
        if (a == Undefined.INSTANCE && b == Undefined.INSTANCE) {
            return true;
        }
        if (a == JSNull.NULL && b == JSNull.NULL) {
            return true;
        }
        if (a instanceof Double && b instanceof Double) {
            double x = (Double) a;
            double y = (Double) b;
            return x == y; // NaN != NaN, +0 == -0 (Java == matches)
        }
        if (a instanceof String && b instanceof String) {
            return a.equals(b);
        }
        if (a instanceof Boolean && b instanceof Boolean) {
            return a.equals(b);
        }
        return a == b; // object identity (and the singletons)
    }

    /// Convenience: wrap a Java boolean as a JS value.
    public static Object bool(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    /// Convenience: wrap a Java double as a JS value.
    public static Object num(double d) {
        return d;
    }
}
