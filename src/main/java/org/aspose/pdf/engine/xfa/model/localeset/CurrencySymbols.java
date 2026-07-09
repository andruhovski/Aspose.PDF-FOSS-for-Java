package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>currencySymbols</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class CurrencySymbols extends XfaNode {

    /** Wraps a backing <code>currencySymbols</code> element. */
    public CurrencySymbols(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>currencySymbol</code> child (typed), or null. */
    public CurrencySymbol getCurrencySymbol() { return (CurrencySymbol) getChild("currencySymbol"); }
    /** Ensures and returns the <code>currencySymbol</code> child. */
    public CurrencySymbol ensureCurrencySymbol() { return (CurrencySymbol) ensureChild("currencySymbol"); }
}
