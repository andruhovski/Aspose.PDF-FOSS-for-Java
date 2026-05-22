package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for all text-related operators (ISO 32000-1:2008, §9).
 * <p>
 * Text operators control the selection of fonts, the positioning of glyphs, and the
 * rendering of text strings within a content stream. Concrete subclasses represent
 * specific PDF text operators such as BT, ET, Tf, Td, Tj, TJ, and others.
 * </p>
 */
public abstract class TextOperator extends Operator {

    /**
     * Creates a text operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "BT", "Tf", "Td", "Tj")
     */
    protected TextOperator(String name) {
        super(name);
    }

    /**
     * Creates a text operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected TextOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
