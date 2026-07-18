package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a table header cell (TH) structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.4, Table 337).
public class TableTHElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTHElement.class.getName());

    /// Creates a TH element wrapping the given structure element.
    ///
    /// @param se the structure element with type TH
    public TableTHElement(StructureElement se) {
        super(se);
    }
}
