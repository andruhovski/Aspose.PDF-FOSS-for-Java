package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a table body (TBody) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 337).
 *
 * <p>A TBody element groups rows that form the body (main content) of a table.
 * It may contain one or more {@link TableTRElement} children.</p>
 */
public class TableTBodyElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableTBodyElement.class.getName());

    /**
     * Creates a table body element wrapping the given structure element.
     *
     * @param se the structure element with type TBody
     */
    public TableTBodyElement(StructureElement se) {
        super(se);
    }
}
