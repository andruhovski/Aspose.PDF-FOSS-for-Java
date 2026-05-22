package org.aspose.pdf.engine.xmp;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Parses XMP XML (ISO 16684-1) into an internal property map.
 * <p>
 * Handles all standard RDF value forms: simple properties (text and attributes),
 * Language Alternatives (rdf:Alt), ordered arrays (rdf:Seq), unordered arrays
 * (rdf:Bag), structures (rdf:Description), and rdf:parseType="Resource".
 * Merges properties from multiple rdf:Description elements.
 * </p>
 */
public final class XmpParser {

    private static final Logger LOG = Logger.getLogger(XmpParser.class.getName());
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    private XmpParser() {}

    /**
     * Parses XMP XML bytes into a property map and populates the namespace registry.
     *
     * @param data     UTF-8 XMP XML bytes (may include xpacket PIs)
     * @param registry the namespace registry to populate with discovered namespaces
     * @return map of "prefix:localName" → XmpProperty
     */
    public static Map<String, XmpProperty> parse(byte[] data, XmpNamespaceRegistry registry) {
        Map<String, XmpProperty> properties = new LinkedHashMap<>();
        if (data == null || data.length == 0) return properties;

        try {
            // Strip xpacket PIs if present — they're not valid XML
            String xml = new String(data, StandardCharsets.UTF_8);
            xml = stripXpacketPIs(xml);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // Disable external entities for security
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            // Find rdf:RDF element
            NodeList rdfNodes = xmlDoc.getElementsByTagNameNS(RDF_NS, "RDF");
            if (rdfNodes.getLength() == 0) return properties;

            Element rdfElement = (Element) rdfNodes.item(0);

            // Iterate rdf:Description children
            NodeList children = rdfElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                Element desc = (Element) child;
                if (!"Description".equals(desc.getLocalName())) continue;

                // Collect namespace declarations
                collectNamespaces(desc, registry);

                // Process attributes as property shorthand
                processAttributes(desc, properties, registry);

                // Process child property elements
                processChildElements(desc, properties, registry);
            }
        } catch (Exception e) {
            LOG.warning(() -> "Failed to parse XMP: " + e.getMessage());
        }

