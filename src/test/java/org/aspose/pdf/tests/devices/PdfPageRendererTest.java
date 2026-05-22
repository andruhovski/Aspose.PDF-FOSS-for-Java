package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.aspose.pdf.Page;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.render.PdfPageRenderer;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PdfPageRenderer}.
 */
public class PdfPageRendererTest {

    /**
     * Creates a minimal Page with a MediaBox but no content stream.
     */
    private Page createEmptyPage(double width, double height) {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.of("Type"), COSName.of("Page"));
        COSArray mediaBox = new COSArray(4);
        mediaBox.add(new COSFloat(0));
        mediaBox.add(new COSFloat(0));
        mediaBox.add(new COSFloat(width));
        mediaBox.add(new COSFloat(height));
        pageDict.set(COSName.of("MediaBox"), mediaBox);
        return new Page(pageDict, null);
    }

    @Test
    public void renderEmptyPageAt72Dpi() throws Exception {
        Page page = createEmptyPage(612, 792);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 72, 72);
        assertNotNull(img);
        assertEquals(612, img.getWidth());
        assertEquals(792, img.getHeight());
        assertEquals(BufferedImage.TYPE_INT_ARGB, img.getType());
    }

    @Test
    public void renderEmptyPageAt150Dpi() throws Exception {
        Page page = createEmptyPage(612, 792);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 150, 150);
        assertNotNull(img);
        // 612 * 150/72 ≈ 1275
        assertEquals(1275, img.getWidth());
        // 792 * 150/72 = 1650
        assertEquals(1650, img.getHeight());
    }

    @Test
    public void renderEmptyPageAt300Dpi() throws Exception {
        Page page = createEmptyPage(612, 792);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 300, 300);
        assertNotNull(img);
        assertEquals(2550, img.getWidth());
        assertEquals(3300, img.getHeight());
    }

    @Test
    public void renderEmptyPageHasWhiteBackground() throws Exception {
        Page page = createEmptyPage(100, 100);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 72, 72);
        // Check center pixel is white
        int argb = img.getRGB(50, 50);
        assertEquals(0xFFFFFFFF, argb, "Center pixel should be white");
    }

    @Test
    public void renderA3Page() throws Exception {
        // A3 = 841.89 x 1190.55 points
        Page page = createEmptyPage(841.89, 1190.55);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 72, 72);
        assertNotNull(img);
        assertEquals(842, img.getWidth());
        assertEquals(1191, img.getHeight());
    }

    @Test
    public void renderSmallPage() throws Exception {
        Page page = createEmptyPage(1, 1);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 72, 72);
        assertNotNull(img);
        assertEquals(1, img.getWidth());
        assertEquals(1, img.getHeight());
    }

    @Test
    public void asymmetricDpi() throws Exception {
        Page page = createEmptyPage(100, 100);
        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage img = renderer.renderPage(page, 72, 144);
        assertEquals(100, img.getWidth());
        assertEquals(200, img.getHeight());
    }
}
