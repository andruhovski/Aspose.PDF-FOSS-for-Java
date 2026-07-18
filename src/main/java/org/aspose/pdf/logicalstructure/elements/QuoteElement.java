package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a quote (Quote) inline structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.4, Table 338).
public class QuoteElement extends Element {

    private static final Logger LOG = Logger.getLogger(QuoteElement.class.getName());

    /// Creates a Quote element wrapping the given structure element.
    ///
    /// @param se the structure element with type Quote
    public QuoteElement(StructureElement se) {
        super(se);
    }
}
