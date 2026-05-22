package org.aspose.pdf.tests;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.font.FontEncoding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FontEncoding}.
 */
public class FontEncodingTest {

    @Test
    public void testWinAnsiBasicAscii() {
        assertEquals("A", FontEncoding.WIN_ANSI.getGlyphName(65));
        assertEquals(0x0041, FontEncoding.WIN_ANSI.getUnicode(65));
    }

    @Test
    public void testWinAnsiQuoteDblleft() {
        assertEquals("quotedblleft", FontEncoding.WIN_ANSI.getGlyphName(0x93));
        assertEquals(0x201C, FontEncoding.WIN_ANSI.getUnicode(0x93));
    }

    @Test
    public void testWinAnsiEuro() {
        assertEquals("Euro", FontEncoding.WIN_ANSI.getGlyphName(0x80));
        assertEquals(0x20AC, FontEncoding.WIN_ANSI.getUnicode(0x80));
    }

    @Test
    public void testMacRomanNbsp() {
        // MacRoman 0xCA = space (NBSP)
        assertEquals("space", FontEncoding.MAC_ROMAN.getGlyphName(0xCA));
        assertEquals(0x0020, FontEncoding.MAC_ROMAN.getUnicode(0xCA));
    }

    @Test
    public void testDifferences() {
        COSArray diff = new COSArray();
        diff.add(COSInteger.valueOf(128));
        diff.add(COSName.of("Euro"));
        diff.add(COSName.of("trademark"));

        FontEncoding custom = FontEncoding.WIN_ANSI.withDifferences(diff);
        assertEquals("Euro", custom.getGlyphName(128));
        assertEquals("trademark", custom.getGlyphName(129));
        assertEquals(0x20AC, custom.getUnicode(128));
        assertEquals(0x2122, custom.getUnicode(129));
        // Unchanged entries should still work
        assertEquals(0x0041, custom.getUnicode(65));
    }

    @Test
    public void testGetInstanceSameInstance() {
        assertSame(FontEncoding.getInstance("WinAnsiEncoding"),
                   FontEncoding.getInstance("WinAnsiEncoding"));
    }

    @Test
    public void testGetInstanceUnknown() {
        assertNull(FontEncoding.getInstance("Unknown"));
        assertNull(FontEncoding.getInstance(null));
    }

    @Test
    public void testGetInstanceAll() {
        assertSame(FontEncoding.WIN_ANSI, FontEncoding.getInstance("WinAnsiEncoding"));
        assertSame(FontEncoding.MAC_ROMAN, FontEncoding.getInstance("MacRomanEncoding"));
        assertSame(FontEncoding.STANDARD, FontEncoding.getInstance("StandardEncoding"));
    }

    @Test
    public void testUnmappedCodeReturnsIdentity() {
        // Code 0 is typically unmapped — should return charCode as identity
        int unicode = FontEncoding.WIN_ANSI.getUnicode(0);
        assertEquals(0, unicode);
    }

    @Test
    public void testFromDictionary() {
        COSDictionary encDict = new COSDictionary();
        encDict.set(COSName.of("BaseEncoding"), COSName.of("WinAnsiEncoding"));
        COSArray diff = new COSArray();
        diff.add(COSInteger.valueOf(200));
        diff.add(COSName.of("Euro"));
        encDict.set(COSName.of("Differences"), diff);

        FontEncoding enc = FontEncoding.fromDictionary(encDict);
        assertNotNull(enc);
        assertEquals("Euro", enc.getGlyphName(200));
        assertEquals(0x20AC, enc.getUnicode(200));
    }

    @Test
    public void testFromDictionaryNoBase() {
        COSDictionary encDict = new COSDictionary();
        // No /BaseEncoding → defaults to StandardEncoding
        FontEncoding enc = FontEncoding.fromDictionary(encDict);
        assertNotNull(enc);
    }

    @Test
    public void testStandardEncoding() {
        assertEquals("space", FontEncoding.STANDARD.getGlyphName(0x20));
        assertEquals("A", FontEncoding.STANDARD.getGlyphName(0x41));
        assertEquals("fi", FontEncoding.STANDARD.getGlyphName(0xAE));
    }

    @Test
    public void testWinAnsiAllAccented() {
        // Check a range of accented characters
        assertEquals("Agrave", FontEncoding.WIN_ANSI.getGlyphName(0xC0));
        assertEquals("ydieresis", FontEncoding.WIN_ANSI.getGlyphName(0xFF));
        assertEquals(0x00C0, FontEncoding.WIN_ANSI.getUnicode(0xC0));
        assertEquals(0x00FF, FontEncoding.WIN_ANSI.getUnicode(0xFF));
    }
}
