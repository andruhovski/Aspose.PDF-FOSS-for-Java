package org.aspose.pdf;

import java.util.logging.Logger;

/**
 * Represents a page number stamp that renders the current page number and total page count
 * on each page of the PDF document.
 * <p>
 * Extends {@link TextStamp} and uses a format string where {@code #} is replaced
 * with the current page number and {@code $P} is replaced with the total number of pages.
 * For example, the default format {@code "Page # of $P"} renders as "Page 1 of 10".
 * </p>
 */
public class PageNumberStamp extends TextStamp {

    private static final Logger LOG = Logger.getLogger(PageNumberStamp.class.getName());

    /** Default format string for page numbering. */
    private static final String DEFAULT_FORMAT = "Page # of $P";

    private String format;
    private int startingNumber = 1;

    /**
     * Creates a new PageNumberStamp with the default format "Page # of $P".
     */
    public PageNumberStamp() {
        super(DEFAULT_FORMAT);
        this.format = DEFAULT_FORMAT;
    }

    /**
     * Creates a new PageNumberStamp with the specified format string.
     * <p>
     * In the format string, {@code #} is replaced with the current page number and
     * {@code $P} is replaced with the total page count.
     * </p>
     *
     * @param format the format string
     */
    public PageNumberStamp(String format) {
        super(format != null ? format : DEFAULT_FORMAT);
        this.format = format != null ? format : DEFAULT_FORMAT;
    }

    /**
     * Returns the format string for the page number.
     * <p>
     * {@code #} is replaced with the current page number and {@code $P} with the total pages.
     * </p>
     *
     * @return the format string
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format string for the page number.
     *
     * @param format the format string
     */
    public void setFormat(String format) {
        this.format = format != null ? format : DEFAULT_FORMAT;
    }

    /**
     * Returns the starting page number.
     *
     * @return the starting number; defaults to 1
     */
    public int getStartingNumber() {
        return startingNumber;
    }

    /**
     * Sets the starting page number.
     *
     * @param startingNumber the starting number
     */
    public void setStartingNumber(int startingNumber) {
        this.startingNumber = startingNumber;
    }

    /**
     * Formats the page number text for the given page index and total page count.
     * <p>
     * Replaces {@code #} with {@code (pageIndex + startingNumber)} and {@code $P}
     * with {@code totalPages}.
     * </p>
     *
     * @param pageIndex  the zero-based page index
     * @param totalPages the total number of pages in the document
     * @return the formatted page number string
     */
    public String formatPageNumber(int pageIndex, int totalPages) {
        int pageNum = pageIndex + startingNumber;
        String result = format.replace("#", String.valueOf(pageNum));
        result = result.replace("$P", String.valueOf(totalPages));
        return result;
    }
}
