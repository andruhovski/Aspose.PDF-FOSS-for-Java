package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.Collections;
import java.util.List;

/**
 * Set text rendering mode operator (Tr).
 * <p>
 * Sets the text rendering mode in the text state. The mode determines whether text
 * is filled, stroked, used as a clipping boundary, or some combination of these.
 * Valid values are 0 through 7. See ISO 32000-1:2008, §9.3.6, Table 106.
 * </p>
 * <ul>
 *   <li>0 = Fill text</li>
 *   <li>1 = Stroke text</li>
 *   <li>2 = Fill then stroke text</li>
 *   <li>3 = Invisible</li>
 *   <li>4 = Fill text and add to path for clipping</li>
 *   <li>5 = Stroke text and add to path for clipping</li>
 *   <li>6 = Fill then stroke text and add to path for clipping</li>
 *   <li>7 = Add text to path for clipping</li>
 * </ul>
 */
public class SetTextRenderingMode extends TextStateOperator {

    private final int mode;

    /**
     * Creates a SetTextRenderingMode (Tr) operator with the specified mode.
     *
     * @param mode the text rendering mode (0-7)
     * @throws IllegalArgumentException if mode is outside the range 0-7
     */
    public SetTextRenderingMode(int mode) {
        super("Tr", Collections.singletonList(num(mode)));
        if (mode < 0 || mode > 7) {
            throw new IllegalArgumentException("Text rendering mode must be 0-7, got " + mode);
        }
        this.mode = mode;
    }

    /**
     * Creates a SetTextRenderingMode (Tr) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetTextRenderingMode(List<COSBase> operands) {
        super("Tr", operands);
        this.mode = (operands != null && !operands.isEmpty())
                ? (int) getNumber(operands.get(0))
                : 0;
    }

    /**
     * Returns the text rendering mode.
     *
     * @return the rendering mode (0-7)
     */
    public int getMode() {
        return mode;
    }
}
