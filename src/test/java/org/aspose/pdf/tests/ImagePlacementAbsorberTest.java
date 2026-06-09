package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ImagePlacementAbsorber} and {@link ImagePlacement}.
 */
public class ImagePlacementAbsorberTest {

    /**
     * Creates a page with an image XObject and a content stream that references it.
     */
    private Page createPageWithImage(String imgName, int w, int h,
                                      double cmA, double cmB, double cmC, double cmD,
                                      double cmE, double cmF) {
        // Image stream
        PdfStream imgStream = new PdfStream();
        imgStream.set(PdfName.of("Subtype"), PdfName.of("Image"));
        imgStream.set(PdfName.of("Width"), PdfInteger.valueOf(w));
        imgStream.set(PdfName.of("Height"), PdfInteger.valueOf(h));
        imgStream.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(8));
        imgStream.set(PdfName.of("ColorSpace"), PdfName.of("DeviceRGB"));

        // XObject dictionary
        PdfDictionary xobjects = new PdfDictionary();
        xobjects.set(PdfName.of(imgName), imgStream);

        // Resources
        PdfDictionary resourcesDict = new PdfDictionary();
        resourcesDict.set(PdfName.of("XObject"), xobjects);

        // Content stream: q cm Do Q
        String content = String.format(java.util.Locale.US,
                "q %.1f %.1f %.1f %.1f %.1f %.1f cm /%s Do Q",
                cmA, cmB, cmC, cmD, cmE, cmF, imgName);
        PdfStream contentStream = new PdfStream(content.getBytes());

        // Page
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        pageDict.set(PdfName.RESOURCES, resourcesDict);
        pageDict.set(PdfName.CONTENTS, contentStream);

        return new Page(pageDict, null);
    }

    @Test
    public void testFindSingleImage() throws IOException {
        Page page = createPageWithImage("Im1", 100, 80, 200, 0, 0, 160, 72, 700);

        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        List<ImagePlacement> placements = absorber.getImagePlacements();
        assertEquals(1, placements.size());

        ImagePlacement ip = placements.get(0);
        assertNotNull(ip.getImage());
        assertEquals(100, ip.getImage().getWidth());
        assertEquals(80, ip.getImage().getHeight());
        assertNotNull(ip.getMatrix());
        assertNotNull(ip.getRectangle());
        assertSame(page, ip.getPage());
    }

    @Test
    public void testImagePlacementPosition() throws IOException {
        // Place image at (72, 700) with size 200x160 pts
        Page page = createPageWithImage("Im1", 100, 80, 200, 0, 0, 160, 72, 700);

        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        ImagePlacement ip = absorber.getImagePlacements().get(0);
        Rectangle r = ip.getRectangle();
        // The image is placed at (72, 700) with width 200 and height 160
        assertEquals(72.0, r.getLLX(), 1.0);
        assertEquals(700.0, r.getLLY(), 1.0);
        assertTrue(r.getWidth() > 0);
        assertTrue(r.getHeight() > 0);
    }

    @Test
    public void testImageResolution() throws IOException {
        // 100px wide image placed at 200pt width → 100/200*72 = 36 DPI
        Page page = createPageWithImage("Im1", 100, 80, 200, 0, 0, 160, 72, 700);

        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        ImagePlacement ip = absorber.getImagePlacements().get(0);
        double dpi = ip.getResolution();
        assertEquals(36.0, dpi, 1.0);
    }

    @Test
    public void testMultipleImages() throws IOException {
        // Page with two images
        PdfStream img1 = new PdfStream();
        img1.set(PdfName.of("Subtype"), PdfName.of("Image"));
        img1.set(PdfName.of("Width"), PdfInteger.valueOf(50));
        img1.set(PdfName.of("Height"), PdfInteger.valueOf(50));

        PdfStream img2 = new PdfStream();
        img2.set(PdfName.of("Subtype"), PdfName.of("Image"));
        img2.set(PdfName.of("Width"), PdfInteger.valueOf(100));
        img2.set(PdfName.of("Height"), PdfInteger.valueOf(100));

        PdfDictionary xobjects = new PdfDictionary();
        xobjects.set(PdfName.of("Im1"), img1);
        xobjects.set(PdfName.of("Im2"), img2);

        PdfDictionary resources = new PdfDictionary();
        resources.set(PdfName.of("XObject"), xobjects);

        String content = "q 100 0 0 100 72 700 cm /Im1 Do Q q 200 0 0 200 300 500 cm /Im2 Do Q";
        PdfStream contentStream = new PdfStream(content.getBytes());

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        pageDict.set(PdfName.RESOURCES, resources);
        pageDict.set(PdfName.CONTENTS, contentStream);

        Page page = new Page(pageDict, null);
        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        assertEquals(2, absorber.getImagePlacements().size());
    }

    @Test
    public void testNoImages() throws IOException {
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        Page page = new Page(pageDict, null);

        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        assertTrue(absorber.getImagePlacements().isEmpty());
    }

    @Test
    public void testFormXObjectIgnored() throws IOException {
        // Form XObjects should NOT produce image placements
        PdfStream form = new PdfStream("q Q".getBytes());
        form.set(PdfName.of("Subtype"), PdfName.of("Form"));

        PdfDictionary xobjects = new PdfDictionary();
        xobjects.set(PdfName.of("Fm1"), form);

        PdfDictionary resources = new PdfDictionary();
        resources.set(PdfName.of("XObject"), xobjects);

        String content = "q 100 0 0 100 72 700 cm /Fm1 Do Q";
        PdfStream contentStream = new PdfStream(content.getBytes());

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        pageDict.set(PdfName.RESOURCES, resources);
        pageDict.set(PdfName.CONTENTS, contentStream);

        Page page = new Page(pageDict, null);
        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        assertTrue(absorber.getImagePlacements().isEmpty());
    }

    @Test
    public void testReset() throws IOException {
        Page page = createPageWithImage("Im1", 100, 80, 200, 0, 0, 160, 72, 700);
        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);
        assertEquals(1, absorber.getImagePlacements().size());

        absorber.reset();
        assertTrue(absorber.getImagePlacements().isEmpty());
    }
}
