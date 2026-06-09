package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/**
 * Set flatness tolerance operator (i).
 * <p>
 * Sets the flatness tolerance in the graphics state. The flatness value controls
 * the maximum permitted distance in device pixels between the mathematically correct
 * path and an approximation constructed from flat line segments.
 * See ISO 32000-1:2008, §10.6.2, Table 57.
 * </p>
 */
public class SetFlat extends Operator {

    private final double flatness;

    /**
     * Creates a SetFlat (i) operator with the specified flatness tolerance.
     *
     * @param flatness the flatness tolerance value
     */
    public SetFlat(double flatness) {
        super("i", Collections.singletonList(num(flatness)));
        this.flatness = flatness;
    }

    /**
     * Creates a SetFlat (i) operator from parsed operands.
     * <p>
     * Expects one numeric operand: the flatness tolerance.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetFlat(List<PdfBase> operands) {
        super("i", operands);
        this.flatness = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
    }

    /**
     * Returns the flatness tolerance.
     *
     * @return the flatness tolerance value
     */
    public double getFlatness() {
        return flatness;
    }
}
