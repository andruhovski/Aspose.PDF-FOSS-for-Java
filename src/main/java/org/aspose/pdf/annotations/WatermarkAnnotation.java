package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Watermark annotation (ISO 32000-1:2008, Section 12.5.6.22, /Subtype /Watermark).
 * <p>
 * A watermark annotation is used to represent graphics that are expected to be
 * printed at a fixed size and position on a page, regardless of the dimensions
 * of the printed page. It is typically used for paper-independent graphics
 * such as company logos or letterhead.
 * </p>
 */
public class WatermarkAnnotation extends MarkupAnnotation {

    /**
     * Constructs a watermark annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public WatermarkAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new watermark annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public WatermarkAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Watermark"));
    }
}
