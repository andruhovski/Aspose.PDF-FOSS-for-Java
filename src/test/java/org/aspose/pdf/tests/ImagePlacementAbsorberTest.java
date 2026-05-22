package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
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
        COSStream imgStream = new COSStream();
        imgStream.set(COSName.of("Subtype"), COSName.of("Image"));
        imgStream.set(COSName.of("Width"), COSInteger.valueOf(w));
        imgStream.set(COSName.of("Height"), COSInteger.valueOf(h));
        imgStream.set(COSName.of("BitsPerComponent"), COSInteger.valueOf(8));
        imgStream.set(COSName.of("ColorSpace"), COSName.of("DeviceRGB"));

        // XObject dictionary
        COSDictionary xobjects = new COSDictionary();
        xobjects.set(COSName.of(imgName), imgStream);

        // Resources
        COSDictionary resourcesDict = new COSDictionary();
        resourcesDict.set(COSName.of("XObject"), xobjects);

        // Content stream: q cm Do Q
        String content = String.format(java.util.Locale.US,
                "q %.1f %.1f %.1f %.1f %.1f %.1f cm /%s Do Q",
                cmA, cmB, cmC, cmD, cmE, cmF, imgName);
        COSStream contentStream = new COSStream(content.getBytes());

        // Page
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        pageDict.set(COSName.RESOURCES, resourcesDict);
        pageDict.set(COSName.CONTENTS, contentStream);

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
        COSStream img1 = new COSStream();
        img1.set(COSName.of("Subtype"), COSName.of("Image"));
        img1.set(COSName.of("Width"), COSInteger.valueOf(50));
        img1.set(COSName.of("Height"), COSInteger.valueOf(50));

        COSStream img2 = new COSStream();
        img2.set(COSName.of("Subtype"), COSName.of("Image"));
        img2.set(COSName.of("Width"), COSInteger.valueOf(100));
        img2.set(COSName.of("Height"), COSInteger.valueOf(100));

        COSDictionary xobjects = new COSDictionary();
        xobjects.set(COSName.of("Im1"), img1);
        xobjects.set(COSName.of("Im2"), img2);

        COSDictionary resources = new COSDictionary();
        resources.set(COSName.of("XObject"), xobjects);

        String content = "q 100 0 0 100 72 700 cm /Im1 Do Q q 200 0 0 200 300 500 cm /Im2 Do Q";
        COSStream contentStream = new COSStream(content.getBytes());

        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        pageDict.set(COSName.RESOURCES, resources);
        pageDict.set(COSName.CONTENTS, contentStream);

        Page page = new Page(pageDict, null);
        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        assertEquals(2, absorber.getImagePlacements().size());
    }

    @Test
    public void testNoImages() throws IOException {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        Page page = new Page(pageDict, null);

        ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
        page.accept(absorber);

        assertTrue(absorber.getImagePlacements().isEmpty());
    }

    @Test
    public void testFormXObjectIgnored() throws IOException {
        // Form XObjects should NOT produce image placements
        COSStream form = new COSStream("q Q".getBytes());
        form.set(COSName.of("Subtype"), COSName.of("Form"));

        COSDictionary xobjects = new COSDictionary();
        xobjects.set(COSName.of("Fm1"), form);

        COSDictionary resources = new COSDictionary();
        resources.set(COSName.of("XObject"), xobjects);

        String content = "q 100 0 0 100 72 700 cm /Fm1 Do Q";
        COSStream contentStream = new COSStream(content.getBytes());

        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        pageDict.set(COSName.MEDIABOX, new Rectangle(0, 0, 595, 842).toCOSArray());
        pageDict.set(COSName.RESOURCES, resources);
        pageDict.set(COSName.CONTENTS, contentStream);

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
