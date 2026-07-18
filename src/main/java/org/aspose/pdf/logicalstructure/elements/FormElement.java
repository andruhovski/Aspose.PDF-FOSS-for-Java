package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/// Represents a form (Form) illustration structure element in the logical structure tree
/// (ISO 32000-1:2008, §14.8.4.5).
public class FormElement extends Element {

    private static final Logger LOG = Logger.getLogger(FormElement.class.getName());

    /// Creates a Form element wrapping the given structure element.
    ///
    /// @param se the structure element with type Form
    public FormElement(StructureElement se) {
        super(se);
    }
}
