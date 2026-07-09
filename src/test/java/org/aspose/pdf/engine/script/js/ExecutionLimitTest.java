package org.aspose.pdf.engine.script.js;

import org.aspose.pdf.engine.script.js.interp.JsExecutionLimitError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bounded-execution guard (DoS hardening): an untrusted script containing
 * {@code while(true){}} or unbounded recursion must abort within the step budget
 * as a reported script failure — never hang the thread or escape as an uncaught
 * crash of the host. Normal scripts run to completion unaffected.
 */
public class ExecutionLimitTest {

    private String savedMaxSteps;

    @BeforeEach
    public void lowerBudgetForTest() {
        savedMaxSteps = System.getProperty("xfa.js.maxSteps");
        // small budget so the abort happens in milliseconds (default is 50M steps)
        System.setProperty("xfa.js.maxSteps", "200000");
    }

    @AfterEach
    public void restoreBudget() {
        if (savedMaxSteps == null) {
            System.clearProperty("xfa.js.maxSteps");
        } else {
            System.setProperty("xfa.js.maxSteps", savedMaxSteps);
        }
    }

    @Test
    @Timeout(30)
    public void infiniteLoopAbortsWithinBudget() {
        assertThrows(JsExecutionLimitError.class, () -> Engine.eval("while (true) {}"));
    }

    @Test
    @Timeout(30)
    public void infiniteLoopWithBodyAborts() {
        assertThrows(JsExecutionLimitError.class,
                () -> Engine.eval("var i = 0; for (;;) { i = i + 1; }"));
    }

    @Test
    @Timeout(30)
    public void scriptCatchCannotSwallowTheLimit() {
        // a script-level try/catch must NOT be able to eat the abort and keep looping
        assertThrows(JsExecutionLimitError.class,
                () -> Engine.eval("while (true) { try { var x = 1; } catch (e) {} }"));
    }

    @Test
    @Timeout(30)
    public void infiniteRecursionAbortsCleanly() {
        // recursion is stopped by the call-depth cap (catchable RangeError) or,
        // if the script keeps re-triggering it, by the step budget — either way
        // it terminates promptly as a RuntimeException, never a hang/StackOverflowError
        assertThrows(RuntimeException.class, () -> Engine.eval("function f() { f(); } f();"));
        assertThrows(RuntimeException.class, () -> Engine.eval(
                "function f() { try { f(); } catch (e) { f(); } } f();"));
    }

    @Test
    @Timeout(30)
    public void normalScriptWellUnderBudgetUnaffected() {
        assertEquals(4950.0, Engine.eval(
                "var s = 0; for (var i = 0; i < 100; i++) { s += i; } s;"));
        assertEquals("done", Engine.eval("var r = 'done'; r;"));
    }
}
