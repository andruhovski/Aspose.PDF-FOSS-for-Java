package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Collections;
import java.util.List;

/**
 * Set color space for stroking operations operator (CS).
 * <p>
 * Sets the current color space for stroking operations. The operand
 * is a name object identifying a color space resource.
 * See ISO 32000-1:2008, §8.6.8, Table 74.
 * </p>
 */
public class SetColorSpaceStroke extends SetColorOperator {

    private final String colorSpaceName;

    /**
     * Creates a SetColorSpaceStroke (CS) operator with the specified color space name.
     *
     * @param colorSpaceName the color space resource name (e.g., "DeviceRGB", "CS1")
     * @throws IllegalArgumentException if colorSpaceName is null or empty
     */
    public SetColorSpaceStroke(String colorSpaceName) {
        super("CS", Collections.singletonList(COSName.of(colorSpaceName)));
        if (colorSpaceName == null || colorSpaceName.isEmpty()) {
            throw new IllegalArgumentException("Color space name must not be null or empty");
        }
        this.colorSpaceName = colorSpaceName;
    }

    /**
     * Creates a SetColorSpaceStroke (CS) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSName} identifying the color space.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public SetColorSpaceStroke(List<COSBase> operands) {
        super("CS", operands);
        this.colorSpaceName = (operands != null && operands.size() > 0
                && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
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
