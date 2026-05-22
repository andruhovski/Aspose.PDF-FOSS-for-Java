package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

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
        super("ri", Collections.singletonList(COSName.of(intent)));
        if (intent == null || intent.isEmpty()) {
            throw new IllegalArgumentException("Intent name must not be null or empty");
        }
        this.intent = intent;
    }

    /**
     * Creates a SetColorRenderingIntent (ri) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSName} for the intent.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetColorRenderingIntent(List<COSBase> operands) {
        super("ri", operands);
        this.intent = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
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
