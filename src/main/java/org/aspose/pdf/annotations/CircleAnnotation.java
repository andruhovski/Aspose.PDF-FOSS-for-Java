package org.aspose.pdf.annotations;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// Circle annotation (ISO 32000-1:2008, Section 12.5.6.8, /Subtype /Circle).
///
/// A circle annotation displays an ellipse on the page. Despite its name,
/// the annotation need not be circular; the ellipse is inscribed within
/// the annotation's bounding rectangle.
///
public class CircleAnnotation extends MarkupAnnotation {

    /// Constructs a circle annotation from an existing PDF dictionary.
    ///
    /// @param dict the PDF dictionary backing this annotation
    /// @param page the page this annotation belongs to
    public CircleAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /// Constructs a new circle annotation with the given rectangle on the specified page.
    ///
    /// @param page the page this annotation belongs to
    /// @param rect the annotation rectangle
    public CircleAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Circle"));
    }
}
