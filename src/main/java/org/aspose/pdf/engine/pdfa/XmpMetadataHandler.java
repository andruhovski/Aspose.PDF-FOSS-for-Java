package org.aspose.pdf.engine.pdfa;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads and writes XMP metadata packets for PDF/A compliance.
 * <p>
 * XMP (Extensible Metadata Platform) is stored in a PDF as an unfiltered XML stream
 * referenced by the catalog's {@code /Metadata} entry. PDF/A requires specific XMP
 * properties including the {@code pdfaid:part} and {@code pdfaid:conformance} identifiers.
 * </p>
 * <p>
 * This handler supports both reading XMP from an existing catalog and creating new XMP
 * packets with the required namespaces: dc, xmp, pdf, pdfaid, rdf, pdfaExtension,
 * pdfaSchema, and pdfaProperty.
 * </p>
 *
 * @see <a href="https://www.iso.org/standard/51502.html">ISO 19005 (PDF/A)</a>
 */
public final class XmpMetadataHandler {

    private static final Logger LOG = Logger.getLogger(XmpMetadataHandler.class.getName());

    // XMP namespace URIs
    private static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
    private static final String NS_XMP = "http://ns.adobe.com/xap/1.0/";
    private static final String NS_PDF = "http://ns.adobe.com/pdf/1.3/";
    private static final String NS_PDFAID = "http://www.aiim.org/pdfa/ns/id/";
    private static final String NS_PDFA_EXTENSION = "http://www.aiim.org/pdfa/ns/extension/";
    private static final String NS_PDFA_SCHEMA = "http://www.aiim.org/pdfa/ns/schema#";
    private static final String NS_PDFA_PROPERTY = "http://www.aiim.org/pdfa/ns/property#";

    private static final String XPACKET_BEGIN = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n";
    private static final String XPACKET_END = "<?xpacket end=\"w\"?>";
    private static final int MIN_PACKET_SIZE = 2048;

    /** Parsed properties in the form "namespace#localName" -> value. */
    private final Map<String, String> properties;

    /**
     * Private constructor; use factory methods.
     *
     * @param properties the parsed XMP properties
     */
    private XmpMetadataHandler(Map<String, String> properties) {
        this.properties = properties != null ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
    }

    /**
     * Reads XMP metadata from a PDF catalog dictionary.
     * <p>
     * Locates the {@code /Metadata} stream in the catalog, extracts the raw (unfiltered)
     * XML bytes, and parses properties from the RDF structure.
     * </p>
     *
     * @param catalog the document catalog dictionary
     * @return an {@code XmpMetadataHandler} with parsed properties, or an empty handler
     *         if no metadata stream is found
     * @throws IllegalArgumentException if catalog is {@code null}
     */
    public static XmpMetadataHandler readFromCatalog(COSDictionary catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("catalog must not be null");
        }

