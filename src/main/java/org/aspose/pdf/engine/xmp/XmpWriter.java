package org.aspose.pdf.engine.xmp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Serializes an XMP property map to UTF-8 XMP XML with packet wrapper (ISO 16684-1).
 * <p>
 * Generates valid XMP/RDF XML with proper namespace declarations,
 * xpacket processing instructions, and ~2000 bytes of whitespace padding.
 * </p>
 */
public final class XmpWriter {

    private static final Logger LOG = Logger.getLogger(XmpWriter.class.getName());
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    private static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

    private XmpWriter() {}

    /**
     * Serializes XMP properties to UTF-8 bytes with packet wrapper.
     *
     * @param properties the property map
     * @param registry   the namespace registry
     * @return UTF-8 XMP XML bytes
     */
    public static byte[] serialize(Map<String, XmpProperty> properties,
                                    XmpNamespaceRegistry registry) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Packet header PI
            out.write("<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
                    .getBytes(StandardCharsets.UTF_8));

            // Build XML DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = builder.newDocument();

            org.w3c.dom.Element xmpmeta = xmlDoc.createElementNS("adobe:ns:meta/", "x:xmpmeta");
            xmpmeta.setAttributeNS(XMLNS_NS, "xmlns:x", "adobe:ns:meta/");
            xmlDoc.appendChild(xmpmeta);

            org.w3c.dom.Element rdfRDF = xmlDoc.createElementNS(RDF_NS, "rdf:RDF");
            rdfRDF.setAttributeNS(XMLNS_NS, "xmlns:rdf", RDF_NS);
            xmpmeta.appendChild(rdfRDF);

            org.w3c.dom.Element desc = xmlDoc.createElementNS(RDF_NS, "rdf:Description");
            desc.setAttributeNS(RDF_NS, "rdf:about", "");
            rdfRDF.appendChild(desc);

            // Add namespace declarations for used namespaces
            Map<String, String> usedNs = registry.getUsedNamespaces(properties);
            for (Map.Entry<String, String> ns : usedNs.entrySet()) {
                desc.setAttributeNS(XMLNS_NS, "xmlns:" + ns.getKey(), ns.getValue());
            }

            // Add properties
            for (Map.Entry<String, XmpProperty> entry : properties.entrySet()) {
                appendProperty(xmlDoc, desc, entry.getKey(), entry.getValue(), registry);
            }

            // Serialize XML
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            ByteArrayOutputStream xmlBytes = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(xmlBytes));
            out.write(xmlBytes.toByteArray());

            // Padding (~2000 bytes)
            out.write('\n');
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 100; j++) out.write(' ');
                out.write('\n');
            }

            // Packet trailer PI
            out.write("<?xpacket end=\"w\"?>".getBytes(StandardCharsets.UTF_8));

            return out.toByteArray();
        } catch (Exception e) {
            LOG.warning(() -> "Failed to serialize XMP: " + e.getMessage());
            return new byte[0];
        }
    }

    private static void appendProperty(org.w3c.dom.Document xmlDoc,
                                        org.w3c.dom.Element parent,
                                        String key, XmpProperty prop,
                                        XmpNamespaceRegistry registry) {
        int colon = key.indexOf(':');
        if (colon <= 0) return;
        String prefix = key.substring(0, colon);
        String localName = key.substring(colon + 1);
        String nsUri = registry.getUri(prefix);
        if (nsUri == null) return;

        org.w3c.dom.Element elem = xmlDoc.createElementNS(nsUri, prefix + ":" + localName);

        switch (prop.getType()) {
            case SIMPLE:
                elem.setTextContent(prop.getValue() != null ? prop.getValue() : "");
                break;

            case URI:
                elem.setAttributeNS(RDF_NS, "rdf:resource",
                        prop.getValue() != null ? prop.getValue() : "");
                break;

            case LANG_ALT:
                appendLangAlt(xmlDoc, elem, prop);
                break;

            case SEQ:
                appendArray(xmlDoc, elem, prop, "rdf:Seq");
                break;

            case BAG:
                appendArray(xmlDoc, elem, prop, "rdf:Bag");
                break;

            case STRUCT:
                appendStruct(xmlDoc, elem, prop, registry);
                break;
        }

        parent.appendChild(elem);
    }

    private static void appendLangAlt(org.w3c.dom.Document xmlDoc,
                                       org.w3c.dom.Element parent, XmpProperty prop) {
        org.w3c.dom.Element alt = xmlDoc.createElementNS(RDF_NS, "rdf:Alt");
        List<XmpProperty.LangAltEntry> entries = prop.getLangAltEntries();
        if (entries.isEmpty() && prop.getValue() != null) {
            // Create single x-default entry
            org.w3c.dom.Element li = xmlDoc.createElementNS(RDF_NS, "rdf:li");
            li.setAttributeNS(XML_NS, "xml:lang", "x-default");
            li.setTextContent(prop.getValue());
            alt.appendChild(li);
        } else {
            for (XmpProperty.LangAltEntry entry : entries) {
                org.w3c.dom.Element li = xmlDoc.createElementNS(RDF_NS, "rdf:li");
                li.setAttributeNS(XML_NS, "xml:lang", entry.lang);
                li.setTextContent(entry.value);
                alt.appendChild(li);
            }
        }
        parent.appendChild(alt);
    }

    private static void appendArray(org.w3c.dom.Document xmlDoc,
                                     org.w3c.dom.Element parent, XmpProperty prop,
                                     String arrayTag) {
        org.w3c.dom.Element array = xmlDoc.createElementNS(RDF_NS, arrayTag);
        List<String> items = prop.getArrayItems();
        if (items.isEmpty() && prop.getValue() != null) {
            // Single-item array from value
            org.w3c.dom.Element li = xmlDoc.createElementNS(RDF_NS, "rdf:li");
            li.setTextContent(prop.getValue());
            array.appendChild(li);
        } else {
            for (String item : items) {
                org.w3c.dom.Element li = xmlDoc.createElementNS(RDF_NS, "rdf:li");
                li.setTextContent(item);
                array.appendChild(li);
            }
        }
        parent.appendChild(array);
    }

    private static void appendStruct(org.w3c.dom.Document xmlDoc,
                                      org.w3c.dom.Element parent, XmpProperty prop,
                                      XmpNamespaceRegistry registry) {
        org.w3c.dom.Element structDesc = xmlDoc.createElementNS(RDF_NS, "rdf:Description");
        for (XmpProperty field : prop.getStructFields()) {
            appendProperty(xmlDoc, structDesc, field.getKey(), field, registry);
        }
        parent.appendChild(structDesc);
    }
}
