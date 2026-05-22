package org.aspose.pdf.operators;

/**
 * Represents a single element in a TJ (show text with glyph positioning) array.
 * <p>
 * Each element is either a text string or a numeric adjustment. Text strings are shown
 * using the current font. Numeric adjustments shift the text position by the given amount
 * (in thousandths of a unit of text space), with positive values moving the text to the left
 * in horizontal writing mode.
 * See ISO 32000-1:2008, §9.4.3.
 * </p>
 */
public class GlyphPosition {

    private final String text;
    private final double adjustment;
    private final boolean isText;

    /**
     * Creates a text glyph position with no adjustment.
     *
     * @param text the text string to display
     */
    public GlyphPosition(String text) {
        this.text = text;
        this.adjustment = 0;
        this.isText = true;
    }

    /**
     * Creates a numeric adjustment glyph position.
     *
     * @param adjustment the position adjustment (in thousandths of text space units)
     */
    public GlyphPosition(double adjustment) {
        this.text = null;
        this.adjustment = adjustment;
        this.isText = false;
    }

    /**
     * Creates a text glyph position with an associated adjustment.
     *
     * @param text       the text string to display
     * @param adjustment the position adjustment
     */
    public GlyphPosition(String text, double adjustment) {
        this.text = text;
        this.adjustment = adjustment;
        this.isText = true;
    }

    /**
     * Returns the text string, or null if this is a numeric adjustment only.
     *
     * @return the text, or null
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the position adjustment value.
     *
     * @return the adjustment in thousandths of text space units
     */
    public double getAdjustment() {
        return adjustment;
    }

    /**
     * Returns whether this element represents a text string.
     *
     * @return true if this is a text element, false if it is a numeric adjustment
     */
    public boolean isText() {
        return isText;
    }

    @Override
    public String toString() {
        if (isText) {
            return "GlyphPosition{text=\"" + text + "\", adjustment=" + adjustment + "}";
        }
        return "GlyphPosition{adjustment=" + adjustment + "}";
    }
}
