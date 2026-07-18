package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.interp.Interpreter;
import org.aspose.pdf.engine.script.js.runtime.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Installs String and String.prototype (ECMA-262 3rd ed., sec 15.5) with the
/// ES3 method set (no `trim`/`includes` which are later editions).
final class StringBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private StringBuiltins() { }

    static void install(Realm r) {
        JSObject proto = new JSObject(r.objectPrototype);
        proto.setClassName("String");
        proto.primitiveValue = "";
        proto.define("length", 0.0, false, false, false);
        r.stringPrototype = proto;

        r.method(proto, "toString", 0, (i, t, a) -> thisStr(r, i, t, true));
        r.method(proto, "valueOf", 0, (i, t, a) -> thisStr(r, i, t, true));
        r.method(proto, "charAt", 1, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            int pos = (int) JSNumber.toInteger(i.toNumber(Builtins.arg(a, 0)));
            return pos >= 0 && pos < s.length() ? String.valueOf(s.charAt(pos)) : "";
        });
        r.method(proto, "charCodeAt", 1, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            int pos = (int) JSNumber.toInteger(i.toNumber(Builtins.arg(a, 0)));
            return pos >= 0 && pos < s.length() ? (double) s.charAt(pos) : Double.NaN;
        });
        r.method(proto, "indexOf", 1, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            String search = i.toStringJS(Builtins.arg(a, 0));
            int pos = (int) JSNumber.toInteger(i.toNumber(Builtins.arg(a, 1)));
            return (double) s.indexOf(search, Math.max(0, Math.min(pos, s.length())));
        });
        r.method(proto, "lastIndexOf", 1, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            String search = i.toStringJS(Builtins.arg(a, 0));
            double posD = i.toNumber(Builtins.arg(a, 1));
            int from = Double.isNaN(posD) ? s.length() : (int) JSNumber.toInteger(posD);
            return (double) s.lastIndexOf(search, Math.max(0, Math.min(from, s.length())));
        });
        r.method(proto, "concat", 1, (i, t, a) -> {
            StringBuilder sb = new StringBuilder(thisStr(r, i, t, false));
            for (Object v : a) {
                sb.append(i.toStringJS(v));
            }
            return sb.toString();
        });
        r.method(proto, "slice", 2, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            int len = s.length();
            int start = relIndex(i, Builtins.arg(a, 0), len, 0);
            int end = Builtins.arg(a, 1) == UNDEF ? len : relIndex(i, a[1], len, len);
            return start < end ? s.substring(start, end) : "";
        });
        r.method(proto, "substring", 2, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            int len = s.length();
            int start = clamp(toIntOr(i, Builtins.arg(a, 0), 0), 0, len);
            int end = Builtins.arg(a, 1) == UNDEF ? len : clamp(toIntOr(i, a[1], 0), 0, len);
            return s.substring(Math.min(start, end), Math.max(start, end));
        });
        r.method(proto, "substr", 2, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            int len = s.length();
            int start = toIntOr(i, Builtins.arg(a, 0), 0);
            if (start < 0) {
                start = Math.max(len + start, 0);
            }
            int length = Builtins.arg(a, 1) == UNDEF ? len - start : toIntOr(i, a[1], 0);
            length = Math.max(0, Math.min(length, len - start));
            if (start >= len || length <= 0) {
                return "";
            }
            return s.substring(start, start + length);
        });
        r.method(proto, "toLowerCase", 0, (i, t, a) -> thisStr(r, i, t, false).toLowerCase(java.util.Locale.ROOT));
        r.method(proto, "toUpperCase", 0, (i, t, a) -> thisStr(r, i, t, false).toUpperCase(java.util.Locale.ROOT));
        r.method(proto, "toLocaleLowerCase", 0, (i, t, a) -> thisStr(r, i, t, false).toLowerCase());
        r.method(proto, "toLocaleUpperCase", 0, (i, t, a) -> thisStr(r, i, t, false).toUpperCase());
        r.method(proto, "localeCompare", 1, (i, t, a) ->
                (double) Integer.signum(thisStr(r, i, t, false).compareTo(i.toStringJS(Builtins.arg(a, 0)))));
        r.method(proto, "search", 1, (i, t, a) -> {
            String s = thisStr(r, i, t, false);
            JSRegExp re = toRegExp(r, i, Builtins.arg(a, 0));
            Matcher m = re.pattern.matcher(s);
            return m.find() ? (double) m.start() : -1.0;
        });
        r.method(proto, "match", 1, (i, t, a) -> match(r, i, thisStr(r, i, t, false), Builtins.arg(a, 0)));
        r.method(proto, "replace", 2, (i, t, a) ->
                replace(r, i, thisStr(r, i, t, false), Builtins.arg(a, 0), Builtins.arg(a, 1)));
        r.method(proto, "split", 2, (i, t, a) ->
                split(r, i, thisStr(r, i, t, false), Builtins.arg(a, 0), Builtins.arg(a, 1)));

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "String", 1, (i, t, a) ->
                a.length == 0 ? "" : i.toStringJS(a[0])).withConstructor((i, a) -> {
            String s = a.length == 0 ? "" : i.toStringJS(a[0]);
            JSObject o = new JSObject(proto);
            o.setClassName("String");
            o.primitiveValue = s;
            o.define("length", (double) s.length(), false, false, false);
            return o;
        });
        Builtins.link(ctor, proto);
        r.method(ctor, "fromCharCode", 1, (i, t, a) -> {
            StringBuilder sb = new StringBuilder();
            for (Object v : a) {
                sb.append((char) (int) JSNumber.toUint32(i.toNumber(v)));
            }
            return sb.toString();
        });
        r.stringConstructor = ctor;
    }

    private static String thisStr(Realm r, Interpreter i, Object t, boolean strict) {
        if (t instanceof String) {
            return (String) t;
        }
        if (t instanceof JSObject && ((JSObject) t).primitiveValue instanceof String) {
            return (String) ((JSObject) t).primitiveValue;
        }
        if (strict) {
            throw r.typeError("String.prototype method called on incompatible receiver");
        }
        return i.toStringJS(t);
    }

    private static int toIntOr(Interpreter i, Object v, int dflt) {
        if (v == UNDEF) {
            return dflt;
        }
        double d = i.toNumber(v);
        return (int) JSNumber.toInteger(d);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(v, hi));
    }

    private static int relIndex(Interpreter i, Object v, int len, int dflt) {
        if (v == UNDEF) {
            return dflt;
        }
        int idx = (int) JSNumber.toInteger(i.toNumber(v));
        if (idx < 0) {
            return Math.max(len + idx, 0);
        }
        return Math.min(idx, len);
    }

    private static JSRegExp toRegExp(Realm r, Interpreter i, Object v) {
        if (v instanceof JSRegExp) {
            return (JSRegExp) v;
        }
        Object made = ((JSFunction) r.regexpConstructor).construct(i,
                new Object[]{v == UNDEF ? "" : i.toStringJS(v)});
        return (JSRegExp) made;
    }

    private static Object match(Realm r, Interpreter i, String s, Object rx) {
        JSRegExp re = toRegExp(r, i, rx);
        if (!re.global) {
            return RegExpBuiltins.exec(r, re, s);
        }
        re.setLastIndex(0);
        JSArray out = r.newArray();
        Matcher m = re.pattern.matcher(s);
        int n = 0;
        int last = 0;
        while (m.find(last)) {
            out.put(Integer.toString(n++), m.group());
            last = m.end() == m.start() ? m.end() + 1 : m.end();
            if (last > s.length()) {
                break;
            }
        }
        re.setLastIndex(0);
        if (n == 0) {
            return JSNull.NULL;
        }
        out.setLength(n);
        return out;
    }

    private static Object replace(Realm r, Interpreter i, String s, Object search, Object repl) {
        if (search instanceof JSRegExp) {
            return regexReplace(r, i, s, (JSRegExp) search, repl);
        }
        String needle = i.toStringJS(search);
        int idx = s.indexOf(needle);
        if (idx < 0) {
            return s;
        }
        String matched = needle;
        String replacement;
        if (repl instanceof JSFunction) {
            Object res = ((JSFunction) repl).call(i, UNDEF,
                    new Object[]{matched, (double) idx, s});
            replacement = i.toStringJS(res);
        } else {
            replacement = expandDollar(i.toStringJS(repl), matched, s, idx, new String[0]);
        }
        return s.substring(0, idx) + replacement + s.substring(idx + needle.length());
    }

    private static Object regexReplace(Realm r, Interpreter i, String s, JSRegExp re, Object repl) {
        Matcher m = re.pattern.matcher(s);
        StringBuilder out = new StringBuilder();
        int last = 0;
        boolean found = false;
        int searchPos = 0;
        while (searchPos <= s.length() && m.find(searchPos)) {
            found = true;
            int start = m.start();
            int end = m.end();
            out.append(s, last, start);
            String[] groups = new String[m.groupCount()];
            for (int g = 1; g <= m.groupCount(); g++) {
                groups[g - 1] = m.group(g);
            }
            if (repl instanceof JSFunction) {
                Object[] cbArgs = new Object[groups.length + 3];
                cbArgs[0] = m.group();
                for (int g = 0; g < groups.length; g++) {
                    cbArgs[g + 1] = groups[g] == null ? UNDEF : groups[g];
                }
                cbArgs[groups.length + 1] = (double) start;
                cbArgs[groups.length + 2] = s;
                out.append(i.toStringJS(((JSFunction) repl).call(i, UNDEF, cbArgs)));
            } else {
                out.append(expandDollar(i.toStringJS(repl), m.group(), s, start, groups));
            }
            last = end;
            if (!re.global) {
                break;
            }
            searchPos = end == start ? end + 1 : end;
            if (end == start && end < s.length()) {
                out.append(s.charAt(end));
                last = end + 1;
            }
        }
        if (!found) {
            return s;
        }
        out.append(s.substring(last));
        return out.toString();
    }

    private static String expandDollar(String repl, String matched, String whole, int idx, String[] groups) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < repl.length(); k++) {
            char c = repl.charAt(k);
            if (c == '$' && k + 1 < repl.length()) {
                char d = repl.charAt(k + 1);
                if (d == '$') {
                    sb.append('$');
                    k++;
                } else if (d == '&') {
                    sb.append(matched);
                    k++;
                } else if (d == '`') {
                    sb.append(whole, 0, idx);
                    k++;
                } else if (d == '\'') {
                    sb.append(whole.substring(idx + matched.length()));
                    k++;
                } else if (Character.isDigit(d)) {
                    int gi;
                    int consumed;
                    if (k + 2 < repl.length() && Character.isDigit(repl.charAt(k + 2))
                            && (gi = Integer.parseInt(repl.substring(k + 1, k + 3))) <= groups.length
                            && gi > 0) {
                        consumed = 2;
                    } else {
                        gi = d - '0';
                        consumed = 1;
                    }
                    if (gi > 0 && gi <= groups.length) {
                        String g = groups[gi - 1];
                        if (g != null) {
                            sb.append(g);
                        }
                        k += consumed;
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Object split(Realm r, Interpreter i, String s, Object sep, Object limitArg) {
        JSArray out = r.newArray();
        long limit = limitArg == UNDEF ? 4294967295L : JSNumber.toUint32(i.toNumber(limitArg));
        if (limit == 0) {
            return out;
        }
        if (sep == UNDEF) {
            out.put("0", s);
            out.setLength(1);
            return out;
        }

        if (sep instanceof JSRegExp) {
            return splitRegex(r, i, s, (JSRegExp) sep, limit, out);
        }

        String sepStr = i.toStringJS(sep);
        if (s.isEmpty()) {
            if (!sepStr.isEmpty()) {
                out.put("0", s);
                out.setLength(1);
            }
            return out;
        }
        if (sepStr.isEmpty()) {
            int n = 0;
            for (int k = 0; k < s.length() && n < limit; k++) {
                out.put(Integer.toString(n++), String.valueOf(s.charAt(k)));
            }
            out.setLength(n);
            return out;
        }
        int n = 0;
        int from = 0;
        while (n < limit) {
            int idx = s.indexOf(sepStr, from);
            if (idx < 0) {
                out.put(Integer.toString(n++), s.substring(from));
                break;
            }
            out.put(Integer.toString(n++), s.substring(from, idx));
            from = idx + sepStr.length();
        }
        out.setLength(n);
        return out;
    }

    private static Object splitRegex(Realm r, Interpreter i, String s, JSRegExp re, long limit, JSArray out) {
        Pattern p = re.pattern;
        Matcher m = p.matcher(s);
        if (s.isEmpty()) {
            if (!m.find()) {
                out.put("0", s);
                out.setLength(1);
            }
            return out;
        }
        int n = 0;
        int last = 0;
        int searchPos = 0;
        while (searchPos <= s.length() && m.find(searchPos)) {
            int start = m.start();
            int end = m.end();
            if (end == last && start == last) {
                searchPos = end + 1;
                if (searchPos > s.length()) {
                    break;
                }
                continue;
            }
            if (start >= s.length()) {
                break;
            }
            out.put(Integer.toString(n++), s.substring(last, start));
            if (n >= limit) {
                out.setLength((int) limit);
                return out;
            }
            for (int g = 1; g <= m.groupCount(); g++) {
                out.put(Integer.toString(n++), m.group(g) == null ? UNDEF : m.group(g));
                if (n >= limit) {
                    out.setLength((int) limit);
                    return out;
                }
            }
            last = end;
            searchPos = end == start ? end + 1 : end;
        }
        out.put(Integer.toString(n++), s.substring(last));
        out.setLength(n);
        return out;
    }
}
