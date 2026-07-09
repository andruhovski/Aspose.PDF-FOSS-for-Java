package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>xmlConnection</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class XmlConnection extends XfaNode {

    /** Wraps a backing <code>xmlConnection</code> element. */
    public XmlConnection(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>dataDescription</code> attribute, or null. */
    public String getDataDescription() { return getString("dataDescription"); }
    /** Sets the <code>dataDescription</code> attribute. */
    public void setDataDescription(String value) { setAttribute("dataDescription", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the <code>uri</code> child (typed), or null. */
    public Uri getUri() { return (Uri) getChild("uri"); }
    /** Ensures and returns the <code>uri</code> child. */
    public Uri ensureUri() { return (Uri) ensureChild("uri"); }
}
