package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a table row (TR) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.4, Table 337).
 */
public class TableTRElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTRElement.class.getName());

    /**
     * Creates a TR element wrapping the given structure element.
     *
     * @param se the structure element with type TR
     */
    public TableTRElement(StructureElement se) {
        super(se);
    }
}
