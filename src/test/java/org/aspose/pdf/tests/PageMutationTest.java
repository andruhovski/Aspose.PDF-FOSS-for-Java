package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for Page mutation methods (setMediaBox, setCropBox, setRotation, etc.).
public class PageMutationTest {

    private Page createEmptyPage() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.TYPE, PdfName.PAGE);
        // Default A4 media box
        PdfArray mediaBox = new PdfArray(4);
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(new PdfFloat(595.0));
        mediaBox.add(new PdfFloat(842.0));
        dict.set(PdfName.MEDIABOX, mediaBox);
        return new Page(dict, null);
    }

    @Test
    public void setMediaBox() {
        Page page = createEmptyPage();
        Rectangle newBox = new Rectangle(0, 0, 612, 792);
        page.setMediaBox(newBox);

        Rectangle result = page.getMediaBox();
        assertNotNull(result);
        assertEquals(0, result.getLLX(), 1e-3);
        assertEquals(0, result.getLLY(), 1e-3);
        assertEquals(612, result.getURX(), 1e-3);
        assertEquals(792, result.getURY(), 1e-3);
    }

    @Test
    public void setMediaBoxNull() {
        Page page = createEmptyPage();
        assertThrows(IllegalArgumentException.class, () -> page.setMediaBox(null));
    }

    @Test
    public void setCropBox() {
        Page page = createEmptyPage();
        Rectangle cropBox = new Rectangle(10, 10, 585, 832);
        page.setCropBox(cropBox);

        Rectangle result = page.getCropBox();
        assertNotNull(result);
        assertEquals(10, result.getLLX(), 1e-3);
        assertEquals(10, result.getLLY(), 1e-3);
        assertEquals(585, result.getURX(), 1e-3);
        assertEquals(832, result.getURY(), 1e-3);
    }

    @Test
    public void setArtBox() {
        Page page = createEmptyPage();
        Rectangle artBox = new Rectangle(20, 20, 575, 822);
        page.setArtBox(artBox);

        Rectangle result = page.getArtBox();
        assertNotNull(result);
        assertEquals(20, result.getLLX(), 1e-3);
        assertEquals(575, result.getURX(), 1e-3);
    }

    @Test
    public void setBleedBox() {
        Page page = createEmptyPage();
        Rectangle bleedBox = new Rectangle(5, 5, 590, 837);
        page.setBleedBox(bleedBox);

        Rectangle result = page.getBleedBox();
        assertNotNull(result);
        assertEquals(5, result.getLLX(), 1e-3);
        assertEquals(590, result.getURX(), 1e-3);
    }

    @Test
    public void setTrimBox() {
        Page page = createEmptyPage();
        Rectangle trimBox = new Rectangle(15, 15, 580, 827);
        page.setTrimBox(trimBox);

        Rectangle result = page.getTrimBox();
        assertNotNull(result);
        assertEquals(15, result.getLLX(), 1e-3);
        assertEquals(580, result.getURX(), 1e-3);
    }

    @Test
    public void setRotation0() {
        Page page = createEmptyPage();
        page.setRotation(0);
        assertEquals(0, page.getRotate());
    }

    @Test
    public void setRotation90() {
        Page page = createEmptyPage();
        page.setRotation(90);
        assertEquals(90, page.getRotate());
    }

    @Test
    public void setRotation180() {
        Page page = createEmptyPage();
        page.setRotation(180);
        assertEquals(180, page.getRotate());
    }

    @Test
    public void setRotation270() {
        Page page = createEmptyPage();
        page.setRotation(270);
        assertEquals(270, page.getRotate());
    }

    @Test
    public void setRotationInvalid() {
        Page page = createEmptyPage();
        assertThrows(IllegalArgumentException.class, () -> page.setRotation(45));
        assertThrows(IllegalArgumentException.class, () -> page.setRotation(360));
        assertThrows(IllegalArgumentException.class, () -> page.setRotation(-90));
    }

    @Test
    public void setPageSize() {
        Page page = createEmptyPage();
        page.setPageSize(612, 792);

        Rectangle mediaBox = page.getMediaBox();
        assertNotNull(mediaBox);
        assertEquals(0, mediaBox.getLLX(), 1e-3);
        assertEquals(0, mediaBox.getLLY(), 1e-3);
        assertEquals(612, mediaBox.getURX(), 1e-3);
        assertEquals(792, mediaBox.getURY(), 1e-3);
    }

    @Test
    public void setPageSizeInvalid() {
        Page page = createEmptyPage();
        assertThrows(IllegalArgumentException.class, () -> page.setPageSize(0, 100));
        assertThrows(IllegalArgumentException.class, () -> page.setPageSize(100, -1));
    }

    @Test
    public void setBoxUpdatesUnderlyingDictionary() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.TYPE, PdfName.PAGE);
        PdfArray mediaBox = new PdfArray(4);
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(PdfInteger.valueOf(0));
        mediaBox.add(PdfInteger.valueOf(100));
        mediaBox.add(PdfInteger.valueOf(100));
        dict.set(PdfName.MEDIABOX, mediaBox);
        Page page = new Page(dict, null);

        page.setMediaBox(new Rectangle(0, 0, 200, 300));

        // Verify the underlying dictionary was updated
        PdfArray updated = (PdfArray) dict.get(PdfName.MEDIABOX);
        assertNotNull(updated);
        assertEquals(4, updated.size());
    }

    @Test
    public void setAllBoxesNull() {
        Page page = createEmptyPage();
        assertThrows(IllegalArgumentException.class, () -> page.setCropBox(null));
        assertThrows(IllegalArgumentException.class, () -> page.setArtBox(null));
        assertThrows(IllegalArgumentException.class, () -> page.setBleedBox(null));
        assertThrows(IllegalArgumentException.class, () -> page.setTrimBox(null));
    }
}
