package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set horizontal text scaling operator (Tz).
 * <p>
 * Sets the horizontal scaling parameter in the text state. The scaling value is specified
 * as a percentage; the default value is 100 (normal width).
 * See ISO 32000-1:2008, §9.3.4, Table 105.
 * </p>
 */
public class SetHorizontalTextScaling extends TextStateOperator {

    private final double scale;

    /**
     * Creates a SetHorizontalTextScaling (Tz) operator with the specified scale.
     *
     * @param scale the horizontal scaling percentage (100 = normal)
     */
    public SetHorizontalTextScaling(double scale) {
        super("Tz", Collections.singletonList(num(scale)));
        this.scale = scale;
    }

    /**
     * Creates a SetHorizontalTextScaling (Tz) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetHorizontalTextScaling(List<COSBase> operands) {
        super("Tz", operands);
        this.scale = (operands != null && !operands.isEmpty())
                ? getNumber(operands.get(0))
                : 100;
    }

    /**
     * Returns the horizontal text scaling percentage.
     *
     * @return the scaling percentage (100 = normal width)
     */
    public double getScale() {
        return scale;
    }
}
