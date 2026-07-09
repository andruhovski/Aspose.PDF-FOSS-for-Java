package org.aspose.pdf.engine.script.js.builtins;

import java.util.regex.Pattern;

/**
 * Translates an ECMAScript 3 regular-expression source + flags to a compiled
 * {@link java.util.regex.Pattern} (strategy (b) of {@code docs/xfa-dev/REGEX_STRATEGY.md}).
 *
 * <p>Most ES3 syntax maps directly onto {@code java.util.regex}. The one rewrite
 * applied is the non-multiline {@code ^}/{@code $} anchor correction (see
 * {@link #translateAnchors}) — Java's {@code $} otherwise matches before a
 * trailing line terminator, which ECMAScript (without {@code m}) does not.
 * Remaining minor nuances ({@code m}-flag line-terminator set) are documented and
 * not worked around here.</p>
 */
final class RegexTranslator {

    private RegexTranslator() { }

    /**
     * Compiles an ES3 regex.
     *
     * @param source ES3 pattern body
     * @param flags  flag characters ({@code g}, {@code i}, {@code m})
     * @return compiled pattern
     * @throws java.util.regex.PatternSyntaxException if the body is invalid
     */
    static Pattern compile(String source, String flags) {
        int f = 0;
        if (flags.indexOf('i') >= 0) {
            f |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        boolean multiline = flags.indexOf('m') >= 0;
        if (multiline) {
            f |= Pattern.MULTILINE;
        }
        String body = translateAnchors(source, multiline);
        if (body.isEmpty()) {
            body = "(?:)";
        }
        return Pattern.compile(body, f);
    }

    /**
     * Corrects the one ECMAScript-vs-{@code java.util.regex} anchor divergence that bites: without
     * the {@code m} flag, ECMAScript {@code ^}/{@code $} match only the start/end of the whole input,
     * whereas Java's {@code $} also matches <em>before a trailing line terminator</em> (so
     * {@code /^\d+$/.test('123\n')} is {@code false} in ECMAScript but would be {@code true} on raw
     * Java). We map the top-level (outside a character class, unescaped) {@code ^}→{@code \A} and
     * {@code $}→{@code \z} (absolute input boundaries). With {@code m}, Java's {@code MULTILINE} line
     * semantics are kept (the close approximation), so the anchors are left untouched.
     *
     * @param source   the ES regex body
     * @param multiline whether the {@code m} flag is set
     * @return the body with non-multiline anchors made ECMAScript-exact
     */
    static String translateAnchors(String source, boolean multiline) {
        if (multiline || (source.indexOf('^') < 0 && source.indexOf('$') < 0)) {
            return source;
        }
        StringBuilder out = new StringBuilder(source.length() + 4);
        boolean inClass = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\\' && i + 1 < source.length()) {
                out.append(c).append(source.charAt(++i)); // escaped char — copy verbatim
                continue;
            }
            if (c == '[') {
                inClass = true;
                out.append(c);
            } else if (c == ']') {
                inClass = false;
                out.append(c);
            } else if (c == '^' && !inClass) {
                out.append("\\A");
            } else if (c == '$' && !inClass) {
                out.append("\\z");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Validates the flag string (only {@code g}/{@code i}/{@code m}, no repeats).
     *
     * @param flags flag string
     * @return {@code true} if valid for ES3
     */
    static boolean validFlags(String flags) {
        boolean g = false;
        boolean i = false;
        boolean m = false;
        for (int k = 0; k < flags.length(); k++) {
            char c = flags.charAt(k);
            if (c == 'g' && !g) {
                g = true;
            } else if (c == 'i' && !i) {
                i = true;
            } else if (c == 'm' && !m) {
                m = true;
            } else {
                return false;
            }
        }
        return true;
    }
}
