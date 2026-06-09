package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.Page;
import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.logicalstructure.StructureTypeStandard;
import org.aspose.pdf.tagged.TaggedException;
import org.aspose.pdf.tagged.TOCpageHasNoTitleException;
import org.aspose.pdf.text.TextFragment;

import java.util.logging.Logger;

/**
 * Represents a Table of Contents (TOC) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.2, Table 333).
 *
 * <p>A TOC element may only be a direct child of the root structure element
 * (the /Document element). Its children must be TOCIElement instances.</p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>TOC can only be a child of the root element</li>
 *   <li>TOC children must be TOCI elements only</li>
 *   <li>Multiple TOC elements at the root level are permitted</li>
 * </ul>
 */
public class TOCElement extends Element {

    private static final Logger LOG = Logger.getLogger(TOCElement.class.getName());

    private Page linkedTocPage;
    private HeaderElement linkedTitleHeader;

    /**
     * Creates a TOC element wrapping the given structure element.
     *
     * @param se the structure element with type TOC
     */
    public TOCElement(StructureElement se) {
        super(se);
    }

    /**
     * Appends a child element. Only {@link TOCIElement} children are allowed.
     *
     * @param child the child element to append
     * @throws TaggedException if the child is not a TOCIElement
     */
    @Override
    public void appendChild(Element child) {
        if (!(child instanceof TOCIElement)) {
            throw new TaggedException(
                "TOC element can only contain TOCI children, got: "
                + child.getStructureElement().getStructureType());
        }
        super.appendChild(child);
    }

    /**
     * Adds a reference to a target element. The reference is recorded
     * as a /Ref entry on the underlying structure element dictionary.
     *
     * @param target the element to reference
     */
    public void addRef(Element target) {
        if (target == null) {
            throw new IllegalArgumentException("Reference target must not be null");
        }
        LOG.fine(() -> "Adding Ref from TOC to " + target.getStructureElement().getStructureType());
        // Store the reference in the PDF dictionary
        structureElement.getPdfDictionary().set(
            org.aspose.pdf.engine.pdfobjects.PdfName.of("Ref"),
            target.getStructureElement().getPdfDictionary());
    }

    /**
     * Links the TOC page title to a header element. The TOC page must have
     * a title set via {@code TocInfo.setTitle()}.
     *
     * @param page   the TOC page (must have TocInfo with a title)
     * @param header the header element to link to
     * @throws TOCpageHasNoTitleException if the page has no TocInfo title
     */
    public void linkTocPageTitleToHeaderElement(Page page, HeaderElement header) {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        if (header == null) {
            throw new IllegalArgumentException("Header must not be null");
        }
        // Verify the page has a TocInfo with a title
        if (page.getTocInfo() == null || page.getTocInfo().getTitle() == null) {
            throw new TOCpageHasNoTitleException(
                "TOC page has no title. Set TocInfo with a title on the page before linking.");
        }
        this.linkedTocPage = page;
        this.linkedTitleHeader = header;
        LOG.fine(() -> "Linked TOC page title to header element");
    }

    /**
     * Returns the linked TOC page, or {@code null} if not set.
     *
     * @return the linked page
     */
    public Page getLinkedTocPage() {
        return linkedTocPage;
    }

    /**
     * Returns the linked title header, or {@code null} if not set.
     *
     * @return the linked header element
     */
    public HeaderElement getLinkedTitleHeader() {
        return linkedTitleHeader;
    }
}
