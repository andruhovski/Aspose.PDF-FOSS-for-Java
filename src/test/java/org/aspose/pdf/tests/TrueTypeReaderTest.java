package org.aspose.pdf.tests;

import org.aspose.pdf.engine.font.ttf.TrueTypeReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TrueTypeReader}.
 */
public class TrueTypeReaderTest {

    @Test
    public void testInvalidDataThrows() {
        assertThrows(IOException.class, () -> new TrueTypeReader(new byte[]{1, 2, 3}));
    }

    @Test
    public void testNullDataThrows() {
        assertThrows(IOException.class, () -> new TrueTypeReader(null));
    }

    /**
     * Build a minimal valid sfnt structure for testing.
     * Contains just a head table with unitsPerEm and maxp with numGlyphs.
     */
    @Test
    public void testMinimalSfnt() throws IOException {
        byte[] sfnt = buildMinimalSfnt(1000, 100);
        TrueTypeReader reader = new TrueTypeReader(sfnt);
        assertEquals(1000, reader.getUnitsPerEm());
        assertEquals(100, reader.getNumGlyphs());
    }

    @Test
    public void testDefaultGlyphId() throws IOException {
        byte[] sfnt = buildMinimalSfnt(2048, 10);
        TrueTypeReader reader = new TrueTypeReader(sfnt);
        // No cmap table → all glyph lookups return 0
        assertEquals(0, reader.getGlyphId(65));
    }

    /**
     * A font with no cmap is exactly the case where {@code java.awt.Font}
     * silently substitutes Arial (corpus APS/37100): the glyph outline must be
     * recoverable straight from the {@code glyf}/{@code loca} tables instead.
     * Builds a 2-glyph font whose glyph 1 is a rectangle in font units and
     * asserts the returned outline is em-normalised and Y-up.
     */
    @Test
    public void testGlyphOutlineFromGlyfLoca() throws IOException {
        byte[] sfnt = buildSfntWithSquareGlyph(1000);
        TrueTypeReader reader = new TrueTypeReader(sfnt);
        assertEquals(2, reader.getNumGlyphs());

        // Glyph 0 is empty (.notdef placeholder) → empty path, not null.
        java.awt.geom.GeneralPath g0 = reader.getGlyphPath(0);
        assertNotNull(g0);
        assertTrue(g0.getCurrentPoint() == null || g0.getBounds2D().isEmpty());

        // Glyph 1 is a 100..300 × 200..700 rectangle in font units (em 1000),
        // so the normalised outline spans x:[0.1,0.3], y:[0.2,0.7].
        java.awt.geom.GeneralPath g1 = reader.getGlyphPath(1);
        assertNotNull(g1);
        java.awt.geom.Rectangle2D b = g1.getBounds2D();
        assertEquals(0.1, b.getMinX(), 1e-6);
        assertEquals(0.2, b.getMinY(), 1e-6);
        assertEquals(0.3, b.getMaxX(), 1e-6);
        assertEquals(0.7, b.getMaxY(), 1e-6);
    }

    @Test
    public void testGlyphOutlineOutOfRangeReturnsNull() throws IOException {
        TrueTypeReader reader = new TrueTypeReader(buildSfntWithSquareGlyph(2048));
        assertNull(reader.getGlyphPath(99));
        assertNull(reader.getGlyphPath(-1));
    }

    /** No glyf/loca tables → outline lookup yields null (caller falls back). */
    @Test
    public void testGlyphOutlineAbsentTablesReturnsNull() throws IOException {
        TrueTypeReader reader = new TrueTypeReader(buildMinimalSfnt(1000, 10));
        assertNull(reader.getGlyphPath(1));
    }

