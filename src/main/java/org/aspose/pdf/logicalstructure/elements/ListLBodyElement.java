package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a list body (LBody) structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.3, Table 336).
///
/// An LBody element holds the descriptive content of a list item,
/// which may include nested lists or other block-level elements.
public class ListLBodyElement extends Element {

    private static final Logger LOG = Logger.getLogger(ListLBodyElement.class.getName());

    /// Creates a list body element wrapping the given structure element.
    ///
    /// @param se the structure element with type LBody
    public ListLBodyElement(StructureElement se) {
        super(se);
    }
}
