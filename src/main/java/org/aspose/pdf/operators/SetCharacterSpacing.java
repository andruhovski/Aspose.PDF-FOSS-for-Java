package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set character spacing operator (Tc).
 * <p>
 * Sets the character spacing parameter in the text state. Character spacing is a number
 * expressed in unscaled text space units that is added to each glyph's displacement.
 * See ISO 32000-1:2008, §9.3.2, Table 105.
 * </p>
 */
public class SetCharacterSpacing extends TextStateOperator {

    private final double charSpace;

    /**
     * Creates a SetCharacterSpacing (Tc) operator with the specified spacing.
     *
     * @param charSpace the character spacing value
     */
    public SetCharacterSpacing(double charSpace) {
        super("Tc", Collections.singletonList(num(charSpace)));
        this.charSpace = charSpace;
    }

    /**
     * Creates a SetCharacterSpacing (Tc) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetCharacterSpacing(List<COSBase> operands) {
        super("Tc", operands);
        this.charSpace = (operands != null && !operands.isEmpty())
                ? getNumber(operands.get(0))
                : 0;
    }

    /**
     * Returns the character spacing value.
     *
     * @return the character spacing in unscaled text space units
     */
    public double getCharSpace() {
        return charSpace;
    }
}
