package org.aspose.pdf.engine.script.js.runtime;

import org.aspose.pdf.engine.script.js.interp.Interpreter;

/**
 * Base class for all callable ECMAScript objects (ECMA-262 3rd ed., sec 13, 15.3).
 *
 * <p>Concrete subclasses implement {@code [[Call]]} via {@link #call}. The
 * default {@code [[Construct]]} (sec 13.2.2) creates a fresh object whose
 * prototype is the function's {@code prototype} property, invokes
 * {@link #call} with that object as {@code this}, and returns the object
 * unless the call returns another object.</p>
 */
public abstract class JSFunction extends JSObject {

    /**
     * Creates a function object.
     *
     * @param functionPrototype the {@code Function.prototype} to link as this
     *                          object's prototype (may be {@code null} while
     *                          bootstrapping)
     */
    protected JSFunction(JSObject functionPrototype) {
        super(functionPrototype);
        setClassName("Function");
    }

    /**
     * {@code [[Call]]}: invokes the function.
     *
     * @param interp   the active interpreter (realm + coercions)
     * @param thisVal  the {@code this} binding
     * @param args     argument values
     * @return the return value
     */
    public abstract Object call(Interpreter interp, Object thisVal, Object[] args);

    /**
     * {@code [[Construct]]}: default object construction semantics.
     *
     * @param interp the active interpreter
     * @param args   argument values
     * @return the constructed object
     */
    public Object construct(Interpreter interp, Object[] args) {
        Object protoProp = get("prototype");
        JSObject proto = protoProp instanceof JSObject ? (JSObject) protoProp
                : interp.getRealm().objectPrototype;
        JSObject obj = new JSObject(proto);
        Object result = call(interp, obj, args);
        return result instanceof JSObject ? result : obj;
    }

    /** @return {@code true} (functions are always callable). */
    public boolean isCallable() {
        return true;
    }
}
