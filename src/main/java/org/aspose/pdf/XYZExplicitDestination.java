package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/**
 * XYZ explicit destination (ISO 32000-1:2008, Table 151).
 * <p>
 * Display the page with coordinates (left, top) at the upper-left corner
 * and the page magnified by factor zoom. A null (NaN) value for any parameter
 * means to use the current value.
 * </p>
 */
public class XYZExplicitDestination extends ExplicitDestination {

    private final double left;
    private final double top;
    private final double zoom;

    /**
     * Creates an XYZ destination targeting a page.
     *
     * @param page the target page
     * @param left the left coordinate (NaN = unchanged)
     * @param top  the top coordinate (NaN = unchanged)
     * @param zoom the zoom factor (0 or NaN = unchanged)
     */
    public XYZExplicitDestination(Page page, double left, double top, double zoom) {
        super(page);
        this.left = left;
        this.top = top;
        this.zoom = zoom;
    }

    XYZExplicitDestination(Page page, int pageNum, double left, double top, double zoom) {
        super(page != null ? page : null);
        this.left = left;
        this.top = top;
        this.zoom = zoom;
    }

    /**
     * Creates an XYZ destination by page number (no page object yet known).
     * Useful when constructing destinations before pages exist or by index.
     *
     * @param pageNumber 1-based page number
     * @param left       the left coordinate (NaN = unchanged)
     * @param top        the top coordinate (NaN = unchanged)
     * @param zoom       the zoom factor (0 or NaN = unchanged)
     */
    public XYZExplicitDestination(int pageNumber, double left, double top, double zoom) {
        super(pageNumber);
        this.left = left;
        this.top = top;
        this.zoom = zoom;
    }

    /** Returns the left coordinate, or NaN if unspecified. */
    public double getLeft() { return left; }

    /** Returns the top coordinate, or NaN if unspecified. */
    public double getTop() { return top; }

    /** Returns the zoom factor (0 = unchanged). */
    public double getZoom() { return Double.isNaN(zoom) ? 0.0 : zoom; }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("XYZ"));
        arr.add(numOrNull(left));
        arr.add(numOrNull(top));
        arr.add(Double.isNaN(zoom) || zoom == 0 ? org.aspose.pdf.engine.cos.COSNull.INSTANCE : numOrNull(zoom));
        return arr;
    }
}
