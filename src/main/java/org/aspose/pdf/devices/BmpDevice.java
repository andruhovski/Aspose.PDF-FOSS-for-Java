package org.aspose.pdf.devices;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Renders a PDF page to BMP format.
 * <p>
 * BMP does not support alpha; the rendered image is flattened to RGB
 * with a white background.
 * </p>
 */
public class BmpDevice extends PageDevice {

    private static final Logger LOG = Logger.getLogger(BmpDevice.class.getName());

    /**
     * Creates a BMP device with the given resolution.
     *
     * @param resolution the rendering resolution
     */
    public BmpDevice(Resolution resolution) {
        super(resolution);
    }

    /**
     * Creates a BMP device with explicit dimensions and resolution.
     *
     * @param width      target width in pixels
     * @param height     target height in pixels
     * @param resolution the rendering resolution
     */
    public BmpDevice(int width, int height, Resolution resolution) {
        super(width, height, resolution);
    }

    @Override
    protected void writeImage(BufferedImage image, OutputStream output) throws IOException {
        // BMP doesn't support alpha — flatten to RGB
        BufferedImage rgb = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();

        if (!ImageIO.write(rgb, "BMP", output)) {
            throw new IOException("No BMP ImageIO writer available");
        }
        LOG.fine(() -> "BMP image written: " + rgb.getWidth() + "x" + rgb.getHeight());
    }
}
