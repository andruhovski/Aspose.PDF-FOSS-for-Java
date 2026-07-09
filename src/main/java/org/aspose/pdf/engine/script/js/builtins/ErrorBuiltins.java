package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.runtime.JSFunction;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.NativeFunction;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

/**
 * Installs the Error constructor and its native subtypes (ECMA-262 3rd ed.,
 * sec 15.11): EvalError, RangeError, ReferenceError, SyntaxError, TypeError
 * and URIError.
 */
final class ErrorBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private ErrorBuiltins() { }

    static void install(Realm r) {
        r.errorPrototype = makeErrorType(r, "Error", r.objectPrototype, true);
        r.errorConstructor = (JSFunction) r.globalObject.get("Error");

        r.evalErrorPrototype = makeErrorType(r, "EvalError", r.errorPrototype, false);
        r.rangeErrorPrototype = makeErrorType(r, "RangeError", r.errorPrototype, false);
        r.referenceErrorPrototype = makeErrorType(r, "ReferenceError", r.errorPrototype, false);
        r.syntaxErrorPrototype = makeErrorType(r, "SyntaxError", r.errorPrototype, false);
        r.typeErrorPrototype = makeErrorType(r, "TypeError", r.errorPrototype, false);
        r.uriErrorPrototype = makeErrorType(r, "URIError", r.errorPrototype, false);
    }

    private static JSObject makeErrorType(Realm r, String name, JSObject parentProto, boolean isBase) {
        JSObject proto = new JSObject(parentProto);
        proto.setClassName("Error");
        proto.defineHidden("name", name);
        proto.defineHidden("message", "");

        if (isBase) {
            r.method(proto, "toString", 0, (i, t, a) -> {
                if (!(t instanceof JSObject)) {
                    throw r.typeError("Error.prototype.toString called on non-object");
                }
                JSObject o = (JSObject) t;
                Object nm = o.get("name");
                String nameStr = nm == UNDEF ? "Error" : i.toStringJS(nm);
                Object msg = o.get("message");
                String msgStr = msg == UNDEF ? "" : i.toStringJS(msg);
                if (msgStr.isEmpty()) {
                    return nameStr;
                }
                if (nameStr.isEmpty()) {
                    return msgStr;
                }
                return nameStr + ": " + msgStr;
            });
        }

        NativeFunction ctor = new NativeFunction(r.functionPrototype, name, 1, (i, t, a) ->
                buildError(i, proto, a)).withConstructor((i, a) -> buildError(i, proto, a));
        Builtins.link(ctor, proto);
        r.globalObject.defineHidden(name, ctor);
        return proto;
    }

    private static Object buildError(org.aspose.pdf.engine.script.js.interp.Interpreter i,
                                     JSObject proto, Object[] a) {
        JSObject e = new JSObject(proto);
        e.setClassName("Error");
        Object msg = Builtins.arg(a, 0);
        if (msg != UNDEF) {
            e.defineHidden("message", i.toStringJS(msg));
        }
        return e;
    }
}
