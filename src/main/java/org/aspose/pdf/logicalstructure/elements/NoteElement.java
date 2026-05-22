package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a note (Note) inline structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.4, Table 338).
 */
public class NoteElement extends Element {

    private static final Logger LOG = Logger.getLogger(NoteElement.class.getName());

    /**
     * Creates a Note element wrapping the given structure element.
     *
     * @param se the structure element with type Note
     */
    public NoteElement(StructureElement se) {
        super(se);
    }
}
