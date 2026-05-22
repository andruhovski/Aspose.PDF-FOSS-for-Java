package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Underline annotation (ISO 32000-1:2008, Section 12.5.6.10, /Subtype /Underline).
 * <p>
 * An underline annotation appears as an underline beneath the text. The region
 * is specified by the /QuadPoints array.
 * </p>
 */
public class UnderlineAnnotation extends MarkupAnnotation {

    /**
     * Constructs an underline annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public UnderlineAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new underline annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public UnderlineAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Underline"));
        setQuadPoints(new double[] {
                rect.getLLX(), rect.getURY(),
                rect.getURX(), rect.getURY(),
                rect.getLLX(), rect.getLLY(),
                rect.getURX(), rect.getLLY()
        });
    }

    /**
     * Returns the quadrilateral points defining the underlined regions.
     *
     * @return the quad points array, or null if not set
     */
    public double[] getQuadPoints() {
        COSBase qp = dict.get("QuadPoints");
        if (qp instanceof COSArray) {
            COSArray arr = (COSArray) qp;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0);
            }
            return result;
        }
        return null;
    }

    /**
     * Sets the quadrilateral points defining the underlined regions.
     *
     * @param points the quad points array (multiples of 8 values)
     */
    public void setQuadPoints(double[] points) {
        if (points == null) return;
        COSArray arr = new COSArray();
        for (double p : points) {
            arr.add(new COSFloat(p));
        }
        dict.set(COSName.of("QuadPoints"), arr);
    }
}
