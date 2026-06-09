package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the object ordering plan for writing a linearized PDF.
 * Objects are grouped into the parts defined in ISO 32000-1 Annex F §F.3.
 *
 * <ul>
 *   <li>Part 6: First page objects (private + shared needed for first page)</li>
 *   <li>Part 7: Other pages (each page's private objects)</li>
 *   <li>Part 8: Shared objects (referenced by multiple pages, not first)</li>
 *   <li>Parts 4/9: Document-level objects (catalog, page tree, etc.)</li>
 * </ul>
 */
public final class LinearizationPlan {

    private final List<PdfObjectKey> pageKeys;
    private final int firstPageIndex;
    private final List<PdfObjectKey> firstPagePrivate;
    private final List<PdfObjectKey> firstPageShared;
    private final Map<Integer, List<PdfObjectKey>> otherPagePrivate;
    private final List<PdfObjectKey> sharedObjects;
    private final List<PdfObjectKey> documentLevel;
    private final int numPages;

    /**
     * Creates a new linearization plan.
     *
     * @param pageKeys          page object keys in document order
     * @param firstPageIndex    index of the first page to display (usually 0)
     * @param firstPagePrivate  objects private to the first page
     * @param firstPageShared   shared objects needed by the first page
     * @param otherPagePrivate  per-page private objects (keyed by page index)
     * @param sharedObjects     objects shared between non-first pages
     * @param documentLevel     document-level objects not tied to any page
     * @param numPages          total number of pages
     */
    public LinearizationPlan(
            List<PdfObjectKey> pageKeys, int firstPageIndex,
            List<PdfObjectKey> firstPagePrivate, List<PdfObjectKey> firstPageShared,
            Map<Integer, List<PdfObjectKey>> otherPagePrivate,
            List<PdfObjectKey> sharedObjects, List<PdfObjectKey> documentLevel,
            int numPages) {
        this.pageKeys = Collections.unmodifiableList(new ArrayList<>(pageKeys));
        this.firstPageIndex = firstPageIndex;
        this.firstPagePrivate = Collections.unmodifiableList(new ArrayList<>(firstPagePrivate));
        this.firstPageShared = Collections.unmodifiableList(new ArrayList<>(firstPageShared));
        this.otherPagePrivate = otherPagePrivate;
        this.sharedObjects = Collections.unmodifiableList(new ArrayList<>(sharedObjects));
        this.documentLevel = Collections.unmodifiableList(new ArrayList<>(documentLevel));
        this.numPages = numPages;
    }

    /** Returns page object keys in document order. */
    public List<PdfObjectKey> getPageKeys() { return pageKeys; }

    /** Returns the index of the first page to display. */
    public int getFirstPageIndex() { return firstPageIndex; }

    /** Returns objects private to the first page (Part 6). */
    public List<PdfObjectKey> getFirstPagePrivate() { return firstPagePrivate; }

    /** Returns shared objects needed for the first page (Part 6). */
    public List<PdfObjectKey> getFirstPageShared() { return firstPageShared; }

    /** Returns all objects for Part 6 (first page private + shared). */
    public List<PdfObjectKey> getFirstPageObjects() {
        List<PdfObjectKey> result = new ArrayList<>(firstPagePrivate.size() + firstPageShared.size());
        result.addAll(firstPagePrivate);
        result.addAll(firstPageShared);
        return result;
    }

    /** Returns private objects for a given page index (Part 7). */
    public List<PdfObjectKey> getPagePrivateObjects(int pageIndex) {
        List<PdfObjectKey> list = otherPagePrivate.get(pageIndex);
        return list != null ? list : Collections.emptyList();
    }

    /** Returns the per-page private objects map. */
    public Map<Integer, List<PdfObjectKey>> getOtherPagePrivate() { return otherPagePrivate; }

    /** Returns shared objects for non-first pages (Part 8). */
    public List<PdfObjectKey> getSharedObjects() { return sharedObjects; }

    /** Returns document-level objects (Parts 4/9). */
    public List<PdfObjectKey> getDocumentLevel() { return documentLevel; }

    /** Returns the total number of pages. */
    public int getNumPages() { return numPages; }
}
