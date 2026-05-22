package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a list (L) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 336).
 */
public class ListElement extends Element {

    private static final Logger LOG = Logger.getLogger(ListElement.class.getName());

    /**
     * Creates a List element wrapping the given structure element.
     *
     * @param se the structure element with type L
     */
    public ListElement(StructureElement se) {
        super(se);
    }
}
