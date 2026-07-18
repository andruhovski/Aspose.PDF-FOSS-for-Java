package org.aspose.pdf.engine.function;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.io.IOException;

/// Type 3 (Stitching) function (ISO 32000-1:2008, §7.10.4).
/// Combines multiple subfunctions over non-overlapping subdomains.
///
/// The input domain is divided into k intervals by k-1 boundary values.
/// Each interval is mapped to a subfunction's input via the Encode array.
public final class StitchingFunction extends PdfFunction {

    private final PdfFunction[] functions;
    private final double[] bounds;
    private final double[] encode;

    /// Creates a stitching function from a PDF dictionary.
    ///
    /// @param dict   the function dictionary
    /// @param domain the input domain
    /// @param range  the output range
    /// @param parser the PDF parser for resolving subfunctions
    /// @throws IOException if subfunctions cannot be parsed
    public StitchingFunction(PdfDictionary dict, double[] domain, double[] range,
                              PDFParser parser) throws IOException {
        super(domain, range);

        PdfBase funcsObj = dict.get("Functions");
        if (funcsObj instanceof PdfArray) {
            PdfArray funcsArr = (PdfArray) funcsObj;
            this.functions = new PdfFunction[funcsArr.size()];
            for (int i = 0; i < funcsArr.size(); i++) {
                this.functions[i] = PdfFunction.parse(funcsArr.get(i), parser);
            }
        } else {
            this.functions = new PdfFunction[0];
        }
        this.bounds = getNumberArray(dict, "Bounds");
        this.encode = getNumberArray(dict, "Encode");
    }

    /// Creates a stitching function directly (for testing).
    public StitchingFunction(double[] domain, double[] range,
                              PdfFunction[] functions, double[] bounds, double[] encode) {
        super(domain, range);
        this.functions = functions;
        this.bounds = bounds;
        this.encode = encode;
    }

    @Override
    public double[] evaluate(double[] input) {
        if (functions.length == 0) return new double[0];
        double x = clamp(input[0], domain[0], domain[1]);

        // Find which subdomain x falls into
        int k = functions.length;
        int i = 0;
        if (bounds != null) {
            for (; i < k - 1; i++) {
                if (x < bounds[i]) break;
            }
        }

        // Map x from subdomain to subfunction input via encode
        double subMin = (i == 0) ? domain[0] : bounds[i - 1];
        double subMax = (i == k - 1) ? domain[1] : bounds[i];
        double encMin = (encode != null && 2 * i < encode.length) ? encode[2 * i] : 0;
        double encMax = (encode != null && 2 * i + 1 < encode.length) ? encode[2 * i + 1] : 1;

        double mapped;
        if (subMax == subMin) {
            mapped = encMin;
        } else {
            mapped = encMin + (x - subMin) * (encMax - encMin) / (subMax - subMin);
        }

        if (functions[i] != null) {
            return functions[i].evaluate(new double[]{mapped});
        }
        return new double[0];
    }
}
