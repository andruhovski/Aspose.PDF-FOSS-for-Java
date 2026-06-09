package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.List;

/**
 * Set color for stroking operations with pattern support operator (SCN).
 * <p>
 * Similar to {@link SetColorStroke} (SC) but additionally supports Pattern and Separation
 * color spaces. When the current color space is a Pattern space, the last operand
 * is a pattern name ({@link PdfName}); the preceding operands (if any) are numeric
 * color components for the underlying color space.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetAdvancedColorStroke extends BasicSetColorAndPatternOperator {

    /**
     * Creates a SetAdvancedColorStroke (SCN) operator from parsed operands.
     * <p>
     * Operands may be a variable number of numeric values, optionally followed
     * by a {@link PdfName} identifying a pattern.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetAdvancedColorStroke(List<PdfBase> operands) {
        super("SCN", operands);
    }

    /**
     * Returns the numeric color components. If the last operand is a pattern name,
     * it is excluded from the returned array.
     *
     * @return the color component values
     */
    public double[] getComponents() {
        List<PdfBase> ops = getOperands();
        if (ops == null || ops.isEmpty()) {
            return new double[0];
        }
        int count = ops.size();
        if (ops.get(count - 1) instanceof PdfName) {
            count--;
        }
        double[] result = new double[count];
        for (int i = 0; i < count; i++) {
            result[i] = getNumber(ops.get(i));
        }
        return result;
    }

    /**
     * Returns the pattern name if the last operand is a {@link PdfName}, or {@code null}
     * if no pattern name is present.
     *
     * @return the pattern name, or {@code null}
     */
    public String getPatternName() {
        List<PdfBase> ops = getOperands();
        if (ops != null && !ops.isEmpty()) {
            PdfBase last = ops.get(ops.size() - 1);
            if (last instanceof PdfName) {
                return ((PdfName) last).getName();
            }
        }
        return null;
    }
}
