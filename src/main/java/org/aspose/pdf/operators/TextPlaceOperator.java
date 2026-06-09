package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/**
 * Abstract base class for text positioning operators (ISO 32000-1:2008, §9.4.2).
 * <p>
 * Text positioning operators adjust the text matrix and text line matrix to control
 * where subsequent text is placed within a text object. Concrete subclasses include
 * operators for moving to the next line (Td, TD, T*) and setting the text matrix (Tm).
 * </p>
 */
public abstract class TextPlaceOperator extends TextOperator {

    /**
     * Creates a text positioning operator with the given name and no operands.
     *
     * @param name the operator keyword (e.g., "Td", "TD", "Tm", "T*")
     */
    protected TextPlaceOperator(String name) {
        super(name);
    }

    /**
     * Creates a text positioning operator with the given name and operands.
     *
     * @param name     the operator keyword
     * @param operands the operands preceding this operator in the content stream
     */
    protected TextPlaceOperator(String name, List<PdfBase> operands) {
        super(name, operands);
    }
}