        try {
            COSBase metaObj = catalog.get(COSName.of("Metadata"));
            if (metaObj instanceof COSObjectReference) {
                metaObj = ((COSObjectReference) metaObj).dereference();
            }
            if (!(metaObj instanceof COSStream)) {
                LOG.fine("No /Metadata stream found in catalog");
                return new XmpMetadataHandler(null);
            }

            COSStream metaStream = (COSStream) metaObj;
            // XMP metadata streams must not be filtered (PDF/A requirement)
            byte[] xmlBytes = metaStream.getDecodedData();
            return parseXmpBytes(xmlBytes);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read XMP metadata from catalog", e);
            return new XmpMetadataHandler(null);
        }
    }

    /**
     * Parses XMP properties from raw XML bytes.
     */
    private static XmpMetadataHandler parseXmpBytes(byte[] xmlBytes) {
        Map<String, String> props = new LinkedHashMap<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Disable external entities for security
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlBytes));

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();

            // Find all rdf:Description elements
            NodeList descriptions = doc.getElementsByTagNameNS(NS_RDF, "Description");
            for (int i = 0; i < descriptions.getLength(); i++) {
                Element desc = (Element) descriptions.item(i);
                parseDescription(desc, props);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to parse XMP XML", e);
        }
        return new XmpMetadataHandler(props);
    }

    /**
     * Extracts properties from an rdf:Description element.
     */
    private static void parseDescription(Element desc, Map<String, String> props) {
        // Check attributes for simple properties
        for (int a = 0; a < desc.getAttributes().getLength(); a++) {
            Node attr = desc.getAttributes().item(a);
            String ns = attr.getNamespaceURI();
            String local = attr.getLocalName();
            if (ns != null && !NS_RDF.equals(ns)
                    && !"xmlns".equals(local) && !"xmlns".equals(attr.getPrefix())) {
                props.put(ns + local, attr.getNodeValue());
            }
        }

        // Check child elements
        NodeList children = desc.getChildNodes();
        for (int c = 0; c < children.getLength(); c++) {
            Node child = children.item(c);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element) child;
            String ns = elem.getNamespaceURI();
            String local = elem.getLocalName();
            if (ns == null) {
                continue;
            }

            String key = ns + local;

            // Check for rdf:Alt (dc:title, dc:description) -- get x-default
            Element alt = getFirstChildElement(elem, NS_RDF, "Alt");
            if (alt != null) {
                String val = getAltDefaultValue(alt);
                if (val != null) {
                    props.put(key, val);
                }
                continue;
            }

            // Check for rdf:Seq (dc:creator) -- get first li
            Element seq = getFirstChildElement(elem, NS_RDF, "Seq");
            if (seq != null) {
                String val = getFirstSeqValue(seq);
                if (val != null) {
                    props.put(key, val);
                }
                continue;
            }

            // Check for rdf:Bag
            Element bag = getFirstChildElement(elem, NS_RDF, "Bag");
            if (bag != null) {
                String val = getFirstSeqValue(bag); // same structure as Seq
                if (val != null) {
                    props.put(key, val);
                }
                continue;
            }

            // Simple text content
            String text = getTextContent(elem);
            if (text != null && !text.isEmpty()) {
                props.put(key, text);
            }
        }
    }

    /**
     * Returns the first child element with the given namespace and local name, or null.
     */
    private static Element getFirstChildElement(Element parent, String ns, String localName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && localName.equals(child.getLocalName())
                    && ns.equals(child.getNamespaceURI())) {
                return (Element) child;
            }
        }
        return null;
    }

    /**
     * Gets the x-default value from an rdf:Alt element.
     */
    private static String getAltDefaultValue(Element alt) {
        NodeList items = alt.getElementsByTagNameNS(NS_RDF, "li");
        for (int i = 0; i < items.getLength(); i++) {
            Element li = (Element) items.item(i);
            String lang = li.getAttributeNS("http://www.w3.org/XML/1998/namespace", "lang");
            if ("x-default".equals(lang) || items.getLength() == 1) {
                return getTextContent(li);
            }
        }
        // Fallback to first item
        if (items.getLength() > 0) {
            return getTextContent((Element) items.item(0));
        }
        return null;
    }

    /**
     * Gets the first value from an rdf:Seq or rdf:Bag element.
     */
    private static String getFirstSeqValue(Element seqOrBag) {
        NodeList items = seqOrBag.getElementsByTagNameNS(NS_RDF, "li");
        if (items.getLength() > 0) {
            return getTextContent((Element) items.item(0));
        }
        return null;
    }

    /**
     * Returns the direct text content of an element, trimmed.
     */
    private static String getTextContent(Element elem) {
        StringBuilder sb = new StringBuilder();
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(child.getNodeValue());
            }
        }
        return sb.toString().trim();
    }

    /**
     * Creates an XMP metadata byte array suitable for embedding in a PDF stream.
     * <p>
     * The resulting bytes include the xpacket processing instructions and are padded
     * to at least {@value MIN_PACKET_SIZE} bytes.
     * </p>
     *
     * @param properties     additional properties as namespace#name to value mappings
     * @param pdfaPart       the PDF/A part number (1, 2, or 3), or 0 to omit pdfaid
     * @param pdfaConformance the PDF/A conformance level ("A", "B", "U"), or null
     * @return the XMP packet bytes in UTF-8
     */
    public byte[] createXmpBytes(Map<String, String> properties, int pdfaPart, String pdfaConformance) {
        StringBuilder xmp = new StringBuilder();
        xmp.append(XPACKET_BEGIN);
        xmp.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xmp.append("<rdf:RDF xmlns:rdf=\"").append(NS_RDF).append("\">\n");
        xmp.append("<rdf:Description rdf:about=\"\"\n");
        xmp.append("  xmlns:dc=\"").append(NS_DC).append("\"\n");
        xmp.append("  xmlns:xmp=\"").append(NS_XMP).append("\"\n");
        xmp.append("  xmlns:pdf=\"").append(NS_PDF).append("\"\n");
        if (pdfaPart > 0) {
            xmp.append("  xmlns:pdfaid=\"").append(NS_PDFAID).append("\"\n");
        }
        xmp.append("  xmlns:pdfaExtension=\"").append(NS_PDFA_EXTENSION).append("\"\n");
        xmp.append("  xmlns:pdfaSchema=\"").append(NS_PDFA_SCHEMA).append("\"\n");
        xmp.append("  xmlns:pdfaProperty=\"").append(NS_PDFA_PROPERTY).append("\"");

        // Emit simple (attribute-level) properties
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                // Only emit as attribute if it is a simple, non-structured property
                // (not dc:title, dc:creator, dc:description which need rdf:Alt/Seq)
                if (!isStructuredProperty(key)) {
                    String prefix = prefixFor(key);
                    String local = localNameOf(key);
                    if (prefix != null) {
                        xmp.append("\n  ").append(prefix).append(':').append(local)
                                .append("=\"").append(escapeXmlAttr(value)).append('"');
                    }
                }
            }
        }

        if (pdfaPart > 0) {
            xmp.append("\n  pdfaid:part=\"").append(pdfaPart).append('"');
            if (pdfaConformance != null) {
                xmp.append("\n  pdfaid:conformance=\"").append(escapeXmlAttr(pdfaConformance)).append('"');
            }
        }
        xmp.append(">\n");

        // Emit structured properties (dc:title, dc:creator, dc:description)
        if (properties != null) {
            emitStructuredProperties(xmp, properties);
        }

        xmp.append("</rdf:Description>\n");
        xmp.append("</rdf:RDF>\n");
        xmp.append("</x:xmpmeta>\n");

        // Pad to minimum size
        int currentLen = xmp.length() + XPACKET_END.length();
        if (currentLen < MIN_PACKET_SIZE) {
            int padNeeded = MIN_PACKET_SIZE - currentLen;
            for (int i = 0; i < padNeeded; i++) {
                xmp.append(' ');
            }
            xmp.append('\n');
        }

        xmp.append(XPACKET_END);
        return xmp.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns a property value by namespace URI and local name.
     *
     * @param namespace the XMP namespace URI (e.g. "http://purl.org/dc/elements/1.1/")
     * @param name      the local property name (e.g. "title")
     * @return the value, or {@code null} if not found
     */
    public String getProperty(String namespace, String name) {
        return properties.get(namespace + name);
    }

    /**
     * Returns an unmodifiable map of all parsed properties.
     * Keys are in the form "namespaceURI" + "localName".
     *
     * @return all properties
     */
    public Map<String, String> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns {@code true} if the XMP metadata contains a {@code pdfaid:part} property.
     *
     * @return true if PDF/A identification is present
     */
    public boolean hasPdfAId() {
        return properties.containsKey(NS_PDFAID + "part");
    }

    /**
     * Returns the PDF/A part number from the XMP metadata, or 0 if not present.
     *
     * @return the pdfaid:part value (1, 2, 3, ...) or 0
     */
    public int getPdfAPart() {
        String val = properties.get(NS_PDFAID + "part");
        if (val == null) {
            return 0;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            LOG.warning("Invalid pdfaid:part value: " + val);
            return 0;
        }
    }

    /**
     * Returns the PDF/A conformance level from the XMP metadata, or {@code null} if not present.
     *
     * @return the pdfaid:conformance value ("A", "B", "U") or null
     */
    public String getPdfAConformance() {
        return properties.get(NS_PDFAID + "conformance");
    }

    /**
     * Converts a PDF date string to XMP date format.
     * <p>
     * PDF dates have the form {@code D:YYYYMMDDHHmmSSOHH'mm'} and are converted to
     * ISO 8601 format {@code YYYY-MM-DDTHH:mm:ss+HH:mm}.
     * </p>
     *
     * @param pdfDate the PDF date string (e.g. "D:20040408152500+02'00'")
     * @return the XMP date string (e.g. "2004-04-08T15:25:00+02:00"), or the original
     *         string if parsing fails
     */
    public static String pdfDateToXmpDate(String pdfDate) {
        if (pdfDate == null || pdfDate.isEmpty()) {
            return pdfDate;
        }

        String s = pdfDate;
        // Strip "D:" prefix
        if (s.startsWith("D:")) {
            s = s.substring(2);
        }

        // Minimum: YYYY
        if (s.length() < 4) {
            return pdfDate;
        }

        try {
            String year = s.substring(0, 4);
            String month = s.length() >= 6 ? s.substring(4, 6) : "01";
            String day = s.length() >= 8 ? s.substring(6, 8) : "01";
            String hour = s.length() >= 10 ? s.substring(8, 10) : "00";
            String minute = s.length() >= 12 ? s.substring(10, 12) : "00";
            String second = s.length() >= 14 ? s.substring(12, 14) : "00";

            StringBuilder result = new StringBuilder();
            result.append(year).append('-').append(month).append('-').append(day);
            result.append('T').append(hour).append(':').append(minute).append(':').append(second);

            // Timezone
            if (s.length() > 14) {
                char tzSign = s.charAt(14);
                if (tzSign == 'Z') {
                    result.append('Z');
                } else if (tzSign == '+' || tzSign == '-') {
                    String tzHour = s.length() >= 17 ? s.substring(15, 17) : "00";
                    // Skip the apostrophe
                    String tzMin = "00";
                    int apostropheIdx = s.indexOf('\'', 15);
                    if (apostropheIdx >= 0 && apostropheIdx + 3 <= s.length()) {
                        tzMin = s.substring(apostropheIdx + 1, Math.min(apostropheIdx + 3, s.length()));
                        // Remove trailing apostrophe if present
                        tzMin = tzMin.replace("'", "");
                    }
                    if (tzMin.isEmpty()) {
                        tzMin = "00";
                    }
                    result.append(tzSign).append(tzHour).append(':').append(tzMin);
                }
            }

            return result.toString();
        } catch (IndexOutOfBoundsException e) {
            LOG.fine("Could not fully parse PDF date: " + pdfDate);
            return pdfDate;
        }
    }

    /**
     * Builds a complete XMP packet with all standard namespaces for PDF/A compliance.
     * <p>
     * The packet includes Dublin Core (dc), XMP basic (xmp), Adobe PDF (pdf), and
     * PDF/A identification (pdfaid) namespaces. The result is wrapped in xpacket
     * processing instructions and padded to at least {@value MIN_PACKET_SIZE} bytes.
     * </p>
     *
     * @param dcTitle          the document title (may be {@code null})
     * @param dcCreator        the document creator/author (may be {@code null})
     * @param dcDescription    the document description (may be {@code null})
     * @param pdfKeywords      PDF keywords (may be {@code null})
     * @param xmpCreatorTool   the creating application name (may be {@code null})
     * @param pdfProducer      the PDF producer (may be {@code null})
     * @param xmpCreateDate    creation date in XMP format (may be {@code null})
     * @param xmpModifyDate    modification date in XMP format (may be {@code null})
     * @param pdfaPart         the PDF/A part number (1, 2, 3), or 0 to omit
     * @param pdfaConformance  the PDF/A conformance level ("A", "B", "U"), or null
     * @return the complete XMP packet as UTF-8 bytes
     */
    public static byte[] buildXmpPacket(String dcTitle, String dcCreator, String dcDescription,
                                        String pdfKeywords, String xmpCreatorTool, String pdfProducer,
                                        String xmpCreateDate, String xmpModifyDate,
                                        int pdfaPart, String pdfaConformance) {
        StringBuilder xmp = new StringBuilder();
        xmp.append(XPACKET_BEGIN);
        xmp.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xmp.append("<rdf:RDF xmlns:rdf=\"").append(NS_RDF).append("\">\n");
        xmp.append("<rdf:Description rdf:about=\"\"\n");
        xmp.append("  xmlns:dc=\"").append(NS_DC).append("\"\n");
        xmp.append("  xmlns:xmp=\"").append(NS_XMP).append("\"\n");
        xmp.append("  xmlns:pdf=\"").append(NS_PDF).append("\"\n");
        if (pdfaPart > 0) {
            xmp.append("  xmlns:pdfaid=\"").append(NS_PDFAID).append("\"\n");
        }
        xmp.append("  xmlns:pdfaExtension=\"").append(NS_PDFA_EXTENSION).append("\"\n");
        xmp.append("  xmlns:pdfaSchema=\"").append(NS_PDFA_SCHEMA).append("\"\n");
        xmp.append("  xmlns:pdfaProperty=\"").append(NS_PDFA_PROPERTY).append("\"");

        // Simple attribute properties
        if (pdfKeywords != null) {
            xmp.append("\n  pdf:Keywords=\"").append(escapeXmlAttr(pdfKeywords)).append('"');
        }
        if (xmpCreatorTool != null) {
            xmp.append("\n  xmp:CreatorTool=\"").append(escapeXmlAttr(xmpCreatorTool)).append('"');
        }
        if (pdfProducer != null) {
            xmp.append("\n  pdf:Producer=\"").append(escapeXmlAttr(pdfProducer)).append('"');
        }
        if (xmpCreateDate != null) {
            xmp.append("\n  xmp:CreateDate=\"").append(escapeXmlAttr(xmpCreateDate)).append('"');
        }
        if (xmpModifyDate != null) {
            xmp.append("\n  xmp:ModifyDate=\"").append(escapeXmlAttr(xmpModifyDate)).append('"');
        }
        if (pdfaPart > 0) {
            xmp.append("\n  pdfaid:part=\"").append(pdfaPart).append('"');
            if (pdfaConformance != null) {
                xmp.append("\n  pdfaid:conformance=\"").append(escapeXmlAttr(pdfaConformance)).append('"');
            }
        }
        xmp.append(">\n");

        // dc:title as rdf:Alt
        if (dcTitle != null) {
            xmp.append("  <dc:title>\n");
            xmp.append("    <rdf:Alt>\n");
            xmp.append("      <rdf:li xml:lang=\"x-default\">").append(escapeXml(dcTitle)).append("</rdf:li>\n");
            xmp.append("    </rdf:Alt>\n");
            xmp.append("  </dc:title>\n");
        }

        // dc:description as rdf:Alt
        if (dcDescription != null) {
            xmp.append("  <dc:description>\n");
            xmp.append("    <rdf:Alt>\n");
            xmp.append("      <rdf:li xml:lang=\"x-default\">").append(escapeXml(dcDescription)).append("</rdf:li>\n");
            xmp.append("    </rdf:Alt>\n");
            xmp.append("  </dc:description>\n");
        }

        // dc:creator as rdf:Seq
        if (dcCreator != null) {
            xmp.append("  <dc:creator>\n");
            xmp.append("    <rdf:Seq>\n");
            xmp.append("      <rdf:li>").append(escapeXml(dcCreator)).append("</rdf:li>\n");
            xmp.append("    </rdf:Seq>\n");
            xmp.append("  </dc:creator>\n");
        }

        xmp.append("</rdf:Description>\n");
        xmp.append("</rdf:RDF>\n");
        xmp.append("</x:xmpmeta>\n");

        // Pad to minimum size
        int currentLen = xmp.length() + XPACKET_END.length();
        if (currentLen < MIN_PACKET_SIZE) {
            int padNeeded = MIN_PACKET_SIZE - currentLen;
            for (int i = 0; i < padNeeded; i++) {
                xmp.append(' ');
            }
            xmp.append('\n');
        }

        xmp.append(XPACKET_END);
        return xmp.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Checks whether the given property key represents a structured property
     * (rdf:Alt or rdf:Seq) that cannot be expressed as a simple XML attribute.
     */
    private static boolean isStructuredProperty(String key) {
        return key.equals(NS_DC + "title")
                || key.equals(NS_DC + "creator")
                || key.equals(NS_DC + "description");
    }

    /**
     * Emits structured properties (dc:title, dc:creator, dc:description) as child elements.
     */
    private static void emitStructuredProperties(StringBuilder xmp, Map<String, String> properties) {
        String title = properties.get(NS_DC + "title");
        if (title != null) {
            xmp.append("  <dc:title>\n");
            xmp.append("    <rdf:Alt>\n");
            xmp.append("      <rdf:li xml:lang=\"x-default\">").append(escapeXml(title)).append("</rdf:li>\n");
            xmp.append("    </rdf:Alt>\n");
            xmp.append("  </dc:title>\n");
        }

        String description = properties.get(NS_DC + "description");
        if (description != null) {
            xmp.append("  <dc:description>\n");
            xmp.append("    <rdf:Alt>\n");
            xmp.append("      <rdf:li xml:lang=\"x-default\">").append(escapeXml(description)).append("</rdf:li>\n");
            xmp.append("    </rdf:Alt>\n");
            xmp.append("  </dc:description>\n");
        }

        String creator = properties.get(NS_DC + "creator");
        if (creator != null) {
            xmp.append("  <dc:creator>\n");
            xmp.append("    <rdf:Seq>\n");
            xmp.append("      <rdf:li>").append(escapeXml(creator)).append("</rdf:li>\n");
            xmp.append("    </rdf:Seq>\n");
            xmp.append("  </dc:creator>\n");
        }
    }

    /**
     * Returns the XMP namespace prefix for a given property key, or null if unknown.
     */
    private static String prefixFor(String key) {
        if (key.startsWith(NS_DC)) return "dc";
        if (key.startsWith(NS_XMP)) return "xmp";
        if (key.startsWith(NS_PDF)) return "pdf";
        if (key.startsWith(NS_PDFAID)) return "pdfaid";
        if (key.startsWith(NS_PDFA_EXTENSION)) return "pdfaExtension";
        if (key.startsWith(NS_PDFA_SCHEMA)) return "pdfaSchema";
        if (key.startsWith(NS_PDFA_PROPERTY)) return "pdfaProperty";
        return null;
    }

    /**
     * Extracts the local name from a "namespaceURI + localName" key.
     */
    private static String localNameOf(String key) {
        // Namespace URIs end with / or #
        int idx = key.lastIndexOf('/');
        int idx2 = key.lastIndexOf('#');
        int split = Math.max(idx, idx2);
        if (split >= 0 && split < key.length() - 1) {
            return key.substring(split + 1);
        }
        return key;
    }

    /**
     * Escapes XML special characters in text content.
     */
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escapes XML special characters in attribute values.
     */
    private static String escapeXmlAttr(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
