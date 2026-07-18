package org.aspose.pdf.engine.pattern;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/// Renders shading fills onto a Graphics2D context.
/// Handles the `sh` operator and Pattern color spaces with shading patterns.
///
/// For axial, radial, and function-based shadings, the renderer samples the function
/// at each pixel within the clipping bounds. For mesh shadings (types 4–7), a fallback
/// color is used.
public final class ShadingRenderer {

    private static final Logger LOG = Logger.getLogger(ShadingRenderer.class.getName());

    private ShadingRenderer() {}

    /// Renders a shading fill onto the Graphics2D context.
    /// Fills the current clipping region with the shading colors.
    ///
    /// @param g2d       the graphics context
    /// @param shading   the shading to render
    /// @param ctm       current transformation matrix (user space → device space)
    /// @param clipBounds the clipping bounds in device space (may be `null`)
    public static void render(Graphics2D g2d, Shading shading,
                               AffineTransform ctm, java.awt.Rectangle clipBounds) {
        if (shading == null) return;
        if (clipBounds == null || clipBounds.width <= 0 || clipBounds.height <= 0) {
            clipBounds = g2d.getClipBounds();
            if (clipBounds == null) return;
        }

        if (shading instanceof AxialShading || shading instanceof RadialShading
                || shading instanceof FunctionBasedShading) {
            renderPixelBased(g2d, shading, ctm, clipBounds);
        } else {
            renderFallback(g2d, shading, clipBounds);
        }
    }

    /// Renders a shading by sampling the function at each pixel.
    /// Works for axial, radial, and function-based shadings.
    private static void renderPixelBased(Graphics2D g2d, Shading shading,
                                          AffineTransform ctm, java.awt.Rectangle clipUser) {
        // ctm maps shading space → DEVICE pixels (base g2d transform × state
        // CTM), so sampling must walk device pixels. clipUser however is
        // expressed in g2d USER space — convert the clip (the precise clip
        // shape when one is set) to device space first. Feeding user-space
        // coords into the device-space inverse samples the wrong spot and,
        // with /Extend [false false], returns null everywhere — nothing
        // painted (corpus 28762: all chart gradient bars missing).
        AffineTransform base = g2d.getTransform();
        java.awt.Shape clipShape = g2d.getClip();
        java.awt.Rectangle clip = base.createTransformedShape(
                clipShape != null ? clipShape : clipUser).getBounds();
        int w = clip.width;
        int h = clip.height;
        if (w <= 0 || h <= 0) return;
        if ((long) w * h > MAX_SHADING_PIXELS) {
            LOG.fine(() -> "Shading area too large, skipping: " + clip);
            return;
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        AffineTransform inverse;
        try {
            inverse = ctm.createInverse();
        } catch (NoninvertibleTransformException e) {
            LOG.fine(() -> "Non-invertible CTM, using identity for shading");
            inverse = new AffineTransform();
        }

        double[] pt = new double[2];
        for (int py = 0; py < h; py++) {
            // Cancellation check per row: function-based shadings evaluate a
            // (possibly PostScript) function per pixel — minutes on big clips.
            if (Thread.currentThread().isInterrupted()) return;
            for (int px = 0; px < w; px++) {
                // Transform device pixel to shading coordinate space
                pt[0] = clip.x + px;
                pt[1] = clip.y + py;
                inverse.transform(pt, 0, pt, 0, 1);

                double[] color = shading.getColorAt(pt[0], pt[1]);
                if (color == null) {
                    continue; // outside the gradient, /Extend false → unpainted
                }
                int argb = colorToARGB(color, shading.getColorSpace());
                img.setRGB(px, py, argb);
            }
        }
        // Paint in device space: reset the transform for the blit. The g2d
        // clip still applies — Java2D tracks it in device space — so pixels
        // outside a non-rectangular clip path remain masked.
        g2d.setTransform(new AffineTransform());
        try {
            g2d.drawImage(img, clip.x, clip.y, null);
        } finally {
            g2d.setTransform(base);
        }
    }

    /// Upper bound on the sampled shading raster (≈ a few full pages at 300 dpi).
    private static final long MAX_SHADING_PIXELS = 64L * 1024 * 1024;

    /// Renders a fallback for mesh shadings (types 4–7): fills with background or gray.
    private static void renderFallback(Graphics2D g2d, Shading shading,
                                         java.awt.Rectangle clip) {
        double[] bg = shading.getBackground();
        int argb;
        if (bg != null) {
            argb = colorToARGB(bg, shading.getColorSpace());
        } else {
            argb = 0xFF808080; // mid-gray fallback
        }
        java.awt.Color color = new java.awt.Color(argb, true);
        g2d.setColor(color);
        g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
    }

    /// Converts shading-function output to packed ARGB via the shading's
    /// ColorSpace. The components are in the SHADING's color space — reading
    /// them as RGB renders a DeviceCMYK blue `[1 .6 0 0]` as orange
    /// (corpus 29077/10734); Separation/DeviceN need their tint transform.
    private static int colorToARGB(double[] color,
                                   org.aspose.pdf.engine.colorspace.ColorSpaceBase cs) {
        if (color == null || color.length == 0) return 0xFF000000;
        if (cs != null) {
            try {
                return 0xFF000000 | cs.toRGBInt(color);
            } catch (Exception e) {
                LOG.fine(() -> "Shading colorspace conversion failed: " + e.getMessage());
            }
        }
        // No colorspace — fall back to mapping by component count.
        int r = clamp255(color[0]);
        int g = clamp255(color.length > 1 ? color[1] : color[0]);
        int b = clamp255(color.length > 2 ? color[2] : color[0]);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int clamp255(double v) {
        return Math.max(0, Math.min(255, (int) (v * 255)));
    }
}
