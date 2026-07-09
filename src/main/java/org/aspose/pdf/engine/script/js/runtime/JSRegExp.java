package org.aspose.pdf.engine.script.js.runtime;

import java.util.regex.Pattern;

/**
 * A RegExp object (ECMA-262 3rd ed., sec 15.10) backed by a compiled
 * {@link java.util.regex.Pattern}. The ES3 pattern is translated to Java regex
 * syntax (strategy (b); see {@code docs/xfa-dev/REGEX_STRATEGY.md}). The {@code global},
 * {@code ignoreCase} and {@code multiline} flags and the mutable
 * {@code lastIndex} are exposed as own properties.
 */
public final class JSRegExp extends JSObject {

    /** Compiled Java pattern. */
    public final Pattern pattern;
    /** Original ES3 source text. */
    public final String source;
    /** {@code g} flag. */
    public final boolean global;
    /** {@code i} flag. */
    public final boolean ignoreCase;
    /** {@code m} flag. */
    public final boolean multiline;

    /**
     * Creates a RegExp.
     *
     * @param proto      {@code RegExp.prototype}
     * @param source     ES3 source text
     * @param pattern    compiled Java pattern
     * @param global     {@code g} flag
     * @param ignoreCase {@code i} flag
     * @param multiline  {@code m} flag
     */
    public JSRegExp(JSObject proto, String source, Pattern pattern,
                    boolean global, boolean ignoreCase, boolean multiline) {
        super(proto);
        setClassName("RegExp");
        this.source = source.isEmpty() ? "(?:)" : source;
        this.pattern = pattern;
        this.global = global;
        this.ignoreCase = ignoreCase;
        this.multiline = multiline;
        define("source", this.source, false, false, false);
        define("global", global, false, false, false);
        define("ignoreCase", ignoreCase, false, false, false);
        define("multiline", multiline, false, false, false);
        define("lastIndex", 0.0, true, false, false);
    }

    /** @return the current {@code lastIndex} as an int. */
    public int lastIndex() {
        Object v = get("lastIndex");
        return v instanceof Double ? (int) (double) (Double) v : 0;
    }

    /**
     * Sets {@code lastIndex}.
     *
     * @param idx new value
     */
    public void setLastIndex(int idx) {
        put("lastIndex", (double) idx);
    }
}
