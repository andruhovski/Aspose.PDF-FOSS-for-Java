package org.aspose.pdf;

/**
 * Hyperlink to an external URL. Used by Generator-mode layout — when a
 * {@link TextFragment} (or {@link Image}, {@code Heading}, etc.) has a
 * {@code WebHyperlink} set, the renderer emits a {@code /Link} annotation
 * with a {@code /URI} action pointing at {@link #getUrl()}.
 *
 * <p>Mirrors Aspose.PDF's {@code Aspose.Pdf.WebHyperlink} class.</p>
 */
public class WebHyperlink extends Hyperlink {

    private String url;

    /** Creates a hyperlink with no URL set (must be supplied later via {@link #setUrl(String)}). */
    public WebHyperlink() {
    }

    /**
     * Creates a hyperlink pointing to {@code url}.
     *
     * @param url the target URL
     */
    public WebHyperlink(String url) {
        this.url = url;
    }

    /** @return the target URL, or {@code null} if not yet set */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the target URL.
     *
     * @param url the URL the hyperlink should point to
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
