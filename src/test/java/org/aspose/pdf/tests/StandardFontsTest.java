package org.aspose.pdf.tests;

import org.aspose.pdf.engine.font.FontEncoding;
import org.aspose.pdf.engine.font.StandardFonts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link StandardFonts}.
 */
public class StandardFontsTest {

    @Test
    public void testIsStandardHelvetica() {
        assertTrue(StandardFonts.isStandard("Helvetica"));
    }

    @Test
    public void testIsStandardArial() {
        assertFalse(StandardFonts.isStandard("Arial"));
    }

    @Test
    public void testIsStandardCourierBold() {
        assertTrue(StandardFonts.isStandard("Courier-Bold"));
    }

    @Test
    public void testIsStandardTimesRoman() {
        assertTrue(StandardFonts.isStandard("Times-Roman"));
    }

    @Test
    public void testIsStandardNull() {
        assertFalse(StandardFonts.isStandard(null));
    }

    @Test
    public void testHelveticaWidthA() {
        int[] widths = StandardFonts.getWidths("Helvetica");
        assertNotNull(widths);
        assertEquals(667, widths[65]); // 'A'
    }

    @Test
    public void testHelveticaWidthSpace() {
        int[] widths = StandardFonts.getWidths("Helvetica");
        assertNotNull(widths);
        assertEquals(278, widths[32]); // space
    }

    @Test
    public void testCourierFixedWidth() {
        int[] widths = StandardFonts.getWidths("Courier");
        assertNotNull(widths);
        // All Courier widths should be 600
        assertEquals(600, widths[65]);
        assertEquals(600, widths[32]);
        assertEquals(600, widths[122]);
    }

    @Test
    public void testTimesRomanWidthA() {
        int[] widths = StandardFonts.getWidths("Times-Roman");
        assertNotNull(widths);
        assertEquals(722, widths[65]); // 'A'
    }

    @Test
    public void testSymbolEncoding() {
        FontEncoding enc = StandardFonts.getEncoding("Symbol");
        assertNotNull(enc);
        assertNotSame(FontEncoding.WIN_ANSI, enc);
    }

    @Test
    public void testHelveticaEncoding() {
        FontEncoding enc = StandardFonts.getEncoding("Helvetica");
        assertNotNull(enc);
    }

    @Test
    public void testGetWidthsReturnsClone() {
        int[] w1 = StandardFonts.getWidths("Helvetica");
        int[] w2 = StandardFonts.getWidths("Helvetica");
        assertNotSame(w1, w2);
        assertArrayEquals(w1, w2);
    }

    @Test
    public void testNonStandardReturnsNull() {
        assertNull(StandardFonts.getWidths("NonExistentFont"));
        assertNull(StandardFonts.getEncoding("NonExistentFont"));
    }

    @Test
    public void testAllStandard14() {
        String[] std14 = {
            "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
            "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
            "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic",
            "Symbol", "ZapfDingbats"
        };
        for (String name : std14) {
            assertTrue(StandardFonts.isStandard(name), name + " should be standard");
            assertNotNull(StandardFonts.getWidths(name), name + " should have widths");
            assertNotNull(StandardFonts.getEncoding(name), name + " should have encoding");
        }
    }
}
