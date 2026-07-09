package org.aspose.pdf.engine.xfa.binding.som;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses a SOM expression string into a {@link SomExpr}. Hand-written
 * char-scanner (no dependency on the JS engine). Recognises accessor roots,
 * dotted names, {@code [n]}/{@code [*]}/{@code [predicate]}, {@code #class},
 * {@code .#property} and {@code ..} parent steps.
 *
 * <p>A {@code #word} is a class step when {@code word} is a known element class
 * (supplied set) and a property step otherwise. Predicates are parsed as a
 * simple {@code path OP literal} comparison or a bare truthiness path; anything
 * that looks like script (function calls, etc.) is flagged
 * {@link SomExpr.Predicate#script} and left un-evaluated.</p>
 */
public final class SomParser {

    private final Set<String> classNames;

    /**
     * Creates a parser.
     *
     * @param classNames the set of element class names (for {@code #class} vs {@code .#property})
     */
    public SomParser(Set<String> classNames) {
        this.classNames = classNames;
    }

    private String s;
    private int pos;

    /**
     * Parses an expression.
     *
     * @param expr the SOM text
     * @return the parsed expression (never {@code null}; empty steps for blank input)
     */
    public synchronized SomExpr parse(String expr) {
        this.s = expr == null ? "" : expr.trim();
        this.pos = 0;
        SomExpr.Root root = SomExpr.Root.NONE;
        List<SomExpr.Step> steps = new ArrayList<>();

        if (peek() == '$') {
            pos++;
            String w = readWord();
            root = mapRoot(w);
            if (root == SomExpr.Root.NONE && !w.isEmpty()) {
                // $unknownWord -> treat as a relative name step
                steps.add(new SomExpr.NameStep(w, readIndex()));
                root = SomExpr.Root.CURRENT;
            }
        }

        while (pos < s.length()) {
            char c = peek();
            if (c == '.') {
                pos++;
                if (peek() == '.') {
                    pos++;
                    steps.add(new SomExpr.ParentStep());
                    continue;
                }
                if (peek() == '#') {
                    pos++;
                    steps.add(hashStep());
                    continue;
                }
                steps.add(new SomExpr.NameStep(readName(), readIndex()));
            } else if (c == '#') {
                pos++;
                steps.add(hashStep());
            } else if (c == '[') {
                // standalone index after a root (e.g. $record[0]) — attach to a wildcard name step
                steps.add(new SomExpr.NameStep("*", readIndex()));
            } else if (isNameStart(c)) {
                steps.add(new SomExpr.NameStep(readName(), readIndex()));
            } else {
                pos++; // skip unexpected char
            }
        }
        return new SomExpr(root, steps, s);
    }

    private SomExpr.Step hashStep() {
        String w = readWord();
        if (classNames.contains(w)) {
            return new SomExpr.ClassStep(w, readIndex());
        }
        return new SomExpr.PropertyStep(w);
    }

    private static SomExpr.Root mapRoot(String w) {
        switch (w) {
            case "": return SomExpr.Root.CURRENT;
            case "xfa": return SomExpr.Root.XFA;
            case "template": return SomExpr.Root.TEMPLATE;
            case "data": return SomExpr.Root.DATA;
            case "form": return SomExpr.Root.FORM;
            case "record": return SomExpr.Root.RECORD;
            case "dataWindow": return SomExpr.Root.DATAWINDOW;
            default: return SomExpr.Root.NONE;
        }
    }

    private SomExpr.Index readIndex() {
        if (peek() != '[') {
            return SomExpr.Index.NONE_IDX;
        }
        pos++; // [
        int start = pos;
        int depth = 1;
        while (pos < s.length() && depth > 0) {
            char c = s.charAt(pos);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
            pos++;
        }
        String inner = s.substring(start, pos).trim();
        if (pos < s.length()) {
            pos++; // ]
        }
        if (inner.equals("*")) {
            return SomExpr.Index.ALL_IDX;
        }
        if (isAllDigits(inner)) {
            return new SomExpr.Index(SomExpr.Index.Kind.NUM, Integer.parseInt(inner), null);
        }
        return new SomExpr.Index(SomExpr.Index.Kind.PRED, 0, parsePredicate(inner));
    }

    private static SomExpr.Predicate parsePredicate(String inner) {
        // Script if it looks like a call / contains script-only tokens.
        if (inner.indexOf('(') >= 0 || inner.indexOf(')') >= 0
                || inner.contains("&&") || inner.contains("||")) {
            return new SomExpr.Predicate(inner, SomExpr.Predicate.Op.NONE, null, true);
        }
        String[][] ops = {{"==", "EQ"}, {"!=", "NE"}, {"<=", "LE"}, {">=", "GE"}, {"<", "LT"}, {">", "GT"}};
        for (String[] o : ops) {
            int i = inner.indexOf(o[0]);
            if (i >= 0) {
                String left = inner.substring(0, i).trim();
                String right = inner.substring(i + o[0].length()).trim();
                right = unquote(right);
                return new SomExpr.Predicate(left, SomExpr.Predicate.Op.valueOf(o[1]), right, false);
            }
        }
        // Bare path -> truthiness predicate.
        return new SomExpr.Predicate(inner, SomExpr.Predicate.Op.NONE, null, false);
    }

    private static String unquote(String v) {
        if (v.length() >= 2 && (v.charAt(0) == '"' || v.charAt(0) == '\'')
                && v.charAt(v.length() - 1) == v.charAt(0)) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    /* ----------------------------- scanning -------------------------- */

    private char peek() {
        return pos < s.length() ? s.charAt(pos) : '\0';
    }

    private String readWord() {
        int start = pos;
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                pos++;
            } else {
                break;
            }
        }
        return s.substring(start, pos);
    }

    private String readName() {
        int start = pos;
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':') {
                pos++;
            } else {
                break;
            }
        }
        return s.substring(start, pos);
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_' || c == ':';
    }

    private static boolean isAllDigits(String t) {
        if (t.isEmpty()) {
            return false;
        }
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
