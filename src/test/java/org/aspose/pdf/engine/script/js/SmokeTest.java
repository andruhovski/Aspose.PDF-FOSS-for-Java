package org.aspose.pdf.engine.script.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Quick smoke checks for the ES3 engine bring-up. */
public class SmokeTest {

    private static Object run(String s) {
        return Engine.eval(s);
    }

    @Test
    void arithmetic() {
        assertEquals(42.0, run("var x = 2; x * 21;"));
    }

    @Test
    void stringConcat() {
        assertEquals("ab1", run("'a' + 'b' + 1;"));
    }

    @Test
    void closure() {
        assertEquals(3.0, run(
                "function adder(n){ return function(x){ return x + n; }; }"
                        + " var add2 = adder(2); add2(1);"));
    }

    @Test
    void prototypeInheritance() {
        assertEquals("hi from A", run(
                "function A(){} A.prototype.greet = function(){ return 'hi from A'; };"
                        + " var a = new A(); a.greet();"));
    }

    @Test
    void arrayAndJoin() {
        assertEquals("1,2,3", run("[1,2,3].join(',');"));
    }

    @Test
    void tryCatch() {
        assertEquals("caught: boom", run(
                "var r; try { throw new Error('boom'); } catch (e) { r = 'caught: ' + e.message; } r;"));
    }

    @Test
    void regexReplace() {
        assertEquals("hello-world", run("'hello world'.replace(/ /g, '-');"));
    }
}
