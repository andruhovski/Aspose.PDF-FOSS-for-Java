package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Show text with glyph positioning operator (TJ).
 * <p>
 * Shows one or more text strings, allowing individual glyph positioning. The operand
 * is a COSArray of interleaved strings and numbers. Each string is shown in the current
 * font. Each number adjusts the text position by that amount (in thousandths of a unit
 * of text space); negative values move right in horizontal mode.
 * See ISO 32000-1:2008, §9.4.3, Table 109.
 * </p>
 */
public class SetGlyphsPositionShowText extends TextShowOperator {

    /**
     * Creates a SetGlyphsPositionShowText (TJ) operator from parsed operands.
     * <p>
     * The operands list should contain a single {@link COSArray} with interleaved
     * strings and numbers. The raw operands are stored as-is; access them via
     * {@link #getOperands()}.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetGlyphsPositionShowText(List<COSBase> operands) {
        super("TJ", operands);
    }

    /**
     * Creates a SetGlyphsPositionShowText (TJ) operator from an array of glyph positions.
     * <p>
     * Builds a COSArray from the provided glyph positions. Text elements are encoded
     * as COSString using ISO 8859-1; numeric adjustments are encoded as numbers.
     * </p>
     *
     * @param positions the glyph positions
     * @throws IllegalArgumentException if positions is null
     */
    public SetGlyphsPositionShowText(GlyphPosition[] positions) {
        super("TJ", buildOperands(positions));
    }

    /**
     * Returns the COSArray operand containing interleaved strings and numbers,
     * or null if not available.
     *
     * @return the TJ array, or null
     */
    public COSArray getArray() {
        List<COSBase> ops = getOperands();
        if (ops != null && !ops.isEmpty() && ops.get(0) instanceof COSArray) {
            return (COSArray) ops.get(0);
        }
        return null;
    }

    private static List<COSBase> buildOperands(GlyphPosition[] positions) {
        if (positions == null) {
            throw new IllegalArgumentException("Positions must not be null");
        }
        COSArray array = new COSArray(positions.length);
        for (GlyphPosition gp : positions) {
            if (gp.isText()) {
                String text = gp.getText();
                array.add(new COSString(
                        text != null ? text.getBytes(StandardCharsets.ISO_8859_1) : new byte[0]));
                if (gp.getAdjustment() != 0) {
                    array.add(num(gp.getAdjustment()));
                }
            } else {
                array.add(num(gp.getAdjustment()));
            }
        }
        return Collections.singletonList(array);
    }
}