    /**
     * Builds a valid sfnt with head, maxp, loca (short) and glyf tables. Glyph 0
     * is empty; glyph 1 is a four-point rectangle (100,200)-(300,700).
     */
    private byte[] buildSfntWithSquareGlyph(int unitsPerEm) {
        // glyf for glyph 1 (34 bytes, even length for short loca).
        ByteBuffer gb = ByteBuffer.allocate(34);
        gb.putShort((short) 1);                 // numberOfContours
        gb.putShort((short) 100); gb.putShort((short) 200);  // xMin,yMin
        gb.putShort((short) 300); gb.putShort((short) 700);  // xMax,yMax
        gb.putShort((short) 3);                 // endPtsOfContours[0] = last point idx
        gb.putShort((short) 0);                 // instructionLength
        for (int i = 0; i < 4; i++) gb.put((byte) 0x01); // flags: on-curve, 16-bit x/y
        // x deltas (16-bit): 100, +200, 0, -200  → 100,300,300,100
        gb.putShort((short) 100); gb.putShort((short) 200); gb.putShort((short) 0); gb.putShort((short) -200);
        // y deltas (16-bit): 200, 0, +500, 0     → 200,200,700,700
        gb.putShort((short) 200); gb.putShort((short) 0); gb.putShort((short) 500); gb.putShort((short) 0);
        byte[] glyf = gb.array();

        int numGlyphs = 2;
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.putInt(0x00010000);   // sfVersion
        buf.putShort((short) 4);  // numTables
        buf.putShort((short) 0); buf.putShort((short) 0); buf.putShort((short) 0);

        int dir = 12;
        int headOffset = dir + 4 * 16;          // 76
        int maxpOffset = headOffset + 54;        // 130
        int locaOffset = maxpOffset + 6;         // 136
        int glyfOffset = locaOffset + (numGlyphs + 1) * 2; // 142

        // Table directory (tag order is irrelevant to the reader).
        buf.put("head".getBytes()); buf.putInt(0); buf.putInt(headOffset); buf.putInt(54);
        buf.put("maxp".getBytes()); buf.putInt(0); buf.putInt(maxpOffset); buf.putInt(6);
        buf.put("loca".getBytes()); buf.putInt(0); buf.putInt(locaOffset); buf.putInt((numGlyphs + 1) * 2);
        buf.put("glyf".getBytes()); buf.putInt(0); buf.putInt(glyfOffset); buf.putInt(glyf.length);

        // head (54 bytes): unitsPerEm at +18, indexToLocFormat at +50 (0 = short).
        buf.position(headOffset);
        buf.putInt(0x00010000); buf.putInt(0x00010000); buf.putInt(0); buf.putInt(0x5F0F3CF5);
        buf.putShort((short) 0); buf.putShort((short) unitsPerEm);
        for (int i = 0; i < 36; i++) buf.put((byte) 0); // remainder (indexToLocFormat stays 0)

        // maxp (6 bytes)
        buf.position(maxpOffset);
        buf.putInt(0x00010000); buf.putShort((short) numGlyphs);

        // loca (short): offset/2 per entry. glyph0 empty (0..0), glyph1 (0..34).
        buf.position(locaOffset);
        buf.putShort((short) 0);            // glyph 0 start
        buf.putShort((short) 0);            // glyph 1 start (glyph 0 empty)
        buf.putShort((short) (glyf.length / 2)); // end of glyph 1

        // glyf
        buf.position(glyfOffset);
        buf.put(glyf);

        byte[] result = new byte[glyfOffset + glyf.length];
        buf.rewind();
        buf.get(result);
        return result;
    }

    /**
     * The simple-TrueType render chain (corpus 46679): a format-4 cmap maps the
     * character code to a glyph id, then the glyf outline is drawn directly.
     * Verifies cmap lookup feeds the right outline.
     */
    @Test
    public void testCmapFormat4ToGlyphOutline() throws IOException {
        TrueTypeReader reader = new TrueTypeReader(buildSfntWithCmap(1000));
        assertEquals(1, reader.getGlyphId(0x41));  // 'A' → glyph 1 via cmap
        assertEquals(0, reader.getGlyphId(0x42));  // unmapped → .notdef
        java.awt.geom.GeneralPath path = reader.getGlyphPath(reader.getGlyphId(0x41));
        assertNotNull(path);
        java.awt.geom.Rectangle2D b = path.getBounds2D();
        assertEquals(0.1, b.getMinX(), 1e-6);
        assertEquals(0.7, b.getMaxY(), 1e-6);
    }

