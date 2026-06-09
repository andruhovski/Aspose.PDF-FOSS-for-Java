package org.aspose.pdf.engine.parser;

import org.aspose.pdf.Operator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 63 — content-stream parser tolerance.
 *
 * <p>A content stream with an out-of-int-range operand or a malformed inline
 * dictionary must not abort the whole page; the parser returns the operators it
 * could read (best-effort, mirroring Acrobat / pdf.js).</p>
 */
public class ContentStreamToleranceTest {

    private static List<Operator> parse(String content) {
        return assertDoesNotThrow(() ->
                ContentStreamParser.parse(content.getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Test
    public void outOfRangeOperandDoesNotThrow() {
        // 3301174862 > 2^31: previously threw ArithmeticException in Operator.getNumber.
        List<Operator> ops = parse("100 100 m 3301174862 0 l S\n");
        assertFalse(ops.isEmpty(), "operators before/at the large-coordinate op should be returned");
    }

    @Test
    public void malformedDictKeyRecoversPartialContent() {
        // The "<< 0 ..." dict has an integer where a name key is required. The
        // text-showing operators before it must still be recovered.
        List<Operator> ops = parse("BT (hello) Tj ET\n<< 0 /Bad >> q\n");
        assertFalse(ops.isEmpty(), "operators before the malformed dict should be recovered");
        boolean sawTextShow = ops.stream().anyMatch(o ->
                o instanceof org.aspose.pdf.operators.TextShowOperator);
        assertTrue(sawTextShow, "the Tj before the malformed dict should have been parsed");
    }
}
