package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Arrays;
import java.util.List;

/// Begin marked content with properties operator (BDC).
///
/// Begins a marked-content sequence with an associated tag and a properties dictionary
/// (or an indirect reference to one). The sequence is terminated by the matching
/// [EMC] operator.
/// See ISO 32000-1:2008, §14.6, Table 320.
///
public class BDC extends Operator {

    private final String tag;
    private final PdfBase properties;

    /// Creates a BDC operator with the specified tag and properties.
    ///
    /// @param tag        the marked-content tag name
    /// @param properties the properties dictionary or resource name
    /// @throws IllegalArgumentException if tag is null or empty
    public BDC(String tag, PdfBase properties) {
        super("BDC", Arrays.asList(PdfName.of(tag), properties));
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be null or empty");
        }
        this.tag = tag;
        this.properties = properties;
    }

    /// Creates a BDC operator from parsed operands.
    ///
    /// Expects two operands: a [PdfName] for the tag and a PDF object
    /// (typically a [org.aspose.pdf.engine.pdfobjects.PdfDictionary] or
    /// [PdfName]) for the properties.
    ///
    /// @param operands the operands from the content stream parser
    public BDC(List<PdfBase> operands) {
        super("BDC", operands);
        this.tag = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
        this.properties = (operands != null && operands.size() > 1)
                ? operands.get(1)
                : null;
    }

    /// Returns the marked-content tag name.
    ///
    /// @return the tag name
    public String getTag() {
        return tag;
    }

    /// Returns the properties associated with this marked content.
    ///
    /// @return the properties dictionary or resource name, or `null` if absent
    public PdfBase getProperties() {
        return properties;
    }
}
