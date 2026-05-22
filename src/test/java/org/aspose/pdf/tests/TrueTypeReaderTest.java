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
