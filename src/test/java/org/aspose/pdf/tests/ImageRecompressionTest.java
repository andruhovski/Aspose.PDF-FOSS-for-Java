package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Image;
import org.aspose.pdf.ImagePlacementAbsorber;
import org.aspose.pdf.Page;
import org.aspose.pdf.optimization.OptimizationOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Guards the image passes of `optimizeResources(OptimizationOptions)`:
/// `compressImages/imageQuality` converts photographic rasters to JPEG,
/// `resizeImages/maxResolution` downsamples oversized images.
public class ImageRecompressionTest {

    @TempDir
    Path tempDir;

    /// A photo-like image: smooth 2-D gradients with mild noise.
    private File writePhotoPng(int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        java.util.Random rnd = new java.util.Random(7);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = (int) (127 + 120 * Math.sin(x / 37.0) * Math.cos(y / 29.0));
                int g = (int) (127 + 120 * Math.sin((x + y) / 41.0));
                int b = (x * 255 / w + rnd.nextInt(8)) & 0xFF;
                img.setRGB(x, y, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
            }
        }
        File png = tempDir.resolve("photo" + w + "x" + h + ".png").toFile();
        ImageIO.write(img, "png", png);
        return png;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private Path buildPdf(File png, double fixW, double fixH, String name) throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Image image = new Image();
            image.setFile(png.getAbsolutePath());
            if (fixW > 0) image.setFixWidth(fixW);
            if (fixH > 0) image.setFixHeight(fixH);
            page.getParagraphs().add(image);
            Path out = tempDir.resolve(name);
            doc.save(out.toString());
            return out;
        }
    }

    @Test
    public void compressImagesConvertsToJpeg() throws Exception {
        File png = writePhotoPng(400, 300);
        Path src = buildPdf(png, 400, 300, "photo.pdf");
        long plainSize = Files.size(src);

        Path out = tempDir.resolve("photo_jpeg.pdf");
        try (Document doc = new Document(src.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setCompressImages(true);
            options.setImageQuality(60);
            options.setRemoveUnusedObjects(true);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        long optimizedSize = Files.size(out);
        assertTrue(optimizedSize < plainSize / 2,
                "JPEG at q60 must at least halve the photo PDF: "
                        + optimizedSize + " vs " + plainSize);

        try (Document reopened = new Document(out.toString())) {
            ImagePlacementAbsorber abs = new ImagePlacementAbsorber();
            abs.visit(reopened.getPages().get(1));
            assertEquals(1, abs.getImagePlacements().size(), "image must survive");
            org.aspose.pdf.XImage image = abs.getImagePlacements().get(0).getImage();
            assertEquals(400, image.getWidth(), "no resize was requested");
            // The payload is now a JPEG stream.
            byte[] encoded = image.getEncodedData();
            assertTrue(encoded != null && (encoded[0] & 0xFF) == 0xFF && (encoded[1] & 0xFF) == 0xD8,
                    "image stream must carry JPEG (DCTDecode) bytes");
        }
    }

    @Test
    public void resizeImagesDownsamplesOversizedImage() throws Exception {
        // 800×600 pixels displayed at 144×108 pt (2in × 1.5in): effective
        // 400 DPI. With maxResolution=150 the stored image must shrink to
        // ~300 px wide (150 DPI × 2 in).
        File png = writePhotoPng(800, 600);
        Path src = buildPdf(png, 144, 108, "big.pdf");
        long plainSize = Files.size(src);

        Path out = tempDir.resolve("big_resized.pdf");
        try (Document doc = new Document(src.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setCompressImages(true);
            options.setImageQuality(75);
            options.setResizeImages(true);
            options.setMaxResolution(150);
            options.setRemoveUnusedObjects(true);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        assertTrue(Files.size(out) < plainSize / 3,
                "downsample+JPEG must cut the file to a fraction: "
                        + Files.size(out) + " vs " + plainSize);

        try (Document reopened = new Document(out.toString())) {
            ImagePlacementAbsorber abs = new ImagePlacementAbsorber();
            abs.visit(reopened.getPages().get(1));
            assertEquals(1, abs.getImagePlacements().size());
            int width = abs.getImagePlacements().get(0).getImage().getWidth();
            assertTrue(width <= 320 && width >= 260,
                    "expected ~300px after 150-DPI downsample, got " + width);
        }
    }

    /// Images with transparency (SMask) must never be touched by the JPEG pass.
    @Test
    public void maskedImagesAreLeftAlone() throws Exception {
        // A PNG with an alpha channel becomes an image + SMask pair.
        BufferedImage img = new BufferedImage(120, 80, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 80; y++)
            for (int x = 0; x < 120; x++)
                img.setRGB(x, y, ((x * 2) << 24) | (0x3366CC));
        File png = tempDir.resolve("alpha.png").toFile();
        ImageIO.write(img, "png", png);
        Path src = buildPdf(png, 120, 80, "alpha.pdf");

        Path out = tempDir.resolve("alpha_opt.pdf");
        try (Document doc = new Document(src.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setCompressImages(true);
            options.setImageQuality(50);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        try (Document reopened = new Document(out.toString())) {
            ImagePlacementAbsorber abs = new ImagePlacementAbsorber();
            abs.visit(reopened.getPages().get(1));
            assertEquals(1, abs.getImagePlacements().size(), "image must survive untouched");
        }
    }
}
