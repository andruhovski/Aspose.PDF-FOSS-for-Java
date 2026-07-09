package org.aspose.pdf.engine.xfa.binding.som;

import java.util.List;

/**
 * Parsed SOM (Scripting Object Model) expression — XFA 3.0 SOM grammar.
 *
 * <p>An expression is an optional accessor {@link Root} followed by a sequence of
 * {@link Step}s: dotted name paths ({@code Receipt.Total_Price}), index notation
 * ({@code name[n]}, {@code name[*]}, {@code name[predicate]}), class syntax
 * ({@code #subform[1]}, {@code #dataValue}), property syntax ({@code .#name}),
 * and parent navigation ({@code ..}).</p>
 *
 * <p>This is a pure structural language — predicates are limited to structural /
 * comparison expressions; script-bearing predicates are flagged
 * ({@link Predicate#script}) and not evaluated here (Stage B). The resolver does
 * NOT depend on the JavaScript engine.</p>
 */
public final class SomExpr {

    /** Accessor shortcut roots (sec on SOM accessors). */
    public enum Root {
        /** No explicit root (relative to the current node). */
        NONE,
        /** {@code $xfa} — the application/data model root. */
        XFA,
        /** {@code $template} — the template root. */
        TEMPLATE,
        /** {@code $data} — the data model root. */
        DATA,
        /** {@code $form} — the form (merged) root. */
        FORM,
        /** {@code $record} — the current data record. */
        RECORD,
        /** {@code $dataWindow} — the data window. */
        DATAWINDOW,
        /** {@code $} — the current node. */
        CURRENT
    }

    /** Index modifier on a step. */
    public static final class Index {
        /** Index kinds. */
        public enum Kind {
            /** No index (all of that name in the immediate scope, occurrence semantics). */
            NONE,
            /** {@code [*]} — every sibling of that name. */
            ALL,
            /** {@code [n]} — the n-th occurrence (0-based). */
            NUM,
            /** {@code [predicate]} — filter candidates by a predicate. */
            PRED
        }

        public final Kind kind;
        public final int n;
        public final Predicate predicate;

        Index(Kind kind, int n, Predicate predicate) {
            this.kind = kind;
            this.n = n;
            this.predicate = predicate;
        }

        static final Index NONE_IDX = new Index(Kind.NONE, 0, null);
        static final Index ALL_IDX = new Index(Kind.ALL, 0, null);
    }

    /** A structural/comparison predicate {@code relativePath OP literal} (or a bare path = truthiness). */
    public static final class Predicate {
        /** Comparison operators. */
        public enum Op { NONE, EQ, NE, LT, GT, LE, GE }

        public final String path;
        public final Op op;
        public final String value;
        /** {@code true} if the predicate contains script (FormCalc/JS) — deferred to Stage B, not evaluated. */
        public final boolean script;

        Predicate(String path, Op op, String value, boolean script) {
            this.path = path;
            this.op = op;
            this.value = value;
            this.script = script;
        }
    }

    /** One navigation step. */
    public abstract static class Step {
        /** @return the index modifier on this step. */
        public Index index() {
            return Index.NONE_IDX;
        }
    }

    /** A named child step ({@code name}, possibly indexed). */
    public static final class NameStep extends Step {
        public final String name;
        public final Index index;
        NameStep(String name, Index index) {
            this.name = name;
            this.index = index;
        }
        @Override public Index index() {
            return index;
        }
    }

    /** A class step ({@code #subform}, {@code #dataValue}, possibly indexed). */
    public static final class ClassStep extends Step {
        public final String className;
        public final Index index;
        ClassStep(String className, Index index) {
            this.className = className;
            this.index = index;
        }
        @Override public Index index() {
            return index;
        }
    }

    /** A property step ({@code .#name}, {@code .#x}) — yields an attribute/property value. */
    public static final class PropertyStep extends Step {
        public final String property;
        PropertyStep(String property) {
            this.property = property;
        }
    }

    /** Parent navigation ({@code ..}). */
    public static final class ParentStep extends Step { }

    private final Root root;
    private final List<Step> steps;
    private final String source;

    SomExpr(Root root, List<Step> steps, String source) {
        this.root = root;
        this.steps = steps;
        this.source = source;
    }

    /** @return the accessor root. */
    public Root getRoot() {
        return root;
    }

    /** @return the ordered steps. */
    public List<Step> getSteps() {
        return steps;
    }

    /** @return the original expression text. */
    public String getSource() {
        return source;
    }

    /** @return {@code true} if the last step is a property accessor. */
    public boolean endsWithProperty() {
        return !steps.isEmpty() && steps.get(steps.size() - 1) instanceof PropertyStep;
    }

    /** @return {@code true} if any predicate in the expression contains script (deferred). */
    public boolean hasScriptPredicate() {
        for (Step s : steps) {
            Index idx = s.index();
            if (idx != null && idx.kind == Index.Kind.PRED && idx.predicate != null && idx.predicate.script) {
                return true;
            }
        }
        return false;
    }
}
