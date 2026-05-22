package org.aspose.pdf.tests;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.font.FontDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FontDescriptor}.
 */
public class FontDescriptorTest {

    @Test
    public void testAscent() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Ascent"), COSInteger.valueOf(800));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(800, fd.getAscent());
    }

    @Test
    public void testDescent() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Descent"), COSInteger.valueOf(-200));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(-200, fd.getDescent());
    }

    @Test
    public void testFlags() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Flags"), COSInteger.valueOf(4));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(4, fd.getFlags());
        assertTrue(fd.isSymbolic());
        assertFalse(fd.isFixedPitch());
        assertFalse(fd.isSerif());
    }

    @Test
    public void testMissingWidthDefault() {
        COSDictionary dict = new COSDictionary();
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(0, fd.getMissingWidth());
    }

    @Test
    public void testFontBBox() {
        COSDictionary dict = new COSDictionary();
        COSArray bbox = new COSArray();
        bbox.add(COSInteger.valueOf(0));
        bbox.add(COSInteger.valueOf(-200));
        bbox.add(COSInteger.valueOf(1000));
        bbox.add(COSInteger.valueOf(800));
        dict.set(COSName.of("FontBBox"), bbox);
        FontDescriptor fd = new FontDescriptor(dict);
        Rectangle r = fd.getFontBBox();
        assertNotNull(r);
        assertEquals(0, r.getLLX());
        assertEquals(-200, r.getLLY());
        assertEquals(1000, r.getURX());
        assertEquals(800, r.getURY());
    }

    @Test
    public void testFontName() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("FontName"), COSName.of("Helvetica"));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals("Helvetica", fd.getFontName());
    }

    @Test
    public void testItalicFlag() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("Flags"), COSInteger.valueOf(0x40)); // bit 7
        FontDescriptor fd = new FontDescriptor(dict);
        assertTrue(fd.isItalic());
    }

    @Test
    public void testNullDictThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FontDescriptor(null));
    }

    @Test
    public void testCapHeight() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("CapHeight"), COSInteger.valueOf(700));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(700, fd.getCapHeight());
    }

    @Test
    public void testStemV() {
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("StemV"), COSInteger.valueOf(80));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(80, fd.getStemV());
    }
}
