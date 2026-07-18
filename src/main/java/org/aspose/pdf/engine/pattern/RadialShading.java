package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.function.PdfFunction;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.io.IOException;

/// Radial shading / radial gradient — ShadingType 3 (ISO 32000-1:2008, §8.7.4.5.3).
/// Defines a gradient between two circles: (x0, y0, r0) and (x1, y1, r1).
public final class RadialShading extends Shading {

    private final double x0, y0, r0, x1, y1, r1;
    private final double t0, t1;
    private final PdfFunction function;
    private final boolean extendStart, extendEnd;

    /// Creates a RadialShading from its dictionary.
    ///
    /// @param dict   the shading dictionary
    /// @param parser the PDF parser
    /// @throws IOException if the function cannot be parsed
    public RadialShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
        double[] coords = getNumberArray(dict, "Coords");
        if (coords == null || coords.length < 6) coords = new double[]{0, 0, 0, 0, 0, 1};
        x0 = coords[0]; y0 = coords[1]; r0 = coords[2];
        x1 = coords[3]; y1 = coords[4]; r1 = coords[5];

        double[] domain = getNumberArray(dict, "Domain");
        t0 = (domain != null && domain.length >= 2) ? domain[0] : 0;
        t1 = (domain != null && domain.length >= 2) ? domain[1] : 1;

        this.function = PdfFunction.parse(dict.get("Function"), parser);

        boolean[] ext = getBooleanArray(dict, "Extend");
        extendStart = ext != null && ext.length >= 1 && ext[0];
        extendEnd = ext != null && ext.length >= 2 && ext[1];
    }

    @Override
    public int getShadingType() { return 3; }

    @Override
    public double[] getColorAt(double x, double y) {
        // Find parameter t such that (x,y) lies on the interpolated circle at t.
        // Circle at t: center = (1-t)·P0 + t·P1, radius = (1-t)·r0 + t·r1
        // Approximate by distance ratio from both centers.
        double dist0 = Math.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0));
        double dist1 = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));

        double s;
        if (Math.abs(r1 - r0) < 1e-10 && dist0 + dist1 > 1e-10) {
            s = dist0 / (dist0 + dist1);
        } else {
            // For concentric circles: t based on distance from center0 relative to radii
            if (r1 > r0 + 1e-10) {
                s = (dist0 - r0) / (r1 - r0);
            } else if (r0 > r1 + 1e-10) {
                s = 1.0 - (dist1 - r1) / (r0 - r1);
            } else {
                s = 0.5;
            }
        }
        // Outside the gradient with /Extend false -> NOT painted (ISO 32000
        // 8.7.4.5.3). Clamping instead floods everything beyond the end
        // circle with the end color - corpus 10734 paints its banner
        // gradients via hundreds of tiny sh tiles whose out-of-range
        // neighbours must stay empty.
        if (s < 0) {
            if (!extendStart) return null;
            s = 0;
        } else if (s > 1) {
            if (!extendEnd) return null;
            s = 1;
        }
        double t = t0 + s * (t1 - t0);
        t = Math.max(t0, Math.min(t1, t));

        if (function == null) return background != null ? background : new double[]{0, 0, 0};
        return function.evaluate(new double[]{t});
    }

    /// Returns the start circle X center.
    public double getX0() { return x0; }
    /// Returns the start circle Y center.
    public double getY0() { return y0; }
    /// Returns the start circle radius.
    public double getR0() { return r0; }
    /// Returns the end circle X center.
    public double getX1() { return x1; }
    /// Returns the end circle Y center.
    public double getY1() { return y1; }
    /// Returns the end circle radius.
    public double getR1() { return r1; }
}
