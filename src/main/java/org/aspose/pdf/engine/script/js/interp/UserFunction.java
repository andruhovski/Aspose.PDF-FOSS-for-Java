package org.aspose.pdf.engine.script.js.interp;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.runtime.JSFunction;
import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Scope;

import java.util.List;

/// A function defined in ECMAScript source (a function declaration or
/// expression). Captures its defining scope for closures; execution is
/// delegated to the [Interpreter].
public final class UserFunction extends JSFunction {

    /// Source name (`""` for anonymous).
    public final String name;
    /// Formal parameter names.
    public final List<String> params;
    /// Function body block.
    public final Node.Block body;
    /// The lexical scope in which the function was created.
    public final Scope closure;

    /// Creates a user function.
    ///
    /// @param functionPrototype`Function.prototype`
    /// @param name              source name or `""`
    /// @param params            formal parameters
    /// @param body              body block
    /// @param closure           defining scope
    public UserFunction(JSObject functionPrototype, String name, List<String> params,
                        Node.Block body, Scope closure) {
        super(functionPrototype);
        this.name = name == null ? "" : name;
        this.params = params;
        this.body = body;
        this.closure = closure;
        define("length", (double) params.size(), false, false, false);
    }

    @Override
    public Object call(Interpreter interp, Object thisVal, Object[] args) {
        return interp.callUserFunction(this, thisVal, args);
    }
}
