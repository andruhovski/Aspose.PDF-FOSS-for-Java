package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.nio.charset.StandardCharsets;

/**
 * URI action — open a Uniform Resource Identifier (ISO 32000-1:2008, §12.6.4.7).
 */
public class UriAction extends PdfAction {

    /**
     * Creates a UriAction for the given URI string.
     *
     * @param uri the URI to open
     */
    public UriAction(String uri) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("URI"));
        actionDict.set(PdfName.of("URI"), new PdfString(uri));
    }

    /**
     * Parses a UriAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public UriAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the URI string.
     *
     * @return the URI, or null
     */
    public String getUri() {
        PdfBase uri = actionDict.get("URI");
        if (uri instanceof PdfObjectReference) {
            try {
                uri = ((PdfObjectReference) uri).dereference();
            } catch (java.io.IOException e) {
                return null;
            }
        }
        if (uri instanceof PdfString) return ((PdfString) uri).getString();
        if (uri instanceof PdfName) return ((PdfName) uri).getName();
        return null;
    }

    /**
     * Sets the URI string.
     *
     * @param uri the URI
     */
    public void setUri(String uri) {
        actionDict.set(PdfName.of("URI"), new PdfString(uri));
    }
}
