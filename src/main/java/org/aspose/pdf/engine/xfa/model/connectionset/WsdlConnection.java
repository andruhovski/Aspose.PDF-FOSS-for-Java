package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `wsdlConnection`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class WsdlConnection extends XfaNode {

    /// Wraps a backing `wsdlConnection` element.
    public WsdlConnection(Element element, XfaNode parent) {
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

    /// @return the `effectiveInputPolicy` child (typed), or null.
    public EffectiveInputPolicy getEffectiveInputPolicy() { return (EffectiveInputPolicy) getChild("effectiveInputPolicy"); }
    /// Ensures and returns the `effectiveInputPolicy` child.
    public EffectiveInputPolicy ensureEffectiveInputPolicy() { return (EffectiveInputPolicy) ensureChild("effectiveInputPolicy"); }

    /// @return the `effectiveOutputPolicy` child (typed), or null.
    public EffectiveOutputPolicy getEffectiveOutputPolicy() { return (EffectiveOutputPolicy) getChild("effectiveOutputPolicy"); }
    /// Ensures and returns the `effectiveOutputPolicy` child.
    public EffectiveOutputPolicy ensureEffectiveOutputPolicy() { return (EffectiveOutputPolicy) ensureChild("effectiveOutputPolicy"); }

    /// @return the `operation` child (typed), or null.
    public Operation getOperation() { return (Operation) getChild("operation"); }
    /// Ensures and returns the `operation` child.
    public Operation ensureOperation() { return (Operation) ensureChild("operation"); }

    /// @return the `soapAction` child (typed), or null.
    public SoapAction getSoapAction() { return (SoapAction) getChild("soapAction"); }
    /// Ensures and returns the `soapAction` child.
    public SoapAction ensureSoapAction() { return (SoapAction) ensureChild("soapAction"); }

    /// @return the `soapAddress` child (typed), or null.
    public SoapAddress getSoapAddress() { return (SoapAddress) getChild("soapAddress"); }
    /// Ensures and returns the `soapAddress` child.
    public SoapAddress ensureSoapAddress() { return (SoapAddress) ensureChild("soapAddress"); }

    /// @return the `wsdlAddress` child (typed), or null.
    public WsdlAddress getWsdlAddress() { return (WsdlAddress) getChild("wsdlAddress"); }
    /// Ensures and returns the `wsdlAddress` child.
    public WsdlAddress ensureWsdlAddress() { return (WsdlAddress) ensureChild("wsdlAddress"); }
}
