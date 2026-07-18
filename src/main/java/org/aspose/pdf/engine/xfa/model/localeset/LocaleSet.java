package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `localeSet`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class LocaleSet extends XfaNode {

    /// Wraps a backing `localeSet` element.
    public LocaleSet(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `locale` children (typed).
    public java.util.List<Locale> getLocaleList() {
        java.util.List<Locale> r = new java.util.ArrayList<Locale>();
        for (XfaNode n : getChildren("locale")) { r.add((Locale) n); }
        return r;
    }
    /// Appends a new `locale` child.
    public Locale addLocale() { return (Locale) addChild("locale"); }
}
