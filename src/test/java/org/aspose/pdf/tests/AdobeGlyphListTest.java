package org.aspose.pdf.tests;

import org.aspose.pdf.engine.font.AdobeGlyphList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AdobeGlyphList}.
 */
public class AdobeGlyphListTest {

    @Test
    public void testBasicLatinUppercase() {
        assertEquals(0x0041, AdobeGlyphList.getUnicode("A"));
        assertEquals(0x005A, AdobeGlyphList.getUnicode("Z"));
    }

    @Test
    public void testBasicLatinLowercase() {
        assertEquals(0x0061, AdobeGlyphList.getUnicode("a"));
        assertEquals(0x007A, AdobeGlyphList.getUnicode("z"));
    }

    @Test
    public void testSpace() {
        assertEquals(0x0020, AdobeGlyphList.getUnicode("space"));
    }

    @Test
    public void testLigatures() {
        assertEquals(0xFB01, AdobeGlyphList.getUnicode("fi"));
        assertEquals(0xFB02, AdobeGlyphList.getUnicode("fl"));
    }

    @Test
    public void testTypographic() {
        assertEquals(0x2022, AdobeGlyphList.getUnicode("bullet"));
        assertEquals(0x2013, AdobeGlyphList.getUnicode("endash"));
        assertEquals(0x2014, AdobeGlyphList.getUnicode("emdash"));
        assertEquals(0x2026, AdobeGlyphList.getUnicode("ellipsis"));
        assertEquals(0x201C, AdobeGlyphList.getUnicode("quotedblleft"));
        assertEquals(0x201D, AdobeGlyphList.getUnicode("quotedblright"));
    }

    @Test
    public void testCurrency() {
        assertEquals(0x20AC, AdobeGlyphList.getUnicode("Euro"));
        assertEquals(0x00A3, AdobeGlyphList.getUnicode("sterling"));
        assertEquals(0x00A5, AdobeGlyphList.getUnicode("yen"));
        assertEquals(0x00A2, AdobeGlyphList.getUnicode("cent"));
    }

    @Test
    public void testAccented() {
        assertEquals(0x00C0, AdobeGlyphList.getUnicode("Agrave"));
        assertEquals(0x00E9, AdobeGlyphList.getUnicode("eacute"));
        assertEquals(0x00FC, AdobeGlyphList.getUnicode("udieresis"));
    }

    @Test
    public void testNonexistent() {
        assertEquals(-1, AdobeGlyphList.getUnicode("nonexistent"));
        assertEquals(-1, AdobeGlyphList.getUnicode(null));
    }

    @Test
    public void testContains() {
        assertTrue(AdobeGlyphList.contains("bullet"));
        assertTrue(AdobeGlyphList.contains("A"));
        assertFalse(AdobeGlyphList.contains("doesNotExist"));
        assertFalse(AdobeGlyphList.contains(null));
    }

    @Test
    public void testDigits() {
        assertEquals(0x0030, AdobeGlyphList.getUnicode("zero"));
        assertEquals(0x0039, AdobeGlyphList.getUnicode("nine"));
    }

    @Test
    public void testMathSymbols() {
        assertEquals(0x00D7, AdobeGlyphList.getUnicode("multiply"));
        assertEquals(0x00F7, AdobeGlyphList.getUnicode("divide"));
        assertEquals(0x00B1, AdobeGlyphList.getUnicode("plusminus"));
    }

    @Test
    public void testUniPattern() {
        // "uniXXXX" pattern should resolve to Unicode codepoint
        assertEquals(0x0041, AdobeGlyphList.getUnicode("uni0041"));
        assertEquals(0x20AC, AdobeGlyphList.getUnicode("uni20AC"));
    }

    @Test
    public void testGreek() {
        assertEquals(0x03B1, AdobeGlyphList.getUnicode("alpha"));
        assertEquals(0x03A9, AdobeGlyphList.getUnicode("Omega"));
        assertEquals(0x03C0, AdobeGlyphList.getUnicode("pi"));
    }

    @Test
    public void testTrademark() {
        assertEquals(0x2122, AdobeGlyphList.getUnicode("trademark"));
        assertEquals(0x00A9, AdobeGlyphList.getUnicode("copyright"));
        assertEquals(0x00AE, AdobeGlyphList.getUnicode("registered"));
    }
}
