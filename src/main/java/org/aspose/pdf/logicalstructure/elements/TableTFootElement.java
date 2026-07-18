package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a table footer (TFoot) structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.3, Table 337).
///
/// A TFoot element groups rows that form the footer of a table.
/// It may contain one or more [TableTRElement] children.
public class TableTFootElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTFootElement.class.getName());

    /// Creates a table footer element wrapping the given structure element.
    ///
    /// @param se the structure element with type TFoot
    public TableTFootElement(StructureElement se) {
        super(se);
    }
}
