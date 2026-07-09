package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>setProperty</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class SetProperty extends XfaNode {

    /** Wraps a backing <code>setProperty</code> element. */
    public SetProperty(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>connection</code> attribute, or null. */
    public String getConnection() { return getString("connection"); }
    /** Sets the <code>connection</code> attribute. */
    public void setConnection(String value) { setAttribute("connection", value); }

    /** @return the typed <code>ref</code> attribute, or null. */
    public String getRef() { return getString("ref"); }
    /** Sets the <code>ref</code> attribute. */
    public void setRef(String value) { setAttribute("ref", value); }

    /** @return the typed <code>target</code> attribute, or null. */
    public String getTarget() { return getString("target"); }
    /** Sets the <code>target</code> attribute. */
    public void setTarget(String value) { setAttribute("target", value); }
}
