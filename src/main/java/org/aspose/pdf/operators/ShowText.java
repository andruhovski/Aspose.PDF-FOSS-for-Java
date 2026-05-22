package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Show text operator (Tj).
 * <p>
 * Shows a text string. The string is encoded according to the current font's encoding.
 * See ISO 32000-1:2008, §9.4.3, Table 109.
 * </p>
 */
public class ShowText extends TextShowOperator {

    private String text;

    /**
     * Creates a ShowText (Tj) operator with the specified text.
     *
     * @param text the text string to show
     * @throws IllegalArgumentException if text is null
     */
    public ShowText(String text) {
        super("Tj", Collections.singletonList(
                new COSString(text.getBytes(StandardCharsets.ISO_8859_1))));
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        this.text = text;
    }

    /**
     * Creates a ShowText (Tj) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSString}.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public ShowText(List<COSBase> operands) {
        super("Tj", operands);
        if (operands != null && !operands.isEmpty() && operands.get(0) instanceof COSString) {
            this.text = ((COSString) operands.get(0)).getString();
        } else {
            this.text = "";
        }
    }

    /**
     * Returns the text string.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text string.
     *
     * @param text the new text value
     * @throws IllegalArgumentException if text is null
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        this.text = text;
    }
}
