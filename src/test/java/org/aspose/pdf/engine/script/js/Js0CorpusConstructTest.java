package org.aspose.pdf.engine.script.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// B3.0 — locks in JS-0 correctness for the **actual** regex / Date / eval constructs the XFA
/// corpus scripts use (extracted by `XfaJsConstructProbeTest`). The B3.0.0 demand probe found
/// the three "critical-path gaps" (regex 42 / Date 28 / eval 8 forms) are **not exercised** by the
/// corpus: every one of 172 distinct regex patterns compiles, there is no JS-layer Date-string
/// parsing, and every eval is global-scope dynamic dispatch. These fixtures exercise the real
/// _semantics_ (not just compilation) so B3.1 builds on a verified base and a future regression
/// is caught. They pass on the current engine — no hardening was required.
public class Js0CorpusConstructTest {

    private static Object run(String s) {
        return Engine.eval(s);
    }

    private static boolean b(String s) {
        return (Boolean) run(s);
    }

    private static String s(String s) {
        return (String) run(s);
    }

    private static double n(String s) {
        return (Double) run(s);
    }

    /* ----------------------------- regex (B3.0.1) ----------------------------- */

    @Test
    void postalCodeValidationPatterns() {
        // Canadian postal code — a representative corpus field-validation regex.
        assertTrue(b("/^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$/.test('K1A 0B1')"));
        assertFalse(b("/^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$/.test('123 456')"));
        // explicit-letter postal class from the corpus
        assertTrue(b("/^[ABCEGHJKLMNPRSTVXY]\\d[ABCEGHJKLMNPRSTVWXYZ]\\d[ABCEGHJKLMNPRSTVWXYZ]\\d$/.test('A1B2C3')"));
    }

    @Test
    void anchorsAndCharClassesAreEcmaEquivalent() {
        assertTrue(b("/^\\d+$/.test('123')"));
        assertFalse(b("/^\\d+$/.test('12a')"));
        assertTrue(b("/^[0-9.\\-]+$/.test('-3.14')"));   // numeric field class
        assertTrue(b("/[^0-9-.]/.test('x')"));            // negated class (corpus)
        assertFalse(b("/[^0-9-.]/.test('7')"));
        assertTrue(b("/[A-z]/.test('a')"));               // sloppy A-z range (corpus) behaves same in both engines
    }

    @Test
    void nonMultilineAnchorsAreEcmaExactNotJava() {
        // B3.0.1 fix: ECMAScript $ (no /m) matches only end-of-input — NOT before a trailing newline
        // the way raw java.util.regex does. A field value with a trailing newline must fail /^\d+$/.
        assertFalse(b("/^\\d+$/.test('123\\n')"), "ECMAScript $ does not match before a trailing \\n");
        assertFalse(b("/^abc$/.test('abc\\n')"));
        assertTrue(b("/^abc$/.test('abc')"));
        // ^ anchors to start of input, not after an embedded newline (no /m)
        assertFalse(b("/^b/.test('a\\nb')"));
        // a '$' or '^' inside a character class is literal and must be untouched by the rewrite
        assertTrue(b("/[$^]/.test('$')"));
        assertTrue(b("/[$^]/.test('^')"));
        // with the m flag, line-anchored matching still works
        assertTrue(b("/^b/m.test('a\\nb')"));
    }

    @Test
    void digitGroupingReplacementDollarSemantics() {
        // $1/$2 replacement (ECMAScript replacement, not Java Matcher) — corpus number formatting.
        assertEquals("1,234", s("'1234'.replace(/(\\d+)(\\d{3})/, '$1,$2')"));
        // thousands grouping via lookahead + global replace
        assertEquals("1,234,567", s("'1234567'.replace(/(\\d)(?=(\\d{3})+$)/g, '$1,')"));
    }

    @Test
    void trimAndWhitespaceNormalization() {
        assertEquals("hi", s("'   hi   '.replace(/^\\s*|\\s*$/g, '')"));
        assertEquals("a b", s("'a   b'.replace(/\\s+/g, ' ')"));
    }

    @Test
    void unicodeClassesNormalizeQuotes() {
        // corpus uses smart-quote / non-ASCII classes; java.util.regex is Unicode-correct here.
        assertEquals("\"hi\"", s("'“hi”'.replace(/[“”]/g, '\"')"));
        assertEquals("'x'", s("'‘x’'.replace(/[‘’]/g, \"'\")"));
    }

    @Test
    void splitMatchExecBehaviour() {
        assertEquals(3.0, n("'a\\nb\\r\\nc'.split(/[\\n\\r]+/).length"));
        assertEquals(2.0, n("/(\\d+)/.exec('ab123').index"));
        assertEquals("123", s("/(\\d+)/.exec('ab123')[1]"));
        assertEquals("aBc", s("'abc'.replace(/b/, function(m){return m.toUpperCase();})"));
        assertTrue(b("'2020-01-15'.match(/^\\d{4}-\\d{2}-\\d{2}$/) != null"));
    }

    /* ----------------------------- eval (B3.0.3) ----------------------------- */

    @Test
    void evalGlobalFunctionDispatch() {
        // corpus: eval('service.' + method) / eval(functionName + "(oNode)") — call by computed name.
        assertEquals(7.0, n("function svc(){return 7;} eval('svc()')"));
        assertEquals(42.0, n("function dbl(x){return x*2;} var fn='dbl'; eval(fn + '(21)')"));
        assertEquals(5.0, n("var service={apply:function(){return 5;}}; eval('service.apply')()"));
        // eval a code string stored in a field-like variable (corpus: eval(COMMON_ApplySigFunc.value))
        assertEquals(9.0, n("var stored='3*3'; eval(stored)"));
    }

    /* ----------------------------- Date (B3.0.2) ----------------------------- */

    @Test
    void dateComponentConstructionAndUtc() {
        // corpus Date usage is component/epoch construction + UTC accessors (no JS string parsing).
        assertEquals(2020.0, n("new Date(Date.UTC(2020,0,15)).getUTCFullYear()"));
        assertEquals(0.0, n("new Date(Date.UTC(2020,0,15)).getUTCMonth()"));
        assertEquals(15.0, n("new Date(Date.UTC(2020,0,15)).getUTCDate()"));
        assertEquals(2020.0, n("new Date(2020,5,1).getFullYear()"));
        assertEquals(5.0, n("new Date(2020,5,1).getMonth()"));
        assertTrue(b("(new Date()) instanceof Date"));
        assertEquals("number", s("typeof (new Date()).getTime()"));
        // ISO parse (the one parse form that IS supported) still works
        assertEquals(2020.0, n("new Date('2020-01-15').getUTCFullYear()"));
    }

    /* ----------------------- tracked residual divergences ----------------------- */

    @Test
    void trackedResidualGapsDocumented() {
        // Residual boundaries NOT exercised by the corpus (probe-confirmed) — tracked, not fixed:
        // Date.parse of non-ISO strings → NaN. The corpus never parses date strings in JS (date
        // formatting is the XFA host layer's util.printd/scand — B3.1), so this is not on the path.
        assertTrue(b("isNaN(Date.parse('Jan 1, 2000'))"));
        // eval declares into GLOBAL scope (not the caller's variable environment); the value is still
        // observable via the scope chain, so corpus dynamic-dispatch evals work. The only divergence
        // is that an eval'd `var` leaks to global rather than staying local — not hit by the corpus.
        assertEquals("number", s("(function(){ eval('var g_leak = 1'); return typeof g_leak; })()"));
    }
}
