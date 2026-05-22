package org.aspose.pdf;

/**
 * Hyperlink to another location inside the same document — either a target
 * {@link BaseParagraph} (set via {@link #setTarget(BaseParagraph)}) or a
 * specific 1-based page number ({@link #setTargetPageNumber(int)}). Mirrors
 * Aspose.PDF's {@code Aspose.Pdf.LocalHyperlink}.
 */
public class LocalHyperlink extends Hyperlink {

    private BaseParagraph target;
    private int targetPageNumber;

    /** Creates an empty local hyperlink. */
    public LocalHyperlink() {
    }

    /**
     * Creates a local hyperlink pointing at a paragraph anchor inside the same document.
     *
     * @param target the paragraph the link should jump to
     */
    public LocalHyperlink(BaseParagraph target) {
        this.target = target;
    }

    /** @return the linked paragraph, or {@code null} if a page-number link is used instead */
    public BaseParagraph getTarget() {
        return target;
    }

    /**
     * Sets the paragraph anchor this link should jump to. Clears any
     * previously-set {@link #setTargetPageNumber(int) page number}.
     *
     * @param target the target paragraph
     */
    public void setTarget(BaseParagraph target) {
        this.target = target;
    }

    /** @return the 1-based target page number, or {@code 0} if a paragraph target is used instead */
    public int getTargetPageNumber() {
        return targetPageNumber;
    }

    /**
     * Sets the page number this link should jump to (1-based). Clears any
     * previously-set {@link #setTarget(BaseParagraph) paragraph target}.
     *
     * @param targetPageNumber the page number to jump to
     */
    public void setTargetPageNumber(int targetPageNumber) {
        this.targetPageNumber = targetPageNumber;
    }
}
