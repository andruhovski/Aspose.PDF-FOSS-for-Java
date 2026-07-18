package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `timePatterns`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class TimePatterns extends XfaNode {

    /// Wraps a backing `timePatterns` element.
    public TimePatterns(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `timePattern` child (typed), or null.
    public TimePattern getTimePattern() { return (TimePattern) getChild("timePattern"); }
    /// Ensures and returns the `timePattern` child.
    public TimePattern ensureTimePattern() { return (TimePattern) ensureChild("timePattern"); }
}
