package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Image;
import org.aspose.pdf.Page;
import org.aspose.pdf.optimization.OptimizationOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the {@code linkDuplicateStreams} optimization pass: the same image
 * placed on several pages as separate (byte-identical) XObjects must collapse
 * to a single stream on {@code optimizeResources()} + save.
 */
public class DuplicateStreamLinkTest {

    @TempDir
    Path tempDir;

    /** A PNG with enough incompressible content to dominate the file size. */
    private File writeNoisePng() throws Exception {
        BufferedImage img = new BufferedImage(180, 120, BufferedImage.TYPE_INT_RGB);
        java.util.Random rnd = new java.util.Random(42);   // fixed seed — deterministic bytes
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, rnd.nextInt(0xFFFFFF));
            }
        }
        File png = tempDir.resolve("noise.png").toFile();
        ImageIO.write(img, "png", png);
        return png;
    }

    private Path buildDocWithRepeatedImage(File png, int pages) throws Exception {
        try (Document doc = new Document()) {
            for (int p = 0; p < pages; p++) {
                Page page = doc.getPages().add();
                Image image = new Image();
                image.setFile(png.getAbsolutePath());
                image.setFixWidth(180);
                image.setFixHeight(120);
                page.getParagraphs().add(image);
            }
            Path out = tempDir.resolve("repeated.pdf");
            doc.save(out.toString());
            return out;
        }
    }

    @Test
    public void duplicateImagesCollapseToOneStream() throws Exception {
        File png = writeNoisePng();
        long pngSize = png.length();
        Path src = buildDocWithRepeatedImage(png, 3);
        long plainSize = Files.size(src);
        // Sanity: the plain save carries ~3 copies of the image payload.
        assertTrue(plainSize > pngSize * 2,
                "test premise broken: plain file " + plainSize + " should hold 3 image copies");

        Path out = tempDir.resolve("linked.pdf");
        try (Document doc = new Document(src.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setLinkDuplicateStreams(true);
            options.setRemoveUnusedObjects(true);
            options.setRemoveUnusedStreams(true);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        long linkedSize = Files.size(out);

        // Two of the three copies must be gone: the linked file fits in the
        // budget of ONE image payload (plus structure), far below two.
        assertTrue(linkedSize < plainSize - pngSize,
                "expected dedup to drop at least one image copy: linked=" + linkedSize
                        + " plain=" + plainSize + " png=" + pngSize);

        // Content intact: pages round-trip and each still renders the image.
        try (Document reopened = new Document(out.toString())) {
            assertEquals(3, reopened.getPages().getCount());
            for (int p = 1; p <= 3; p++) {
                org.aspose.pdf.ImagePlacementAbsorber abs =
                        new org.aspose.pdf.ImagePlacementAbsorber();
                abs.visit(reopened.getPages().get(p));
                assertEquals(1, abs.getImagePlacements().size(),
                        "page " + p + " must still show the image after dedup");
            }
        }
    }

    @Test
    public void parameterlessOptimizeAlsoLinks() throws Exception {
        File png = writeNoisePng();
        Path src = buildDocWithRepeatedImage(png, 3);
        long plainSize = Files.size(src);

        Path out = tempDir.resolve("linked_default.pdf");
        try (Document doc = new Document(src.toString())) {
            doc.optimizeResources();   // Aspose default options include linking
            doc.save(out.toString());
        }
        assertTrue(Files.size(out) < plainSize - png.length(),
                "parameterless optimizeResources must link duplicates too");
        try (Document reopened = new Document(out.toString())) {
            assertEquals(3, reopened.getPages().getCount());
        }
    }

    /** Different streams must NOT be linked (no false positives). */
    @Test
    public void distinctImagesAreNotLinked() throws Exception {
        try (Document doc = new Document()) {
            for (int p = 0; p < 2; p++) {
                BufferedImage img = new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB);
                java.util.Random rnd = new java.util.Random(p);   // different content per page
                for (int y = 0; y < 40; y++)
                    for (int x = 0; x < 60; x++)
                        img.setRGB(x, y, rnd.nextInt(0xFFFFFF));
                File png = tempDir.resolve("distinct" + p + ".png").toFile();
                ImageIO.write(img, "png", png);
                Page page = doc.getPages().add();
                Image image = new Image();
                image.setFile(png.getAbsolutePath());
                page.getParagraphs().add(image);
            }
            Path src = tempDir.resolve("distinct.pdf");
            doc.save(src.toString());

            Path out = tempDir.resolve("distinct_opt.pdf");
            try (Document loaded = new Document(src.toString())) {
                loaded.optimizeResources();
                loaded.save(out.toString());
            }
            try (Document reopened = new Document(out.toString())) {
                assertEquals(2, reopened.getPages().getCount());
                // Both pages must still hold their own (different) image.
                ByteArrayOutputStream p1 = new ByteArrayOutputStream();
                ByteArrayOutputStream p2 = new ByteArrayOutputStream();
                org.aspose.pdf.ImagePlacementAbsorber abs1 =
                        new org.aspose.pdf.ImagePlacementAbsorber();
                abs1.visit(reopened.getPages().get(1));
                org.aspose.pdf.ImagePlacementAbsorber abs2 =
                        new org.aspose.pdf.ImagePlacementAbsorber();
                abs2.visit(reopened.getPages().get(2));
                assertEquals(1, abs1.getImagePlacements().size());
                assertEquals(1, abs2.getImagePlacements().size());
                abs1.getImagePlacements().get(0).getImage().save(p1);
                abs2.getImagePlacements().get(0).getImage().save(p2);
                assertTrue(!java.util.Arrays.equals(p1.toByteArray(), p2.toByteArray()),
                        "distinct images must stay distinct after optimization");
            }
        }
    }
}
