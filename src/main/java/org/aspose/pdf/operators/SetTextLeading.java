package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set text leading operator (TL).
 * <p>
 * Sets the text leading parameter in the text state. The leading value specifies the
 * vertical distance between the baselines of adjacent lines of text.
 * See ISO 32000-1:2008, §9.3.5, Table 105.
 * </p>
 */
public class SetTextLeading extends TextStateOperator {

    private final double leading;

    /**
     * Creates a SetTextLeading (TL) operator with the specified leading.
     *
     * @param leading the text leading value
     */
    public SetTextLeading(double leading) {
        super("TL", Collections.singletonList(num(leading)));
        this.leading = leading;
    }

    /**
     * Creates a SetTextLeading (TL) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetTextLeading(List<COSBase> operands) {
        super("TL", operands);
        this.leading = (operands != null && !operands.isEmpty())
                ? getNumber(operands.get(0))
                : 0;
    }

    /**
     * Returns the text leading value.
     *
     * @return the text leading in text space units
     */
    public double getLeading() {
        return leading;
    }
}
