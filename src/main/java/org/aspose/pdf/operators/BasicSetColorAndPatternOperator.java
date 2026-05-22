package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for set-color operators that support pattern color spaces
 * (ISO 32000-1:2008, §8.6.8).
 * <p>
 * The scn and SCN operators are similar to sc and SC but additionally support
 * Pattern color spaces. When the current color space is a Pattern space, the last
 * operand is a pattern name; otherwise these operators behave identically to their
 * basic counterparts.
 * </p>
 */
public abstract class BasicSetColorAndPatternOperator extends BasicSetColorOperator {

    /**
     * Creates a set-color-and-pattern operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "scn", "SCN")
     */
    protected BasicSetColorAndPatternOperator(String name) {
        super(name);
    }

    /**
     * Creates a set-color-and-pattern operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected BasicSetColorAndPatternOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
