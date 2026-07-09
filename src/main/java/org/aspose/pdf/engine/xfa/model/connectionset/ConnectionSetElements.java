package org.aspose.pdf.engine.xfa.model.connectionset;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/**
 * Registry of generated typed element constructors for this XFA grammar
 * (element local name -> typed node).
 */
public final class ConnectionSetElements {

    private ConnectionSetElements() { }

    /** The grammar's (version-independent) target namespace. */
    public static final String NAMESPACE = "http://www.xfa.org/schema/xfa-connection-set/";

    /** Number of generated typed element classes. */
    public static final int COUNT = 12;

    /**
     * Registers all typed element constructors.
     * @param reg the factory registry map
     */
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("connectionSet", ConnectionSet::new);
        reg.put("effectiveInputPolicy", EffectiveInputPolicy::new);
        reg.put("effectiveOutputPolicy", EffectiveOutputPolicy::new);
        reg.put("operation", Operation::new);
        reg.put("rootElement", RootElement::new);
        reg.put("soapAction", SoapAction::new);
        reg.put("soapAddress", SoapAddress::new);
        reg.put("uri", Uri::new);
        reg.put("wsdlAddress", WsdlAddress::new);
        reg.put("wsdlConnection", WsdlConnection::new);
        reg.put("xmlConnection", XmlConnection::new);
        reg.put("xsdConnection", XsdConnection::new);
    }
}
