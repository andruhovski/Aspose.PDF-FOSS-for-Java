package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>meridiemNames</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class MeridiemNames extends XfaNode {

    /** Wraps a backing <code>meridiemNames</code> element. */
    public MeridiemNames(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>meridiem</code> child (typed), or null. */
    public Meridiem getMeridiem() { return (Meridiem) getChild("meridiem"); }
    /** Ensures and returns the <code>meridiem</code> child. */
    public Meridiem ensureMeridiem() { return (Meridiem) ensureChild("meridiem"); }
}
