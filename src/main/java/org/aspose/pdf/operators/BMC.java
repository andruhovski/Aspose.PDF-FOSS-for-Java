package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/// Begin marked content operator (BMC).
///
/// Begins a marked-content sequence with an associated tag but no properties dictionary.
/// The sequence is terminated by the matching [EMC] operator.
/// See ISO 32000-1:2008, §14.6, Table 320.
///
public class BMC extends Operator {

    private final String tag;

    /// Creates a BMC operator with the specified tag.
    ///
    /// @param tag the marked-content tag name
    /// @throws IllegalArgumentException if tag is null or empty
    public BMC(String tag) {
        super("BMC", Collections.singletonList(PdfName.of(tag)));
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be null or empty");
        }
        this.tag = tag;
    }

    /// Creates a BMC operator from parsed operands.
    ///
    /// Expects one operand: a [PdfName] for the tag.
    ///
    /// @param operands the operands from the content stream parser
    public BMC(List<PdfBase> operands) {
        super("BMC", operands);
        this.tag = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
    }

    /// Returns the marked-content tag name.
    ///
    /// @return the tag name
    public String getTag() {
        return tag;
    }
}
