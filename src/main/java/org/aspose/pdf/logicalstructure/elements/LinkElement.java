package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a link (Link) inline structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.4, Table 338).
public class LinkElement extends Element {

    private static final Logger LOG = Logger.getLogger(LinkElement.class.getName());

    private String hyperlink;

    /// Creates a Link element wrapping the given structure element.
    ///
    /// @param se the structure element with type Link
    public LinkElement(StructureElement se) {
        super(se);
    }

    /// Sets the hyperlink URL for this link element.
    ///
    /// @param url the URL string
    public void setHyperlink(String url) {
        this.hyperlink = url;
        LOG.fine(() -> "Set hyperlink: " + url);
    }

    /// Returns the hyperlink URL.
    ///
    /// @return the URL, or `null`
    public String getHyperlink() {
        return hyperlink;
    }
}
