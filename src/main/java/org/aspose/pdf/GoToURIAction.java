package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

/**
 * GoToURI action — alias for {@link UriAction} for API compatibility with Aspose.PDF.
 * Opens a Uniform Resource Identifier (ISO 32000-1:2008, §12.6.4.7).
 */
public class GoToURIAction extends UriAction {

    /**
     * Creates a GoToURIAction for the given URI string.
     *
     * @param uri the URI to open
     */
    public GoToURIAction(String uri) {
        super(uri);
    }

    /**
     * Parses a GoToURIAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public GoToURIAction(PdfDictionary dict) {
        super(dict);
    }

    /**
     * Returns the URI string.
     *
     * @return the URI, or null
     */
    public String getURI() {
        return getUri();
    }

    /**
     * Sets the URI string.
     *
     * @param uri the URI
     */
    public void setURI(String uri) {
        setUri(uri);
    }
}
