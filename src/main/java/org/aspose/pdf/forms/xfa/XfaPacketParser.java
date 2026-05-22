package org.aspose.pdf.forms.xfa;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extracts and manages XML packets from the /XFA entry in a PDF AcroForm dictionary.
 * <p>
 * XFA (XML Forms Architecture) data in a PDF can be stored either as a single
 * COSStream containing a complete XDP document, or as a COSArray of interleaved
 * name/stream pairs representing individual packets (template, datasets, config, etc.).
 * </p>
 * <p>
 * This parser handles both representations, providing access to individual packets
 * as DOM {@link Document} objects and supporting write-back of modified packets
 * to the underlying COS structures.
 * </p>
 *
 * @see <a href="https://www.iso.org/standard/51502.html">ISO 32000-1:2008, §12.7.8</a>
 */
public class XfaPacketParser {

    private static final Logger LOG = Logger.getLogger(XfaPacketParser.class.getName());

    /** The XDP namespace URI used by Adobe XFA. */
    private static final String XDP_NAMESPACE = "http://ns.adobe.com/xdp/";

    /** Parsed packets keyed by name, preserving insertion order. */
    private final Map<String, Document> packets;

    /** The assembled XDP document containing all packets. */
    private Document xdpDocument;

    /**
     * Parses the /XFA entry from an AcroForm dictionary.
     * <p>
     * The entry may be either a {@link COSStream} containing a complete XDP document,
     * or a {@link COSArray} of interleaved name/stream pairs.
     * </p>
     *
     * @param xfaEntry the resolved /XFA value (COSArray or COSStream)
     * @throws IOException              if reading or parsing stream data fails
     * @throws IllegalArgumentException if xfaEntry is null or not a COSArray/COSStream
     */
    public XfaPacketParser(COSBase xfaEntry) throws IOException {
        if (xfaEntry == null) {
            throw new IllegalArgumentException("XFA entry must not be null");
        }

        this.packets = new LinkedHashMap<>();
        COSBase resolved = resolveRef(xfaEntry);

        if (resolved instanceof COSStream) {
            parseFromStream((COSStream) resolved);
        } else if (resolved instanceof COSArray) {
            parseFromArray((COSArray) resolved);
        } else {
            throw new IllegalArgumentException(
                    "XFA entry must be a COSStream or COSArray, got: " + resolved.getClass().getSimpleName());
        }
    }

    /**
     * Returns a specific packet by name.
     *
     * @param name the packet name (e.g. "template", "datasets", "config", "localeSet", "form")
     * @return the parsed XML document for the packet, or null if not found
     */
    public Document getPacket(String name) {
        if (name == null) {
            return null;
        }
        return packets.get(name);
    }

    /**
     * Returns the assembled XDP document containing all packets.
     * <p>
     * When the source was a single COSStream, this is the parsed XDP document directly.
     * When the source was a COSArray, this is a synthesized {@code <xdp:xdp>} document
     * with all packet root elements imported as children.
     * </p>
     *
     * @return the XDP document, or null if parsing failed
     */
    public Document getXDP() {
        return xdpDocument;
    }

    /**
     * Returns all parsed packets as an unmodifiable-order map.
     * <p>
     * The map preserves the order in which packets appeared in the XFA entry.
     * Keys are packet names; values are parsed DOM documents.
     * </p>
     *
     * @return a map of packet name to parsed XML document
     */
    public Map<String, Document> getAllPackets() {
        return new LinkedHashMap<>(packets);
    }

    /**
     * Writes modified packet DOMs back to the COS structures.
     * <p>
     * For a COSArray source, each stream in the name/stream pairs is updated with the
     * serialized content of the corresponding packet. For a COSStream source, the entire
     * XDP document is serialized and written back.
     * </p>
     *
     * @param xfaEntry the original /XFA COSBase (COSArray or COSStream)
     * @throws IOException if serialization or writing fails
     */
    public void writeBack(COSBase xfaEntry) throws IOException {
        if (xfaEntry == null) {
            throw new IllegalArgumentException("XFA entry must not be null");
        }

        COSBase resolved = resolveRef(xfaEntry);

        if (resolved instanceof COSStream) {
            writeBackToStream((COSStream) resolved);
        } else if (resolved instanceof COSArray) {
            writeBackToArray((COSArray) resolved);
        } else {
            throw new IllegalArgumentException(
                    "XFA entry must be a COSStream or COSArray, got: " + resolved.getClass().getSimpleName());
        }
    }

