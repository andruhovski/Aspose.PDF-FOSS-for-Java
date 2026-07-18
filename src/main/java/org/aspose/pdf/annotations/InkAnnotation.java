package org.aspose.pdf.annotations;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.*;

import java.util.ArrayList;
import java.util.List;

/// Ink annotation (ISO 32000-1:2008, Section 12.5.6.13, /Subtype /Ink).
///
/// An ink annotation represents freehand "scribble" composed of one or more
/// disjoint paths. Each path is an array of alternating x and y coordinates.
///
public class InkAnnotation extends MarkupAnnotation {

    /// Constructs an ink annotation from an existing PDF dictionary.
    ///
    /// @param dict the PDF dictionary backing this annotation
    /// @param page the page this annotation belongs to
    public InkAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /// Constructs a new ink annotation with the given rectangle on the specified page.
    ///
    /// @param page the page this annotation belongs to
    /// @param rect the annotation rectangle
    public InkAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Ink"));
    }

    /// Returns the ink list, which is a list of strokes. Each stroke is an array
    /// of alternating x and y coordinates.
    ///
    /// @return the list of strokes, each represented as a double array of coordinate pairs
    public List<double[]> getInkList() {
        List<double[]> result = new ArrayList<>();
        PdfBase ink = dict.get("InkList");
        if (ink instanceof PdfArray) {
            PdfArray outer = (PdfArray) ink;
            for (int i = 0; i < outer.size(); i++) {
                PdfBase inner = outer.get(i);
                if (inner instanceof PdfArray) {
                    PdfArray pts = (PdfArray) inner;
                    double[] coords = new double[pts.size()];
                    for (int j = 0; j < pts.size(); j++) {
                        coords[j] = pts.getFloat(j, 0);
                    }
                    result.add(coords);
                }
            }
        }
        return result;
    }

    /// Replaces the ink list. Each stroke is an array of alternating x and y
    /// coordinates; the outer list orders the strokes.
    ///
    /// @param paths the new ink list (null clears the entry)
    public void setInkList(List<double[]> paths) {
        if (paths == null) {
            dict.remove(PdfName.of("InkList"));
            return;
        }
        PdfArray outer = new PdfArray();
        for (double[] stroke : paths) {
            if (stroke == null) continue;
            PdfArray inner = new PdfArray();
            for (double v : stroke) {
                inner.add(new PdfFloat(v));
            }
            outer.add(inner);
        }
        dict.set(PdfName.of("InkList"), outer);
    }
}
