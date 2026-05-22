package org.aspose.pdf;

import org.aspose.pdf.text.TextSegment;
import org.aspose.pdf.text.TextState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a heading element that can be used in a table of contents.
 * <p>
 * A heading has a level (1-based), display text, and can link to a
 * destination page in the document.
 * </p>
 */
public class Heading extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(Heading.class.getName());

    private int level;
    private String text;
    private Page tocPage;
    private Page destinationPage;
    private double top;
    private final List<TextSegment> segments;
    private boolean isAutoSequence;
    private boolean isInList = true;
    private NumberingStyle style = NumberingStyle.None;
    private int startNumber = 1;
    private TextState textState = new TextState();
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.Left;

    /**
     * Creates a heading with the specified level.
     *
     * @param level the heading level (1 for top-level, 2 for sub-heading, etc.)
     */
    public Heading(int level) {
        this.level = level;
        this.segments = new ArrayList<>();
    }

    /**
     * Returns the heading level.
     *
     * @return the level (1-based)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the heading level.
     *
     * @param level the level (1-based)
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the display text of this heading.
     *
     * @return the text, or {@code null} if not set
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the display text of this heading.
     *
     * @param text the text to display
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the TOC page where this heading entry appears.
     *
     * @return the TOC page, or {@code null}
     */
    public Page getTocPage() {
        return tocPage;
    }

    /**
     * Sets the TOC page where this heading entry appears.
     *
     * @param tocPage the TOC page
     */
    public void setTocPage(Page tocPage) {
        this.tocPage = tocPage;
    }

    /**
     * Returns the destination page that this heading links to.
     *
     * @return the destination page, or {@code null}
     */
    public Page getDestinationPage() {
        return destinationPage;
    }

    /**
     * Sets the destination page that this heading links to.
     *
     * @param destinationPage the destination page
     */
    public void setDestinationPage(Page destinationPage) {
        this.destinationPage = destinationPage;
    }

    /**
     * Returns the top Y coordinate for the destination on the target page.
     *
     * @return the top position in points
     */
    public double getTop() {
        return top;
    }

    /**
     * Sets the top Y coordinate for the destination on the target page.
     *
     * @param top the top position in points
     */
    public void setTop(double top) {
        this.top = top;
    }

    /**
     * Returns the list of text segments composing this heading.
     *
     * @return the segments list (never {@code null})
     */
    public List<TextSegment> getSegments() {
        return segments;
    }

    /** Returns whether this heading is auto-sequenced in the TOC. */
    public boolean isAutoSequence() { return isAutoSequence; }

    /** Sets whether this heading is auto-sequenced in the TOC. */
    public void setIsAutoSequence(boolean autoSequence) { this.isAutoSequence = autoSequence; }

    /** Returns whether this heading appears in the TOC list. Default: true. */
    public boolean isInList() { return isInList; }

    /** Sets whether this heading appears in the TOC list. */
    public void setIsInList(boolean inList) { this.isInList = inList; }

    /** Returns the numbering style for this heading. */
    public NumberingStyle getStyle() { return style; }

    /** Sets the numbering style for this heading. */
    public void setStyle(NumberingStyle style) { this.style = style; }

    /** Returns the start number for auto-sequencing. */
    public int getStartNumber() { return startNumber; }

    /** Sets the start number for auto-sequencing. */
    public void setStartNumber(int startNumber) { this.startNumber = startNumber; }

    /** Returns the text state (font, size, color) for this heading. */
    public TextState getTextState() { return textState; }

    /** Returns the horizontal alignment. */
    public HorizontalAlignment getHorizontalAlignment() { return horizontalAlignment; }

    /** Sets the horizontal alignment. */
    public void setHorizontalAlignment(HorizontalAlignment alignment) { this.horizontalAlignment = alignment; }
}