    // -----------------------------------------------------------------------
    // Parsing from COSStream (single XDP document)
    // -----------------------------------------------------------------------

    /**
     * Parses a single COSStream containing a complete XDP document.
     * Walks child elements to extract individual packets by local name.
     */
    private void parseFromStream(COSStream stream) throws IOException {
        byte[] data = stream.getDecodedData();
        if (data == null || data.length == 0) {
            LOG.warning("XFA stream contains no data");
            return;
        }

        Document doc = parseXml(data);
        if (doc == null) {
            LOG.warning("Failed to parse XFA stream as XML");
            return;
        }

        this.xdpDocument = doc;

        // Walk child elements of the root (expected to be <xdp:xdp>)
        Element root = doc.getDocumentElement();
        if (root == null) {
            return;
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String localName = child.getLocalName();
            if (localName == null) {
                localName = child.getNodeName();
            }

            // Create a standalone Document for this packet element
            Document packetDoc = createDocumentBuilder().newDocument();
            Node imported = packetDoc.importNode(child, true);
            packetDoc.appendChild(imported);
            packets.put(localName, packetDoc);

            LOG.fine(() -> "Extracted packet from XDP stream: " + child.getLocalName());
        }
    }

    // -----------------------------------------------------------------------
    // Parsing from COSArray (interleaved name/stream pairs)
    // -----------------------------------------------------------------------

    /**
     * Parses a COSArray of interleaved name/stream pairs.
     * Format: [name0, stream0, name1, stream1, ...]
     * Builds a synthetic XDP document from the individual packets.
     */
    private void parseFromArray(COSArray array) throws IOException {
        if (array.size() < 2) {
            LOG.warning("XFA array has fewer than 2 elements, cannot form name/stream pairs");
            return;
        }

        for (int i = 0; i + 1 < array.size(); i += 2) {
            COSBase nameObj = resolveRef(array.get(i));
            COSBase streamObj = resolveRef(array.get(i + 1));

            // Extract packet name
            String packetName;
            if (nameObj instanceof COSString) {
                packetName = ((COSString) nameObj).getString();
            } else if (nameObj instanceof COSName) {
                packetName = ((COSName) nameObj).getName();
            } else {
                LOG.warning("XFA array element at index " + i
                        + " is not a COSString or COSName, skipping pair");
                continue;
            }

            // Extract and parse stream data
            if (!(streamObj instanceof COSStream)) {
                LOG.warning("XFA array element at index " + (i + 1) + " is not a COSStream, skipping pair");
                continue;
            }

            byte[] data = ((COSStream) streamObj).getDecodedData();
            if (data == null || data.length == 0) {
                LOG.fine("XFA packet '" + packetName + "' stream is empty");
                continue;
            }

            Document packetDoc = parseXml(data);
            if (packetDoc == null) {
                // Handle preamble/postamble or malformed XML fragments by wrapping
                packetDoc = wrapInDummyElement(data, packetName);
            }

            if (packetDoc != null) {
                packets.put(packetName, packetDoc);
                LOG.fine("Parsed XFA packet from array: " + packetName);
            }
        }

        // Build synthetic XDP document from all packets
        buildXdpFromPackets();
    }

