package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ContentStreamParser}.
 */
public class ContentStreamParserTest {

    @Test
    public void parseSimpleTextStream() throws IOException {
        String content = "BT /F1 12 Tf 100 700 Td (Hello World) Tj ET";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(5, ops.size());

        // BT — no operands
        assertEquals("BT", ops.get(0).getName());
        assertTrue(ops.get(0).getOperands().isEmpty());

        // /F1 12 Tf
        assertEquals("Tf", ops.get(1).getName());
        assertEquals(2, ops.get(1).getOperands().size());
        assertTrue(ops.get(1).getOperands().get(0) instanceof COSName);
        assertEquals("F1", ((COSName) ops.get(1).getOperands().get(0)).getName());
        assertTrue(ops.get(1).getOperands().get(1) instanceof COSInteger);
        assertEquals(12, ((COSInteger) ops.get(1).getOperands().get(1)).intValue());

        // 100 700 Td
        assertEquals("Td", ops.get(2).getName());
        assertEquals(2, ops.get(2).getOperands().size());
        assertEquals(100, ((COSInteger) ops.get(2).getOperands().get(0)).intValue());
        assertEquals(700, ((COSInteger) ops.get(2).getOperands().get(1)).intValue());

        // (Hello World) Tj
        assertEquals("Tj", ops.get(3).getName());
        assertEquals(1, ops.get(3).getOperands().size());
        assertTrue(ops.get(3).getOperands().get(0) instanceof COSString);
        assertEquals("Hello World", ((COSString) ops.get(3).getOperands().get(0)).getString());

        // ET — no operands
        assertEquals("ET", ops.get(4).getName());
        assertTrue(ops.get(4).getOperands().isEmpty());
    }

    @Test
    public void parsePathOperations() throws IOException {
        String content = "0 0 100 100 re S";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(2, ops.size());

        // 0 0 100 100 re
        assertEquals("re", ops.get(0).getName());
        assertEquals(4, ops.get(0).getOperands().size());
        assertEquals(0, ((COSInteger) ops.get(0).getOperands().get(0)).intValue());
        assertEquals(0, ((COSInteger) ops.get(0).getOperands().get(1)).intValue());
        assertEquals(100, ((COSInteger) ops.get(0).getOperands().get(2)).intValue());
        assertEquals(100, ((COSInteger) ops.get(0).getOperands().get(3)).intValue());

        // S
        assertEquals("S", ops.get(1).getName());
        assertTrue(ops.get(1).getOperands().isEmpty());
    }

    @Test
    public void parseGraphicsState() throws IOException {
        String content = "q 1 0 0 1 50 50 cm Q";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(3, ops.size());
        assertEquals("q", ops.get(0).getName());
        assertEquals("cm", ops.get(1).getName());
        assertEquals(6, ops.get(1).getOperands().size());
        assertEquals("Q", ops.get(2).getName());
    }

