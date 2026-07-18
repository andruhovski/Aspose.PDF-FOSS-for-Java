package org.aspose.pdf.engine.xfa.packet;

import org.aspose.pdf.engine.pdfobjects.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Reads the AcroForm `/XFA` entry into a typed [XfaPacketSet].
///
/// The `/XFA` value is either a single [PdfStream] holding a
/// complete XDP document, or a [PdfArray] of interleaved name/stream pairs
/// (ISO 32000-1 sec 12.7.8). This reader handles both, preserving each packet's
/// source bytes (array form) and assembling an XDP document. The packet-splitting
/// behaviour is identical to the former `forms.xfa.XfaPacketParser`.
public final class XfaPacketReader {

    private static final Logger LOG = Logger.getLogger(XfaPacketReader.class.getName());
    private static final String XDP_NAMESPACE = "http://ns.adobe.com/xdp/";

    private XfaPacketReader() { }

    /// Reads and splits the `/XFA` entry.
    ///
    /// @param xfaEntry the resolved `/XFA` value (PdfArray or PdfStream)
    /// @return the parsed packet set
    /// @throws IOException              if reading/parsing fails
    /// @throws IllegalArgumentException if `xfaEntry` is null or an unsupported type
    public static XfaPacketSet read(PdfBase xfaEntry) throws IOException {
        if (xfaEntry == null) {
            throw new IllegalArgumentException("XFA entry must not be null");
        }
        XfaPacketSet set = new XfaPacketSet();
        PdfBase resolved = resolveRef(xfaEntry);
        if (resolved instanceof PdfStream) {
            readFromStream((PdfStream) resolved, set);
        } else if (resolved instanceof PdfArray) {
            readFromArray((PdfArray) resolved, set);
        } else {
            throw new IllegalArgumentException(
                    "XFA entry must be a PdfStream or PdfArray, got: " + resolved.getClass().getSimpleName());
        }
        return set;
    }

    /* ------------------------- single XDP stream ---------------------- */

    private static void readFromStream(PdfStream stream, XfaPacketSet set) throws IOException {
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
        set.setXdp(doc);

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
            Document packetDoc = newDocument();
            Node imported = packetDoc.importNode(child, true);
            packetDoc.appendChild(imported);
            set.put(new XfaPacket(localName, packetDoc, null));
        }
    }

    /* --------------------- interleaved name/stream array -------------- */

    private static void readFromArray(PdfArray array, XfaPacketSet set) throws IOException {
        if (array.size() < 2) {
            LOG.warning("XFA array has fewer than 2 elements, cannot form name/stream pairs");
            return;
        }
        for (int i = 0; i + 1 < array.size(); i += 2) {
            PdfBase nameObj = resolveRef(array.get(i));
            PdfBase streamObj = resolveRef(array.get(i + 1));

            String packetName;
            if (nameObj instanceof PdfString) {
                packetName = ((PdfString) nameObj).getString();
            } else if (nameObj instanceof PdfName) {
                packetName = ((PdfName) nameObj).getName();
            } else {
                LOG.warning("XFA array element at index " + i + " is not a name, skipping pair");
                continue;
            }
            if (!(streamObj instanceof PdfStream)) {
                LOG.warning("XFA array element at index " + (i + 1) + " is not a stream, skipping pair");
                continue;
            }
            byte[] data = ((PdfStream) streamObj).getDecodedData();
            if (data == null || data.length == 0) {
                LOG.fine("XFA packet '" + packetName + "' stream is empty");
                continue;
            }
            Document packetDoc = parseXml(data);
            if (packetDoc == null) {
                packetDoc = wrapInDummyElement(data, packetName);
            }
            if (packetDoc != null) {
                set.put(new XfaPacket(packetName, packetDoc, data));
            }
        }
        set.setXdp(buildXdpFromPackets(set));
    }

    private static Document wrapInDummyElement(byte[] data, String packetName) {
        String content = new String(data, StandardCharsets.UTF_8);
        String tag = sanitizeElementName(packetName);
        String wrapped = "<" + tag + ">" + content + "</" + tag + ">";
        try {
            return parseXml(wrapped.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to wrap XFA packet '" + packetName + "'", e);
            try {
                Document doc = newDocument();
                Element elem = doc.createElement(tag);
                elem.setTextContent(content);
                doc.appendChild(elem);
                return doc;
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Fallback document failed for '" + packetName + "'", ex);
                return null;
            }
        }
    }

    private static String sanitizeElementName(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                sb.append(Character.isLetter(c) || c == '_' ? c : '_');
            } else {
                sb.append(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' ? c : '_');
            }
        }
        return sb.toString();
    }

    private static Document buildXdpFromPackets(XfaPacketSet set) {
        try {
            Document xdp = newDocument();
            Element root = xdp.createElementNS(XDP_NAMESPACE, "xdp:xdp");
            root.setAttribute("xmlns:xdp", XDP_NAMESPACE);
            xdp.appendChild(root);
            for (XfaPacket p : set.all()) {
                Document packetDoc = p.getDocument();
                if (packetDoc != null && packetDoc.getDocumentElement() != null) {
                    root.appendChild(xdp.importNode(packetDoc.getDocumentElement(), true));
                }
            }
            return xdp;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to build synthetic XDP document", e);
            return null;
        }
    }

    /* ------------------------------ XML utils ------------------------- */

    static Document parseXml(byte[] data) {
        try {
            return newBuilder().parse(new ByteArrayInputStream(data));
        } catch (SAXException | IOException e) {
            LOG.log(Level.FINE, "XML parsing failed", e);
            return null;
        }
    }

    static Document newDocument() {
        return newBuilder().newDocument();
    }

    static DocumentBuilder newBuilder() {
        // shared XXE-hardened factory (disallow DOCTYPE, no external entities/XInclude)
        return org.aspose.pdf.engine.xml.SecureXml.newBuilder(true);
    }

    static PdfBase resolveRef(PdfBase val) throws IOException {
        while (val instanceof PdfObjectReference) {
            val = ((PdfObjectReference) val).dereference();
        }
        return val;
    }
}
