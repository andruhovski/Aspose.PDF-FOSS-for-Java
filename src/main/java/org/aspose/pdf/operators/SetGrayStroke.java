package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/**
 * Set gray level for stroking operations operator (G).
 * <p>
 * Sets the stroking color to the specified gray level. The value
 * must be in the range 0.0 (black) to 1.0 (white).
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetGrayStroke extends SetColorOperator {

    private final double gray;

    /**
     * Creates a SetGrayStroke (G) operator with the specified gray level.
     *
     * @param gray the gray level (0.0 to 1.0)
     */
    public SetGrayStroke(double gray) {
        super("G", Collections.singletonList(num(gray)));
        this.gray = gray;
    }

    /**
     * Creates a SetGrayStroke (G) operator from parsed operands.
     * <p>
     * Expects one numeric operand: the gray level.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetGrayStroke(List<PdfBase> operands) {
        super("G", operands);
        this.gray = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
    }

    /**
     * Returns the gray level.
     *
     * @return the gray value (0.0 to 1.0)
     */
    public double getGray() {
        return gray;
    }
}
