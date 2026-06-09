package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Collections;
import java.util.List;

/**
 * Set word spacing operator (Tw).
 * <p>
 * Sets the word spacing parameter in the text state. Word spacing works the same way
 * as character spacing but applies only to the ASCII space character (0x20).
 * See ISO 32000-1:2008, §9.3.3, Table 105.
 * </p>
 */
public class SetWordSpacing extends TextStateOperator {

    private final double wordSpace;

    /**
     * Creates a SetWordSpacing (Tw) operator with the specified spacing.
     *
     * @param wordSpace the word spacing value
     */
    public SetWordSpacing(double wordSpace) {
        super("Tw", Collections.singletonList(num(wordSpace)));
        this.wordSpace = wordSpace;
    }

    /**
     * Creates a SetWordSpacing (Tw) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetWordSpacing(List<PdfBase> operands) {
        super("Tw", operands);
        this.wordSpace = (operands != null && !operands.isEmpty())
                ? getNumber(operands.get(0))
                : 0;
    }

    /**
     * Returns the word spacing value.
     *
     * @return the word spacing in unscaled text space units
     */
    public double getWordSpace() {
        return wordSpace;
    }
}
