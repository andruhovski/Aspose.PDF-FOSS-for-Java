package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Collections;
import java.util.List;

/**
 * Invoke named XObject operator (Do).
 * <p>
 * Paints the specified XObject. The operand is the name of the XObject resource
 * in the current resource dictionary. See ISO 32000-1:2008, §8.8, Table 57.
 * </p>
 */
public class Do extends Operator {

    private final String xobjectName;

    /**
     * Creates a Do operator with the specified XObject resource name.
     *
     * @param xobjectName the XObject resource name (e.g., "Im1", "Fm0")
     * @throws IllegalArgumentException if xobjectName is null or empty
     */
    public Do(String xobjectName) {
        super("Do", Collections.singletonList(COSName.of(xobjectName)));
        if (xobjectName == null || xobjectName.isEmpty()) {
            throw new IllegalArgumentException("XObject name must not be null or empty");
        }
        this.xobjectName = xobjectName;
    }

    /**
     * Creates a Do operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSName} identifying the XObject resource.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public Do(List<COSBase> operands) {
        super("Do", operands);
        this.xobjectName = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the XObject resource name.
     * <p>
     * Note: use this method to get the resource name, not {@link #getName()},
     * which returns the operator keyword "Do".
     * </p>
     *
     * @return the XObject resource name
     */
    public String getXObjectName() {
        return xobjectName;
    }
}
