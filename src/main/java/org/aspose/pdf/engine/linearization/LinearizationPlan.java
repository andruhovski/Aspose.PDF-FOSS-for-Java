package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.cos.COSObjectKey;

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

    private final List<COSObjectKey> pageKeys;
    private final int firstPageIndex;
    private final List<COSObjectKey> firstPagePrivate;
    private final List<COSObjectKey> firstPageShared;
    private final Map<Integer, List<COSObjectKey>> otherPagePrivate;
    private final List<COSObjectKey> sharedObjects;
    private final List<COSObjectKey> documentLevel;
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
            List<COSObjectKey> pageKeys, int firstPageIndex,
            List<COSObjectKey> firstPagePrivate, List<COSObjectKey> firstPageShared,
            Map<Integer, List<COSObjectKey>> otherPagePrivate,
            List<COSObjectKey> sharedObjects, List<COSObjectKey> documentLevel,
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
    public List<COSObjectKey> getPageKeys() { return pageKeys; }

    /** Returns the index of the first page to display. */
    public int getFirstPageIndex() { return firstPageIndex; }

    /** Returns objects private to the first page (Part 6). */
    public List<COSObjectKey> getFirstPagePrivate() { return firstPagePrivate; }

    /** Returns shared objects needed for the first page (Part 6). */
    public List<COSObjectKey> getFirstPageShared() { return firstPageShared; }

    /** Returns all objects for Part 6 (first page private + shared). */
    public List<COSObjectKey> getFirstPageObjects() {
        List<COSObjectKey> result = new ArrayList<>(firstPagePrivate.size() + firstPageShared.size());
        result.addAll(firstPagePrivate);
        result.addAll(firstPageShared);
        return result;
    }

    /** Returns private objects for a given page index (Part 7). */
    public List<COSObjectKey> getPagePrivateObjects(int pageIndex) {
        List<COSObjectKey> list = otherPagePrivate.get(pageIndex);
        return list != null ? list : Collections.emptyList();
    }

    /** Returns the per-page private objects map. */
    public Map<Integer, List<COSObjectKey>> getOtherPagePrivate() { return otherPagePrivate; }

    /** Returns shared objects for non-first pages (Part 8). */
    public List<COSObjectKey> getSharedObjects() { return sharedObjects; }

    /** Returns document-level objects (Parts 4/9). */
    public List<COSObjectKey> getDocumentLevel() { return documentLevel; }

    /** Returns the total number of pages. */
    public int getNumPages() { return numPages; }
}
