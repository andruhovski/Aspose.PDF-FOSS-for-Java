package org.aspose.pdf.tests;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.font.FontDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FontDescriptor}.
 */
public class FontDescriptorTest {

    @Test
    public void testAscent() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Ascent"), PdfInteger.valueOf(800));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(800, fd.getAscent());
    }

    @Test
    public void testDescent() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Descent"), PdfInteger.valueOf(-200));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(-200, fd.getDescent());
    }

    @Test
    public void testFlags() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Flags"), PdfInteger.valueOf(4));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(4, fd.getFlags());
        assertTrue(fd.isSymbolic());
        assertFalse(fd.isFixedPitch());
        assertFalse(fd.isSerif());
    }

    @Test
    public void testMissingWidthDefault() {
        PdfDictionary dict = new PdfDictionary();
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(0, fd.getMissingWidth());
    }

    @Test
    public void testFontBBox() {
        PdfDictionary dict = new PdfDictionary();
        PdfArray bbox = new PdfArray();
        bbox.add(PdfInteger.valueOf(0));
        bbox.add(PdfInteger.valueOf(-200));
        bbox.add(PdfInteger.valueOf(1000));
        bbox.add(PdfInteger.valueOf(800));
        dict.set(PdfName.of("FontBBox"), bbox);
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
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("FontName"), PdfName.of("Helvetica"));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals("Helvetica", fd.getFontName());
    }

    @Test
    public void testItalicFlag() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Flags"), PdfInteger.valueOf(0x40)); // bit 7
        FontDescriptor fd = new FontDescriptor(dict);
        assertTrue(fd.isItalic());
    }

    @Test
    public void testNullDictThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FontDescriptor(null));
    }

    @Test
    public void testCapHeight() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("CapHeight"), PdfInteger.valueOf(700));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(700, fd.getCapHeight());
    }

    @Test
    public void testStemV() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("StemV"), PdfInteger.valueOf(80));
        FontDescriptor fd = new FontDescriptor(dict);
        assertEquals(80, fd.getStemV());
    }
}
