package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>wsdlConnection</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class WsdlConnection extends XfaNode {

    /** Wraps a backing <code>wsdlConnection</code> element. */
    public WsdlConnection(Element element, XfaNode parent) {
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

    /** @return the <code>effectiveInputPolicy</code> child (typed), or null. */
    public EffectiveInputPolicy getEffectiveInputPolicy() { return (EffectiveInputPolicy) getChild("effectiveInputPolicy"); }
    /** Ensures and returns the <code>effectiveInputPolicy</code> child. */
    public EffectiveInputPolicy ensureEffectiveInputPolicy() { return (EffectiveInputPolicy) ensureChild("effectiveInputPolicy"); }

    /** @return the <code>effectiveOutputPolicy</code> child (typed), or null. */
    public EffectiveOutputPolicy getEffectiveOutputPolicy() { return (EffectiveOutputPolicy) getChild("effectiveOutputPolicy"); }
    /** Ensures and returns the <code>effectiveOutputPolicy</code> child. */
    public EffectiveOutputPolicy ensureEffectiveOutputPolicy() { return (EffectiveOutputPolicy) ensureChild("effectiveOutputPolicy"); }

    /** @return the <code>operation</code> child (typed), or null. */
    public Operation getOperation() { return (Operation) getChild("operation"); }
    /** Ensures and returns the <code>operation</code> child. */
    public Operation ensureOperation() { return (Operation) ensureChild("operation"); }

    /** @return the <code>soapAction</code> child (typed), or null. */
    public SoapAction getSoapAction() { return (SoapAction) getChild("soapAction"); }
    /** Ensures and returns the <code>soapAction</code> child. */
    public SoapAction ensureSoapAction() { return (SoapAction) ensureChild("soapAction"); }

    /** @return the <code>soapAddress</code> child (typed), or null. */
    public SoapAddress getSoapAddress() { return (SoapAddress) getChild("soapAddress"); }
    /** Ensures and returns the <code>soapAddress</code> child. */
    public SoapAddress ensureSoapAddress() { return (SoapAddress) ensureChild("soapAddress"); }

    /** @return the <code>wsdlAddress</code> child (typed), or null. */
    public WsdlAddress getWsdlAddress() { return (WsdlAddress) getChild("wsdlAddress"); }
    /** Ensures and returns the <code>wsdlAddress</code> child. */
    public WsdlAddress ensureWsdlAddress() { return (WsdlAddress) ensureChild("wsdlAddress"); }
}
