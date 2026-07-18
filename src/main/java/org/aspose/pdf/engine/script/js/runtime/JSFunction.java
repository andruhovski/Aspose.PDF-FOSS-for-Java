package org.aspose.pdf.engine.script.js.runtime;

import org.aspose.pdf.engine.script.js.interp.Interpreter;

/// Base class for all callable ECMAScript objects (ECMA-262 3rd ed., sec 13, 15.3).
///
/// Concrete subclasses implement `[[Call]]` via [#call]. The
/// default `[[Construct]]` (sec 13.2.2) creates a fresh object whose
/// prototype is the function's `prototype` property, invokes
/// [#call] with that object as `this`, and returns the object
/// unless the call returns another object.
public abstract class JSFunction extends JSObject {

    /// Creates a function object.
    ///
    /// @param functionPrototype the `Function.prototype` to link as this
    ///                          object's prototype (may be `null` while
    ///                          bootstrapping)
    protected JSFunction(JSObject functionPrototype) {
        super(functionPrototype);
        setClassName("Function");
    }

    /// `[[Call]]`: invokes the function.
    ///
    /// @param interp   the active interpreter (realm + coercions)
    /// @param thisVal  the `this` binding
    /// @param args     argument values
    /// @return the return value
    public abstract Object call(Interpreter interp, Object thisVal, Object[] args);

    /// `[[Construct]]`: default object construction semantics.
    ///
    /// @param interp the active interpreter
    /// @param args   argument values
    /// @return the constructed object
    public Object construct(Interpreter interp, Object[] args) {
        Object protoProp = get("prototype");
        JSObject proto = protoProp instanceof JSObject ? (JSObject) protoProp
                : interp.getRealm().objectPrototype;
        JSObject obj = new JSObject(proto);
        Object result = call(interp, obj, args);
        return result instanceof JSObject ? result : obj;
    }

    /// @return `true` (functions are always callable).
    public boolean isCallable() {
        return true;
    }
}
