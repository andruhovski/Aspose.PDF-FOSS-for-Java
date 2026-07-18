package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.function.PdfFunction;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.io.IOException;

/// Axial shading / linear gradient — ShadingType 2 (ISO 32000-1:2008, §8.7.4.5.2).
/// Defines a color gradient along an axis from (x0, y0) to (x1, y1).
///
/// A function maps parameter t ∈ [t0, t1] to color component values.
public final class AxialShading extends Shading {

    private final double x0, y0, x1, y1;
    private final double t0, t1;
    private final PdfFunction function;
    private final boolean extendStart, extendEnd;

    /// Creates an AxialShading from its dictionary.
    ///
    /// @param dict   the shading dictionary
    /// @param parser the PDF parser
    /// @throws IOException if the function cannot be parsed
    public AxialShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
        double[] coords = getNumberArray(dict, "Coords");
        if (coords == null || coords.length < 4) coords = new double[]{0, 0, 1, 0};
        x0 = coords[0]; y0 = coords[1];
        x1 = coords[2]; y1 = coords[3];

        double[] domain = getNumberArray(dict, "Domain");
        t0 = (domain != null && domain.length >= 2) ? domain[0] : 0;
        t1 = (domain != null && domain.length >= 2) ? domain[1] : 1;

        this.function = PdfFunction.parse(dict.get("Function"), parser);

        boolean[] ext = getBooleanArray(dict, "Extend");
        extendStart = ext != null && ext.length >= 1 && ext[0];
        extendEnd = ext != null && ext.length >= 2 && ext[1];
    }

    @Override
    public int getShadingType() { return 2; }

    @Override
    public double[] getColorAt(double x, double y) {
        // Project (x,y) onto the axis to get parameter t
        double dx = x1 - x0, dy = y1 - y0;
        double lenSq = dx * dx + dy * dy;
        double t;
        if (lenSq < 1e-10) {
            t = t0;
        } else {
            t = ((x - x0) * dx + (y - y0) * dy) / lenSq;
        }
        // Outside the axis span with /Extend false -> NOT painted
        // (ISO 32000 8.7.4.5.2); see RadialShading for the corpus rationale.
        if (t < 0) {
            if (!extendStart) return null;
            t = 0;
        } else if (t > 1) {
            if (!extendEnd) return null;
            t = 1;
        }
        // Map from [0,1] projection to [t0,t1]
        t = t0 + t * (t1 - t0);
        t = Math.max(t0, Math.min(t1, t));

        return evaluateFunction(t);
    }

    private double[] evaluateFunction(double t) {
        if (function == null) return background != null ? background : new double[]{0, 0, 0};
        return function.evaluate(new double[]{t});
    }

    /// Returns the start point X coordinate.
    public double getX0() { return x0; }
    /// Returns the start point Y coordinate.
    public double getY0() { return y0; }
    /// Returns the end point X coordinate.
    public double getX1() { return x1; }
    /// Returns the end point Y coordinate.
    public double getY1() { return y1; }
    /// Returns the start of the parametric domain.
    public double getT0() { return t0; }
    /// Returns the end of the parametric domain.
    public double getT1() { return t1; }
    /// Returns whether the gradient extends before the start point.
    public boolean isExtendStart() { return extendStart; }
    /// Returns whether the gradient extends past the end point.
    public boolean isExtendEnd() { return extendEnd; }
}
