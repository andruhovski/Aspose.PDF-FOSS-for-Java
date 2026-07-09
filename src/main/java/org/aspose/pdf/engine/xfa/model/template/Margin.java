package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>margin</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Margin extends XfaNode {

    /** Wraps a backing <code>margin</code> element. */
    public Margin(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>bottomInset</code> attribute, or null. */
    public XfaMeasurement getBottomInset() { return getMeasurement("bottomInset"); }
    /** Sets the <code>bottomInset</code> attribute. */
    public void setBottomInset(XfaMeasurement value) { setAttribute("bottomInset", value == null ? null : value.format()); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>leftInset</code> attribute, or null. */
    public XfaMeasurement getLeftInset() { return getMeasurement("leftInset"); }
    /** Sets the <code>leftInset</code> attribute. */
    public void setLeftInset(XfaMeasurement value) { setAttribute("leftInset", value == null ? null : value.format()); }

    /** @return the typed <code>rightInset</code> attribute, or null. */
    public XfaMeasurement getRightInset() { return getMeasurement("rightInset"); }
    /** Sets the <code>rightInset</code> attribute. */
    public void setRightInset(XfaMeasurement value) { setAttribute("rightInset", value == null ? null : value.format()); }

    /** @return the typed <code>topInset</code> attribute, or null. */
    public XfaMeasurement getTopInset() { return getMeasurement("topInset"); }
    /** Sets the <code>topInset</code> attribute. */
    public void setTopInset(XfaMeasurement value) { setAttribute("topInset", value == null ? null : value.format()); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
