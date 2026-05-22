package org.aspose.pdf.text;

import org.aspose.pdf.Rectangle;

/**
 * Represents a segment of text with uniform formatting (same font, size, color).
 * <p>
 * A {@link TextFragment} may consist of multiple segments if the text changes
 * formatting mid-run. Each segment has its own {@link TextState}, position, and
 * bounding rectangle.
 * </p>
 */
public class TextSegment {

    private String text;
    private TextState textState;
    private Position position;
    private Rectangle rectangle;
    private int startCharIndex;
    private int endCharIndex = -1;

    /**
     * Creates a TextSegment with the given text.
     *
     * @param text the segment text
     */
    public TextSegment(String text) {
        this.text = text != null ? text : "";
        this.textState = new TextState();
    }

    /**
     * Creates an empty TextSegment.
     */
    public TextSegment() {
        this("");
    }

    /**
     * Returns the segment text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the segment text.
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    /**
     * Returns the text state (font, size, color, etc.).
     *
     * @return the text state
     */
    public TextState getTextState() {
        return textState;
    }

    /**
     * Sets the text state.
     *
     * @param textState the text state
     */
    public void setTextState(TextState textState) {
        this.textState = textState;
    }

    /**
     * Returns the position on the page where this segment begins.
     *
     * @return the position, or null
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the bounding rectangle of this segment on the page.
     *
     * @return the rectangle, or null
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Sets the bounding rectangle.
     *
     * @param rectangle the rectangle
     */
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    /**
     * Returns the zero-based start character index of this segment
     * within the matched source text.
     *
     * @return the start character index
     */
    public int getStartCharIndex() {
        return startCharIndex;
    }

    /**
     * Sets the zero-based start character index of this segment
     * within the matched source text.
     *
     * @param startCharIndex the start character index
     */
    public void setStartCharIndex(int startCharIndex) {
        this.startCharIndex = Math.max(0, startCharIndex);
        if (endCharIndex >= 0 && endCharIndex < this.startCharIndex) {
            endCharIndex = this.startCharIndex;
        }
    }

    /**
     * Returns the zero-based inclusive end character index of this segment
     * within the matched source text.
     *
     * @return the end character index, or {@code -1} when unknown
     */
    public int getEndCharIndex() {
        return endCharIndex;
    }

    /**
     * Sets the zero-based inclusive end character index of this segment
     * within the matched source text.
     *
     * @param endCharIndex the end character index
     */
    public void setEndCharIndex(int endCharIndex) {
        this.endCharIndex = endCharIndex < 0 ? -1 : Math.max(endCharIndex, startCharIndex);
    }

    @Override
    public String toString() {
        return text;
    }
}
