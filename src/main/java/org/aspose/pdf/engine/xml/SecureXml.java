package org.aspose.pdf.engine.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Factory for XXE-hardened XML parsers, shared by every site that parses
 * <em>untrusted</em> XML (XFA packets, XFDF import, bookmark XML import).
 * <p>
 * A crafted document containing {@code <!DOCTYPE ... SYSTEM "file:///...">} could
 * otherwise read local files, trigger SSRF, or mount an entity-expansion DoS
 * ("billion laughs"). The returned factory therefore disallows DOCTYPE
 * declarations entirely and disables external general/parameter entities,
 * XInclude processing and entity-reference expansion. Each feature is set in its
 * own try/catch so an unsupported feature on an exotic JAXP implementation does
 * not break parsing (defence in depth: the remaining features still apply).
 * </p>
 */
public final class SecureXml {

    private SecureXml() {
    }

    /**
     * Creates an XXE-hardened {@link DocumentBuilderFactory}.
     *
     * @param namespaceAware whether the parser should be namespace-aware
     *                       (callers keep their existing setting; hardening is identical)
     * @return the hardened factory
     */
    public static DocumentBuilderFactory newBuilderFactory(boolean namespaceAware) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException ignore) {
            // not supported by this parser
        }
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        } catch (ParserConfigurationException ignore) {
            // not supported by this parser
        }
        try {
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException ignore) {
            // not supported by this parser
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     * Creates an XXE-hardened {@link DocumentBuilder}.
     *
     * @param namespaceAware whether the parser should be namespace-aware
     * @return the hardened builder
     * @throws IllegalStateException if no DocumentBuilder can be created at all
     */
    public static DocumentBuilder newBuilder(boolean namespaceAware) {
        try {
            return newBuilderFactory(namespaceAware).newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create XML DocumentBuilder", e);
        }
    }
}
