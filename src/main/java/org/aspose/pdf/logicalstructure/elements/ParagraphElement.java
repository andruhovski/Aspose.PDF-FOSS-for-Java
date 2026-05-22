package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a paragraph (P) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 334).
 */
public class ParagraphElement extends Element {

    private static final Logger LOG = Logger.getLogger(ParagraphElement.class.getName());

    /**
     * Creates a paragraph element wrapping the given structure element.
     *
     * @param se the structure element with type P
     */
    public ParagraphElement(StructureElement se) {
        super(se);
    }
}
