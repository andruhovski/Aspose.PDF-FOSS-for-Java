package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.tagged.TaggedException;

import java.util.logging.Logger;

/**
 * Represents a division (Div) grouping structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.2, Table 333).
 */
public class DivElement extends Element {

    private static final Logger LOG = Logger.getLogger(DivElement.class.getName());

    /**
     * Creates a Div element wrapping the given structure element.
     *
     * @param se the structure element with type Div
     */
    public DivElement(StructureElement se) {
        super(se);
    }

    /**
     * Appends a child element. TOC elements are not allowed as children of Div;
     * TOC may only be a child of the root element.
     *
     * @param child the child element to append
     * @throws TaggedException if the child is a TOCElement
     */
    @Override
    public void appendChild(Element child) {
        if (child instanceof TOCElement) {
            throw new TaggedException(
                "TOC element may only be a child of the root element, not of Div");
        }
        super.appendChild(child);
    }
}
