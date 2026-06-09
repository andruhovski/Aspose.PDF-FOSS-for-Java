package org.aspose.pdf.forms.xfa;

import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents an XFA (XML Forms Architecture) form embedded in a PDF document.
 * <p>
 * XFA forms store their field definitions and data as XML packets inside the
 * PDF's /AcroForm dictionary under the /XFA entry. This class provides access
 * to XFA field values, field names, template XML, datasets XML, and other
 * XFA packets.
 * </p>
 * <p>
 * Field names use a dotted SOM (Scripting Object Model) syntax, for example:
 * {@code "form1.Page1.TextField1"} or {@code "form1[0].Page1[0].TextField1[0]"}.
 * Both forms are supported interchangeably.
 * </p>
 *
 * @see Form#getXFA()
 */
public class XfaForm {

    private static final Logger LOG = Logger.getLogger(XfaForm.class.getName());

    private final XfaPacketParser parser;
    private final XfaNamespaceContext nsContext;
    private final PdfBase xfaEntry;

    /**
     * Creates an XfaForm from the AcroForm dictionary.
     *
     * @param acroFormDict the AcroForm dictionary containing an /XFA entry
     * @throws IOException if the XFA packets cannot be parsed
     */
    public XfaForm(PdfDictionary acroFormDict) throws IOException {
        PdfBase xfa = resolveRef(acroFormDict.get("XFA"));
        if (xfa == null) {
            throw new IllegalArgumentException("AcroForm dictionary does not contain an /XFA entry");
        }
        this.xfaEntry = xfa;
        this.parser = new XfaPacketParser(xfa);

        org.w3c.dom.Document tpl = parser.getPacket("template");
        org.w3c.dom.Document ds = parser.getPacket("datasets");
        this.nsContext = new XfaNamespaceContext(tpl, ds);
    }

    // ── XML Document Access ──

    /**
     * Returns the XFA template packet as a DOM Document.
     *
     * @return the template document, or null if not present
     */
    public org.w3c.dom.Document getTemplate() {
        return parser.getPacket("template");
    }

    /**
     * Returns the XFA datasets packet as a DOM Document.
     *
     * @return the datasets document, or null if not present
     */
    public org.w3c.dom.Document getDatasets() {
        return parser.getPacket("datasets");
    }

    /**
     * Returns the XFA config packet as a DOM Document.
     *
     * @return the config document, or null if not present
     */
    public org.w3c.dom.Document getConfig() {
        return parser.getPacket("config");
    }

    /**
     * Returns the assembled XDP document containing all packets.
     *
     * @return the full XDP document
     */
    public org.w3c.dom.Document getXDP() {
        return parser.getXDP();
    }

    /**
     * Returns the XFA form packet as a DOM Document (runtime form DOM).
     *
     * @return the form document, or null if not present
     */
    public org.w3c.dom.Document getForm() {
        return parser.getPacket("form");
    }

    /**
     * Returns the namespace context for XPath queries over XFA XML.
     *
     * @return the namespace context
     */
    public XfaNamespaceContext getNamespaceManager() {
        return nsContext;
    }

    // ── Field Value Access ──

    /**
     * Gets the value of an XFA field by its SOM-like dotted path.
     * <p>
     * Both indexed ({@code "form1[0].Page1[0].TextField1[0]"}) and
     * unindexed ({@code "form1.Page1.TextField1"}) forms are supported.
     * </p>
     *
     * @param fieldName the dotted field path
     * @return the field value as a string, or null if not found
     */
    public String get(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return null;

        org.w3c.dom.Document datasets = getDatasets();
        if (datasets == null) return null;

        org.w3c.dom.Element dataRoot = findDataRoot(datasets);
        if (dataRoot == null) return null;

        SomSegment[] segments = parseSomExpression(fieldName);
        org.w3c.dom.Element current = dataRoot;

        // Navigate from the data root's first child (the root data group)
        // The first segment usually matches the root data group name
        for (int i = 0; i < segments.length; i++) {
            SomSegment seg = segments[i];

            // Skip #subform segments in data navigation
            if (seg.name.startsWith("#")) continue;

            org.w3c.dom.Element child = findChildElement(current, seg.name, seg.index);
            if (child == null) return null;
            current = child;
        }

        return current.getTextContent();
    }

