package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.Collections;
import java.util.List;

/// Invoke named XObject operator (Do).
///
/// Paints the specified XObject. The operand is the name of the XObject resource
/// in the current resource dictionary. See ISO 32000-1:2008, §8.8, Table 57.
///
public class Do extends Operator {

    private final String xobjectName;

    /// Creates a Do operator with the specified XObject resource name.
    ///
    /// @param xobjectName the XObject resource name (e.g., "Im1", "Fm0")
    /// @throws IllegalArgumentException if xobjectName is null or empty
    public Do(String xobjectName) {
        super("Do", Collections.singletonList(PdfName.of(xobjectName)));
        if (xobjectName == null || xobjectName.isEmpty()) {
            throw new IllegalArgumentException("XObject name must not be null or empty");
        }
        this.xobjectName = xobjectName;
    }

    /// Creates a Do operator from parsed operands.
    ///
    /// Expects one operand: a [PdfName] identifying the XObject resource.
    ///
    /// @param operands the operands from the content stream parser
    public Do(List<PdfBase> operands) {
        super("Do", operands);
        this.xobjectName = (operands != null && operands.size() > 0 && operands.get(0) instanceof PdfName)
                ? ((PdfName) operands.get(0)).getName()
                : "";
    }

    /// Returns the XObject resource name.
    ///
    /// Note: use this method to get the resource name, not [#getName()],
    /// which returns the operator keyword "Do".
    ///
    /// @return the XObject resource name
    public String getXObjectName() {
        return xobjectName;
    }
}
