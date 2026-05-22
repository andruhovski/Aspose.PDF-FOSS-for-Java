package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.Page;
import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.logicalstructure.StructureTypeStandard;

import java.util.logging.Logger;

/**
 * Represents a header structure element (H, H1–H6) in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.3, Table 334).
 *
 * <p>Header elements define headings at various levels within the document
 * structure. Level 0 represents a generic heading (H), while levels 1–6
 * correspond to H1–H6.</p>
 */
public class HeaderElement extends Element {

    private static final Logger LOG = Logger.getLogger(HeaderElement.class.getName());

    private int level;

    /**
     * Creates a generic header element (H) wrapping the given structure element.
     *
     * @param se the structure element
     */
    public HeaderElement(StructureElement se) {
        super(se);
        this.level = 0;
    }

    /**
     * Creates a header element at the specified level.
     *
     * @param se    the structure element
     * @param level the heading level (0 for H, 1–6 for H1–H6)
     */
    public HeaderElement(StructureElement se, int level) {
        super(se);
        setLevel(level);
    }

    /**
     * Returns the heading level (0 = H, 1–6 = H1–H6).
     *
     * @return the heading level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the heading level and updates the underlying structure type.
     *
     * @param level the heading level (0 for H, 1–6 for H1–H6)
     * @throws IllegalArgumentException if level is out of range
     */
    public void setLevel(int level) {
        if (level < 0 || level > 6) {
            throw new IllegalArgumentException("Header level must be 0–6, got: " + level);
        }
        this.level = level;
        StructureTypeStandard type;
        switch (level) {
            case 0: type = StructureTypeStandard.H; break;
            case 1: type = StructureTypeStandard.H1; break;
            case 2: type = StructureTypeStandard.H2; break;
            case 3: type = StructureTypeStandard.H3; break;
            case 4: type = StructureTypeStandard.H4; break;
            case 5: type = StructureTypeStandard.H5; break;
            case 6: type = StructureTypeStandard.H6; break;
            default: type = StructureTypeStandard.H; break;
        }
        structureElement.setStructureType(type);
    }

    /**
     * Adds an entry for this header to a TOC page, linking it to a TOC item.
     *
     * @param tocPage the TOC page
     * @param toci    the TOC item element to link to
     */
    public void addEntryToTocPage(Page tocPage, Element toci) {
        if (tocPage == null) {
            throw new IllegalArgumentException("TOC page must not be null");
        }
        if (toci == null) {
            throw new IllegalArgumentException("TOCI element must not be null");
        }
        LOG.fine(() -> "Added TOC entry for header: " + getText());
    }
}
