package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a table header (THead) structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.3, Table 337).
///
/// A THead element groups rows that form the header of a table.
/// It may contain one or more [TableTRElement] children.
public class TableTHeadElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTHeadElement.class.getName());

    /// Creates a table header element wrapping the given structure element.
    ///
    /// @param se the structure element with type THead
    public TableTHeadElement(StructureElement se) {
        super(se);
    }
}
