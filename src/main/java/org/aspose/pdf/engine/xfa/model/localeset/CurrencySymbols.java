package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `currencySymbols`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class CurrencySymbols extends XfaNode {

    /// Wraps a backing `currencySymbols` element.
    public CurrencySymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `currencySymbol` child (typed), or null.
    public CurrencySymbol getCurrencySymbol() { return (CurrencySymbol) getChild("currencySymbol"); }
    /// Ensures and returns the `currencySymbol` child.
    public CurrencySymbol ensureCurrencySymbol() { return (CurrencySymbol) ensureChild("currencySymbol"); }
}
