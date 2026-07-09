package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>timePatterns</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class TimePatterns extends XfaNode {

    /** Wraps a backing <code>timePatterns</code> element. */
    public TimePatterns(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>timePattern</code> child (typed), or null. */
    public TimePattern getTimePattern() { return (TimePattern) getChild("timePattern"); }
    /** Ensures and returns the <code>timePattern</code> child. */
    public TimePattern ensureTimePattern() { return (TimePattern) ensureChild("timePattern"); }
}
