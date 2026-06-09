package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Stroke path operator (S).
 * <p>
 * Strokes the current path using the current stroke color and line parameters.
 * See ISO 32000-1:2008, §8.5.3.1, Table 60.
 * </p>
 */
public class Stroke extends Operator {

    /** Creates a Stroke operator. */
    public Stroke() {
        super("S");
    }

    /** Creates a Stroke operator from parsed operands. */
    public Stroke(List<PdfBase> operands) {
        super("S", operands);
    }
}
