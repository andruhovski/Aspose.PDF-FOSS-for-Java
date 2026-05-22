package org.aspose.pdf;

/**
 * Options for saving a document in HTML format.
 */
public class HtmlSaveOptions {

    /** The type of HTML document to generate. */
    private HtmlDocumentType documentType = HtmlDocumentType.Html5;

    /** Whether to use fixed layout rendering. */
    private boolean fixedLayout = true;

    /** Whether to split the document into separate HTML pages. */
    private boolean splitIntoPages = false;

    /** Whether to embed images as base64 data URIs in the HTML. */
    private boolean embedImages = true;

    /** The scale factor applied to the output. */
    private double scale = 1.0;

    /** The folder path for storing external images when {@code embedImages} is {@code false}. */
    private String imageFolder;

    /** A prefix to prepend to CSS class names. */
    private String cssPrefix = "";

    /**
     * Gets the HTML document type.
     *
     * @return the document type
     */
    public HtmlDocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Sets the HTML document type.
     *
     * @param documentType the document type to set
     */
    public void setDocumentType(HtmlDocumentType documentType) {
        this.documentType = documentType;
    }

    /**
     * Gets whether fixed layout rendering is enabled.
     *
     * @return {@code true} if fixed layout is enabled
     */
    public boolean isFixedLayout() {
        return fixedLayout;
    }

    /**
     * Sets whether to use fixed layout rendering.
     *
     * @param fixedLayout {@code true} to enable fixed layout
     */
    public void setFixedLayout(boolean fixedLayout) {
        this.fixedLayout = fixedLayout;
    }

    /**
     * Gets whether the document is split into separate HTML pages.
     *
     * @return {@code true} if splitting is enabled
     */
    public boolean isSplitIntoPages() {
        return splitIntoPages;
    }

    /**
     * Sets whether to split the document into separate HTML pages.
     *
     * @param splitIntoPages {@code true} to split into pages
     */
    public void setSplitIntoPages(boolean splitIntoPages) {
        this.splitIntoPages = splitIntoPages;
    }

    /**
     * Gets whether images are embedded as base64 data URIs.
     *
     * @return {@code true} if images are embedded
     */
    public boolean isEmbedImages() {
        return embedImages;
    }

    /**
     * Sets whether to embed images as base64 data URIs.
     *
     * @param embedImages {@code true} to embed images
     */
    public void setEmbedImages(boolean embedImages) {
        this.embedImages = embedImages;
    }

    /**
     * Gets the scale factor applied to the output.
     *
     * @return the scale factor
     */
    public double getScale() {
        return scale;
    }

    /**
     * Sets the scale factor applied to the output.
     *
     * @param scale the scale factor
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Gets the folder path for storing external images.
     * Used when {@code embedImages} is {@code false}.
     *
     * @return the image folder path, or {@code null} if not set
     */
    public String getImageFolder() {
        return imageFolder;
    }

    /**
     * Sets the folder path for storing external images.
     * Used when {@code embedImages} is {@code false}.
     *
     * @param imageFolder the image folder path
     */
    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    /**
     * Gets the prefix prepended to CSS class names.
     *
     * @return the CSS prefix
     */
    public String getCssPrefix() {
        return cssPrefix;
    }

    /**
     * Sets the prefix to prepend to CSS class names.
     *
     * @param cssPrefix the CSS prefix
     */
    public void setCssPrefix(String cssPrefix) {
        this.cssPrefix = cssPrefix;
    }
}
