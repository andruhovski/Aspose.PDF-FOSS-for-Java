package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

/**
 * Strikeout annotation (ISO 32000-1:2008, Section 12.5.6.10, /Subtype /StrikeOut).
 * <p>
 * A strikeout annotation appears as a line drawn through the middle of the text.
 * The region is specified by the /QuadPoints array.
 * </p>
 */
public class StrikeOutAnnotation extends MarkupAnnotation {

    /**
     * Constructs a strikeout annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public StrikeOutAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new strikeout annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public StrikeOutAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("StrikeOut"));
        setQuadPoints(new double[] {
                rect.getLLX(), rect.getURY(),
                rect.getURX(), rect.getURY(),
                rect.getLLX(), rect.getLLY(),
                rect.getURX(), rect.getLLY()
        });
    }

    /**
     * Returns the quadrilateral points defining the struck-out regions.
     *
     * @return the quad points array, or null if not set
     */
    public double[] getQuadPoints() {
        PdfBase qp = dict.get("QuadPoints");
        if (qp instanceof PdfArray) {
            PdfArray arr = (PdfArray) qp;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0);
            }
            return result;
        }
        return null;
    }

    /**
     * Sets the quadrilateral points defining the struck-out regions.
     *
     * @param points the quad points array (multiples of 8 values)
     */
    public void setQuadPoints(double[] points) {
        if (points == null) return;
        PdfArray arr = new PdfArray();
        for (double p : points) {
            arr.add(new PdfFloat(p));
        }
        dict.set(PdfName.of("QuadPoints"), arr);
    }

    /**
     * Sets multiple quadrilaterals at once. See
     * {@link HighlightAnnotation#setQuadPoints(double[][])} for the contract;
     * each sub-array is {@code [TLx TLy TRx TRy BLx BLy BRx BRy]} per
     * ISO 32000-1:2008 §12.5.6.10 Table 179.
     *
     * @param quads array of {@code double[8]} quadrilaterals;
     *              passing {@code null} removes the {@code /QuadPoints} entry.
     */
    public void setQuadPoints(double[][] quads) {
        if (quads == null) {
            dict.set(PdfName.of("QuadPoints"), (PdfBase) null);
            return;
        }
        int total = 0;
        for (double[] q : quads) {
            if (q == null) throw new IllegalArgumentException("quad sub-array must not be null");
            total += q.length;
        }
        double[] flat = new double[total];
        int p = 0;
        for (double[] q : quads) {
            System.arraycopy(q, 0, flat, p, q.length);
            p += q.length;
        }
        setQuadPoints(flat);
    }
}
