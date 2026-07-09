package org.aspose.pdf.engine.script.js.runtime;

/**
 * A lexical environment / scope-chain node (ECMA-262 3rd ed., sec 10.1.4).
 *
 * <p>Each scope wraps a {@link JSObject} "variable object" and a link to the
 * enclosing scope. Three kinds exist: the global scope (record is the global
 * object), a function activation scope (record is an activation object with a
 * {@code null} prototype so inherited names do not leak), and a {@code with}
 * scope (record is the supplied object, whose prototype chain participates in
 * name resolution).</p>
 */
public final class Scope {

    /** The variable object holding this scope's bindings. */
    public final JSObject record;
    /** Enclosing scope, or {@code null} for the global scope. */
    public final Scope parent;
    /** Whether this scope was introduced by a {@code with} statement. */
    public final boolean withScope;

    /**
     * Creates a scope.
     *
     * @param record    variable object
     * @param parent    enclosing scope (or {@code null})
     * @param withScope whether this is a {@code with} scope
     */
    public Scope(JSObject record, Scope parent, boolean withScope) {
        this.record = record;
        this.parent = parent;
        this.withScope = withScope;
    }

    /**
     * Resolves the record that holds the binding for {@code name}, searching
     * the scope chain from innermost outward.
     *
     * @param name binding name
     * @return the holding record, or {@code null} if unresolved
     */
    public JSObject resolveBase(String name) {
        Scope s = this;
        while (s != null) {
            if (s.record.hasProperty(name)) {
                return s.record;
            }
            s = s.parent;
        }
        return null;
    }

    /** @return the global (outermost) scope's record. */
    public JSObject globalRecord() {
        Scope s = this;
        while (s.parent != null) {
            s = s.parent;
        }
        return s.record;
    }
}
