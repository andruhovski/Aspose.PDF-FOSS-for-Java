package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

/**
 * Polyline annotation (ISO 32000-1:2008, Section 12.5.6.9, /Subtype /PolyLine).
 * <p>
 * A polyline annotation is similar to a polygon annotation, except that the
 * first and last vertices are not implicitly connected.
 * </p>
 */
public class PolylineAnnotation extends MarkupAnnotation {

    /**
     * Constructs a polyline annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public PolylineAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new polyline annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public PolylineAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("PolyLine"));
    }

    /**
     * Returns the vertices of the polyline as an array of coordinate pairs [x1, y1, x2, y2, ...].
     *
     * @return the vertices array, or null if not set
     */
    public double[] getVertices() {
        PdfBase v = dict.get("Vertices");
        if (v instanceof PdfArray) {
            PdfArray arr = (PdfArray) v;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0);
            }
            return result;
        }
        return null;
    }
}
