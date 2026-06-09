package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Set color for non-stroking operations operator (sc).
 * <p>
 * Sets the non-stroking color value in the current color space. The number
 * of operands depends on the current color space: 1 for DeviceGray/CalGray,
 * 3 for DeviceRGB/CalRGB/Lab, 4 for DeviceCMYK, etc.
 * This operator does not support Pattern color spaces; use {@link SetAdvancedColor}
 * for that.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetColor extends BasicSetColorOperator {

    private final double[] components;

    /**
     * Creates a SetColor (sc) operator with the specified color components.
     *
     * @param components the color component values (1, 3, or 4 numbers depending on color space)
     */
    public SetColor(double... components) {
        super("sc", toOperandList(components));
        this.components = components != null ? components.clone() : new double[0];
    }

    /**
     * Creates a SetColor (sc) operator from parsed operands.
     *
     * @param operands the operands from the content stream parser
     */
    public SetColor(List<PdfBase> operands) {
        super("sc", operands);
        this.components = parseComponents(operands);
    }

    /**
     * Returns the color components as an array of doubles.
     *
     * @return the color component values
     */
    public double[] getComponents() {
        return components.clone();
    }

    private static List<PdfBase> toOperandList(double[] components) {
        List<PdfBase> list = new ArrayList<>();
        if (components != null) {
            for (double v : components) {
                list.add(num(v));
            }
        }
        return list;
    }

    private static double[] parseComponents(List<PdfBase> operands) {
        if (operands == null || operands.isEmpty()) {
            return new double[0];
        }
        double[] result = new double[operands.size()];
        for (int i = 0; i < operands.size(); i++) {
            result[i] = getNumber(operands.get(i));
        }
        return result;
    }
}
