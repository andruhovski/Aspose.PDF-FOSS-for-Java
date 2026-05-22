package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSNull;
import org.aspose.pdf.engine.cos.COSObjectReference;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base for explicit destinations (ISO 32000-1:2008, §12.3.2.2, Table 151).
 * <p>
 * An explicit destination defines a view of a page: which page to display
 * and how (zoom level, position, fit mode).
 * </p>
 */
public abstract class ExplicitDestination implements IAppointment {

    private static final Logger LOG = Logger.getLogger(ExplicitDestination.class.getName());

    /** The target page (may be null if not yet resolved). */
    protected final Page page;
    /** The 1-based page number. */
    protected final int pageNumber;

    /**
     * Creates an ExplicitDestination targeting the given page.
     *
     * @param page the target page
     */
    protected ExplicitDestination(Page page) {
        this.page = page;
        this.pageNumber = page != null ? page.getNumber() : 1;
    }

    /**
     * Creates an ExplicitDestination with a page number only (no page object).
     *
     * @param pageNumber the 1-based page number
     */
    protected ExplicitDestination(int pageNumber) {
        this.page = null;
        this.pageNumber = pageNumber;
    }

    /**
     * Returns the target page.
     *
     * @return the page, or null
     */
    public Page getPage() { return page; }

    /**
     * Returns the 1-based page number.
     *
     * @return the page number
     */
    public int getPageNumber() { return pageNumber; }

    /**
     * Converts this destination to a COS array for serialization.
     *
     * @return the COS array representation
     */
    public abstract COSArray toCOSArray();

    /**
     * Parses an ExplicitDestination from a COS array.
     *
     * @param arr the COS array [pageRef /Type params...]
     * @param doc the document for resolving page references (may be null)
     * @return the parsed destination, or null if invalid
     * @throws IOException if resolution fails
     */
    public static ExplicitDestination fromCOSArray(COSArray arr, Document doc) throws IOException {
        if (arr == null || arr.size() < 2) return null;

        COSBase pageRef = arr.get(0);
        Page page = resolvePageRef(pageRef, doc);
        int pageNum = page != null ? page.getNumber() : extractPageNumber(pageRef);

        COSBase typeObj = arr.get(1);
        String type = typeObj instanceof COSName ? ((COSName) typeObj).getName() : "";

        switch (type) {
            case "XYZ":
                return new XYZExplicitDestination(page, pageNum,
                        getNum(arr, 2), getNum(arr, 3), getNum(arr, 4));
            case "Fit":
                return new FitExplicitDestination(page, pageNum);
            case "FitH":
                return new FitHExplicitDestination(page, pageNum, getNum(arr, 2));
            case "FitV":
                return new FitVExplicitDestination(page, pageNum, getNum(arr, 2));
            case "FitR":
                return new FitRExplicitDestination(page, pageNum,
                        getNum(arr, 2), getNum(arr, 3), getNum(arr, 4), getNum(arr, 5));
            case "FitB":
                return new FitBExplicitDestination(page, pageNum);
            case "FitBH":
                return new FitBHExplicitDestination(page, pageNum, getNum(arr, 2));
            case "FitBV":
                return new FitBVExplicitDestination(page, pageNum, getNum(arr, 2));
            default:
                return new FitExplicitDestination(page, pageNum);
        }
    }

    /**
     * Helper: creates a COSBase number or COSNull for NaN.
     */
    protected static COSBase numOrNull(double v) {
        if (Double.isNaN(v)) return COSNull.INSTANCE;
        if (v == Math.floor(v) && !Double.isInfinite(v) && Math.abs(v) < Integer.MAX_VALUE) {
            return COSInteger.valueOf((long) v);
        }
        return new COSFloat(v);
    }

    /**
     * Helper: gets a numeric value from array at index, or NaN.
     */
    private static double getNum(COSArray arr, int index) {
        if (index >= arr.size()) return Double.NaN;
        COSBase val = arr.get(index);
        if (val instanceof COSInteger) return ((COSInteger) val).intValue();
        if (val instanceof COSFloat) return ((COSFloat) val).doubleValue();
        return Double.NaN; // null entries
    }

    private static Page resolvePageRef(COSBase pageRef, Document doc) throws IOException {
        if (doc == null) return null;
        if (pageRef instanceof COSObjectReference) {
            pageRef = ((COSObjectReference) pageRef).dereference();
        }
        if (pageRef instanceof COSDictionary) {
            COSDictionary pageDict = (COSDictionary) pageRef;
            // Find matching page in document
            PageCollection pages = doc.getPages();
            for (int i = 1; i <= pages.getCount(); i++) {
                if (pages.get(i).getCOSDictionary() == pageDict) {
                    return pages.get(i);
                }
            }
        }
        // Try as page index (integer)
        if (pageRef instanceof COSInteger) {
            int idx = ((COSInteger) pageRef).intValue();
            PageCollection pages = doc.getPages();
            if (idx >= 0 && idx < pages.getCount()) {
                return pages.get(idx + 1); // convert 0-based to 1-based
            }
        }
        return null;
    }

    private static int extractPageNumber(COSBase pageRef) {
        if (pageRef instanceof COSInteger) return ((COSInteger) pageRef).intValue() + 1;
        return 1;
    }
}
