package org.aspose.pdf.tests;

import org.aspose.pdf.engine.font.cmap.CMapParser;
import org.aspose.pdf.engine.font.cmap.ToUnicodeCMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CMapParser} and {@link ToUnicodeCMap}.
 */
public class CMapParserTest {

    @Test
    public void testBfCharSimple() throws IOException {
        String cmap = "1 beginbfchar\n<0041> <0041>\nendbfchar";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("A", map.lookup(0x41));
    }

    @Test
    public void testBfCharMultiple() throws IOException {
        String cmap = "2 beginbfchar\n<0041> <0041>\n<0042> <0042>\nendbfchar";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("A", map.lookup(0x41));
        assertEquals("B", map.lookup(0x42));
    }

    @Test
    public void testBfRangeSimple() throws IOException {
        String cmap = "1 beginbfrange\n<0020> <007E> <0020>\nendbfrange";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals(" ", map.lookup(0x20));
        assertEquals("!", map.lookup(0x21));
        assertEquals("A", map.lookup(0x41));
        assertEquals("~", map.lookup(0x7E));
    }

    @Test
    public void testBfRangeWithArray() throws IOException {
        String cmap = "1 beginbfrange\n<0041> <0043> [<0061> <0062> <0063>]\nendbfrange";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("a", map.lookup(0x41));
        assertEquals("b", map.lookup(0x42));
        assertEquals("c", map.lookup(0x43));
    }

    @Test
    public void testTwoByteCode() throws IOException {
        String cmap = "1 beginbfchar\n<0100> <0100>\nendbfchar";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("\u0100", map.lookup(0x100));
    }

    @Test
    public void testAbsurdBfRangeSpanIsClamped() throws IOException {
        // Corrupt bfrange covering 2 billion codes (corpus 38236.PDF): must
        // not materialise billions of entries (was an OutOfMemoryError plus
        // minutes of CPU inside the expansion loop). The clamp keeps the
        // first 64k mappings and drops the corrupt tail.
        String cmap = "1 beginbfrange\n<0000> <7FFFFFFF> <0041>\nendbfrange";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("A", map.lookup(0x00));
        assertEquals("B", map.lookup(0x01));
    }

    @Test
    public void testBfRangeHiBelowLoIsSingleEntry() throws IOException {
        // hi < lo must not desync the scan: the dst token is still consumed
        // and the following entry parses normally.
        String cmap = "2 beginbfrange\n<0042> <0041> <0061>\n<0050> <0051> <0070>\nendbfrange";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("a", map.lookup(0x42));
        assertEquals("p", map.lookup(0x50));
        assertEquals("q", map.lookup(0x51));
    }

    @Test
    public void testEmptyCMap() throws IOException {
        ToUnicodeCMap map = CMapParser.parseToUnicode(new byte[0]);
        assertNotNull(map);
        assertEquals(0, map.size());
        assertNull(map.lookup(0x41));
    }

    @Test
    public void testNullCMap() throws IOException {
        ToUnicodeCMap map = CMapParser.parseToUnicode((byte[]) null);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void testMultiCodepointLigature() throws IOException {
        // "fi" ligature: <FB01> maps to <0066 0069> = "fi"
        String cmap = "1 beginbfchar\n<FB01> <00660069>\nendbfchar";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        String result = map.lookup(0xFB01);
        assertNotNull(result);
        assertEquals("fi", result);
    }

    @Test
    public void testContains() throws IOException {
        String cmap = "1 beginbfchar\n<0041> <0041>\nendbfchar";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(map.contains(0x41));
        assertFalse(map.contains(0x42));
    }

    @Test
    public void testCompleteCMap() throws IOException {
        String cmap = "/CIDInit /ProcSet findresource begin\n" +
                "12 dict begin\n" +
                "begincmap\n" +
                "/CIDSystemInfo\n" +
                "<< /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n" +
                "/CMapName /Adobe-Identity-UCS def\n" +
                "/CMapType 2 def\n" +
                "1 begincodespacerange\n" +
                "<0000> <FFFF>\n" +
                "endcodespacerange\n" +
                "3 beginbfchar\n" +
                "<0003> <0020>\n" +
                "<0011> <002E>\n" +
                "<0024> <0041>\n" +
                "endbfchar\n" +
                "endcmap\n" +
                "CMapName currentdict /CMap defineresource pop\n" +
                "end\n" +
                "end\n";
        ToUnicodeCMap map = CMapParser.parseToUnicode(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals(" ", map.lookup(3));
        assertEquals(".", map.lookup(0x11));
        assertEquals("A", map.lookup(0x24));
    }
}
