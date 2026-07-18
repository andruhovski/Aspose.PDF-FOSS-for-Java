package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/// Sprint 30 Part A — [Operator#writeTo] must serialize operands byte-for-byte,
/// unlike [Operator#toString()] which routes through US-ASCII and corrupts
/// bytes ≥ 0x80 (CID/Identity-H glyph codes, non-Latin literals).
public class OperatorWriteToTest {

    private static byte[] serialize(Operator op) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        op.writeTo(baos);
        baos.write('\n');
        return baos.toByteArray();
    }

    @Test
    void writeTo_asciiTjOperand_roundTrips() throws Exception {
        PdfString text = new PdfString("Hello".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        Operator op = new Operator("Tj", Collections.singletonList(text));

        OperatorCollection rt = ContentStreamParser.parseToCollection(serialize(op));
        assertEquals(1, rt.size());
        assertEquals("Tj", rt.getAt(0).getName());
        PdfString restored = (PdfString) rt.getAt(0).getOperands().get(0);
        assertArrayEquals("Hello".getBytes(), restored.getBytes());
    }

    @Test
    void writeTo_nonAsciiCidOperand_preservesBytes() throws Exception {
        byte[] cidBytes = {(byte) 0x80, (byte) 0xA1, (byte) 0xFF, 0x41, 0x42};
        PdfString text = new PdfString(cidBytes);
        Operator op = new Operator("Tj", Collections.singletonList(text));

        OperatorCollection rt = ContentStreamParser.parseToCollection(serialize(op));
        assertEquals(1, rt.size());
        PdfString restored = (PdfString) rt.getAt(0).getOperands().get(0);
        assertArrayEquals(cidBytes, restored.getBytes(),
                "Non-ASCII bytes must survive operator serialization round-trip");
    }

    @Test
    void writeTo_tjArray_preservesAllStringBytes() throws Exception {
        PdfArray arr = new PdfArray();
        arr.add(new PdfString("hello".getBytes()));
        arr.add(PdfInteger.valueOf(-250));
        arr.add(new PdfString(new byte[]{(byte) 0xC0, (byte) 0xFE}));
        Operator op = new Operator("TJ", Collections.singletonList(arr));

        OperatorCollection rt = ContentStreamParser.parseToCollection(serialize(op));
        assertEquals(1, rt.size());
        assertEquals("TJ", rt.getAt(0).getName());
        PdfArray ra = (PdfArray) rt.getAt(0).getOperands().get(0);
        assertArrayEquals("hello".getBytes(), ((PdfString) ra.get(0)).getBytes());
        assertArrayEquals(new byte[]{(byte) 0xC0, (byte) 0xFE}, ((PdfString) ra.get(2)).getBytes(),
                "High bytes inside a TJ array must survive");
    }

    @Test
    void writeTo_matchesToString_forPurelyAscii() throws Exception {
        PdfString text = new PdfString("plain ASCII text".getBytes());
        Operator op = new Operator("Tj", Collections.singletonList(text));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        op.writeTo(baos);
        String fromWriteTo = baos.toString("US-ASCII").trim();
        assertEquals(op.toString().trim(), fromWriteTo,
                "writeTo() must equal toString() for ASCII-only operators");
    }

    @Test
    void writeTo_noOperands_writesNameOnly() throws Exception {
        Operator op = new Operator("BT");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        op.writeTo(baos);
        assertEquals("BT", baos.toString("US-ASCII"));
    }
}
