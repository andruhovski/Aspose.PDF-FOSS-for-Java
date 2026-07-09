package org.aspose.pdf.engine.script.js.runtime;

import org.aspose.pdf.engine.script.js.interp.Interpreter;

/**
 * A built-in function implemented in Java and backed by a lambda.
 *
 * <p>Used for every standard-library method and constructor. A separate
 * {@link Constructor} hook may be supplied to customise {@code [[Construct]]};
 * otherwise the default {@link JSFunction#construct} behaviour applies.</p>
 */
public class NativeFunction extends JSFunction {

    /** Functional interface for a native {@code [[Call]]}. */
    public interface Native {
        /**
         * @param interp  active interpreter
         * @param thisVal {@code this} binding
         * @param args    arguments
         * @return result value
         */
        Object invoke(Interpreter interp, Object thisVal, Object[] args);
    }

    /** Functional interface for a native {@code [[Construct]]}. */
    public interface Constructor {
        /**
         * @param interp active interpreter
         * @param args   arguments
         * @return constructed object
         */
        Object construct(Interpreter interp, Object[] args);
    }

    private final Native body;
    private Constructor constructor;

    /**
     * Creates a native function.
     *
     * @param funcProto {@code Function.prototype}
     * @param name      function name (the {@code name}/length surface)
     * @param length    declared arity
     * @param body      the call implementation
     */
    public NativeFunction(JSObject funcProto, String name, int length, Native body) {
        super(funcProto);
        this.body = body;
        define("length", (double) length, false, false, false);
        define("name", name == null ? "" : name, false, false, false);
    }

    /**
     * Sets a custom {@code [[Construct]]} implementation.
     *
     * @param c constructor hook
     * @return this (for chaining)
     */
    public NativeFunction withConstructor(Constructor c) {
        this.constructor = c;
        return this;
    }

    @Override
    public Object call(Interpreter interp, Object thisVal, Object[] args) {
        return body.invoke(interp, thisVal, args);
    }

    @Override
    public Object construct(Interpreter interp, Object[] args) {
        if (constructor != null) {
            return constructor.construct(interp, args);
        }
        return super.construct(interp, args);
    }
}
