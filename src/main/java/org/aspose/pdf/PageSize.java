package org.aspose.pdf;

import java.util.logging.Logger;

/**
 * Predefined page sizes and custom page dimensions for PDF documents.
 * <p>
 * Dimensions are specified in PDF user-space units (1/72 inch).
 * Common sizes such as A4 and Letter are available as static constants.
 * </p>
 */
public class PageSize {

    private static final Logger LOG = Logger.getLogger(PageSize.class.getName());

    /** ISO A1: 594 x 841 mm (1684 x 2384 points). */
    public static final PageSize A1 = new PageSize(1684, 2384);

    /** ISO A2: 420 x 594 mm (1190 x 1684 points). */
    public static final PageSize A2 = new PageSize(1190, 1684);

    /** ISO A3: 297 x 420 mm (842 x 1190 points). */
    public static final PageSize A3 = new PageSize(842, 1190);

    /** ISO A4: 210 x 297 mm (595 x 842 points). */
    public static final PageSize A4 = new PageSize(595, 842);

    /** US Letter: 8.5 x 11 in (612 x 792 points). */
    public static final PageSize LETTER = new PageSize(612, 792);

    /** US Legal: 8.5 x 14 in (612 x 1008 points). */
    public static final PageSize LEGAL = new PageSize(612, 1008);

    private final double width;
    private final double height;

    /**
     * Creates a page size with the specified dimensions.
     *
     * @param width  the width in user-space units (points)
     * @param height the height in user-space units (points)
     */
    public PageSize(double width, double height) {
        this.width = width;
        this.height = height;
        LOG.fine(() -> "PageSize created: " + width + " x " + height);
    }

    /**
     * Returns the width in user-space units (points).
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height in user-space units (points).
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "PageSize[" + width + " x " + height + "]";
    }
}
