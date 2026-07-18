package org.aspose.pdf.engine.script.js;

import org.aspose.pdf.engine.script.js.ast.Node;
import org.aspose.pdf.engine.script.js.builtins.Realm;
import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.parser.Parser;
import org.aspose.pdf.engine.script.js.runtime.JSObject;

/// Public entry point of the self-contained ECMAScript 3 engine.
///
/// An `Engine` owns one [Realm] (global object + intrinsics) and
/// an [Interpreter]. It is the only type external callers need; the
/// lexer, parser, runtime and interpreter are internal. Host objects (the
/// future AcroForm / XFA / Acrobat JS surfaces) will be injected onto the
/// global object via [#getGlobalObject()] — this build injects
/// nothing, keeping the seam open with no PDF coupling.
///
/// ```
/// Object result = Engine.eval("var x = 2; x * 21;"); // -> 42.0
/// ```
public final class Engine {

    private final Realm realm;
    private final Interpreter interpreter;

    /// Creates an engine with a fresh standard realm.
    public Engine() {
        this.realm = Realm.createStandard();
        this.interpreter = new Interpreter(realm);
    }

    /// Parses and evaluates a program in this engine's global scope.
    ///
    /// @param source ES3 source text
    /// @return the completion value (the value of the last statement)
    public Object evaluate(String source) {
        Node.Program program = Parser.parse(source);
        return interpreter.run(program);
    }

    /// @return the global object, for host-object injection (the integration
    ///         seam for later scripting consumers)
    public JSObject getGlobalObject() {
        return realm.globalObject;
    }

    /// @return the realm backing this engine.
    public Realm getRealm() {
        return realm;
    }

    /// @return the interpreter backing this engine.
    public Interpreter getInterpreter() {
        return interpreter;
    }

    /// Convenience one-shot evaluation in a fresh engine.
    ///
    /// @param source ES3 source text
    /// @return the completion value
    public static Object eval(String source) {
        return new Engine().evaluate(source);
    }
}
