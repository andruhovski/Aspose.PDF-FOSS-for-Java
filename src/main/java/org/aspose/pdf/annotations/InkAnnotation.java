package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Ink annotation (ISO 32000-1:2008, Section 12.5.6.13, /Subtype /Ink).
 * <p>
 * An ink annotation represents freehand "scribble" composed of one or more
 * disjoint paths. Each path is an array of alternating x and y coordinates.
 * </p>
 */
public class InkAnnotation extends MarkupAnnotation {

    /**
     * Constructs an ink annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public InkAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new ink annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public InkAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Ink"));
    }

    /**
     * Returns the ink list, which is a list of strokes. Each stroke is an array
     * of alternating x and y coordinates.
     *
     * @return the list of strokes, each represented as a double array of coordinate pairs
     */
    public List<double[]> getInkList() {
        List<double[]> result = new ArrayList<>();
        COSBase ink = dict.get("InkList");
        if (ink instanceof COSArray) {
            COSArray outer = (COSArray) ink;
            for (int i = 0; i < outer.size(); i++) {
                COSBase inner = outer.get(i);
                if (inner instanceof COSArray) {
                    COSArray pts = (COSArray) inner;
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

    /**
     * Replaces the ink list. Each stroke is an array of alternating x and y
     * coordinates; the outer list orders the strokes.
     *
     * @param paths the new ink list (null clears the entry)
     */
    public void setInkList(List<double[]> paths) {
        if (paths == null) {
            dict.remove(COSName.of("InkList"));
            return;
        }
        COSArray outer = new COSArray();
        for (double[] stroke : paths) {
            if (stroke == null) continue;
            COSArray inner = new COSArray();
            for (double v : stroke) {
                inner.add(new COSFloat(v));
            }
            outer.add(inner);
        }
        dict.set(COSName.of("InkList"), outer);
    }
}
