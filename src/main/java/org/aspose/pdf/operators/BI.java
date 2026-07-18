package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/// Begin inline image operator (BI).
///
/// Marks the beginning of an inline image object in a content stream. The BI operator
/// is followed by key-value pairs defining the image dictionary, then the [ID]
/// operator, the raw image data bytes, and finally the [EI] operator.
/// See ISO 32000-1:2008, §8.9.7, Table 92.
///
public class BI extends Operator {

    /// Creates a BI operator with no operands.
    public BI() {
        super("BI");
    }

    /// Creates a BI operator from parsed operands.
    ///
    /// When produced by the parser, operands may contain the inline image dictionary
    /// (operands[0]) and the image data as a PDF string (operands[1]).
    ///
    /// @param operands the operands from the content stream parser
    public BI(List<PdfBase> operands) {
        super("BI", operands);
    }

    /// Serialises the full inline image construct: `BI` followed by the
    /// bare key/value pairs (no `<<>>` — BI/ID delimit the dictionary
    /// per ISO 32000-1 §8.9.7), then `ID`, one whitespace byte, the raw
    /// unescaped image data, and the `EI` terminator.
    ///
    /// The generic operand serialisation would emit
    /// `<<...>> (data) BI` — invalid content-stream syntax that breaks
    /// re-parsing of everything after the image once a page is rewritten
    /// (PDFNEWNET-39178 text replacement after an inline image).
    ///
    @Override
    public void writeTo(OutputStream os) throws IOException {
        List<PdfBase> ops = getOperands();
        os.write("BI".getBytes(StandardCharsets.US_ASCII));
        if (ops != null && !ops.isEmpty() && ops.get(0) instanceof PdfDictionary) {
            for (Map.Entry<PdfName, PdfBase> entry : (PdfDictionary) ops.get(0)) {
                os.write(' ');
                entry.getKey().writeTo(os);
                os.write(' ');
                entry.getValue().writeTo(os);
            }
        }
        os.write("\nID ".getBytes(StandardCharsets.US_ASCII));
        if (ops != null && ops.size() > 1 && ops.get(1) instanceof PdfString) {
            os.write(((PdfString) ops.get(1)).getBytes());
        }
        os.write("\nEI".getBytes(StandardCharsets.US_ASCII));
    }
}