    /** Square-glyph font (glyph 1 = rect) plus a format-4 cmap mapping 'A' → glyph 1. */
    private byte[] buildSfntWithCmap(int unitsPerEm) {
        byte[] glyf = squareGlyf();
        // cmap subtable, format 4, segCount 2: [0x41,0x41] + [0xFFFF,0xFFFF].
        ByteBuffer cb = ByteBuffer.allocate(32);
        cb.putShort((short) 4);       // format
        cb.putShort((short) 32);      // length
        cb.putShort((short) 0);       // language
        cb.putShort((short) 4);       // segCountX2
        cb.putShort((short) 4);       // searchRange
        cb.putShort((short) 1);       // entrySelector
        cb.putShort((short) 0);       // rangeShift
        cb.putShort((short) 0x0041); cb.putShort((short) 0xFFFF); // endCode[]
        cb.putShort((short) 0);       // reservedPad
        cb.putShort((short) 0x0041); cb.putShort((short) 0xFFFF); // startCode[]
        cb.putShort((short) -64);    cb.putShort((short) 1);      // idDelta[] (0x41-64=1)
        cb.putShort((short) 0);      cb.putShort((short) 0);      // idRangeOffset[]
        byte[] sub = cb.array();
        ByteBuffer cmapBuf = ByteBuffer.allocate(12 + sub.length);
        cmapBuf.putShort((short) 0);  // version
        cmapBuf.putShort((short) 1);  // numTables
        cmapBuf.putShort((short) 3); cmapBuf.putShort((short) 1); cmapBuf.putInt(12); // (3,1) at offset 12
        cmapBuf.put(sub);
        byte[] cmap = cmapBuf.array();

        int numGlyphs = 2;
        ByteBuffer buf = ByteBuffer.allocate(2048);
        buf.putInt(0x00010000);
        buf.putShort((short) 5);
        buf.putShort((short) 0); buf.putShort((short) 0); buf.putShort((short) 0);

        int headOffset = 12 + 5 * 16;            // 92
        int maxpOffset = headOffset + 54;         // 146
        int locaOffset = maxpOffset + 6;          // 152
        int glyfOffset = locaOffset + (numGlyphs + 1) * 2; // 158
        int cmapOffset = glyfOffset + glyf.length;          // 192

        buf.put("head".getBytes()); buf.putInt(0); buf.putInt(headOffset); buf.putInt(54);
        buf.put("maxp".getBytes()); buf.putInt(0); buf.putInt(maxpOffset); buf.putInt(6);
        buf.put("loca".getBytes()); buf.putInt(0); buf.putInt(locaOffset); buf.putInt((numGlyphs + 1) * 2);
        buf.put("glyf".getBytes()); buf.putInt(0); buf.putInt(glyfOffset); buf.putInt(glyf.length);
        buf.put("cmap".getBytes()); buf.putInt(0); buf.putInt(cmapOffset); buf.putInt(cmap.length);

        buf.position(headOffset);
        buf.putInt(0x00010000); buf.putInt(0x00010000); buf.putInt(0); buf.putInt(0x5F0F3CF5);
        buf.putShort((short) 0); buf.putShort((short) unitsPerEm);
        for (int i = 0; i < 36; i++) buf.put((byte) 0);

        buf.position(maxpOffset);
        buf.putInt(0x00010000); buf.putShort((short) numGlyphs);

        buf.position(locaOffset);
        buf.putShort((short) 0); buf.putShort((short) 0); buf.putShort((short) (glyf.length / 2));

        buf.position(glyfOffset); buf.put(glyf);
        buf.position(cmapOffset); buf.put(cmap);

        byte[] result = new byte[cmapOffset + cmap.length];
        buf.rewind(); buf.get(result);
        return result;
    }

    /** A 4-point rectangle glyph (100,200)-(300,700), 34 bytes (even). */
    private byte[] squareGlyf() {
        ByteBuffer gb = ByteBuffer.allocate(34);
        gb.putShort((short) 1);
        gb.putShort((short) 100); gb.putShort((short) 200);
        gb.putShort((short) 300); gb.putShort((short) 700);
        gb.putShort((short) 3);
        gb.putShort((short) 0);
        for (int i = 0; i < 4; i++) gb.put((byte) 0x01);
        gb.putShort((short) 100); gb.putShort((short) 200); gb.putShort((short) 0); gb.putShort((short) -200);
        gb.putShort((short) 200); gb.putShort((short) 0); gb.putShort((short) 500); gb.putShort((short) 0);
        return gb.array();
    }

    /**
     * Builds a minimal sfnt with head and maxp tables.
     */
    private byte[] buildMinimalSfnt(int unitsPerEm, int numGlyphs) {
        ByteBuffer buf = ByteBuffer.allocate(512);

        // Offset table: sfVersion=0x00010000, numTables=2
        buf.putInt(0x00010000); // sfVersion
        buf.putShort((short) 2); // numTables
        buf.putShort((short) 0); // searchRange
        buf.putShort((short) 0); // entrySelector
        buf.putShort((short) 0); // rangeShift

        // Table directory: head at offset 44, maxp at offset 98
        int headOffset = 12 + 2 * 16; // 12 (offset table) + 32 (2 table records)
        int maxpOffset = headOffset + 54; // head table is 54 bytes

        // head table record
        buf.put("head".getBytes()); // tag
        buf.putInt(0); // checksum
        buf.putInt(headOffset); // offset
        buf.putInt(54); // length

        // maxp table record
        buf.put("maxp".getBytes()); // tag
        buf.putInt(0); // checksum
        buf.putInt(maxpOffset); // offset
        buf.putInt(6); // length

        // head table (54 bytes)
        buf.position(headOffset);
        buf.putInt(0x00010000); // version
        buf.putInt(0x00010000); // fontRevision
        buf.putInt(0); // checksumAdjustment
        buf.putInt(0x5F0F3CF5); // magicNumber
        buf.putShort((short) 0); // flags
        buf.putShort((short) unitsPerEm); // unitsPerEm (offset +18)
        // Rest of head table (padding)
        for (int i = 0; i < 36; i++) buf.put((byte) 0);

        // maxp table (6 bytes)
        buf.position(maxpOffset);
        buf.putInt(0x00010000); // version
        buf.putShort((short) numGlyphs); // numGlyphs

        byte[] result = new byte[buf.position()];
        buf.rewind();
        buf.get(result);
        return result;
    }
}
