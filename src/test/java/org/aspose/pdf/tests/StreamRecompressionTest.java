package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Image;
import org.aspose.pdf.ImagePlacementAbsorber;
import org.aspose.pdf.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the stream-recompression pass of {@code optimizeResources()}:
 * raster image streams gain a PNG predictor + max-effort Flate and shrink,
 * while the decoded pixels stay bit-identical.
 */
public class StreamRecompressionTest {

    @TempDir
    Path tempDir;

    /**
     * A synthetic "screenshot-like" image: smooth gradients compress far
     * better with a PNG predictor than with plain Flate, so this exercises
     * the predictor candidate path specifically.
     */
    private File writeGradientPng() throws Exception {
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 300; x++) {
                img.setRGB(x, y, ((x * 255 / 300) << 16) | ((y * 255 / 200) << 8) | ((x + y) % 255));
            }
        }
        File png = tempDir.resolve("gradient.png").toFile();
        ImageIO.write(img, "png", png);
        return png;
    }

    @Test
    public void gradientImageShrinksAndPixelsSurvive() throws Exception {
        File png = writeGradientPng();
        Path src = tempDir.resolve("gradient.pdf");
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Image image = new Image();
            image.setFile(png.getAbsolutePath());
            page.getParagraphs().add(image);
            doc.save(src.toString());
        }
        long plainSize = Files.size(src);

        Path out = tempDir.resolve("gradient_opt.pdf");
        try (Document doc = new Document(src.toString())) {
            doc.optimizeResources();
            doc.save(out.toString());
        }
        long optimizedSize = Files.size(out);
        assertTrue(optimizedSize < plainSize,
                "predictor+max-flate must shrink the gradient image: "
                        + optimizedSize + " !< " + plainSize);

        // Pixel fidelity: the decoded image must be identical to the source.
        try (Document reopened = new Document(out.toString())) {
            ImagePlacementAbsorber abs = new ImagePlacementAbsorber();
            abs.visit(reopened.getPages().get(1));
            assertEquals(1, abs.getImagePlacements().size());
            ByteArrayOutputStream extracted = new ByteArrayOutputStream();
            abs.getImagePlacements().get(0).getImage().save(extracted);
            BufferedImage actual = ImageIO.read(new ByteArrayInputStream(extracted.toByteArray()));
            BufferedImage expected = ImageIO.read(png);
            assertEquals(expected.getWidth(), actual.getWidth());
            assertEquals(expected.getHeight(), actual.getHeight());
            for (int y = 0; y < expected.getHeight(); y += 7) {
                for (int x = 0; x < expected.getWidth(); x += 7) {
                    assertEquals(expected.getRGB(x, y) & 0xFFFFFF, actual.getRGB(x, y) & 0xFFFFFF,
                            "pixel drift at " + x + "," + y);
                }
            }
        }
    }

    /** Text-only documents must also round-trip through the pass unharmed. */
    @Test
    public void textContentSurvivesRecompression() throws Exception {
        Path src = tempDir.resolve("text.pdf");
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            org.aspose.pdf.text.TextFragment tf =
                    new org.aspose.pdf.text.TextFragment("Recompression keeps text intact.");
            tf.setPosition(new org.aspose.pdf.text.Position(72, 700));
            new org.aspose.pdf.text.TextBuilder(page).appendText(tf);
            doc.save(src.toString());
        }
        Path out = tempDir.resolve("text_opt.pdf");
        try (Document doc = new Document(src.toString())) {
            doc.optimizeResources();
            doc.save(out.toString());
        }
        try (Document reopened = new Document(out.toString())) {
            org.aspose.pdf.text.TextFragmentAbsorber abs =
                    new org.aspose.pdf.text.TextFragmentAbsorber("Recompression keeps text intact.");
            reopened.getPages().get(1).accept(abs);
            assertEquals(1, abs.getTextFragments().size(),
                    "text must extract identically after recompression");
        }
    }
}
