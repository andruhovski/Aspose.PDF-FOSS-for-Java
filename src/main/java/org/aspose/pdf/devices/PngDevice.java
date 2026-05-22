package org.aspose.pdf.devices;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Renders a PDF page to PNG format.
 * <p>
 * Uses {@link javax.imageio.ImageIO} with the "png" writer.
 * PNG output is lossless with full alpha support.
 * </p>
 */
public class PngDevice extends PageDevice {

    private static final Logger LOG = Logger.getLogger(PngDevice.class.getName());

    /**
     * Creates a PNG device with the given resolution.
     *
     * @param resolution the rendering resolution
     */
    public PngDevice(Resolution resolution) {
        super(resolution);
    }

    /**
     * Creates a PNG device with explicit dimensions and resolution.
     *
     * @param width      target width in pixels
     * @param height     target height in pixels
     * @param resolution the rendering resolution
     */
    public PngDevice(int width, int height, Resolution resolution) {
        super(width, height, resolution);
    }

    @Override
    protected void writeImage(BufferedImage image, OutputStream output) throws IOException {
        if (!ImageIO.write(image, "PNG", output)) {
            throw new IOException("No PNG ImageIO writer available");
        }
        LOG.fine(() -> "PNG image written: " + image.getWidth() + "x" + image.getHeight());
    }
}
