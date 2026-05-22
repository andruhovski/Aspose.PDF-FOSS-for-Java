package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for text block operators (ISO 32000-1:2008, §9.4).
 * <p>
 * Text block operators delimit the beginning and end of a text object within a
 * content stream. The two concrete operators in this category are BT (begin text)
 * and ET (end text).
 * </p>
 */
public abstract class BlockTextOperator extends TextOperator {

    /**
     * Creates a block text operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "BT", "ET")
     */
    protected BlockTextOperator(String name) {
        super(name);
    }

    /**
     * Creates a block text operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected BlockTextOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
