package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Move to next line and show text operator (').
 * <p>
 * Moves to the next line and shows a text string. This operator is equivalent to:
 * <pre>
 *   T*
 *   string Tj
 * </pre>
 * See ISO 32000-1:2008, §9.4.3, Table 109.
 * </p>
 */
public class MoveToNextLineShowText extends TextShowOperator {

    private final String text;

    /**
     * Creates a MoveToNextLineShowText (') operator with the specified text.
     *
     * @param text the text string to show
     * @throws IllegalArgumentException if text is null
     */
    public MoveToNextLineShowText(String text) {
        super("'", Collections.singletonList(
                new COSString(text.getBytes(StandardCharsets.ISO_8859_1))));
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        this.text = text;
    }

    /**
     * Creates a MoveToNextLineShowText (') operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public MoveToNextLineShowText(List<COSBase> operands) {
        super("'", operands);
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
}
