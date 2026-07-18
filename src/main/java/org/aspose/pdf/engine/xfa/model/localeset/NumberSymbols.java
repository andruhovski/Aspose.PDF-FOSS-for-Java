package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `numberSymbols`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class NumberSymbols extends XfaNode {

    /// Wraps a backing `numberSymbols` element.
    public NumberSymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `numberSymbol` child (typed), or null.
    public NumberSymbol getNumberSymbol() { return (NumberSymbol) getChild("numberSymbol"); }
    /// Ensures and returns the `numberSymbol` child.
    public NumberSymbol ensureNumberSymbol() { return (NumberSymbol) ensureChild("numberSymbol"); }
}