    /**
     * Wraps raw bytes that failed XML parsing in a dummy root element.
     * This handles "preamble" and "postamble" entries which may contain
     * XML fragments rather than well-formed documents.
     *
     * @param data       the raw bytes
     * @param packetName the packet name (for the wrapper element)
     * @return a Document wrapping the content, or null if wrapping also fails
     */
    private Document wrapInDummyElement(byte[] data, String packetName) {
        String content = new String(data, StandardCharsets.UTF_8);
        String wrapped = "<" + sanitizeElementName(packetName) + ">" + content
                + "</" + sanitizeElementName(packetName) + ">";
        try {
            return parseXml(wrapped.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to wrap XFA packet '" + packetName + "' in dummy element", e);
            // Last resort: create an empty document with just the element
            try {
                Document doc = createDocumentBuilder().newDocument();
                Element elem = doc.createElement(sanitizeElementName(packetName));
                elem.setTextContent(content);
                doc.appendChild(elem);
                return doc;
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to create fallback document for packet '" + packetName + "'", ex);
                return null;
            }
        }
    }

    /**
     * Sanitizes a string for use as an XML element name.
     * Replaces characters invalid in element names with underscores.
     */
    private static String sanitizeElementName(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (Character.isLetter(c) || c == '_') {
                    sb.append(c);
                } else {
                    sb.append('_');
                }
            } else {
                if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
                    sb.append(c);
                } else {
                    sb.append('_');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Builds a synthetic XDP document from all parsed packets.
     * Creates a root {@code <xdp:xdp>} element and imports each packet's
     * root element as a child.
     */
    private void buildXdpFromPackets() {
        try {
            DocumentBuilder builder = createDocumentBuilder();
            xdpDocument = builder.newDocument();

            Element root = xdpDocument.createElementNS(XDP_NAMESPACE, "xdp:xdp");
            root.setAttribute("xmlns:xdp", XDP_NAMESPACE);
            xdpDocument.appendChild(root);

            for (Map.Entry<String, Document> entry : packets.entrySet()) {
                Document packetDoc = entry.getValue();
                Element packetRoot = packetDoc.getDocumentElement();
                if (packetRoot != null) {
                    Node imported = xdpDocument.importNode(packetRoot, true);
                    root.appendChild(imported);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to build synthetic XDP document", e);
            xdpDocument = null;
        }
    }

    // -----------------------------------------------------------------------
    // Write-back
    // -----------------------------------------------------------------------

    /**
     * Serializes the entire XDP document and writes it back to a single COSStream.
     */
    private void writeBackToStream(COSStream stream) throws IOException {
        if (xdpDocument == null) {
            LOG.warning("No XDP document to write back");
            return;
        }

        byte[] serialized = serializeDocument(xdpDocument);
        stream.setDecodedData(serialized);
    }

    /**
     * Iterates the COSArray name/stream pairs and writes each modified packet
     * back to its corresponding COSStream.
     */
    private void writeBackToArray(COSArray array) throws IOException {
        for (int i = 0; i + 1 < array.size(); i += 2) {
            COSBase nameObj = resolveRef(array.get(i));
            COSBase streamObj = resolveRef(array.get(i + 1));

            if (!(streamObj instanceof COSStream)) {
                continue;
            }

            String packetName;
            if (nameObj instanceof COSString) {
                packetName = ((COSString) nameObj).getString();
            } else if (nameObj instanceof COSName) {
                packetName = ((COSName) nameObj).getName();
            } else {
                continue;
            }
            Document packetDoc = packets.get(packetName);
            if (packetDoc == null) {
                continue;
            }

            byte[] serialized = serializeDocument(packetDoc);
            ((COSStream) streamObj).setDecodedData(serialized);

            LOG.fine("Wrote back XFA packet: " + packetName);
        }
    }

    // -----------------------------------------------------------------------
    // XML utilities
    // -----------------------------------------------------------------------

    /**
     * Parses raw bytes as an XML document with namespace awareness and
     * external entity protection.
     *
     * @param data the XML bytes
     * @return the parsed Document, or null if parsing fails
     */
    private Document parseXml(byte[] data) {
        try {
            DocumentBuilder builder = createDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(data));
        } catch (SAXException | IOException e) {
            LOG.log(Level.FINE, "XML parsing failed", e);
            return null;
        }
    }

    /**
     * Creates a namespace-aware DocumentBuilder with external entity protection.
     *
     * @return a configured DocumentBuilder
     */
    private static DocumentBuilder createDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            // Security: disable external entities to prevent XXE attacks
            try {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (ParserConfigurationException e) {
                // Feature not supported by this XML implementation; ignore
            }
            try {
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            } catch (ParserConfigurationException e) {
                // Feature not supported; ignore
            }

            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to create XML DocumentBuilder", e);
        }
    }

    /**
     * Serializes a DOM Document to a byte array using a Transformer.
     * The XML declaration is omitted from the output.
     *
     * @param doc the document to serialize
     * @return the serialized bytes in UTF-8
     * @throws IOException if serialization fails
     */
    private static byte[] serializeDocument(Document doc) throws IOException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));
            return baos.toByteArray();
        } catch (TransformerException e) {
            throw new IOException("Failed to serialize XML document", e);
        }
    }

    /**
     * Resolves COSObjectReference chains to obtain the underlying COS object.
     * Follows references until a non-reference object is found.
     *
     * @param val the value to resolve
     * @return the resolved value (never a COSObjectReference)
     * @throws IOException if dereferencing fails
     */
    private static COSBase resolveRef(COSBase val) throws IOException {
        while (val instanceof COSObjectReference) {
            val = ((COSObjectReference) val).dereference();
        }
        return val;
    }
}
