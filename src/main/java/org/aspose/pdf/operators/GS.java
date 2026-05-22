package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Collections;
import java.util.List;

/**
 * Set graphics state dictionary operator (gs).
 * <p>
 * Sets the specified parameters in the graphics state from an ExtGState resource
 * dictionary. See ISO 32000-1:2008, §8.4.5, Table 57.
 * </p>
 */
public class GS extends Operator {

    private final String dictName;

    /**
     * Creates a GS (gs) operator with the specified ExtGState dictionary name.
     *
     * @param dictName the ExtGState resource name (e.g., "GS0")
     * @throws IllegalArgumentException if dictName is null or empty
     */
    public GS(String dictName) {
        super("gs", Collections.singletonList(COSName.of(dictName)));
        if (dictName == null || dictName.isEmpty()) {
            throw new IllegalArgumentException("Dictionary name must not be null or empty");
        }
        this.dictName = dictName;
    }

    /**
     * Creates a GS (gs) operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSName} identifying the ExtGState resource.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public GS(List<COSBase> operands) {
        super("gs", operands);
        this.dictName = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the ExtGState dictionary resource name.
     *
     * @return the dictionary name
     */
    public String getDictName() {
        return dictName;
    }
}
