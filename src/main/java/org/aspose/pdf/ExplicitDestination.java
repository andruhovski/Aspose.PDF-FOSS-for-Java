package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfNull;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

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
     * Converts this destination to a PDF array for serialization.
     *
     * @return the PDF array representation
     */
    public abstract PdfArray toPdfArray();

    /**
     * Parses an ExplicitDestination from a PDF array.
     *
     * @param arr the PDF array [pageRef /Type params...]
     * @param doc the document for resolving page references (may be null)
     * @return the parsed destination, or null if invalid
     * @throws IOException if resolution fails
     */
    public static ExplicitDestination fromPdfArray(PdfArray arr, Document doc) throws IOException {
        if (arr == null || arr.size() < 2) return null;

        PdfBase pageRef = arr.get(0);
        Page page = resolvePageRef(pageRef, doc);
        int pageNum = page != null ? page.getNumber() : extractPageNumber(pageRef);

        PdfBase typeObj = arr.get(1);
        String type = typeObj instanceof PdfName ? ((PdfName) typeObj).getName() : "";

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
     * Helper: creates a PdfBase number or PdfNull for NaN.
     */
    protected static PdfBase numOrNull(double v) {
        if (Double.isNaN(v)) return PdfNull.INSTANCE;
        if (v == Math.floor(v) && !Double.isInfinite(v) && Math.abs(v) < Integer.MAX_VALUE) {
            return PdfInteger.valueOf((long) v);
        }
        return new PdfFloat(v);
    }

    /**
     * Helper: gets a numeric value from array at index, or NaN.
     */
    private static double getNum(PdfArray arr, int index) {
        if (index >= arr.size()) return Double.NaN;
        PdfBase val = arr.get(index);
        if (val instanceof PdfInteger) return ((PdfInteger) val).intValue();
        if (val instanceof PdfFloat) return ((PdfFloat) val).doubleValue();
        return Double.NaN; // null entries
    }

    private static Page resolvePageRef(PdfBase pageRef, Document doc) throws IOException {
        if (doc == null) return null;
        if (pageRef instanceof PdfObjectReference) {
            pageRef = ((PdfObjectReference) pageRef).dereference();
        }
        if (pageRef instanceof PdfDictionary) {
            PdfDictionary pageDict = (PdfDictionary) pageRef;
            // Find matching page in document
            PageCollection pages = doc.getPages();
            for (int i = 1; i <= pages.getCount(); i++) {
                if (pages.get(i).getPdfDictionary() == pageDict) {
                    return pages.get(i);
                }
            }
        }
        // Try as page index (integer)
        if (pageRef instanceof PdfInteger) {
            int idx = ((PdfInteger) pageRef).intValue();
            PageCollection pages = doc.getPages();
            if (idx >= 0 && idx < pages.getCount()) {
                return pages.get(idx + 1); // convert 0-based to 1-based
            }
        }
        return null;
    }

    private static int extractPageNumber(PdfBase pageRef) {
        if (pageRef instanceof PdfInteger) return ((PdfInteger) pageRef).intValue() + 1;
        return 1;
    }
}
