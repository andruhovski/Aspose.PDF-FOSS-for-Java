package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/**
 * Set color rendering intent operator (ri).
 * <p>
 * Sets the color rendering intent in the graphics state. Common values include
 * AbsoluteColorimetric, RelativeColorimetric, Saturation, and Perceptual.
 * See ISO 32000-1:2008, §8.6.5.8, Table 57.
 * </p>
 */
public class SetColorRenderingIntent extends Operator {

    private final String intent;

    /**
     * Creates a SetColorRenderingIntent (ri) operator with the specified intent name.
     *
     * @param intent the rendering intent name
     * @throws IllegalArgumentException if intent is null or empty
     */
    public SetColorRenderingIntent(String intent) {
        super("ri", Collections.singletonList(PdfName.of(intent)));
        if (intent == null || intent.isEmpty()) {
            throw new IllegalArgumentException("Intent name must not be null or empty");
        }
        this.intent = intent;
    }

    /**
     * Creates a SetColorRenderingIntent (ri) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link PdfName} for the intent.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetColorRenderingIntent(List<PdfBase> operands) {
        super("ri", operands);
        this.intent = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the rendering intent name.
     *
     * @return the intent name
     */
    public String getIntent() {
        return intent;
    }
}
