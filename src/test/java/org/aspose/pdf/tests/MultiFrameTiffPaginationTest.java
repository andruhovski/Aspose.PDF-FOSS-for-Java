package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Image;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.layout.LayoutEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Guards PDFNET-38363: adding a multi-frame TIFF as a single [Image]
/// paragraph must paginate into one page per decodable frame on save.
public class MultiFrameTiffPaginationTest {

    @TempDir
    Path tempDir;

    /// Writes an in-memory TIFF with `frames` solid-colour frames.
    private static byte[] buildTiff(int frames, int width, int height) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
        assertTrue(writers.hasNext(), "JDK TIFF writer expected on Java 11+");
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames; i++) {
                BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = frame.createGraphics();
                g.setColor(new java.awt.Color(40 * i % 255, 80, 120));
                g.fillRect(0, 0, width, height);
                g.dispose();
                writer.writeToSequence(new javax.imageio.IIOImage(frame, null, null), null);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    @Test
    public void multiFrameTiffProducesOnePagePerFrame() throws Exception {
        byte[] tiff = buildTiff(3, 60, 40);
        File tif = tempDir.resolve("three-frames.tif").toFile();
        Files.write(tif.toPath(), tiff);
        File outFile = tempDir.resolve("three-frames.pdf").toFile();

        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Image image = new Image();
            image.setFile(tif.getAbsolutePath());
            page.getParagraphs().add(image);
            doc.save(outFile.getAbsolutePath());
        }

        try (Document doc = new Document(outFile.getAbsolutePath())) {
            assertEquals(3, doc.getPages().getCount(),
                    "Each decodable TIFF frame must land on its own page");
        }
    }

    @Test
    public void selectedFrameRendersSinglePage() throws Exception {
        byte[] tiff = buildTiff(3, 60, 40);
        File tif = tempDir.resolve("pick-frame.tif").toFile();
        Files.write(tif.toPath(), tiff);
        File outFile = tempDir.resolve("pick-frame.pdf").toFile();

        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Image image = new Image();
            image.setFile(tif.getAbsolutePath());
            image.setSelectedFrame(1);   // explicit frame → no expansion
            page.getParagraphs().add(image);
            doc.save(outFile.getAbsolutePath());
        }

        try (Document doc = new Document(outFile.getAbsolutePath())) {
            assertEquals(1, doc.getPages().getCount(),
                    "An Image with an explicit SelectedFrame must not expand");
        }
    }

    @Test
    public void getDecodableImageFramesCountsAndSingleFrame() throws Exception {
        assertArrayEquals(new int[]{0, 1, 2}, LayoutEngine.getDecodableImageFrames(buildTiff(3, 8, 8)));
        assertArrayEquals(new int[]{0}, LayoutEngine.getDecodableImageFrames(buildTiff(1, 8, 8)));

        // Single-frame PNG source stays single.
        BufferedImage png = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(png, "png", out);
        assertArrayEquals(new int[]{0}, LayoutEngine.getDecodableImageFrames(out.toByteArray()));
    }
}
