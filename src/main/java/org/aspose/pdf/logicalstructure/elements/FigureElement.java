package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a figure (Figure) illustration structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.5).
 */
public class FigureElement extends Element {

    private static final Logger LOG = Logger.getLogger(FigureElement.class.getName());

    private String imagePath;

    /**
     * Creates a Figure element wrapping the given structure element.
     *
     * @param se the structure element with type Figure
     */
    public FigureElement(StructureElement se) {
        super(se);
    }

    /**
     * Sets the image file path for this figure element.
     *
     * @param imagePath the path to the image file
     */
    public void setImage(String imagePath) {
        this.imagePath = imagePath;
        LOG.fine(() -> "Set image path: " + imagePath);
    }

    /**
     * Returns the image file path.
     *
     * @return the image path, or {@code null}
     */
    public String getImagePath() {
        return imagePath;
    }
}
