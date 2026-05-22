package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Abstract base class for text showing operators (ISO 32000-1:2008, §9.4.3).
 * <p>
 * Text showing operators render one or more glyphs into the content stream. Concrete
 * subclasses include operators for showing a single string (Tj), showing an array of
 * strings with individual glyph positioning (TJ), moving to the next line and showing
 * a string ('), and setting word/character spacing before showing a string (").
 * </p>
 */
public abstract class TextShowOperator extends TextOperator {

    /**
     * Creates a text showing operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "Tj", "TJ", "'", "\"")
     */
    protected TextShowOperator(String name) {
        super(name);
    }

    /**
     * Creates a text showing operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected TextShowOperator(String name, List<COSBase> operands) {
        super(name, operands);
    }
}
