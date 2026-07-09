package org.aspose.pdf.engine.script.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JS-0 acceptance gate: end-to-end ECMAScript 3 behaviour exercised through
 * {@link Engine#eval(String)}. Each test asserts an observable result of
 * evaluating a small program.
 *
 * <p>Deliberately-unsupported ES3 corners are noted inline. ES5+ features are
 * out of scope (see {@code docs/xfa-dev/JS0_FINDINGS.md}).</p>
 */
public class Js0ConformanceTests {

    private static Object run(String s) {
        return Engine.eval(s);
    }

    private static double n(String s) {
        return ((Double) run(s));
    }

    private static String s(String src) {
        return (String) run(src);
    }

    private static boolean b(String src) {
        return ((Boolean) run(src));
    }

    /* --------------------------- coercion ---------------------------- */

    @Test
    void toNumberCoercions() {
        assertEquals(1.0, n("Number(true)"));
        assertEquals(0.0, n("Number(false)"));
        assertEquals(0.0, n("Number('')"));
        assertEquals(0.0, n("Number(null)"));
        assertEquals(255.0, n("Number('0xff')"));
        assertEquals(3.14, n("Number('3.14')"));
        assertTrue(Double.isNaN(n("Number('abc')")));
        assertTrue(Double.isNaN(n("Number(undefined)")));
    }

    @Test
    void toStringCoercions() {
        assertEquals("true", s("String(true)"));
        assertEquals("null", s("String(null)"));
        assertEquals("undefined", s("String(undefined)"));
        assertEquals("123", s("String(123)"));
        assertEquals("1,2,3", s("String([1,2,3])"));
        assertEquals("[object Object]", s("String({})"));
    }

    @Test
    void toBooleanTruthiness() {
        assertTrue(b("!!1 && !!'x' && !!{} && !![]"));
        assertTrue(b("!0 && !'' && !null && !undefined && !NaN"));
    }

    @Test
    void numberToStringFormatting() {
        assertEquals("0.1", s("String(0.1)"));
        assertEquals("100", s("String(100)"));
        assertEquals("100000000000000000000", s("String(1e20)"));
        assertEquals("1e+21", s("String(1e21)"));
        assertEquals("1e-7", s("String(1e-7)"));
        assertEquals("0.000001", s("String(1e-6)"));
        assertEquals("-0", s("String(-0)").equals("0") ? "-0" : "0"); // -0 prints "0"
        assertEquals("NaN", s("String(NaN)"));
        assertEquals("Infinity", s("String(1/0)"));
        assertEquals("0.3333333333333333", s("String(1/3)"));
    }

    @Test
    void abstractEquality() {
        assertTrue(b("1 == '1'"));
        assertTrue(b("null == undefined"));
        assertTrue(b("0 == false"));
        assertTrue(b("'' == false"));
        assertTrue(b("1 === 1"));
        assertTrue(b("!(1 === '1')"));
        assertTrue(b("!(null == 0)"));
        assertTrue(b("NaN != NaN"));
    }

    /* --------------------------- operators --------------------------- */

    @Test
    void arithmeticAndBitwise() {
        assertEquals(7.0, n("1 + 2 * 3"));
        assertEquals(1.0, n("7 % 3"));
        assertEquals(8.0, n("2 << 2"));
        assertEquals(-1.0, n("~0"));
        assertEquals(5.0, n("(-1 >>> 0) - 4294967290"));
        assertEquals(6.0, n("4 | 2"));
        assertEquals(2.0, n("6 & 2"));
        assertEquals(5.0, n("6 ^ 3"));
    }

    @Test
    void stringPlusNumberDuality() {
        assertEquals("12", s("1 + '2'"));
        assertEquals("12", s("'1' + 2"));
        assertEquals(3.0, n("1 + 2"));
        assertEquals("a1b", s("'a' + 1 + 'b'"));
    }

    @Test
    void unaryOperators() {
        assertEquals("number", s("typeof 5"));
        assertEquals("string", s("typeof 'x'"));
        assertEquals("undefined", s("typeof undefinedVar"));
        assertEquals("function", s("typeof function(){}"));
        assertEquals("object", s("typeof null"));
        assertTrue(b("void 0 === undefined"));
    }

    @Test
    void incrementDecrementPrefixPostfix() {
        assertEquals(5.0, n("var a = 5; a++; "));     // postfix returns old
        assertEquals(6.0, n("var a = 5; a++; a;"));
        assertEquals(6.0, n("var a = 5; ++a;"));      // prefix returns new
        assertEquals(4.0, n("var a = 5; --a;"));
    }

    @Test
    void compoundAssignmentEvaluatesTargetOnce() {
        assertEquals(10.0, n("var a = [3]; var i = 0; a[i++] += 7; a[0];"));
        assertEquals(1.0, n("var a = [3]; var i = 0; a[i++] += 7; i;"));
    }

    /* ------------------------- control flow -------------------------- */

    @Test
    void loopsAndLabels() {
        assertEquals(45.0, n("var s = 0; for (var i = 0; i < 10; i++) s += i; s;"));
        assertEquals(6.0, n("var s = 0, i = 0; while (i < 4) { s += i; i++; } s;"));
        assertEquals(6.0, n("var s = 0, i = 0; do { s += i; i++; } while (i < 4); s;"));
        assertEquals(20.0, n(
                "var c = 0; outer: for (var i = 0; i < 5; i++)"
                        + " for (var j = 0; j < 5; j++) { if (j === 2) continue outer; c += 2; } c;"));
        assertEquals(10.0, n(
                "var c = 0; outer: for (var i = 0; i < 5; i++)"
                        + " for (var j = 0; j < 5; j++) { if (i === 2) break outer; c++; } c;"));
    }

    @Test
    void switchFallthroughAndDefault() {
        assertEquals("bc", s(
                "var r = ''; switch (2) { case 1: r += 'a'; case 2: r += 'b'; case 3: r += 'c'; break;"
                        + " default: r += 'd'; } r;"));
        assertEquals("d", s("var r = ''; switch (9) { case 1: r += 'a'; break; default: r += 'd'; } r;"));
    }

    @Test
    void forInEnumeratesKeys() {
        assertEquals("a,b,c", s(
                "var o = {a:1, b:2, c:3}; var ks = []; for (var k in o) ks.push(k); ks.join(',');"));
    }

    /* --------------------- closures / scope / this ------------------- */

    @Test
    void closuresCaptureVariable() {
        assertEquals(3.0, n(
                "function counter() { var c = 0; return function(){ return ++c; }; }"
                        + " var f = counter(); f(); f(); f();"));
    }

    @Test
    void varHoisting() {
        assertEquals("undefined", s("function f(){ return typeof x; var x = 1; } f();"));
        assertEquals(10.0, n("function f(){ x = 10; return x; var x; } f();"));
    }

    @Test
    void functionHoisting() {
        assertEquals(42.0, n("function f(){ return g(); function g(){ return 42; } } f();"));
    }

    @Test
    void thisBindingForms() {
        assertEquals(true, run("this === (function(){ return this; })()")); // global this
        assertEquals(10.0, n("var o = { x: 10, get: function(){ return this.x; } }; o.get();"));
        assertEquals(7.0, n("function F(){ this.v = 7; } var o = new F(); o.v;"));
        assertEquals(5.0, n("function f(){ return this.n; } f.call({n:5});"));
        assertEquals(9.0, n("function f(a,b){ return this.n + a + b; } f.apply({n:3},[2,4]);"));
    }

    @Test
    void argumentsObject() {
        assertEquals(3.0, n("function f(){ return arguments.length; } f(1,2,3);"));
        assertEquals(6.0, n("function f(){ var s=0; for (var i=0;i<arguments.length;i++) s+=arguments[i]; return s; } f(1,2,3);"));
    }

    /* ----------------------- prototypes / objects -------------------- */

    @Test
    void prototypeChainAndShadowing() {
        assertEquals("base", s(
                "function B(){} B.prototype.tag = 'base'; var b = new B(); b.tag;"));
        assertEquals("own", s(
                "function B(){} B.prototype.tag = 'base'; var b = new B(); b.tag = 'own'; b.tag;"));
    }

    @Test
    void inheritanceTwoLevels() {
        assertEquals("hello, Sam", s(
                "function Animal(n){ this.n = n; }"
                        + " Animal.prototype.hi = function(){ return 'hello, ' + this.n; };"
                        + " function Dog(n){ Animal.call(this, n); }"
                        + " Dog.prototype = new Animal();"
                        + " Dog.prototype.constructor = Dog;"
                        + " var d = new Dog('Sam'); d.hi();"));
    }

    @Test
    void instanceofAndIn() {
        assertTrue(b("function F(){} var o = new F(); o instanceof F"));
        assertTrue(b("[] instanceof Array"));
        assertTrue(b("var o = {a:1}; 'a' in o"));
        assertTrue(b("var o = {a:1}; !('b' in o)"));
    }

    @Test
    void deleteAndHasOwnProperty() {
        assertTrue(b("var o = {a:1}; delete o.a; !('a' in o)"));
        assertTrue(b("var o = {a:1}; o.hasOwnProperty('a')"));
        assertTrue(b("function B(){} B.prototype.x = 1; var o = new B(); !o.hasOwnProperty('x') && ('x' in o)"));
    }

    /* ------------------------- standard lib -------------------------- */

    @Test
    void arrayMutators() {
        assertEquals(4.0, n("var a = [1,2,3]; a.push(4);"));
        assertEquals("1,2,3,4", s("var a = [1,2,3]; a.push(4); a.join(',');"));
        assertEquals(3.0, n("var a = [1,2,3]; a.pop();"));
        assertEquals("3,2,1", s("[1,2,3].reverse().join(',');"));
        assertEquals("2,3", s("[1,2,3].slice(1).join(',');"));
        assertEquals("a,b,x,y,c", s(
                "var a = ['a','b','c']; a.splice(2,0,'x','y'); a.join(',');"));
        assertEquals("1,2,3,4", s("[1,2].concat([3,4]).join(',');"));
        assertEquals(3.0, n("var a=[3,2,1]; a.shift();"));
        assertEquals("0,1,2", s("var a=[2]; a.unshift(0,1); a.join(',');"));
    }

    @Test
    void arraySortDefaultAndComparator() {
        assertEquals("1,10,2,20", s("[2,10,1,20].sort().join(',');"));
        assertEquals("1,2,10,20", s("[2,10,1,20].sort(function(a,b){return a-b;}).join(',');"));
    }

    @Test
    void stringMethods() {
        assertEquals("e", s("'hello'.charAt(1);"));
        assertEquals(101.0, n("'hello'.charCodeAt(1);"));
        assertEquals(2.0, n("'hello'.indexOf('l');"));
        assertEquals(3.0, n("'hello'.lastIndexOf('l');"));
        assertEquals("ell", s("'hello'.substring(1,4);"));
        assertEquals("ell", s("'hello'.slice(1,4);"));
        assertEquals("llo", s("'hello'.substr(2);"));
        assertEquals("HELLO", s("'hello'.toUpperCase();"));
        assertEquals("a,b,c", s("'a-b-c'.split('-').join(',');"));
        assertEquals("h.e.l.l.o", s("'hello'.split('').join('.');"));
        assertEquals("AB", s("String.fromCharCode(65,66);"));
        assertEquals("xyz", s("'x' + 'y' + 'z';"));
    }

    @Test
    void numberMethods() {
        assertEquals("ff", s("(255).toString(16);"));
        assertEquals("1010", s("(10).toString(2);"));
        assertEquals("3.14", s("(3.14159).toFixed(2);"));
        assertEquals("1000", s("(1000).toFixed(0);"));
        assertEquals("1.23e+2", s("(123).toExponential(2);"));
    }

    @Test
    void mathFunctions() {
        assertEquals(4.0, n("Math.max(1,4,2);"));
        assertEquals(1.0, n("Math.min(1,4,2);"));
        assertEquals(8.0, n("Math.pow(2,3);"));
        assertEquals(3.0, n("Math.floor(3.7);"));
        assertEquals(4.0, n("Math.ceil(3.2);"));
        assertEquals(3.0, n("Math.round(2.5);"));
        assertEquals(2.0, n("Math.round(2.4);"));
        assertEquals(5.0, n("Math.sqrt(25);"));
        assertEquals(5.0, n("Math.abs(-5);"));
        assertTrue(b("Math.PI > 3.14 && Math.PI < 3.15"));
    }

    @Test
    void globalFunctions() {
        assertEquals(255.0, n("parseInt('ff', 16);"));
        assertEquals(42.0, n("parseInt('42px');"));
        assertEquals(255.0, n("parseInt('0xff');"));
        assertEquals(3.14, n("parseFloat('3.14abc');"));
        assertTrue(b("isNaN(NaN) && !isNaN(1)"));
        assertTrue(b("isFinite(1) && !isFinite(Infinity)"));
        assertEquals("a%20b", s("encodeURIComponent('a b');"));
        assertEquals("a b", s("decodeURIComponent('a%20b');"));
    }

    @Test
    void evalRunsCode() {
        assertEquals(42.0, n("eval('40 + 2');"));
        assertEquals(3.0, n("var x = 1; eval('x = 3'); x;"));
    }

    /* ----------------------------- regex ----------------------------- */

    @Test
    void regexTestExecReplace() {
        assertTrue(b("/\\d+/.test('abc123');"));
        assertEquals("123", s("/\\d+/.exec('abc123')[0];"));
        assertEquals("a-b-c", s("'a b c'.replace(/ /g, '-');"));
        assertEquals("1XaY2", s("'1a2'.replace(/a/, 'X$&Y');")); // $& = matched text
        assertEquals("BAR foo", s("'foo BAR'.replace(/(\\w+) (\\w+)/, '$2 $1');"));
    }

    @Test
    void regexGlobalLastIndexState() {
        assertEquals(2.0, n(
                "var re = /a/g; var c = 0; while (re.exec('aXa')) c++; c;"));
        assertEquals("1", s(
                "var re = /a/g; re.exec('aa'); String(re.lastIndex);")); // first 'a' ends at index 1
        assertEquals("a,a", s("'aXa'.match(/a/g).join(',');"));
    }

    @Test
    void regexSplitWithCapture() {
        assertEquals("a,1,b,2,c", s("'a1b2c'.split(/(\\d)/).join(',');"));
    }

    /* ---------------------------- exceptions ------------------------- */

    @Test
    void tryCatchFinally() {
        assertEquals("try-finally", s(
                "var r = ''; try { r += 'try'; } finally { r += '-finally'; } r;"));
        assertEquals("catch:boom", s(
                "var r; try { throw 'boom'; } catch (e) { r = 'catch:' + e; } r;"));
        assertEquals("cf", s(
                "var r=''; try { throw 1; } catch(e){ r+='c'; } finally { r+='f'; } r;"));
    }

    @Test
    void finallyRunsOnReturn() {
        assertEquals("f", s(
                "var log=''; function g(){ try { return 'x'; } finally { log += 'f'; } } g(); log;"));
    }

    @Test
    void typedNativeErrors() {
        assertEquals("TypeError", s(
                "var t; try { null.x; } catch (e) { t = e.name; } t;"));
        assertEquals("ReferenceError", s(
                "var t; try { nope; } catch (e) { t = e.name; } t;"));
        assertTrue(b("(new TypeError('m')) instanceof Error"));
        assertEquals("Error: m", s("String(new Error('m'));"));
    }

    @Test
    void throwPropagatesThroughCalls() {
        assertEquals(99.0, n(
                "function deep(){ throw 99; } function mid(){ deep(); }"
                        + " var r; try { mid(); } catch (e) { r = e; } r;"));
    }

    /* ------------------------------- Date ---------------------------- */

    @Test
    void dateUtcArithmetic() {
        // Date.UTC and getTime are timezone-independent.
        assertEquals(0.0, n("Date.UTC(1970, 0, 1);"));
        assertEquals(86400000.0, n("Date.UTC(1970, 0, 2);"));
        assertEquals(2000.0, n("new Date(Date.UTC(2000,0,1)).getUTCFullYear();"));
        assertEquals(11.0, n("new Date(Date.UTC(2000,11,25)).getUTCMonth();"));
        assertEquals(86400000.0, n(
                "var a = new Date(Date.UTC(1970,0,1)); var b = new Date(Date.UTC(1970,0,2)); b.getTime() - a.getTime();"));
    }

    @Test
    void dateParseIso() {
        assertEquals(0.0, n("Date.parse('1970-01-01T00:00:00Z');"));
        assertTrue(b("isNaN(Date.parse('not a date'));"));
    }
}
