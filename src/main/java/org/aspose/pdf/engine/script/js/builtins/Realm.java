package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.interp.JSException;
import org.aspose.pdf.engine.script.js.runtime.*;

/// The set of intrinsic objects for one execution environment (ECMA-262 3rd
/// ed., sec 15): the global object plus every standard prototype and
/// constructor. Built once and shared by an [Interpreter].
public final class Realm {

    /// The global object (also the variable object of global code).
    public final JSObject globalObject = new JSObject();

    // Core prototypes.
    public JSObject objectPrototype;
    public JSObject functionPrototype;
    public JSObject arrayPrototype;
    public JSObject stringPrototype;
    public JSObject numberPrototype;
    public JSObject booleanPrototype;
    public JSObject datePrototype;
    public JSObject regexpPrototype;

    // Error prototypes.
    public JSObject errorPrototype;
    public JSObject evalErrorPrototype;
    public JSObject rangeErrorPrototype;
    public JSObject referenceErrorPrototype;
    public JSObject syntaxErrorPrototype;
    public JSObject typeErrorPrototype;
    public JSObject uriErrorPrototype;

    // Constructors.
    public JSFunction objectConstructor;
    public JSFunction functionConstructor;
    public JSFunction arrayConstructor;
    public JSFunction stringConstructor;
    public JSFunction numberConstructor;
    public JSFunction booleanConstructor;
    public JSFunction dateConstructor;
    public JSFunction regexpConstructor;
    public JSFunction errorConstructor;

    /// Builds a fully populated realm with all standard built-ins installed.
    ///
    /// @return a ready realm
    public static Realm createStandard() {
        Realm r = new Realm();
        Builtins.install(r);
        return r;
    }

    /* ----------------------------- factories -------------------------- */

    /// @return a fresh plain object.
    public JSObject newObject() {
        return new JSObject(objectPrototype);
    }

    /// @return a fresh empty array.
    public JSArray newArray() {
        return new JSArray(arrayPrototype);
    }

    /// Creates an array populated from the given values.
    ///
    /// @param values element values
    /// @return the array
    public JSArray newArray(Object... values) {
        JSArray a = new JSArray(arrayPrototype);
        for (int i = 0; i < values.length; i++) {
            a.put(Integer.toString(i), values[i]);
        }
        return a;
    }

    /// Helper to register a non-enumerable method on a target object.
    ///
    /// @param target target object
    /// @param name   method name
    /// @param length declared arity
    /// @param body   implementation
    public void method(JSObject target, String name, int length, NativeFunction.Native body) {
        target.defineHidden(name, new NativeFunction(functionPrototype, name, length, body));
    }

    /// Builds a native error object of the given prototype.
    ///
    /// @param proto    the error prototype (e.g. [#typeErrorPrototype])
    /// @param name     error name
    /// @param message  message text
    /// @return the error object
    public JSObject makeError(JSObject proto, String name, String message) {
        JSObject e = new JSObject(proto);
        e.setClassName("Error");
        if (message != null) {
            e.defineHidden("message", message);
        }
        return e;
    }

    /// Builds and wraps a TypeError as a [JSException] ready to throw.
    public JSException typeError(String message) {
        return new JSException(makeError(typeErrorPrototype, "TypeError", message));
    }

    /// Builds and wraps a RangeError as a [JSException] ready to throw.
    public JSException rangeError(String message) {
        return new JSException(makeError(rangeErrorPrototype, "RangeError", message));
    }

    /// Builds and wraps a ReferenceError as a [JSException] ready to throw.
    public JSException referenceError(String message) {
        return new JSException(makeError(referenceErrorPrototype, "ReferenceError", message));
    }

    /// Builds and wraps a SyntaxError as a [JSException] ready to throw.
    public JSException syntaxError(String message) {
        return new JSException(makeError(syntaxErrorPrototype, "SyntaxError", message));
    }

    /// Builds and wraps a URIError as a [JSException] ready to throw.
    public JSException makeUriError(String message) {
        return new JSException(makeError(uriErrorPrototype, "URIError", message));
    }

    /// @return undefined singleton (convenience).
    public static Object undef() {
        return Undefined.INSTANCE;
    }

    /// @return null singleton (convenience).
    public static Object nul() {
        return JSNull.NULL;
    }
}
