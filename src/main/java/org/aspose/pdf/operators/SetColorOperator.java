package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for all color-setting operators (ISO 32000-1:2008, §8.6.8).
 * <p>
 * Color operators set the current color used for stroking and non-stroking operations
 * in the graphics state. Concrete subclasses cover device color spaces (rg, RG, k, K,
 * g, G) as well as general and pattern color spaces (sc, SC, scn, SCN, cs, CS).
 * </p>
 */
public abstract class SetColorOperator extends Operator {

    /**
     * Creates a color operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "rg", "RG", "sc", "SCN")
     */
    protected SetColorOperator(String name) {
        super(name);
    }

    /**
     * Creates a color operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected SetColorOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
