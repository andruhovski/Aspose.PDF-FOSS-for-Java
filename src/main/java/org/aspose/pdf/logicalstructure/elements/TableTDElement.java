package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a table data cell (TD) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.4, Table 337).
 */
public class TableTDElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTDElement.class.getName());

    /**
     * Creates a TD element wrapping the given structure element.
     *
     * @param se the structure element with type TD
     */
    public TableTDElement(StructureElement se) {
        super(se);
    }
}
