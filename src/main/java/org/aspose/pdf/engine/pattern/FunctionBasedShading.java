package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.function.PdfFunction;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;

/**
 * Function-based shading — ShadingType 1 (ISO 32000-1:2008, §8.7.4.5.1).
 * A 2-input function maps (x, y) coordinates directly to color values.
 */
public final class FunctionBasedShading extends Shading {

    private final double[] domain; // [xmin xmax ymin ymax]
    private final PdfFunction function;

    /**
     * Creates a FunctionBasedShading from its dictionary.
     *
     * @param dict   the shading dictionary
     * @param parser the PDF parser
     * @throws IOException if the function cannot be parsed
     */
    public FunctionBasedShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
        double[] d = getNumberArray(dict, "Domain");
        this.domain = (d != null && d.length == 4) ? d : new double[]{0, 1, 0, 1};
        this.function = PdfFunction.parse(dict.get("Function"), parser);
    }

    @Override
    public int getShadingType() { return 1; }

    @Override
    public double[] getColorAt(double x, double y) {
        double dx = Math.max(domain[0], Math.min(domain[1], x));
        double dy = Math.max(domain[2], Math.min(domain[3], y));
        if (function == null) return background != null ? background : new double[]{0, 0, 0};
        return function.evaluate(new double[]{dx, dy});
    }

    /** Returns the function domain [xmin, xmax, ymin, ymax]. */
    public double[] getDomain() { return domain.clone(); }
}
