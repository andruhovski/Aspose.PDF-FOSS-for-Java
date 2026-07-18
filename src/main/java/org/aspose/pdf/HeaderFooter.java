package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents the header or footer area of a PDF page.
///
/// A header/footer contains a [Paragraphs] collection of content elements
/// that are rendered at the top or bottom of each page.
///
public class HeaderFooter {

    private static final Logger LOG = Logger.getLogger(HeaderFooter.class.getName());

    private Paragraphs paragraphs;
    private MarginInfo margin;

    /// Creates a new empty HeaderFooter.
    public HeaderFooter() {
        // defaults
    }

    /// Returns the margin for this header/footer.
    ///
    /// @return the margin info; never `null`
    public MarginInfo getMargin() {
        if (margin == null) {
            margin = new MarginInfo();
        }
        return margin;
    }

    /// Sets the margin for this header/footer.
    ///
    /// @param margin the margin info
    public void setMargin(MarginInfo margin) {
        this.margin = margin;
    }

    /// Returns the paragraphs collection for this header/footer, creating it lazily if needed.
    ///
    /// @return the paragraphs collection; never `null`
    public Paragraphs getParagraphs() {
        if (paragraphs == null) {
            paragraphs = new Paragraphs();
        }
        return paragraphs;
    }

    /// Sets the paragraphs collection for this header/footer.
    ///
    /// @param paragraphs the paragraphs collection
    public void setParagraphs(Paragraphs paragraphs) {
        this.paragraphs = paragraphs;
    }
}
