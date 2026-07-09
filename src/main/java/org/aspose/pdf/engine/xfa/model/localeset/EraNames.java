package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>eraNames</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class EraNames extends XfaNode {

    /** Wraps a backing <code>eraNames</code> element. */
    public EraNames(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>era</code> child (typed), or null. */
    public Era getEra() { return (Era) getChild("era"); }
    /** Ensures and returns the <code>era</code> child. */
    public Era ensureEra() { return (Era) ensureChild("era"); }
}
