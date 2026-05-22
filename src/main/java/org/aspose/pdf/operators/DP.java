package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Arrays;
import java.util.List;

/**
 * Marked content point with properties operator (DP).
 * <p>
 * Designates a marked-content point with an associated tag and a properties dictionary
 * (or an indirect reference to one). Unlike {@link BDC}/{@link EMC}, this operator
 * marks a single point rather than a sequence.
 * See ISO 32000-1:2008, §14.6, Table 320.
 * </p>
 */
public class DP extends Operator {

    private final String tag;
    private final COSBase properties;

    /**
     * Creates a DP operator with the specified tag and properties.
     *
     * @param tag        the marked-content tag name
     * @param properties the properties dictionary or resource name
     * @throws IllegalArgumentException if tag is null or empty
     */
    public DP(String tag, COSBase properties) {
        super("DP", Arrays.asList(COSName.of(tag), properties));
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be null or empty");
        }
        this.tag = tag;
        this.properties = properties;
    }

    /**
     * Creates a DP operator from parsed operands.
     * <p>
     * Expects two operands: a {@link COSName} for the tag and a COS object
     * (typically a {@link org.aspose.pdf.engine.cos.COSDictionary} or
     * {@link COSName}) for the properties.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public DP(List<COSBase> operands) {
        super("DP", operands);
        this.tag = (operands != null && operands.size() > 0 && operands.get(0) instanceof COSName)
                ? ((COSName) operands.get(0)).getName()
                : "";
        this.properties = (operands != null && operands.size() > 1)
                ? operands.get(1)
                : null;
    }

    /**
     * Returns the marked-content tag name.
     *
     * @return the tag name
     */
    public String getTag() {
        return tag;
    }

    /**
     * Returns the properties associated with this marked content point.
     *
     * @return the properties dictionary or resource name, or {@code null} if absent
     */
    public COSBase getProperties() {
        return properties;
    }
}
