package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>connectionSet</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class ConnectionSet extends XfaNode {

    /** Wraps a backing <code>connectionSet</code> element. */
    public ConnectionSet(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the <code>wsdlConnection</code> children (typed). */
    public java.util.List<WsdlConnection> getWsdlConnectionList() {
        java.util.List<WsdlConnection> r = new java.util.ArrayList<WsdlConnection>();
        for (XfaNode n : getChildren("wsdlConnection")) { r.add((WsdlConnection) n); }
        return r;
    }
    /** Appends a new <code>wsdlConnection</code> child. */
    public WsdlConnection addWsdlConnection() { return (WsdlConnection) addChild("wsdlConnection"); }

    /** @return the <code>xmlConnection</code> children (typed). */
    public java.util.List<XmlConnection> getXmlConnectionList() {
        java.util.List<XmlConnection> r = new java.util.ArrayList<XmlConnection>();
        for (XfaNode n : getChildren("xmlConnection")) { r.add((XmlConnection) n); }
        return r;
    }
    /** Appends a new <code>xmlConnection</code> child. */
    public XmlConnection addXmlConnection() { return (XmlConnection) addChild("xmlConnection"); }

    /** @return the <code>xsdConnection</code> children (typed). */
    public java.util.List<XsdConnection> getXsdConnectionList() {
        java.util.List<XsdConnection> r = new java.util.ArrayList<XsdConnection>();
        for (XfaNode n : getChildren("xsdConnection")) { r.add((XsdConnection) n); }
        return r;
    }
    /** Appends a new <code>xsdConnection</code> child. */
    public XsdConnection addXsdConnection() { return (XsdConnection) addChild("xsdConnection"); }
}
