package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Arrays;
import java.util.List;

/**
 * Select font and size operator (Tf).
 * <p>
 * Sets the text font to the font identified by {@code fontName} in the Font subdictionary
 * of the current resource dictionary, and sets the text font size to {@code size} units.
 * See ISO 32000-1:2008, §9.3, Table 105.
 * </p>
 */
public class SelectFont extends TextStateOperator {

    private final String fontName;
    private final double size;

    /**
     * Creates a SelectFont (Tf) operator with the specified font name and size.
     *
     * @param fontName the font resource name (e.g., "F1", "Helvetica")
     * @param size     the font size in text space units
     * @throws IllegalArgumentException if fontName is null or empty
     */
    public SelectFont(String fontName, double size) {
        super("Tf", Arrays.asList(COSName.of(fontName), num(size)));
        if (fontName == null || fontName.isEmpty()) {
            throw new IllegalArgumentException("Font name must not be null or empty");
        }
        this.fontName = fontName;
        this.size = size;
    }

    /**
     * Creates a SelectFont (Tf) operator from parsed operands.
     * <p>
     * Expects two operands: a {@link COSName} for the font name and a number for the size.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SelectFont(List<COSBase> operands) {
        super("Tf", operands);
        this.fontName = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
                : "";
        this.size = (operands != null && operands.size() > 1)
                ? getNumber(operands.get(1))
                : 0;
    }

    /**
     * Returns the font resource name.
     *
     * @return the font name (e.g., "F1")
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Returns the font size.
     *
     * @return the font size in text space units
     */
    public double getSize() {
        return size;
    }
}
