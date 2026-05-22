package org.aspose.pdf.engine.pattern;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Renders shading fills onto a Graphics2D context.
 * Handles the {@code sh} operator and Pattern color spaces with shading patterns.
 *
 * <p>For axial, radial, and function-based shadings, the renderer samples the function
 * at each pixel within the clipping bounds. For mesh shadings (types 4–7), a fallback
 * color is used.</p>
 */
public final class ShadingRenderer {

    private static final Logger LOG = Logger.getLogger(ShadingRenderer.class.getName());

    private ShadingRenderer() {}

    /**
     * Renders a shading fill onto the Graphics2D context.
     * Fills the current clipping region with the shading colors.
     *
     * @param g2d       the graphics context
     * @param shading   the shading to render
     * @param ctm       current transformation matrix (user space → device space)
     * @param clipBounds the clipping bounds in device space (may be {@code null})
     */
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

    /**
     * Renders a shading by sampling the function at each pixel.
     * Works for axial, radial, and function-based shadings.
     */
    private static void renderPixelBased(Graphics2D g2d, Shading shading,
                                          AffineTransform ctm, java.awt.Rectangle clip) {
        int w = clip.width;
        int h = clip.height;
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
            for (int px = 0; px < w; px++) {
                // Transform device pixel to shading coordinate space
                pt[0] = clip.x + px;
                pt[1] = clip.y + py;
                inverse.transform(pt, 0, pt, 0, 1);

                double[] color = shading.getColorAt(pt[0], pt[1]);
                int argb = colorToARGB(color);
                img.setRGB(px, py, argb);
            }
        }
        g2d.drawImage(img, clip.x, clip.y, null);
    }

    /**
     * Renders a fallback for mesh shadings (types 4–7): fills with background or gray.
     */
    private static void renderFallback(Graphics2D g2d, Shading shading,
                                         java.awt.Rectangle clip) {
        double[] bg = shading.getBackground();
        int argb;
        if (bg != null) {
            argb = colorToARGB(bg);
        } else {
            argb = 0xFF808080; // mid-gray fallback
        }
        java.awt.Color color = new java.awt.Color(argb, true);
        g2d.setColor(color);
        g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Converts color components (0..1 range) to packed ARGB int.
     */
    private static int colorToARGB(double[] color) {
        if (color == null || color.length == 0) return 0xFF000000;
        int r = clamp255(color.length > 0 ? color[0] : 0);
        int g = clamp255(color.length > 1 ? color[1] : 0);
        int b = clamp255(color.length > 2 ? color[2] : 0);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int clamp255(double v) {
        return Math.max(0, Math.min(255, (int) (v * 255)));
    }
}
