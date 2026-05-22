package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;

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
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("URI"));
        actionDict.set(COSName.of("URI"), new COSString(uri.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Parses a UriAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public UriAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the URI string.
     *
     * @return the URI, or null
     */
    public String getUri() {
        COSBase uri = actionDict.get("URI");
        if (uri instanceof COSObjectReference) {
            try {
                uri = ((COSObjectReference) uri).dereference();
            } catch (java.io.IOException e) {
                return null;
            }
        }
        if (uri instanceof COSString) return ((COSString) uri).getString();
        if (uri instanceof COSName) return ((COSName) uri).getName();
        return null;
    }

    /**
     * Sets the URI string.
     *
     * @param uri the URI
     */
    public void setUri(String uri) {
        actionDict.set(COSName.of("URI"), new COSString(uri.getBytes(StandardCharsets.UTF_8)));
    }
}
