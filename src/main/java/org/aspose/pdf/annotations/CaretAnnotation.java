package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Caret annotation (ISO 32000-1:2008, Section 12.5.6.11, /Subtype /Caret).
 * <p>
 * A caret annotation indicates a proposed insertion point in the document text.
 * It has no special appearance; the caret symbol is typically shown as a blinking
 * cursor at the annotation location.
 * </p>
 */
public class CaretAnnotation extends MarkupAnnotation {

    /**
     * Constructs a caret annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public CaretAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new caret annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public CaretAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Caret"));
    }
}
