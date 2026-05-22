package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for basic set-color operators (ISO 32000-1:2008, §8.6.8).
 * <p>
 * Basic set-color operators (sc, SC) set the current color in the current color space
 * for non-stroking and stroking operations respectively. The number of operands depends
 * on the current color space. These operators do not support pattern color spaces;
 * see {@link BasicSetColorAndPatternOperator} for that capability.
 * </p>
 */
public abstract class BasicSetColorOperator extends SetColorOperator {

    /**
     * Creates a basic set-color operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "sc", "SC")
     */
    protected BasicSetColorOperator(String name) {
        super(name);
    }

    /**
     * Creates a basic set-color operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected BasicSetColorOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