    /**
     * Sets the value of an XFA field by its SOM-like dotted path.
     * Creates intermediate elements as needed.
     *
     * @param fieldName the dotted field path
     * @param value     the value to set
     * @throws IOException if writing the modified datasets back to COS fails
     */
    public void set(String fieldName, String value) throws IOException {
        if (fieldName == null || fieldName.isEmpty()) return;

        org.w3c.dom.Document datasets = getDatasets();
        if (datasets == null) return;

        org.w3c.dom.Element dataRoot = findDataRoot(datasets);
        if (dataRoot == null) return;

        SomSegment[] segments = parseSomExpression(fieldName);
        org.w3c.dom.Element current = dataRoot;

        for (int i = 0; i < segments.length; i++) {
            SomSegment seg = segments[i];
            if (seg.name.startsWith("#")) continue;

            org.w3c.dom.Element child = findChildElement(current, seg.name, seg.index);
            if (child == null) {
                // Create missing intermediate/leaf elements
                child = datasets.createElement(seg.name);
                current.appendChild(child);
            }
            current = child;
        }

        // Remove existing child nodes and set text
        while (current.getFirstChild() != null) {
            current.removeChild(current.getFirstChild());
        }
        current.appendChild(datasets.createTextNode(value != null ? value : ""));

        // Write back to COS
        parser.writeBack(xfaEntry);
    }

    // ── Field Names ──

    /**
     * Returns all field names from the XFA template.
     * Names are in the dotted form without indices (e.g., {@code "form1.Page1.TextField1"}).
     *
     * @return array of all field names, or empty array if template is not available
     */
    public String[] getFieldNames() {
        org.w3c.dom.Document template = getTemplate();
        if (template == null) return new String[0];

        List<String> names = new ArrayList<>();
        org.w3c.dom.Element root = template.getDocumentElement();
        if (root == null) return new String[0];

        // Find root subform (first <subform> child of <template>)
        org.w3c.dom.Element rootSubform = findFirstChildByLocalName(root, "subform");
        if (rootSubform == null) return new String[0];

        String rootName = rootSubform.getAttribute("name");
        String prefix = (rootName != null && !rootName.isEmpty()) ? rootName : "";

        collectFieldNames(rootSubform, prefix, names);
        return names.toArray(new String[0]);
    }

    // ── Template Access ──

    /**
     * Returns the template node for a specific field.
     *
     * @param fieldName the dotted field path
     * @return the template {@code <field>} element, or null if not found
     */
    public org.w3c.dom.Node getFieldTemplate(String fieldName) {
        org.w3c.dom.Document template = getTemplate();
        if (template == null || fieldName == null) return null;

        SomSegment[] segments = parseSomExpression(fieldName);
        if (segments.length == 0) return null;

        org.w3c.dom.Element root = template.getDocumentElement();
        if (root == null) return null;

        // Start from root <subform> child of <template>
        org.w3c.dom.Element current = findFirstChildByLocalName(root, "subform");
        if (current == null) return null;

        // First segment should match root subform name
        String rootName = current.getAttribute("name");
        int startIdx = 0;
        if (segments.length > 0 && rootName != null && rootName.equals(segments[0].name)) {
            startIdx = 1;
        }

        for (int i = startIdx; i < segments.length; i++) {
            SomSegment seg = segments[i];

            if (i == segments.length - 1) {
                // Last segment — look for <field>
                org.w3c.dom.Element field = findTemplateChild(current, "field", seg);
                if (field != null) return field;
                // Also check for <exclGroup>
                org.w3c.dom.Element exclGroup = findTemplateChild(current, "exclGroup", seg);
                if (exclGroup != null) return exclGroup;
                return null;
            } else {
                // Intermediate segment — look for <subform>
                org.w3c.dom.Element subform = findTemplateChild(current, "subform", seg);
                if (subform == null) return null;
                current = subform;
            }
        }
        return null;
    }

    // ── Image Fields ──

    /**
     * Sets an image value for an XFA image field.
     * The image data is Base64-encoded and stored in the datasets XML.
     *
     * @param fieldName   the dotted field path
     * @param imageStream the image data stream
     * @throws IOException if the image cannot be read
     */
    public void setFieldImage(String fieldName, InputStream imageStream) throws IOException {
        if (fieldName == null || imageStream == null) return;

        byte[] imageBytes = readAllBytes(imageStream);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        set(fieldName, base64);
    }

    // ── Internal Helpers ──

