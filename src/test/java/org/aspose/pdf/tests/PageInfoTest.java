package org.aspose.pdf.tests;

import org.aspose.pdf.PageInfo;
import org.aspose.pdf.PageInfo.MarginInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PageInfo}.
 */
public class PageInfoTest {

    @Test
    public void defaultConstructorA4() {
        PageInfo info = new PageInfo();
        assertEquals(595, info.getWidth());
        assertEquals(842, info.getHeight());
    }

    @Test
    public void constructorWithDimensions() {
        PageInfo info = new PageInfo(612, 792);
        assertEquals(612, info.getWidth());
        assertEquals(792, info.getHeight());
    }

    @Test
    public void setWidth() {
        PageInfo info = new PageInfo();
        info.setWidth(300);
        assertEquals(300, info.getWidth());
    }

    @Test
    public void setHeight() {
        PageInfo info = new PageInfo();
        info.setHeight(400);
        assertEquals(400, info.getHeight());
    }

    @Test
    public void defaultMarginIsAsposeDefault() {
        PageInfo info = new PageInfo();
        MarginInfo margin = info.getMargin();
        assertNotNull(margin);
        // Aspose-compatible default page margin is 90pt (~1.27 cm) on all sides,
        // matching Aspose.PDF for .NET PageInfo default. Visual tests rely on this.
        assertEquals(90, margin.getTop());
        assertEquals(90, margin.getBottom());
        assertEquals(90, margin.getLeft());
        assertEquals(90, margin.getRight());
    }

    @Test
    public void setMargin() {
        PageInfo info = new PageInfo();
        MarginInfo margin = new MarginInfo(10, 20, 30, 40);
        info.setMargin(margin);
        assertSame(margin, info.getMargin());
        assertEquals(10, margin.getTop());
        assertEquals(20, margin.getBottom());
        assertEquals(30, margin.getLeft());
        assertEquals(40, margin.getRight());
    }

    @Test
    public void marginSetters() {
        MarginInfo margin = new MarginInfo();
        margin.setTop(5);
        margin.setBottom(10);
        margin.setLeft(15);
        margin.setRight(20);
        assertEquals(5, margin.getTop());
        assertEquals(10, margin.getBottom());
        assertEquals(15, margin.getLeft());
        assertEquals(20, margin.getRight());
    }

    @Test
    public void isLandscapePortrait() {
        PageInfo info = new PageInfo(595, 842);
        assertFalse(info.isLandscape());
    }

    @Test
    public void isLandscapeLandscape() {
        PageInfo info = new PageInfo(842, 595);
        assertTrue(info.isLandscape());
    }

    @Test
    public void isLandscapeSquare() {
        PageInfo info = new PageInfo(500, 500);
        assertFalse(info.isLandscape());
    }

    @Test
    public void constructorComputesLandscapeFromDimensions() {
        assertTrue(new PageInfo(842, 595).isLandscape(), "wide > tall must be landscape");
        assertFalse(new PageInfo(595, 842).isLandscape(), "tall > wide must be portrait");
        assertFalse(new PageInfo(500, 500).isLandscape(), "square is not landscape");

        PageInfo pi = new PageInfo(595, 842);
        pi.setIsLandscape(true);
        assertTrue(pi.isLandscape(), "setter must override derived value");
    }

    @Test
    public void toStringContainsDimensions() {
        PageInfo info = new PageInfo(612, 792);
        String s = info.toString();
        assertTrue(s.contains("612"));
        assertTrue(s.contains("792"));
    }

    @Test
    public void marginToString() {
        MarginInfo margin = new MarginInfo(1, 2, 3, 4);
        String s = margin.toString();
        assertTrue(s.contains("top=1"));
        assertTrue(s.contains("right=4"));
    }
}
