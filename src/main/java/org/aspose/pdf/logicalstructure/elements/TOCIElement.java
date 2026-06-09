package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.tagged.TaggedException;

import java.util.logging.Logger;

/**
 * Represents a Table of Contents Item (TOCI) structure element in the logical
 * structure tree (ISO 32000-1:2008, §14.8.4.2, Table 333).
 *
 * <p>A TOCI element represents a single entry in a table of contents.
 * It may contain header elements, paragraph elements, and references,
 * but may NOT contain other TOCI elements or TOC elements as direct children.</p>
 */
public class TOCIElement extends Element {

    private static final Logger LOG = Logger.getLogger(TOCIElement.class.getName());

    /**
     * Creates a TOCI element wrapping the given structure element.
     *
     * @param se the structure element with type TOCI
     */
    public TOCIElement(StructureElement se) {
        super(se);
    }

    /**
     * Appends a child element. TOCI and TOC elements are not allowed as children.
     *
     * @param child the child element to append
     * @throws TaggedException if the child is a TOCIElement or TOCElement
     */
    @Override
    public void appendChild(Element child) {
        if (child instanceof TOCIElement) {
            throw new TaggedException(
                "TOCI element cannot contain another TOCI element as a direct child");
        }
        if (child instanceof TOCElement) {
            throw new TaggedException(
                "TOCI element cannot contain a TOC element as a child");
        }
        super.appendChild(child);
    }

    /**
     * Adds a reference to a header element. This establishes a link between
     * this TOC item and the referenced content.
     *
     * @param header the header element to reference
     */
    public void addRef(HeaderElement header) {
        if (header == null) {
            throw new IllegalArgumentException("Header reference must not be null");
        }
        LOG.fine(() -> "Adding Ref from TOCI to header: " + header.getText());
        structureElement.getPdfDictionary().set(
            org.aspose.pdf.engine.pdfobjects.PdfName.of("Ref"),
            header.getStructureElement().getPdfDictionary());
    }
}
