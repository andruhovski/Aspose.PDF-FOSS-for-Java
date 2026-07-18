package org.aspose.pdf;

import java.util.logging.Logger;

/// Abstract base class for all content elements that can appear in a PDF page's paragraph collection.
///
/// Subclasses include `TextFragment`, `Table`, `Image`, `FloatingBox`,
/// `HtmlFragment`, and other block-level or inline content elements.
/// Each paragraph carries its own margin, alignment, and layout hint properties.
///
public abstract class BaseParagraph {

    private static final Logger LOG = Logger.getLogger(BaseParagraph.class.getName());

    private MarginInfo margin = new MarginInfo();
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.None;
    private boolean isInLineParagraph;
    private boolean isKeptWithNext;
    private boolean isFirstParagraphInColumn;
    private boolean isInNewPage;
    private Hyperlink hyperlink;

    /// Creates a BaseParagraph with default settings.
    protected BaseParagraph() {
        // defaults applied by field initializers
    }

    /// Gets the margin information for this paragraph.
    ///
    /// @return the margin info, or `null` if not set
    public MarginInfo getMargin() {
        return margin;
    }

    /// Sets the margin information for this paragraph.
    ///
    /// @param margin the margin info to apply
    public void setMargin(MarginInfo margin) {
        this.margin = margin;
    }

    /// Gets the horizontal alignment of this paragraph within its container.
    ///
    /// @return the horizontal alignment; defaults to [HorizontalAlignment#None]
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /// Sets the horizontal alignment of this paragraph within its container.
    ///
    /// @param horizontalAlignment the horizontal alignment to apply
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /// Returns whether this paragraph is an inline element.
    ///
    /// @return `true` if this paragraph is rendered inline
    public boolean isInLineParagraph() {
        return isInLineParagraph;
    }

    /// Sets whether this paragraph should be treated as an inline element.
    ///
    /// @param inLineParagraph`true` to render this paragraph inline
    public void setInLineParagraph(boolean inLineParagraph) {
        this.isInLineParagraph = inLineParagraph;
    }

    /// Returns whether this paragraph should be kept together with the next paragraph
    /// on the same page during layout.
    ///
    /// @return `true` if this paragraph is kept with the next
    public boolean isKeptWithNext() {
        return isKeptWithNext;
    }

    /// Sets whether this paragraph should be kept together with the next paragraph
    /// on the same page during layout.
    ///
    /// @param keptWithNext`true` to keep this paragraph with the next
    public void setKeptWithNext(boolean keptWithNext) {
        this.isKeptWithNext = keptWithNext;
    }

    /// Returns whether this paragraph is the first in its column.
    ///
    /// @return `true` if this is the first paragraph in a column
    public boolean isFirstParagraphInColumn() {
        return isFirstParagraphInColumn;
    }

    /// Sets whether this paragraph is the first in its column.
    ///
    /// @param firstParagraphInColumn`true` to mark as first paragraph in column
    public void setFirstParagraphInColumn(boolean firstParagraphInColumn) {
        this.isFirstParagraphInColumn = firstParagraphInColumn;
    }

    /// Returns whether this paragraph should start on a new page during layout.
    ///
    /// @return `true` if this paragraph starts on a new page
    public boolean isInNewPage() {
        return isInNewPage;
    }

    /// Sets whether this paragraph should start on a new page during layout.
    ///
    /// @param inNewPage`true` to force a page break before this paragraph
    public void setInNewPage(boolean inNewPage) {
        this.isInNewPage = inNewPage;
    }

    /// Returns the hyperlink attached to this paragraph (web, local or file
    /// target), or `null` if the paragraph is not clickable.
    ///
    /// @return the configured hyperlink, or `null`
    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    /// Attaches a hyperlink to this paragraph. When the document is rendered
    /// via Generator-mode layout, a corresponding `/Link` annotation is
    /// emitted over the paragraph's bounds with the appropriate action.
    ///
    /// @param hyperlink the hyperlink to attach, or `null` to remove
    public void setHyperlink(Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
    }
}