    /**
     * Finds the {@code <xfa:data>} element in the datasets document.
     * Returns the {@code <xfa:data>} element itself (not its child), so that
     * the first SOM segment can match the root data group by name.
     */
    private org.w3c.dom.Element findDataRoot(org.w3c.dom.Document datasets) {
        org.w3c.dom.Element root = datasets.getDocumentElement();
        if (root == null) return null;

        // Look for <xfa:data> child — return it so SOM navigation starts here
        org.w3c.dom.NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "data".equals(child.getLocalName())) {
                return (org.w3c.dom.Element) child;
            }
        }

        // If root is itself the data container (some PDFs structure differently)
        return root;
    }

    /**
     * Finds the N-th child element with the given local name.
     */
    private org.w3c.dom.Element findChildElement(org.w3c.dom.Element parent, String localName, int index) {
        int count = 0;
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && matchesName(child, localName)) {
                if (count == index) return (org.w3c.dom.Element) child;
                count++;
            }
        }
        return null;
    }

    /**
     * Checks if a node matches the given name by local name or node name.
     */
    private boolean matchesName(org.w3c.dom.Node node, String name) {
        String ln = node.getLocalName();
        if (ln != null && ln.equals(name)) return true;
        String nn = node.getNodeName();
        return nn != null && nn.equals(name);
    }

    /**
     * Finds a template child element (field or subform) matching a SOM segment.
     */
    private org.w3c.dom.Element findTemplateChild(org.w3c.dom.Element parent,
                                                   String elementType, SomSegment seg) {
        int count = 0;
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            org.w3c.dom.Element el = (org.w3c.dom.Element) child;

            if (!elementType.equals(el.getLocalName())) continue;

            if (seg.name.startsWith("#")) {
                // Class reference — match unnamed elements by tag
                String name = el.getAttribute("name");
                if (name == null || name.isEmpty()) {
                    if (count == seg.index) return el;
                    count++;
                }
            } else {
                String name = el.getAttribute("name");
                if (seg.name.equals(name)) {
                    if (count == seg.index) return el;
                    count++;
                }
            }
        }
        return null;
    }

    /**
     * Finds the first child element with a given local name.
     */
    private org.w3c.dom.Element findFirstChildByLocalName(org.w3c.dom.Element parent, String localName) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && localName.equals(child.getLocalName())) {
                return (org.w3c.dom.Element) child;
            }
        }
        return null;
    }

    /**
     * Recursively collects field names from the template DOM.
     */
    private void collectFieldNames(org.w3c.dom.Element node, String prefix, List<String> names) {
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            org.w3c.dom.Element el = (org.w3c.dom.Element) child;

            String localName = el.getLocalName();
            String nameAttr = el.getAttribute("name");

            if ("field".equals(localName) || "exclGroup".equals(localName)) {
                if (nameAttr != null && !nameAttr.isEmpty()) {
                    String path = prefix.isEmpty() ? nameAttr : prefix + "." + nameAttr;
                    names.add(path);
                }
            } else if ("subform".equals(localName)) {
                String subPrefix;
                if (nameAttr != null && !nameAttr.isEmpty()) {
                    subPrefix = prefix.isEmpty() ? nameAttr : prefix + "." + nameAttr;
                } else {
                    // Unnamed/transparent subform — don't add to path
                    subPrefix = prefix;
                }
                collectFieldNames(el, subPrefix, names);
            }
        }
    }

    /**
     * Parses a SOM expression like "form1[0].Page1[0].TextField1[0]" into segments.
     */
    static SomSegment[] parseSomExpression(String expression) {
        if (expression == null || expression.isEmpty()) return new SomSegment[0];

        String[] parts = expression.split("\\.");
        SomSegment[] segments = new SomSegment[parts.length];
        for (int i = 0; i < parts.length; i++) {
            segments[i] = SomSegment.parse(parts[i]);
        }
        return segments;
    }

    /**
     * Represents a single segment of a SOM expression (e.g., "TextField1[0]").
     */
    static class SomSegment {
        final String name;
        final int index;

        SomSegment(String name, int index) {
            this.name = name;
            this.index = index;
        }

        static SomSegment parse(String segment) {
            int bracketPos = segment.indexOf('[');
            if (bracketPos < 0) {
                return new SomSegment(segment, 0);
            }
            String name = segment.substring(0, bracketPos);
            int endBracket = segment.indexOf(']', bracketPos);
            if (endBracket < 0) endBracket = segment.length();
            int index = 0;
            try {
                index = Integer.parseInt(segment.substring(bracketPos + 1, endBracket));
            } catch (NumberFormatException e) {
                // default to 0
            }
            return new SomSegment(name, index);
        }
    }

    private static PdfBase resolveRef(PdfBase val) {
        while (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }
}
