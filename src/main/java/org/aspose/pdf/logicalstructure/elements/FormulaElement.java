package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a formula (Formula) illustration structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.5).
public class FormulaElement extends Element {

    private static final Logger LOG = Logger.getLogger(FormulaElement.class.getName());

    /// Creates a Formula element wrapping the given structure element.
    ///
    /// @param se the structure element with type Formula
    public FormulaElement(StructureElement se) {
        super(se);
    }
}
