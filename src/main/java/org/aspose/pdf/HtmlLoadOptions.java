package org.aspose.pdf;

/// Options for loading an HTML document into a PDF document.
public class HtmlLoadOptions {

    /// Page layout information for the resulting PDF.
    private PageInfo pageInfo = new PageInfo();

    /// Base path for resolving relative image and resource paths.
    private String basePath;

    /// Whether to disable font license verifications during loading.
    private boolean disableFontLicenseVerifications;

    /// Creates a new `HtmlLoadOptions` with default settings.
    public HtmlLoadOptions() {
    }

    /// Creates a new `HtmlLoadOptions` with the specified base path.
    ///
    /// @param basePath the base path for resolving relative resources
    public HtmlLoadOptions(String basePath) {
        this.basePath = basePath;
    }

    /// Gets the page layout information for the resulting PDF.
    ///
    /// @return the page info, or `null` if not set
    public PageInfo getPageInfo() {
        return pageInfo;
    }

    /// Sets the page layout information for the resulting PDF.
    ///
    /// @param pageInfo the page info to set
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /// Gets the base path for resolving relative image and resource paths.
    ///
    /// @return the base path, or `null` if not set
    public String getBasePath() {
        return basePath;
    }

    /// Sets the base path for resolving relative image and resource paths.
    ///
    /// @param basePath the base path
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /// Gets whether font license verifications are disabled during loading.
    ///
    /// @return `true` if font license verifications are disabled
    public boolean isDisableFontLicenseVerifications() {
        return disableFontLicenseVerifications;
    }

    /// Sets whether to disable font license verifications during loading.
    ///
    /// @param disable`true` to disable font license verifications
    public void setDisableFontLicenseVerifications(boolean disable) {
        this.disableFontLicenseVerifications = disable;
    }
}
