package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `xsdConnection`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class XsdConnection extends XfaNode {

    /// Wraps a backing `xsdConnection` element.
    public XsdConnection(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `dataDescription` attribute, or null.
    public String getDataDescription() { return getString("dataDescription"); }
    /// Sets the `dataDescription` attribute.
    public void setDataDescription(String value) { setAttribute("dataDescription", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the `rootElement` child (typed), or null.
    public RootElement getRootElement() { return (RootElement) getChild("rootElement"); }
    /// Ensures and returns the `rootElement` child.
    public RootElement ensureRootElement() { return (RootElement) ensureChild("rootElement"); }

    /// @return the `uri` child (typed), or null.
    public Uri getUri() { return (Uri) getChild("uri"); }
    /// Ensures and returns the `uri` child.
    public Uri ensureUri() { return (Uri) ensureChild("uri"); }
}
