package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;

/// A stamp consisting of an entire PDF page, overlaid onto another page.
///
/// The source page content is copied into the target page's content stream,
/// wrapped in a graphics state save/restore pair for isolation.
///
public class PdfPageStamp extends Stamp {

    private Page sourcePage;
    private transient PdfObjectReference cachedFormReference;
    private transient Document cachedTargetDocument;

    /// Creates a PdfPageStamp from the given source page.
    ///
    /// @param page the source page whose content will be used as a stamp
    public PdfPageStamp(Page page) {
        this.sourcePage = page;
    }

    /// Returns the source page.
    ///
    /// @return the source page
    public Page getSourcePage() {
        return sourcePage;
    }

    /// Sets the source page.
    ///
    /// @param page the source page
    public void setSourcePage(Page page) {
        this.sourcePage = page;
    }

    PdfObjectReference getCachedFormReference() {
        return cachedFormReference;
    }

    Document getCachedTargetDocument() {
        return cachedTargetDocument;
    }

    void cacheFormReference(Document targetDocument, PdfObjectReference formReference) {
        this.cachedTargetDocument = targetDocument;
        this.cachedFormReference = formReference;
    }

    /// Applies this page stamp to the given page.
    ///
    /// The rendering is delegated to [Page#addStamp(PdfPageStamp)].
    ///
    /// @param page the page to stamp; must not be `null`
    /// @throws IOException if content stream processing fails
    @Override
    public void put(Page page) throws IOException {
        if (page == null) throw new IllegalArgumentException("page must not be null");
        page.addStamp(this);
    }
}
