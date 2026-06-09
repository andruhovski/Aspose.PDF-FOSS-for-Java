package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/**
 * Marked content point operator (MP).
 * <p>
 * Designates a marked-content point with an associated tag but no properties dictionary.
 * Unlike {@link BMC}/{@link EMC}, this operator marks a single point rather than
 * a sequence.
 * See ISO 32000-1:2008, §14.6, Table 320.
 * </p>
 */
public class MP extends Operator {

    private final String tag;

    /**
     * Creates an MP operator with the specified tag.
     *
     * @param tag the marked-content tag name
     * @throws IllegalArgumentException if tag is null or empty
     */
    public MP(String tag) {
        super("MP", Collections.singletonList(PdfName.of(tag)));
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be null or empty");
        }
        this.tag = tag;
    }

    /**
     * Creates an MP operator from parsed operands.
     * <p>
     * Expects one operand: a {@link PdfName} for the tag.
     * </p>
     *
     * @param operands the operands from the content stream parser
     */
    public MP(List<PdfBase> operands) {
        super("MP", operands);
        this.tag = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
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
