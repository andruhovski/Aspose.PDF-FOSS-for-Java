package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>bindItems</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class BindItems extends XfaNode {

    /** Wraps a backing <code>bindItems</code> element. */
    public BindItems(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>connection</code> attribute, or null. */
    public String getConnection() { return getString("connection"); }
    /** Sets the <code>connection</code> attribute. */
    public void setConnection(String value) { setAttribute("connection", value); }

    /** @return the typed <code>labelRef</code> attribute, or null. */
    public String getLabelRef() { return getString("labelRef"); }
    /** Sets the <code>labelRef</code> attribute. */
    public void setLabelRef(String value) { setAttribute("labelRef", value); }

    /** @return the typed <code>ref</code> attribute, or null. */
    public String getRef() { return getString("ref"); }
    /** Sets the <code>ref</code> attribute. */
    public void setRef(String value) { setAttribute("ref", value); }

    /** @return the typed <code>valueRef</code> attribute, or null. */
    public String getValueRef() { return getString("valueRef"); }
    /** Sets the <code>valueRef</code> attribute. */
    public void setValueRef(String value) { setAttribute("valueRef", value); }
}
