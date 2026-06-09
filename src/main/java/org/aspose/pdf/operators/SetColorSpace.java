package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/**
 * Set color space for non-stroking operations operator (cs).
 * <p>
 * Sets the current color space for non-stroking operations. The operand
 * is a name object identifying a color space resource.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetColorSpace extends SetColorOperator {

    private final String colorSpaceName;

    /**
     * Creates a SetColorSpace (cs) operator with the specified color space name.
     *
     * @param colorSpaceName the color space resource name (e.g., "DeviceRGB", "CS1")
     * @throws IllegalArgumentException if colorSpaceName is null or empty
     */
    public SetColorSpace(String colorSpaceName) {
        super("cs", Collections.singletonList(PdfName.of(colorSpaceName)));
        if (colorSpaceName == null || colorSpaceName.isEmpty()) {
            throw new IllegalArgumentException("Color space name must not be null or empty");
        }
        this.colorSpaceName = colorSpaceName;
    }

    /**
     * Creates a SetColorSpace (cs) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link PdfName} identifying the color space.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetColorSpace(List<PdfBase> operands) {
        super("cs", operands);
        this.colorSpaceName = (operands != null && operands.size() > 0
                && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the color space resource name.
     *
     * @return the color space name
     */
    public String getColorSpaceName() {
        return colorSpaceName;
    }
}