    @Test
    public void parseRealNumbers() throws IOException {
        String content = "0.5 0.75 1.0 RG";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, ops.size());
        assertEquals("RG", ops.get(0).getName());
        assertEquals(3, ops.get(0).getOperands().size());
        // 0.5 parses as REAL
        COSBase first = ops.get(0).getOperands().get(0);
        assertTrue(first instanceof COSFloat);
        assertEquals(0.5, ((COSFloat) first).doubleValue(), 1e-10);
    }

    @Test
    public void parseArrayOperand() throws IOException {
        // TJ operator takes an array argument
        String content = "BT [(Hello ) -100 (World)] TJ ET";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(3, ops.size());
        assertEquals("BT", ops.get(0).getName());

        assertEquals("TJ", ops.get(1).getName());
        assertEquals(1, ops.get(1).getOperands().size());
        assertTrue(ops.get(1).getOperands().get(0) instanceof COSArray);
        COSArray arr = (COSArray) ops.get(1).getOperands().get(0);
        assertEquals(3, arr.size());
        assertTrue(arr.get(0) instanceof COSString);
        assertTrue(arr.get(1) instanceof COSInteger);
        assertEquals(-100, ((COSInteger) arr.get(1)).intValue());
        assertTrue(arr.get(2) instanceof COSString);

        assertEquals("ET", ops.get(2).getName());
    }

    @Test
    public void parseEmptyStream() throws IOException {
        List<Operator> ops = ContentStreamParser.parse(new byte[0]);
        assertTrue(ops.isEmpty());
    }

    @Test
    public void parseNullStreamThrows() {
        assertThrows(IllegalArgumentException.class, () -> ContentStreamParser.parse(null));
    }

    @Test
    public void parseToCollectionFromCOSStream() throws IOException {
        String content = "BT /F1 12 Tf ET";
        COSStream stream = new COSStream(content.getBytes(StandardCharsets.US_ASCII));
        OperatorCollection coll = ContentStreamParser.parseToCollection(stream);

        assertEquals(3, coll.size());
        assertEquals("BT", coll.get(1).getName());
        assertEquals("Tf", coll.get(2).getName());
        assertEquals("ET", coll.get(3).getName());
    }

    @Test
    public void parseToCollectionNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> ContentStreamParser.parseToCollection((org.aspose.pdf.engine.cos.COSStream) null));
    }

    @Test
    public void parseHexString() throws IOException {
        String content = "<48656C6C6F> Tj";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, ops.size());
        assertEquals("Tj", ops.get(0).getName());
        assertTrue(ops.get(0).getOperands().get(0) instanceof COSString);
        assertEquals("Hello", ((COSString) ops.get(0).getOperands().get(0)).getString());
    }

    @Test
    public void parseMultipleOperatorsOnSeparateLines() throws IOException {
        String content = "q\n1 0 0 1 0 0 cm\n/Im1 Do\nQ";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(4, ops.size());
        assertEquals("q", ops.get(0).getName());
        assertEquals("cm", ops.get(1).getName());
        assertEquals("Do", ops.get(2).getName());
        assertEquals(1, ops.get(2).getOperands().size());
        assertEquals("Im1", ((COSName) ops.get(2).getOperands().get(0)).getName());
        assertEquals("Q", ops.get(3).getName());
    }

    @Test
    public void parseNegativeNumbers() throws IOException {
        String content = "-10 -20.5 m";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, ops.size());
        assertEquals("m", ops.get(0).getName());
        assertEquals(2, ops.get(0).getOperands().size());
        assertEquals(-10, ((COSInteger) ops.get(0).getOperands().get(0)).intValue());
        assertEquals(-20.5, ((COSFloat) ops.get(0).getOperands().get(1)).doubleValue(), 1e-10);
    }

    @Test
    public void parseMalformedStandaloneRealAsZeroForRecovery() throws IOException {
        String content = ". 10 Td";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, ops.size());
        assertEquals("Td", ops.get(0).getName());
        assertEquals(2, ops.get(0).getOperands().size());
        assertTrue(ops.get(0).getOperands().get(0) instanceof COSFloat);
        assertEquals(0.0, ((COSFloat) ops.get(0).getOperands().get(0)).doubleValue(), 1e-10);
        assertEquals(10, ((COSInteger) ops.get(0).getOperands().get(1)).intValue());
    }

    @Test
    public void parseUnexpectedKeywordInsideArrayAsStringForRecovery() throws IOException {
        String content = "[(Hel) o (lo)] TJ";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, ops.size());
        assertEquals("TJ", ops.get(0).getName());
        assertTrue(ops.get(0).getOperands().get(0) instanceof COSArray);
        COSArray array = (COSArray) ops.get(0).getOperands().get(0);
        assertEquals(3, array.size());
        assertEquals("Hel", ((COSString) array.get(0)).getString());
        assertEquals("o", ((COSString) array.get(1)).getString());
        assertEquals("lo", ((COSString) array.get(2)).getString());
    }

    @Test
    public void parseUnexpectedEofInsideArrayReturnsPartialArrayForRecovery() throws IOException {
        String content = "[(Hel) 10";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertTrue(ops.isEmpty());
    }

    @Test
    public void parseUnexpectedEofInsideInlineImageReturnsRecoveredBiOperator() throws IOException {
        byte[] content = "BI /W 1 /H 1 /BPC 8 /CS /G /F /Fl ID abc".getBytes(StandardCharsets.US_ASCII);
        List<Operator> ops = ContentStreamParser.parse(content);

        assertEquals(1, ops.size());
        assertEquals("BI", ops.get(0).getName());
        assertEquals(2, ops.get(0).getOperands().size());
        assertTrue(ops.get(0).getOperands().get(0) instanceof org.aspose.pdf.engine.cos.COSDictionary);
        assertTrue(ops.get(0).getOperands().get(1) instanceof COSString);
        assertTrue(((COSString) ops.get(0).getOperands().get(1)).getBytes().length > 0);
    }

    @Test
    public void parseColorOperators() throws IOException {
        String content = "0 0 0 rg 1 1 1 RG";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(2, ops.size());
        assertEquals("rg", ops.get(0).getName());
        assertEquals(3, ops.get(0).getOperands().size());
        assertEquals("RG", ops.get(1).getName());
        assertEquals(3, ops.get(1).getOperands().size());
    }

    @Test
    public void parseWithComments() throws IOException {
        String content = "BT\n% This is a comment\n/F1 12 Tf\nET";
        List<Operator> ops = ContentStreamParser.parse(content.getBytes(StandardCharsets.US_ASCII));

        assertEquals(3, ops.size());
        assertEquals("BT", ops.get(0).getName());
        assertEquals("Tf", ops.get(1).getName());
        assertEquals("ET", ops.get(2).getName());
    }
}
