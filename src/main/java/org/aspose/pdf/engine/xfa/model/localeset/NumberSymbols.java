package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>numberSymbols</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class NumberSymbols extends XfaNode {

    /** Wraps a backing <code>numberSymbols</code> element. */
    public NumberSymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>numberSymbol</code> child (typed), or null. */
    public NumberSymbol getNumberSymbol() { return (NumberSymbol) getChild("numberSymbol"); }
    /** Ensures and returns the <code>numberSymbol</code> child. */
    public NumberSymbol ensureNumberSymbol() { return (NumberSymbol) ensureChild("numberSymbol"); }
}
