package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>era</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Era extends XfaNode {

    /** Wraps a backing <code>era</code> element. */
    public Era(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return this element's text content. */
    public String getValue() { return getTextContent(); }
    /** Sets this element's text content. */
    public void setValue(String value) { setTextContent(value); }
}
