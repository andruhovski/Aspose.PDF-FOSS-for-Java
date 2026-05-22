package org.aspose.pdf.tests;

import org.aspose.pdf.Color;
import org.aspose.pdf.html.CssContext;
import org.aspose.pdf.html.CssStyleParser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CSS style parsing.
 */
public class CssStyleParserTest {

    @Test
    public void testParseFontSizePx() {
        assertEquals(14.0, CssStyleParser.parseDimension("14px", 12), 0.01);
    }

    @Test
    public void testParseFontSizePt() {
        assertEquals(12.0, CssStyleParser.parseDimension("12pt", 12), 0.01);
    }

    @Test
    public void testParseFontSizeEm() {
        assertEquals(18.0, CssStyleParser.parseDimension("1.5em", 12), 0.01);
    }

    @Test
    public void testParseColorHex() {
        Color c = CssStyleParser.parseColor("#ff0000");
        assertNotNull(c);
        assertEquals(1.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
        assertEquals(0.0, c.getB(), 0.01);
    }

    @Test
    public void testParseColorRgb() {
        Color c = CssStyleParser.parseColor("rgb(0, 128, 255)");
        assertNotNull(c);
        assertEquals(0.0, c.getR(), 0.01);
        assertEquals(128.0 / 255, c.getG(), 0.01);
        assertEquals(1.0, c.getB(), 0.01);
    }

    @Test
    public void testParseNamedColor_navy() {
        Color c = CssStyleParser.parseColor("navy");
        assertNotNull(c);
        assertEquals(0.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
        assertEquals(0.502, c.getB(), 0.01);
    }

    @Test
    public void testParseFontWeightBold() {
        CssContext ctx = new CssContext();
        CssStyleParser.applyInlineStyle(ctx, "font-weight:bold");
        assertTrue(ctx.isBold());
    }

    @Test
    public void testParseMarginShorthand_twoValues() {
        CssContext ctx = new CssContext();
        CssStyleParser.applyInlineStyle(ctx, "margin:10px 20px");
        assertEquals(10.0, ctx.getMarginTop(), 0.01);
        assertEquals(10.0, ctx.getMarginBottom(), 0.01);
        assertEquals(20.0, ctx.getMarginLeft(), 0.01);
        assertEquals(20.0, ctx.getMarginRight(), 0.01);
    }

    @Test
    public void testInvalidValue_returnsDefault() {
        assertEquals(12.0, CssStyleParser.parseDimension("invalid", 12), 0.01);
    }

    @Test
    public void testEmptyStyle_noCrash() {
        CssContext ctx = new CssContext();
        CssStyleParser.applyInlineStyle(ctx, "");
        CssStyleParser.applyInlineStyle(ctx, null);
        assertEquals(12.0, ctx.getFontSize(), 0.01);
    }

    @Test
    public void testParseColorHexShort() {
        Color c = CssStyleParser.parseColor("#f00");
        assertNotNull(c);
        assertEquals(1.0, c.getR(), 0.01);
        assertEquals(0.0, c.getG(), 0.01);
    }

    @Test
    public void testParseColorTransparent() {
        assertNull(CssStyleParser.parseColor("transparent"));
    }

    @Test
    public void testContextInheritance() {
        CssContext parent = new CssContext();
        parent.setFontSize(20);
        parent.setBold(true);
        parent.setMarginTop(10);
        parent.setBackgroundColor(Color.RED);

        CssContext child = parent.inherit();
        assertEquals(20, child.getFontSize(), 0.01, "Font size should inherit");
        assertTrue(child.isBold(), "Bold should inherit");
        assertEquals(0, child.getMarginTop(), 0.01, "Margin should NOT inherit");
        assertNull(child.getBackgroundColor(), "Background should NOT inherit");
    }

    @Test
    public void testToPdfFontName() {
        CssContext ctx = new CssContext();
        assertEquals("Helvetica", ctx.toPdfFontName());

        ctx.setBold(true);
        assertEquals("Helvetica-Bold", ctx.toPdfFontName());

        ctx.setItalic(true);
        assertEquals("Helvetica-BoldOblique", ctx.toPdfFontName());

        ctx.setFontFamily("Courier");
        assertEquals("Courier-BoldOblique", ctx.toPdfFontName());

        ctx.setBold(false);
        ctx.setItalic(false);
        ctx.setFontFamily("Times");
        assertEquals("Times-Roman", ctx.toPdfFontName());
    }
}
