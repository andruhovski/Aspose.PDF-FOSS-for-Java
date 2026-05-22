package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSObjectReference;

import java.io.IOException;

/**
 * A stamp consisting of an entire PDF page, overlaid onto another page.
 * <p>
 * The source page content is copied into the target page's content stream,
 * wrapped in a graphics state save/restore pair for isolation.
 * </p>
 */
public class PdfPageStamp extends Stamp {

    private Page sourcePage;
    private transient COSObjectReference cachedFormReference;
    private transient Document cachedTargetDocument;

    /**
     * Creates a PdfPageStamp from the given source page.
     *
     * @param page the source page whose content will be used as a stamp
     */
    public PdfPageStamp(Page page) {
        this.sourcePage = page;
    }

    /**
     * Returns the source page.
     *
     * @return the source page
     */
    public Page getSourcePage() {
        return sourcePage;
    }

    /**
     * Sets the source page.
     *
     * @param page the source page
     */
    public void setSourcePage(Page page) {
        this.sourcePage = page;
    }

    COSObjectReference getCachedFormReference() {
        return cachedFormReference;
    }

    Document getCachedTargetDocument() {
        return cachedTargetDocument;
    }

    void cacheFormReference(Document targetDocument, COSObjectReference formReference) {
        this.cachedTargetDocument = targetDocument;
        this.cachedFormReference = formReference;
    }

    /**
     * Applies this page stamp to the given page.
     * <p>
     * The rendering is delegated to {@link Page#addStamp(PdfPageStamp)}.
     * </p>
     *
     * @param page the page to stamp; must not be {@code null}
     * @throws IOException if content stream processing fails
     */
    @Override
    public void put(Page page) throws IOException {
        if (page == null) throw new IllegalArgumentException("page must not be null");
        page.addStamp(this);
    }
}