        return properties;
    }

    private static String stripXpacketPIs(String xml) {
        // Remove <?xpacket ...?> processing instructions
        xml = xml.replaceAll("<\\?xpacket[^?]*\\?>", "").trim();
        return xml;
    }

    private static void collectNamespaces(Element elem, XmpNamespaceRegistry registry) {
        NamedNodeMap attrs = elem.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            String name = attr.getName();
            if (name.startsWith("xmlns:")) {
                String prefix = name.substring(6);
                String uri = attr.getValue();
                if (!"rdf".equals(prefix) && !"xml".equals(prefix) && !"xmlns".equals(prefix)) {
                    registry.register(prefix, uri);
                }
            }
        }
    }

    private static void processAttributes(Element desc, Map<String, XmpProperty> properties,
                                           XmpNamespaceRegistry registry) {
        NamedNodeMap attrs = desc.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            String nsUri = attr.getNamespaceURI();
            String localName = attr.getLocalName();
            String value = attr.getValue();

            // Skip xmlns declarations, rdf:about, xml:lang
            if (nsUri == null) continue;
            if ("http://www.w3.org/2000/xmlns/".equals(nsUri)) continue;
            if (RDF_NS.equals(nsUri) && "about".equals(localName)) continue;
            if (XML_NS.equals(nsUri)) continue;

            String prefix = registry.getPrefix(nsUri);
            if (prefix == null) continue;

            String key = prefix + ":" + localName;
            properties.put(key, new XmpProperty(key, value, XmpProperty.ValueType.SIMPLE));
        }
    }

    private static void processChildElements(Element desc, Map<String, XmpProperty> properties,
                                               XmpNamespaceRegistry registry) {
        NodeList children = desc.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element propElem = (Element) child;

            String nsUri = propElem.getNamespaceURI();
            String localName = propElem.getLocalName();
            if (nsUri == null || localName == null) continue;

            String prefix = registry.getPrefix(nsUri);
            if (prefix == null) {
                // Try to auto-register from element's prefix
                String xmlPrefix = propElem.getPrefix();
                if (xmlPrefix != null) {
                    registry.register(xmlPrefix, nsUri);
                    prefix = xmlPrefix;
                } else {
                    continue;
                }
            }

            String key = prefix + ":" + localName;
            XmpProperty prop = parsePropertyElement(key, propElem, registry);
            if (prop != null) {
                properties.put(key, prop);
            }
        }
    }

    private static XmpProperty parsePropertyElement(String key, Element propElem,
                                                      XmpNamespaceRegistry registry) {
        // Check for rdf:resource attribute → URI value
        if (propElem.hasAttributeNS(RDF_NS, "resource")) {
            return new XmpProperty(key, propElem.getAttributeNS(RDF_NS, "resource"),
                    XmpProperty.ValueType.URI);
        }

        // Check for rdf:parseType="Resource" → structure
        if ("Resource".equals(propElem.getAttributeNS(RDF_NS, "parseType"))) {
            return parseStructureFromChildren(key, propElem, registry);
        }

        // Find first child element
        Element firstChild = getFirstChildElement(propElem);

        if (firstChild == null) {
            // Text content only → simple value
            String text = propElem.getTextContent();
            return new XmpProperty(key, text != null ? text.trim() : "",
                    XmpProperty.ValueType.SIMPLE);
        }

        String childLocalName = firstChild.getLocalName();
        String childNsUri = firstChild.getNamespaceURI();

        if (RDF_NS.equals(childNsUri)) {
            switch (childLocalName) {
                case "Alt":
                    return parseLangAlt(key, firstChild);
                case "Seq":
                    return parseArray(key, firstChild, XmpProperty.ValueType.SEQ);
                case "Bag":
                    return parseArray(key, firstChild, XmpProperty.ValueType.BAG);
                case "Description":
                    return parseStructure(key, firstChild, registry);
            }
        }

        // Fallback: simple value
        String text = propElem.getTextContent();
        return new XmpProperty(key, text != null ? text.trim() : "", XmpProperty.ValueType.SIMPLE);
    }

    private static XmpProperty parseLangAlt(String key, Element altElem) {
        XmpProperty prop = new XmpProperty(key, null, XmpProperty.ValueType.LANG_ALT);
        List<XmpProperty.LangAltEntry> entries = new ArrayList<>();

        NodeList items = altElem.getElementsByTagNameNS(RDF_NS, "li");
        for (int i = 0; i < items.getLength(); i++) {
            Element li = (Element) items.item(i);
            String lang = li.getAttributeNS(XML_NS, "lang");
            if (lang.isEmpty()) lang = "x-default";
            String text = li.getTextContent().trim();
            entries.add(new XmpProperty.LangAltEntry(lang, text));
        }

        prop.setLangAltEntries(entries);
        // Set value to x-default
        for (XmpProperty.LangAltEntry e : entries) {
            if ("x-default".equals(e.lang)) {
                prop.setValue(e.value);
                break;
            }
        }
        if (prop.getValue() == null && !entries.isEmpty()) {
            prop.setValue(entries.get(0).value);
        }
        return prop;
    }

    private static XmpProperty parseArray(String key, Element arrayElem,
                                            XmpProperty.ValueType type) {
        XmpProperty prop = new XmpProperty(key, null, type);
        List<String> items = new ArrayList<>();

        NodeList liNodes = arrayElem.getElementsByTagNameNS(RDF_NS, "li");
        for (int i = 0; i < liNodes.getLength(); i++) {
            items.add(liNodes.item(i).getTextContent().trim());
        }

        prop.setArrayItems(items);
        if (!items.isEmpty()) prop.setValue(items.get(0));
        return prop;
    }

    private static XmpProperty parseStructure(String key, Element descElem,
                                                XmpNamespaceRegistry registry) {
        XmpProperty prop = new XmpProperty(key, null, XmpProperty.ValueType.STRUCT);
        List<XmpProperty> fields = new ArrayList<>();

        // Process attributes
        NamedNodeMap attrs = descElem.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            String nsUri = attr.getNamespaceURI();
            if (nsUri == null || "http://www.w3.org/2000/xmlns/".equals(nsUri)) continue;
            if (RDF_NS.equals(nsUri)) continue;
            String prefix = registry.getPrefix(nsUri);
            if (prefix == null) continue;
            String fieldKey = prefix + ":" + attr.getLocalName();
            fields.add(new XmpProperty(fieldKey, attr.getValue(), XmpProperty.ValueType.SIMPLE));
        }

        // Process child elements
        NodeList children = descElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element) children.item(i);
            String nsUri = child.getNamespaceURI();
            String prefix = nsUri != null ? registry.getPrefix(nsUri) : null;
            if (prefix == null && child.getPrefix() != null) {
                registry.register(child.getPrefix(), nsUri);
                prefix = child.getPrefix();
            }
            if (prefix == null) continue;
            String fieldKey = prefix + ":" + child.getLocalName();
            XmpProperty fieldProp = parsePropertyElement(fieldKey, child, registry);
            if (fieldProp != null) fields.add(fieldProp);
        }

        prop.setStructFields(fields);
        return prop;
    }

    private static XmpProperty parseStructureFromChildren(String key, Element propElem,
                                                            XmpNamespaceRegistry registry) {
        // Treat child elements as struct fields (rdf:parseType="Resource")
        return parseStructure(key, propElem, registry);
    }

    private static Element getFirstChildElement(Element parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return (Element) children.item(i);
            }
        }
        return null;
    }
}
