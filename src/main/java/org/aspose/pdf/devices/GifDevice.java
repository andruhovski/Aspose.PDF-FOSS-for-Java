package org.aspose.pdf.devices;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Renders a PDF page to GIF format.
 * <p>
 * GIF is an indexed-colour format limited to 256 colours per frame and has no
 * alpha channel. The rendered image is flattened to RGB on a white background
 * before encoding; quantisation to a 256-colour palette is performed by the
 * standard {@code javax.imageio} GIF writer.
 * </p>
 * <p>
 * The GIF writer is part of the JDK's built-in ImageIO providers
 * ({@code com.sun.imageio.plugins.gif.GIFImageWriter}) and is available on
 * every supported JDK.
 * </p>
 *
 * @see PngDevice
 * @see BmpDevice
 * @see JpegDevice
 */
public class GifDevice extends PageDevice {

    private static final Logger LOG = Logger.getLogger(GifDevice.class.getName());

    /**
     * Creates a GIF device with the given resolution.
     *
     * @param resolution the rendering resolution
     */
    public GifDevice(Resolution resolution) {
        super(resolution);
    }

    /**
     * Creates a GIF device with explicit pixel dimensions and resolution.
     *
     * @param width      target width in pixels
     * @param height     target height in pixels
     * @param resolution the rendering resolution
     */
    public GifDevice(int width, int height, Resolution resolution) {
        super(width, height, resolution);
    }

    @Override
    protected void writeImage(BufferedImage image, OutputStream output) throws IOException {
        // GIF has no alpha channel — flatten to RGB on a white background.
        // The standard ImageIO GIF writer accepts TYPE_INT_RGB and quantises
        // automatically; TYPE_INT_ARGB tends to produce a mostly-black image
        // due to how the default writer handles alpha.
        BufferedImage rgb;
        if (image.getType() == BufferedImage.TYPE_INT_RGB
                && image.getColorModel().getNumComponents() == 3
                && !image.getColorModel().hasAlpha()) {
            rgb = image;
        } else {
            rgb = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        if (!ImageIO.write(rgb, "GIF", output)) {
            throw new IOException("No GIF ImageIO writer available");
        }
        LOG.fine(() -> "GIF image written: " + rgb.getWidth() + "x" + rgb.getHeight());
    }
}
