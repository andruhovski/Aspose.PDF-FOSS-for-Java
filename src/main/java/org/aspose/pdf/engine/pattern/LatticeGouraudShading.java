package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;

/**
 * Lattice-form Gouraud-shaded triangle mesh — ShadingType 5
 * (ISO 32000-1:2008, §8.7.4.5.5).
 *
 * <p>Stub: returns background or mid-gray for all coordinates.</p>
 */
public final class LatticeGouraudShading extends Shading {

    public LatticeGouraudShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
    }

    @Override
    public int getShadingType() { return 5; }

    @Override
    public double[] getColorAt(double x, double y) {
        return background != null ? background : new double[]{0.5, 0.5, 0.5};
    }
}
