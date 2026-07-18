package org.aspose.pdf;

/// Hyperlink to an external URL. Used by Generator-mode layout — when a
/// [org.aspose.pdf.text.TextFragment] (or [Image], [Heading], etc.) has a
/// `WebHyperlink` set, the renderer emits a `/Link` annotation
/// with a `/URI` action pointing at [#getUrl()].
///
/// Mirrors Aspose.PDF's `Aspose.Pdf.WebHyperlink` class.
public class WebHyperlink extends Hyperlink {

    private String url;

    /// Creates a hyperlink with no URL set (must be supplied later via [#setUrl(String)]).
    public WebHyperlink() {
    }

    /// Creates a hyperlink pointing to `url`.
    ///
    /// @param url the target URL
    public WebHyperlink(String url) {
        this.url = url;
    }

    /// @return the target URL, or `null` if not yet set
    public String getUrl() {
        return url;
    }

    /// Sets the target URL.
    ///
    /// @param url the URL the hyperlink should point to
    public void setUrl(String url) {
        this.url = url;
    }
}
