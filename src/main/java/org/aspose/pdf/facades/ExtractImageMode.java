package org.aspose.pdf.facades;

/**
 * Controls how {@link PdfExtractor#extractImage()} enumerates images.
 */
public enum ExtractImageMode {
    /**
     * Return every image resource defined on the selected pages.
     */
    ResourcesDefined,

    /**
     * Return only image XObjects that are actually invoked from page content.
     */
    ActuallyUsed
}
