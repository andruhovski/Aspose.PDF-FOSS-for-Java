package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a list label (Lbl) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 336).
 *
 * <p>A Lbl (label) element identifies the label or marker for a list item,
 * such as a bullet, number, or other designation.</p>
 */
public class ListLblElement extends Element {

    private static final Logger LOG = Logger.getLogger(ListLblElement.class.getName());

    /**
     * Creates a list label element wrapping the given structure element.
     *
     * @param se the structure element with type Lbl
     */
    public ListLblElement(StructureElement se) {
        super(se);
    }
}
