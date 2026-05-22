package org.aspose.pdf;

/**
 * Describes a page-label range entry used by {@link PageLabels}.
 * <p>
 * A page label can define a numbering style, optional prefix, and starting
 * value for all pages beginning at a specific index in the document.
 * </p>
 */
public class PageLabel {

    private NumberingStyle numberingStyle = NumberingStyle.None;
    private String prefix = "";
    private int startingValue = 1;

    /**
     * Returns the numbering style.
     *
     * @return the numbering style
     */
    public NumberingStyle getNumberingStyle() {
        return numberingStyle;
    }

    /**
     * Sets the numbering style.
     *
     * @param numberingStyle the numbering style
     */
    public void setNumberingStyle(NumberingStyle numberingStyle) {
        this.numberingStyle = numberingStyle != null ? numberingStyle : NumberingStyle.None;
    }

    /**
     * Returns the optional label prefix.
     *
     * @return the label prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the optional label prefix.
     *
     * @param prefix the label prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    /**
     * Returns the starting numeric value for this range.
     *
     * @return the starting value
     */
    public int getStartingValue() {
        return startingValue;
    }

    /**
     * Sets the starting numeric value for this range.
     *
     * @param startingValue the starting value
     */
    public void setStartingValue(int startingValue) {
        this.startingValue = Math.max(1, startingValue);
    }
}
