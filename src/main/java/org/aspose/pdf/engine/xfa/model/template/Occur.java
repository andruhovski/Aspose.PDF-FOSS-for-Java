package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>occur</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Occur extends XfaNode {

    /** Wraps a backing <code>occur</code> element. */
    public Occur(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>initial</code> attribute, or null. */
    public java.lang.Integer getInitial() { return getInteger("initial"); }
    /** Sets the <code>initial</code> attribute. */
    public void setInitial(java.lang.Integer value) { setAttribute("initial", value == null ? null : value.toString()); }

    /** @return the typed <code>max</code> attribute, or null. */
    public java.lang.Integer getMax() { return getInteger("max"); }
    /** Sets the <code>max</code> attribute. */
    public void setMax(java.lang.Integer value) { setAttribute("max", value == null ? null : value.toString()); }

    /** @return the typed <code>min</code> attribute, or null. */
    public java.lang.Integer getMin() { return getInteger("min"); }
    /** Sets the <code>min</code> attribute. */
    public void setMin(java.lang.Integer value) { setAttribute("min", value == null ? null : value.toString()); }

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
