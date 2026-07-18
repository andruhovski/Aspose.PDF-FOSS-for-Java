package org.aspose.pdf.devices;

import org.aspose.pdf.Page;
import org.aspose.pdf.engine.render.PdfPageRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/// Abstract base class for devices that render a PDF page to a raster image.
///
/// Subclasses implement [#writeImage(BufferedImage, OutputStream)] to encode
/// the rendered image in the target format (PNG, JPEG, BMP, etc.).
///
/// @see PngDevice
/// @see JpegDevice
/// @see BmpDevice
public abstract class PageDevice {

    private static final Logger LOG = Logger.getLogger(PageDevice.class.getName());

    /// The rendering resolution.
    protected final Resolution resolution;

    /// Optional target width in pixels (0 = auto from resolution).
    protected final int targetWidth;

    /// Optional target height in pixels (0 = auto from resolution).
    protected final int targetHeight;

    /// Creates a device with the given resolution.
    ///
    /// @param resolution the rendering resolution
    /// @throws IllegalArgumentException if resolution is null
    protected PageDevice(Resolution resolution) {
        this(0, 0, resolution);
    }

    /// Creates a device with explicit pixel dimensions and resolution.
    ///
    /// When both width and height are specified, the page is rendered at
    /// the given resolution then scaled to fit the target dimensions.
    ///
    /// @param width      target width in pixels (0 = auto)
    /// @param height     target height in pixels (0 = auto)
    /// @param resolution the rendering resolution
    /// @throws IllegalArgumentException if resolution is null
    protected PageDevice(int width, int height, Resolution resolution) {
        if (resolution == null) {
            throw new IllegalArgumentException("Resolution must not be null");
        }
        this.targetWidth = width;
        this.targetHeight = height;
        this.resolution = resolution;
    }

    /// Renders a page and writes the result to the output stream.
    ///
    /// @param page   the PDF page to render
    /// @param output the output stream
    /// @throws IOException if rendering or writing fails
    public void process(Page page, OutputStream output) throws IOException {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("OutputStream must not be null");
        }

        PdfPageRenderer renderer = new PdfPageRenderer();
        BufferedImage image = renderer.renderPage(page, resolution.getX(), resolution.getY());

        // Scale to target dimensions if specified
        if (targetWidth > 0 && targetHeight > 0
                && (image.getWidth() != targetWidth || image.getHeight() != targetHeight)) {
            image = scaleImage(image, targetWidth, targetHeight);
        }

        writeImage(image, output);
        BufferedImage finalImage = image;
        LOG.fine(() -> "Page rendered: " + finalImage.getWidth() + "x" + finalImage.getHeight());
    }

    /// Writes the rendered image to the output stream in the device-specific format.
    ///
    /// @param image  the rendered image
    /// @param output the output stream
    /// @throws IOException if writing fails
    protected abstract void writeImage(BufferedImage image, OutputStream output) throws IOException;

    /// Returns the resolution used by this device.
    ///
    /// @return the resolution
    public Resolution getResolution() {
        return resolution;
    }

    /// Scales an image to the target dimensions using bilinear interpolation.
    protected BufferedImage scaleImage(BufferedImage src, int w, int h) {
        BufferedImage scaled = new BufferedImage(w, h, src.getType());
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }
}
