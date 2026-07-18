package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `numberPatterns`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class NumberPatterns extends XfaNode {

    /// Wraps a backing `numberPatterns` element.
    public NumberPatterns(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `numberPattern` child (typed), or null.
    public NumberPattern getNumberPattern() { return (NumberPattern) getChild("numberPattern"); }
    /// Ensures and returns the `numberPattern` child.
    public NumberPattern ensureNumberPattern() { return (NumberPattern) ensureChild("numberPattern"); }
}
