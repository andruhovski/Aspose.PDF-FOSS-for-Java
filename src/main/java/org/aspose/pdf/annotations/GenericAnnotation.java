package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

/**
 * Generic annotation for unknown or unsupported annotation subtypes.
 * <p>
 * This class serves as a fallback when the annotation subtype is not recognized
 * by the {@link Annotation#fromDictionary(PdfDictionary, Page)} factory method.
 * It preserves the underlying PDF dictionary without interpreting type-specific entries.
 * </p>
 */
public class GenericAnnotation extends Annotation {

    /**
     * Constructs a generic annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public GenericAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new generic annotation with the given rectangle on the specified page.
     * <p>
     * Note: no /Subtype is set for generic annotations since the subtype is unknown.
     * </p>
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public GenericAnnotation(Page page, Rectangle rect) {
        super(page, rect);
    }
}
