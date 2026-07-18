package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `datePatterns`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class DatePatterns extends XfaNode {

    /// Wraps a backing `datePatterns` element.
    public DatePatterns(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `datePattern` child (typed), or null.
    public DatePattern getDatePattern() { return (DatePattern) getChild("datePattern"); }
    /// Ensures and returns the `datePattern` child.
    public DatePattern ensureDatePattern() { return (DatePattern) ensureChild("datePattern"); }
}
