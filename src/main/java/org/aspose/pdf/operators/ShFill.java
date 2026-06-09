package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/**
 * Shading fill operator (sh).
 * <p>
 * Paints the shape and color shading described by a shading dictionary, subject to
 * the current clipping path. See ISO 32000-1:2008, §8.7.4.3, Table 77.
 * </p>
 */
public class ShFill extends Operator {

    private final String shadingName;

    /**
     * Creates a ShFill (sh) operator with the specified shading resource name.
     *
     * @param shadingName the shading resource name
     * @throws IllegalArgumentException if shadingName is null or empty
     */
    public ShFill(String shadingName) {
        super("sh", Collections.singletonList(PdfName.of(shadingName)));
        if (shadingName == null || shadingName.isEmpty()) {
            throw new IllegalArgumentException("Shading name must not be null or empty");
        }
        this.shadingName = shadingName;
    }

    /**
     * Creates a ShFill (sh) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link PdfName} identifying the shading resource.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public ShFill(List<PdfBase> operands) {
        super("sh", operands);
        this.shadingName = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the shading resource name.
     *
     * @return the shading name
     */
    public String getShadingName() {
        return shadingName;
    }
}
