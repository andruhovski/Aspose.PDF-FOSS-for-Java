package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>numberPatterns</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class NumberPatterns extends XfaNode {

    /** Wraps a backing <code>numberPatterns</code> element. */
    public NumberPatterns(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>numberPattern</code> child (typed), or null. */
    public NumberPattern getNumberPattern() { return (NumberPattern) getChild("numberPattern"); }
    /** Ensures and returns the <code>numberPattern</code> child. */
    public NumberPattern ensureNumberPattern() { return (NumberPattern) ensureChild("numberPattern"); }
}
