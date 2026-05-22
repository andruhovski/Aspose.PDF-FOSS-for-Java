package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Collections;
import java.util.List;

/**
 * Begin marked content operator (BMC).
 * <p>
 * Begins a marked-content sequence with an associated tag but no properties dictionary.
 * The sequence is terminated by the matching {@link EMC} operator.
 * See ISO 32000-1:2008, §14.6, Table 320.
 * </p>
 */
public class BMC extends Operator {

    private final String tag;

    /**
     * Creates a BMC operator with the specified tag.
     *
     * @param tag the marked-content tag name
     * @throws IllegalArgumentException if tag is null or empty
     */
    public BMC(String tag) {
        super("BMC", Collections.singletonList(COSName.of(tag)));
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be null or empty");
        }
        this.tag = tag;
    }

    /**
     * Creates a BMC operator from parsed operands.
     * <p>
     * Expects one operand: a {@link COSName} for the tag.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public BMC(List<COSBase> operands) {
        super("BMC", operands);
        this.tag = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
                : "";
    }

    /**
     * Returns the marked-content tag name.
     *
     * @return the tag name
     */
    public String getTag() {
        return tag;
    }
}
