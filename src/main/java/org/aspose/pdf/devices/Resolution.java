package org.aspose.pdf.devices;

import java.util.logging.Logger;

/**
 * Represents the resolution (DPI) for rendering PDF pages to images.
 * <p>
 * Horizontal and vertical resolutions can differ. When a single value is
 * provided, it is used for both axes. Standard screen resolution is 72 DPI
 * (matching PDF user-space units); 150 and 300 DPI are common for printing.
 * </p>
 *
 * @see PageDevice
 */
public class Resolution {

    private static final Logger LOG = Logger.getLogger(Resolution.class.getName());

    private final int x;
    private final int y;

    /**
     * Creates a resolution with equal horizontal and vertical DPI.
     *
     * @param dpi the dots-per-inch value for both axes
     * @throws IllegalArgumentException if dpi is not positive
     */
    public Resolution(int dpi) {
        this(dpi, dpi);
    }

    /**
     * Creates a resolution with separate horizontal and vertical DPI.
     *
     * @param x the horizontal DPI
     * @param y the vertical DPI
     * @throws IllegalArgumentException if either value is not positive
     */
    public Resolution(int x, int y) {
        if (x <= 0 || y <= 0) {
            throw new IllegalArgumentException("Resolution must be positive, got " + x + "x" + y);
        }
        this.x = x;
        this.y = y;
        LOG.fine(() -> "Resolution created: " + x + "x" + y + " DPI");
    }

    /**
     * Returns the horizontal resolution in DPI.
     *
     * @return the horizontal DPI
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the vertical resolution in DPI.
     *
     * @return the vertical DPI
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return x == y ? x + " DPI" : x + "x" + y + " DPI";
    }
}
