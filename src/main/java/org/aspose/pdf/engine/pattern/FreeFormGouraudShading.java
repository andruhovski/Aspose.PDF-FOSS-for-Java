package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;

/**
 * Free-form Gouraud-shaded triangle mesh — ShadingType 4
 * (ISO 32000-1:2008, §8.7.4.5.4).
 *
 * <p>Stub: returns background or mid-gray for all coordinates.</p>
 */
public final class FreeFormGouraudShading extends Shading {

    /**
     * Creates a FreeFormGouraudShading from its dictionary.
     *
     * @param dict   the shading dictionary
     * @param parser the PDF parser
     * @throws IOException if parsing fails
     */
    public FreeFormGouraudShading(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict, parser);
    }

    @Override
    public int getShadingType() { return 4; }

    @Override
    public double[] getColorAt(double x, double y) {
        return background != null ? background : new double[]{0.5, 0.5, 0.5};
    }
}
