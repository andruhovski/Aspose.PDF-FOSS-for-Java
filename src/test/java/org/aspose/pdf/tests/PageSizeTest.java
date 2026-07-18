package org.aspose.pdf.tests;

import org.aspose.pdf.PageSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PageSize].
public class PageSizeTest {

    @Test
    public void a4Dimensions() {
        assertEquals(595, PageSize.A4.getWidth());
        assertEquals(842, PageSize.A4.getHeight());
    }

    @Test
    public void letterDimensions() {
        assertEquals(612, PageSize.LETTER.getWidth());
        assertEquals(792, PageSize.LETTER.getHeight());
    }

    @Test
    public void legalDimensions() {
        assertEquals(612, PageSize.LEGAL.getWidth());
        assertEquals(1008, PageSize.LEGAL.getHeight());
    }

    @Test
    public void a3Dimensions() {
        assertEquals(842, PageSize.A3.getWidth());
        assertEquals(1190, PageSize.A3.getHeight());
    }

    @Test
    public void customPageSize() {
        PageSize custom = new PageSize(300, 400);
        assertEquals(300, custom.getWidth());
        assertEquals(400, custom.getHeight());
    }

    @Test
    public void toStringContainsDimensions() {
        String s = PageSize.A4.toString();
        assertTrue(s.contains("595"));
        assertTrue(s.contains("842"));
    }
}
