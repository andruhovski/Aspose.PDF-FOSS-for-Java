package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/**
 * Set text rise operator (Ts).
 * <p>
 * Sets the text rise parameter in the text state. Text rise specifies a distance
 * in unscaled text space units to move the baseline up (positive) or down (negative),
 * useful for superscripts and subscripts.
 * See ISO 32000-1:2008, §9.3.7, Table 105.
 * </p>
 */
public class SetTextRise extends TextStateOperator {

    private final double rise;

    /**
     * Creates a SetTextRise (Ts) operator with the specified rise.
     *
     * @param rise the text rise value
     */
    public SetTextRise(double rise) {
        super("Ts", Collections.singletonList(num(rise)));
        this.rise = rise;
    }

    /**
     * Creates a SetTextRise (Ts) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetTextRise(List<PdfBase> operands) {
        super("Ts", operands);
        this.rise = (operands != null && !operands.isEmpty())
                ? getNumber(operands.get(0))
                : 0;
    }

    /**
     * Returns the text rise value.
     *
     * @return the text rise in unscaled text space units
     */
    public double getRise() {
        return rise;
    }
}
