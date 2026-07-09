package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>contentArea</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class ContentArea extends XfaNode {

    /** Wraps a backing <code>contentArea</code> element. */
    public ContentArea(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>h</code> attribute, or null. */
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /** Sets the <code>h</code> attribute. */
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>relevant</code> attribute, or null. */
    public String getRelevant() { return getString("relevant"); }
    /** Sets the <code>relevant</code> attribute. */
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>w</code> attribute, or null. */
    public XfaMeasurement getW() { return getMeasurement("w"); }
    /** Sets the <code>w</code> attribute. */
    public void setW(XfaMeasurement value) { setAttribute("w", value == null ? null : value.format()); }

    /** @return the typed <code>x</code> attribute, or null. */
    public XfaMeasurement getX() { return getMeasurement("x"); }
    /** Sets the <code>x</code> attribute. */
    public void setX(XfaMeasurement value) { setAttribute("x", value == null ? null : value.format()); }

    /** @return the typed <code>y</code> attribute, or null. */
    public XfaMeasurement getY() { return getMeasurement("y"); }
    /** Sets the <code>y</code> attribute. */
    public void setY(XfaMeasurement value) { setAttribute("y", value == null ? null : value.format()); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
