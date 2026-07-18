package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfString].
public class PdfStringTest {

    @Test
    public void getStringFromText() {
        assertEquals("Hello", new PdfString("Hello").getString());
    }

    @Test
    public void writeToLiteral() throws IOException {
        assertWritesTo("(Hello)", new PdfString("Hello"));
    }

    @Test
    public void writeToWithParens() throws IOException {
        assertWritesTo("(a\\(b\\)c)", new PdfString("a(b)c"));
    }

    @Test
    public void writeToWithBackslash() throws IOException {
        assertWritesTo("(a\\\\b)", new PdfString("a\\b"));
    }

    @Test
    public void writeToWithNewline() throws IOException {
        assertWritesTo("(a\\nb)", new PdfString("a\nb"));
    }

    @Test
    public void fromHex() {
        PdfString s = PdfString.fromHex("48656C6C6F");
        assertEquals("Hello", s.getString());
    }

    @Test
    public void writeToHex() throws IOException {
        PdfString s = PdfString.fromHex("48656C6C6F");
        s.setForceHex(true);
        assertWritesTo("<48656C6C6F>", s);
    }

    @Test
    public void unicodeStringBOM() {
        // "Привет" requires UTF-16BE with BOM
        PdfString s = new PdfString("\u041F\u0440\u0438\u0432\u0435\u0442");
        byte[] bytes = s.getBytes();
        assertEquals((byte) 0xFE, bytes[0]);
        assertEquals((byte) 0xFF, bytes[1]);
        assertEquals("\u041F\u0440\u0438\u0432\u0435\u0442", s.getString());
    }

    @Test
    public void getAsDateValid() {
        PdfString s = new PdfString("D:20231215120000+03'00'");
        LocalDateTime dt = s.getAsDate();
        assertNotNull(dt);
        assertEquals(2023, dt.getYear());
        assertEquals(12, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(12, dt.getHour());
    }

    @Test
    public void getAsDateInvalid() {
        assertNull(new PdfString("invalid").getAsDate());
    }

    @Test
    public void emptyString() throws IOException {
        PdfString s = new PdfString("");
        assertEquals("", s.getString());
        assertWritesTo("()", s);
    }

    @Test
    public void nullByteOctalEscape() throws IOException {
        PdfString s = new PdfString(new byte[]{0x00, 0x41});
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.writeTo(baos);
        String result = baos.toString("US-ASCII");
        assertEquals("(\\000A)", result);
    }

    @Test
    public void pdfDocEncoding() {
        // Byte 0xA9 in PDFDocEncoding = © (same as Latin-1 for this range)
        PdfString s = new PdfString(new byte[]{(byte) 0xA9});
        assertEquals("\u00A9", s.getString());
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(new PdfString("Hello"), new PdfString("Hello"));
        assertNotEquals(new PdfString("Hello"), new PdfString("World"));
    }

    @Test
    public void fromHexOddLength() {
        // Odd hex string should be padded
        PdfString s = PdfString.fromHex("A");
        byte[] bytes = s.getBytes();
        assertEquals(1, bytes.length);
        assertEquals((byte) 0xA0, bytes[0]);
    }

    @Test
    public void fromHexWithWhitespace() {
        PdfString s = PdfString.fromHex("48 65 6C 6C 6F");
        assertEquals("Hello", s.getString());
    }

    private void assertWritesTo(String expected, PdfString str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        str.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
