package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a list item (LI) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 336).
 */
public class ListLIElement extends Element {

    private static final Logger LOG = Logger.getLogger(ListLIElement.class.getName());

    /**
     * Creates a LI element wrapping the given structure element.
     *
     * @param se the structure element with type LI
     */
    public ListLIElement(StructureElement se) {
        super(se);
    }

    /**
     * Adds a reference to a target element from this list item.
     *
     * @param target the element to reference
     */
    public void addRef(Element target) {
        if (target == null) {
            throw new IllegalArgumentException("Reference target must not be null");
        }
        LOG.fine(() -> "Adding Ref from LI to " + target.getStructureElement().getStructureType());
        structureElement.getPdfDictionary().set(
            org.aspose.pdf.engine.pdfobjects.PdfName.of("Ref"),
            target.getStructureElement().getPdfDictionary());
    }
}
