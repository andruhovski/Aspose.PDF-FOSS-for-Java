package org.aspose.pdf.tagged;

import org.aspose.pdf.HorizontalAlignment;
import org.aspose.pdf.MarginInfo;
import org.aspose.pdf.VerticalAlignment;

import java.util.logging.Logger;

/// Position settings for tagged PDF structure elements.
///
/// Controls the placement and layout of tagged elements within
/// the document's visual presentation.
public class PositionSettings {

    private static final Logger LOG = Logger.getLogger(PositionSettings.class.getName());

    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private boolean isFirstParagraphInColumn;
    private boolean isInLineParagraph;
    private boolean isInNewPage;
    private boolean isKeptWithNext;
    private MarginInfo margin;

    /// Creates position settings with default values.
    public PositionSettings() {
    }

    /// Creates position settings with the given margin.
    ///
    /// @param margin the margin info
    public PositionSettings(MarginInfo margin) {
        this.margin = margin;
    }

    /// Returns the horizontal alignment.
    ///
    /// @return the horizontal alignment, or `null`
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /// Sets the horizontal alignment.
    ///
    /// @param horizontalAlignment the alignment
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /// Returns the vertical alignment.
    ///
    /// @return the vertical alignment, or `null`
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /// Sets the vertical alignment.
    ///
    /// @param verticalAlignment the alignment
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    /// Returns whether this paragraph starts in a new column.
    ///
    /// @return `true` if first paragraph in column
    public boolean isFirstParagraphInColumn() {
        return isFirstParagraphInColumn;
    }

    /// Sets whether this paragraph starts in a new column.
    ///
    /// @param firstParagraphInColumn`true` to start in new column
    public void setFirstParagraphInColumn(boolean firstParagraphInColumn) {
        this.isFirstParagraphInColumn = firstParagraphInColumn;
    }

    /// Returns whether this is an inline paragraph.
    ///
    /// @return `true` if inline
    public boolean isInLineParagraph() {
        return isInLineParagraph;
    }

    /// Sets whether this is an inline paragraph.
    ///
    /// @param inLineParagraph`true` for inline
    public void setInLineParagraph(boolean inLineParagraph) {
        this.isInLineParagraph = inLineParagraph;
    }

    /// Returns whether this element forces a new page.
    ///
    /// @return `true` if in new page
    public boolean isInNewPage() {
        return isInNewPage;
    }

    /// Sets whether this element forces a new page.
    ///
    /// @param inNewPage`true` to force new page
    public void setInNewPage(boolean inNewPage) {
        this.isInNewPage = inNewPage;
    }

    /// Returns whether this paragraph is kept with the next one.
    ///
    /// @return `true` if kept with next
    public boolean isKeptWithNext() {
        return isKeptWithNext;
    }

    /// Sets whether this paragraph is kept with the next one.
    ///
    /// @param keptWithNext`true` to keep with next
    public void setKeptWithNext(boolean keptWithNext) {
        this.isKeptWithNext = keptWithNext;
    }

    /// Returns the margin.
    ///
    /// @return the margin info, or `null`
    public MarginInfo getMargin() {
        return margin;
    }

    /// Sets the margin.
    ///
    /// @param margin the margin info
    public void setMargin(MarginInfo margin) {
        this.margin = margin;
    }
}
