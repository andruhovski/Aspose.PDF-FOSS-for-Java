package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>color</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Color extends XfaNode {

    /** Wraps a backing <code>color</code> element. */
    public Color(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>cSpace</code> attribute, or null. */
    public String getCSpace() { return getString("cSpace"); }
    /** Sets the <code>cSpace</code> attribute. */
    public void setCSpace(String value) { setAttribute("cSpace", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>value</code> attribute, or null. */
    public String getValue() { return getString("value"); }
    /** Sets the <code>value</code> attribute. */
    public void setValue(String value) { setAttribute("value", value); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
