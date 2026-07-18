package org.aspose.pdf.engine.script.js.builtins;

import org.aspose.pdf.engine.script.js.runtime.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/// Installs RegExp and RegExp.prototype (ECMA-262 3rd ed., sec 15.10):
/// `exec`, `test` and `toString`, with global
/// `lastIndex` statefulness.
final class RegExpBuiltins {

    private static final Object UNDEF = Undefined.INSTANCE;

    private RegExpBuiltins() { }

    static void install(Realm r) {
        JSObject proto = new JSObject(r.objectPrototype);
        proto.setClassName("Object");
        r.regexpPrototype = proto;

        r.method(proto, "exec", 1, (i, t, a) -> {
            JSRegExp re = asRegExp(r, t);
            String s = i.toStringJS(Builtins.arg(a, 0));
            return exec(r, re, s);
        });
        r.method(proto, "test", 1, (i, t, a) -> {
            JSRegExp re = asRegExp(r, t);
            String s = i.toStringJS(Builtins.arg(a, 0));
            return exec(r, re, s) != JSNull.NULL;
        });
        r.method(proto, "toString", 0, (i, t, a) -> {
            JSRegExp re = asRegExp(r, t);
            return "/" + re.source + "/"
                    + (re.global ? "g" : "") + (re.ignoreCase ? "i" : "") + (re.multiline ? "m" : "");
        });

        NativeFunction ctor = new NativeFunction(r.functionPrototype, "RegExp", 2, (i, t, a) -> {
            Object p = Builtins.arg(a, 0);
            if (p instanceof JSRegExp && Builtins.arg(a, 1) == UNDEF) {
                return p;
            }
            return build(r, i, a);
        }).withConstructor((i, a) -> build(r, i, a));
        Builtins.link(ctor, proto);
        r.regexpConstructor = ctor;
    }

    private static Object build(Realm r,
                               org.aspose.pdf.engine.script.js.interp.Interpreter i, Object[] a) {
        Object p = Builtins.arg(a, 0);
        String source;
        String flags;
        if (p instanceof JSRegExp) {
            JSRegExp src = (JSRegExp) p;
            source = src.source;
            flags = Builtins.arg(a, 1) == UNDEF
                    ? (src.global ? "g" : "") + (src.ignoreCase ? "i" : "") + (src.multiline ? "m" : "")
                    : i.toStringJS(a[1]);
        } else {
            source = p == UNDEF ? "" : i.toStringJS(p);
            flags = Builtins.arg(a, 1) == UNDEF ? "" : i.toStringJS(a[1]);
        }
        if (!RegexTranslator.validFlags(flags)) {
            throw r.syntaxError("Invalid regular expression flags: " + flags);
        }
        Pattern pat;
        try {
            pat = RegexTranslator.compile(source, flags);
        } catch (PatternSyntaxException e) {
            throw r.syntaxError("Invalid regular expression: " + e.getMessage());
        }
        return new JSRegExp(r.regexpPrototype, source, pat,
                flags.indexOf('g') >= 0, flags.indexOf('i') >= 0, flags.indexOf('m') >= 0);
    }

    private static JSRegExp asRegExp(Realm r, Object t) {
        if (t instanceof JSRegExp) {
            return (JSRegExp) t;
        }
        throw r.typeError("Method called on a non-RegExp object");
    }

    /// RegExp.prototype.exec core (sec 15.10.6.2): honours global lastIndex and
    /// returns a match array (with `index`/`input`) or `null`.
    static Object exec(Realm r, JSRegExp re, String s) {
        int start = re.global ? re.lastIndex() : 0;
        if (start < 0 || start > s.length()) {
            re.setLastIndex(0);
            return JSNull.NULL;
        }
        Matcher m = re.pattern.matcher(s);
        if (!m.find(start)) {
            re.setLastIndex(0);
            return JSNull.NULL;
        }
        if (re.global) {
            re.setLastIndex(m.end());
        }
        JSArray result = r.newArray();
        result.put("0", m.group());
        for (int g = 1; g <= m.groupCount(); g++) {
            String gv = m.group(g);
            result.put(Integer.toString(g), gv == null ? UNDEF : gv);
        }
        result.setLength(m.groupCount() + 1);
        result.defineHidden("index", (double) m.start());
        result.defineHidden("input", s);
        return result;
    }
}
