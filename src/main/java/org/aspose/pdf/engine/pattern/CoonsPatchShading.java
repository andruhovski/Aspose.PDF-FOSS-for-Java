package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;

/**
 * Coons patch mesh — ShadingType 6 (ISO 32000-1:2008, §8.7.4.5.6).
 *
 * <p>Stub: returns background or mid-gray for all coordinates.</p>
 */
public final class CoonsPatchShading extends Shading {

    public CoonsPatchShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
    }

    @Override
    public int getShadingType() { return 6; }

    @Override
    public double[] getColorAt(double x, double y) {
        return background != null ? background : new double[]{0.5, 0.5, 0.5};
    }
}
