package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

/**
 * Screen annotation (ISO 32000-1:2008, Section 12.5.6.18, /Subtype /Screen).
 * <p>
 * A screen annotation specifies a region of a page upon which media clips
 * may be played. It also serves as an object from which rendition actions
 * can be triggered.
 * </p>
 */
public class ScreenAnnotation extends Annotation {

    /**
     * Constructs a screen annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public ScreenAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new screen annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public ScreenAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Screen"));
    }
}
