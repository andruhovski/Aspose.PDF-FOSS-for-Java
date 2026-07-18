package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a span (Span) inline structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.4, Table 338).
public class SpanElement extends Element {

    private static final Logger LOG = Logger.getLogger(SpanElement.class.getName());

    /// Creates a Span element wrapping the given structure element.
    ///
    /// @param se the structure element with type Span
    public SpanElement(StructureElement se) {
        super(se);
    }
}
