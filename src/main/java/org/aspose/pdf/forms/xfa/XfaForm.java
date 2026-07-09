package org.aspose.pdf.forms.xfa;

import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.engine.xfa.packet.XfaPacketReader;
import org.aspose.pdf.engine.xfa.packet.XfaPacketSet;
import org.aspose.pdf.engine.xfa.packet.XfaPacketWriter;

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

    private final XfaPacketSet packetSet;
    private final XfaNamespaceContext nsContext;
    private final PdfBase xfaEntry;
    private final PdfDictionary acroFormDict;

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
        this.acroFormDict = acroFormDict;
        this.packetSet = XfaPacketReader.read(xfa);

        org.w3c.dom.Document tpl = packetSet.getDocument("template");
        org.w3c.dom.Document ds = packetSet.getDocument("datasets");
        this.nsContext = new XfaNamespaceContext(tpl, ds);
    }

    // ── XML Document Access ──

    /**
     * Returns the XFA template packet as a DOM Document.
     *
     * @return the template document, or null if not present
     */
    public org.w3c.dom.Document getTemplate() {
        return packetSet.getDocument("template");
    }

    /**
     * Returns the XFA datasets packet as a DOM Document.
     *
     * @return the datasets document, or null if not present
     */
    public org.w3c.dom.Document getDatasets() {
        return packetSet.getDocument("datasets");
    }

    /**
     * @return {@code true} if any XFA packet has been mutated since load (e.g. via {@link #set}).
     * The save path uses this to force a full rewrite over cross-reference-stream sources, where an
     * incremental append of a modified stream is not reliably resolved on reload (BUG-TFA-REPLACE-001).
     */
    public boolean hasDirtyPackets() {
        for (org.aspose.pdf.engine.xfa.packet.XfaPacket p : packetSet.all()) {
            if (p.isDirty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the XFA config packet as a DOM Document.
     *
     * @return the config document, or null if not present
     */
    public org.w3c.dom.Document getConfig() {
        return packetSet.getDocument("config");
    }

    /**
     * Returns the assembled XDP document containing all packets.
     *
     * @return the full XDP document
     */
    public org.w3c.dom.Document getXDP() {
        return packetSet.getXdp();
    }

    /**
     * Returns the XFA form packet as a DOM Document (runtime form DOM).
     *
     * @return the form document, or null if not present
     */
    public org.w3c.dom.Document getForm() {
        return packetSet.getDocument("form");
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
        org.w3c.dom.Element node = resolveDataNode(dataRoot, segments);
        return node == null ? null : node.getTextContent();
    }

    /**
     * Locates the datasets element bound to the field named by {@code segments}, or null.
     *
     * <p>The SOM expression follows the TEMPLATE hierarchy, but XFA datasets are sparse:
     * layout-only container subforms that bind no data produce no data node (ISO 32000 /
     * XFA 3.0 §"Data Binding"). A field {@code us-request.Page1.sfHeader.docket} is therefore
     * stored simply as {@code us-request/docket}. Navigate by matching each segment as a direct
     * child when present, skipping intermediate containers that are absent from the data; the
     * leaf segment must resolve (direct child, then {@code <bind ref>} name, then descendant) or
     * there is no data node.</p>
     *
     * <p>Shared by {@link #get} and {@link #set} so both act on the SAME node the renderer
     * binds to — a value written by {@code set} must land on the real (possibly pre-existing)
     * data node, never a parallel branch that would stay invisible.</p>
     */
    private org.w3c.dom.Element resolveDataNode(org.w3c.dom.Element dataRoot, SomSegment[] segments) {
        int leafIdx = -1;
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].name.startsWith("#")) { leafIdx = i; break; }
        }
        if (leafIdx < 0) return null;

        org.w3c.dom.Element current = dataRoot;
        for (int i = 0; i < segments.length; i++) {
            SomSegment seg = segments[i];
            if (seg.name.startsWith("#")) continue;

            org.w3c.dom.Element child = findChildElement(current, seg.name, seg.index);
            if (child == null) {
                // The data node may be named differently from its SOM name at ANY level
                // (XFA template <bind ref>): a repeated row subform "body" typically binds
                // ref="$.IM_ITEMS.DATA[*]" — renamed groups nested below the current scope.
                // Resolve the segment's bound data path via the template so indexed row
                // navigation ("body[1]") selects the real second row group, not a
                // sparse-skip collapse.
                child = resolveBoundChild(current, segments, i, seg.index);
            }
            if (child != null) {
                current = child;
            } else if (i == leafIdx) {
                // Last resort for the leaf: descendant search by the SOM name.
                child = findDescendantElement(current, seg.name, seg.index);
                if (child == null) return null;
                current = child;
            } else if (seg.index > 0) {
                // An explicitly indexed instance ("row[2]") that is absent must NOT collapse
                // onto another instance: skipping the segment would resolve every row to the
                // same node (all repeated-row reads/writes landing on row 0). No such
                // instance — no data node.
                return null;
            }
            // else: container absent from the sparse data — skip and keep matching.
        }

        if (current == dataRoot) return null;
        return current;
    }

    /**
     * Resolves the data path the template node addressed by {@code segments[0..upTo]} binds
     * to, by walking the XFA template and reading its {@code <bind ref="...">}. A SOM name
     * can differ from the data it stores into at any level: a field "Naam" with
     * {@code <bind ref="$.strNom">} reads/writes strNom; a repeated row subform "body" may
     * bind {@code ref="$.IM_ITEMS.DATA[*]"} — renamed groups NESTED below the current data
     * scope. Returns the bound path components (predicates and the leading {@code $} scope
     * marker stripped), or null if it cannot be determined.
     */
    private String[] templateBindDataPath(SomSegment[] segments, int upTo) {
        org.w3c.dom.Document template = getTemplate();
        if (template == null) return null;
        org.w3c.dom.Element current = template.getDocumentElement();
        if (current == null) return null;
        for (int i = 0; i <= upTo && i < segments.length; i++) {
            SomSegment seg = segments[i];
            if (seg.name.startsWith("#")) continue;
            org.w3c.dom.Element next = findChildByNameAttr(current, seg.name);
            if (next == null) return null;
            current = next;
        }
        org.w3c.dom.NodeList children = current.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && "bind".equals(n.getLocalName())) {
                String ref = ((org.w3c.dom.Element) n).getAttribute("ref");
                if (ref == null || ref.isEmpty()) return null;
                List<String> comps = new ArrayList<>();
                for (String raw : ref.split("\\.")) {
                    String c = raw.trim();
                    int bracket = c.indexOf('['); // strip SOM predicate: "DATA[*]" -> "DATA"
                    if (bracket >= 0) c = c.substring(0, bracket);
                    if (c.isEmpty()) continue;
                    if (c.startsWith("$") || c.startsWith("!")) {
                        // leading scope marker ($, $record, $data) is the current scope;
                        // an EMBEDDED one is a SOM function we do not support here.
                        if (comps.isEmpty()) continue;
                        return null;
                    }
                    comps.add(c);
                }
                return comps.isEmpty() ? null : comps.toArray(new String[0]);
            }
        }
        return null;
    }

    /**
     * Navigates from {@code start} along the template-bound data path of
     * {@code segments[upTo]} (all components; {@code index} selects the instance of the
     * LAST component). Returns the resolved element or null.
     */
    private org.w3c.dom.Element resolveBoundChild(org.w3c.dom.Element start,
                                                  SomSegment[] segments, int upTo, int index) {
        String[] comps = templateBindDataPath(segments, upTo);
        if (comps == null) return null;
        org.w3c.dom.Element cur = start;
        for (int i = 0; i < comps.length - 1; i++) {
            cur = findChildElement(cur, comps[i], 0);
            if (cur == null) return null;
        }
        return findChildElement(cur, comps[comps.length - 1], index);
    }

    /**
     * Navigates/creates the bound data path {@code comps} below {@code start}: intermediate
     * components are entered when present or created once, the LAST component is padded up to
     * {@code index + 1} instances (see {@link #ensureInstance}). Returns the {@code index}-th
     * instance of the last component.
     */
    private org.w3c.dom.Element ensureBoundChild(org.w3c.dom.Document datasets,
                                                 org.w3c.dom.Element start,
                                                 String[] comps, int index) {
        org.w3c.dom.Element cur = start;
        for (int i = 0; i < comps.length - 1; i++) {
            org.w3c.dom.Element next = findChildElement(cur, comps[i], 0);
            cur = next != null ? next : ensureInstance(datasets, cur, comps[i], 0);
        }
        return ensureInstance(datasets, cur, comps[comps.length - 1], index);
    }

    /**
     * Ensures {@code parent} has at least {@code index + 1} child elements named {@code name},
     * creating the missing instances as empty siblings inserted right after the last existing
     * one (so repeated data groups stay adjacent, the order data-driven binding consumes them
     * in). Returns the {@code index}-th instance.
     */
    private org.w3c.dom.Element ensureInstance(org.w3c.dom.Document datasets,
                                               org.w3c.dom.Element parent,
                                               String name, int index) {
        int count = 0;
        org.w3c.dom.Element last = null;
        org.w3c.dom.NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            org.w3c.dom.Node n = kids.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && matchesName((org.w3c.dom.Element) n, name)) {
                count++;
                last = (org.w3c.dom.Element) n;
            }
        }
        while (count <= index) {
            org.w3c.dom.Element inst = datasets.createElement(name);
            if (last != null && last.getNextSibling() != null) {
                parent.insertBefore(inst, last.getNextSibling());
            } else {
                parent.appendChild(inst);
            }
            last = inst;
            count++;
        }
        return findChildElement(parent, name, index);
    }

    /**
     * True when the template node addressed by {@code segments[0..i]} is authored as repeatable
     * ({@code <occur>} with {@code max="-1"}, {@code max > 1}, {@code initial > 1} or
     * {@code min > 1}) — the signal that an explicitly indexed data instance ("Zaznam[0]")
     * should exist as a real repeated data group rather than collapse into sparse-skip
     * semantics. Returns false when the template walk cannot resolve the node.
     */
    private boolean templateAllowsRepeat(SomSegment[] segments, int upTo) {
        org.w3c.dom.Document template = getTemplate();
        if (template == null) return false;
        org.w3c.dom.Element current = template.getDocumentElement();
        if (current == null) return false;
        for (int i = 0; i <= upTo; i++) {
            SomSegment seg = segments[i];
            if (seg.name.startsWith("#")) continue;
            org.w3c.dom.Element next = findChildByNameAttr(current, seg.name);
            if (next == null) return false;
            current = next;
        }
        org.w3c.dom.NodeList children = current.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE || !"occur".equals(n.getLocalName())) {
                continue;
            }
            org.w3c.dom.Element occur = (org.w3c.dom.Element) n;
            return occurAllowsRepeat(occur.getAttribute("max"))
                    || occurAllowsRepeat(occur.getAttribute("initial"))
                    || occurAllowsRepeat(occur.getAttribute("min"));
        }
        return false;
    }

    /** True when an {@code <occur>} attribute value denotes more than one instance ({@code -1} = unbounded). */
    private static boolean occurAllowsRepeat(String v) {
        if (v == null || v.isEmpty()) return false;
        try {
            int n = Integer.parseInt(v.trim());
            return n == -1 || n > 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** True if {@code el} has at least one child ELEMENT node (i.e. it is a dataGroup container,
     *  not a leaf dataValue). Used to protect containers from being overwritten by {@link #set}. */
    private static boolean hasElementChild(org.w3c.dom.Element el) {
        org.w3c.dom.NodeList kids = el.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            if (kids.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) return true;
        }
        return false;
    }

    /** Finds the first child element of {@code parent} whose {@code name} attribute
     *  equals {@code name} (XFA template nodes are keyed by their name attribute,
     *  not their element tag). */
    private org.w3c.dom.Element findChildByNameAttr(org.w3c.dom.Element parent, String name) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && name.equals(((org.w3c.dom.Element) n).getAttribute("name"))) {
                return (org.w3c.dom.Element) n;
            }
        }
        return null;
    }

    /**
     * Depth-first search for the {@code index}-th descendant element of
     * {@code parent} whose name matches {@code localName}. Used to resolve an XFA
     * field whose containing subforms were collapsed out of the sparse datasets.
     */
    private org.w3c.dom.Element findDescendantElement(org.w3c.dom.Element parent,
                                                      String localName, int index) {
        int[] remaining = {index};
        return findDescendantElement(parent, localName, remaining);
    }

    private org.w3c.dom.Element findDescendantElement(org.w3c.dom.Element parent,
                                                      String localName, int[] remaining) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node node = children.item(i);
            if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            org.w3c.dom.Element el = (org.w3c.dom.Element) node;
            if (matchesName(el, localName)) {
                if (remaining[0] == 0) return el;
                remaining[0]--;
            }
            org.w3c.dom.Element deeper = findDescendantElement(el, localName, remaining);
            if (deeper != null) return deeper;
        }
        return null;
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

        // Update the EXISTING bound data node the renderer uses — resolved exactly as get() does
        // (sparse-aware). Only when the field has NO data node at all do we build a fresh path;
        // otherwise strict segment-by-segment creation would append a parallel branch that the
        // template binds nothing to, so the written value would never render (invisible data).
        org.w3c.dom.Element current = resolveDataNode(dataRoot, segments);
        // A field value is a dataValue (leaf); never clobber a dataGroup container. A sparse leaf
        // that is absent from the data can fall back (via <bind ref>/descendant search) onto an
        // ancestor container — overwriting it would delete the whole bound subtree (e.g. a spacer
        // field "…body.space" resolving onto a table row and wiping its cells). Treat a container
        // as unresolved so the value goes to an isolated fresh path instead of destroying data.
        if (current != null && hasElementChild(current)) {
            current = null;
        }
        if (current == null) {
            // No existing data node for this field. Create ONLY the leaf, attached to the deepest
            // container along the SOM path that ACTUALLY EXISTS in the (sparse) data — exactly where
            // get() would descendant-search for it. We must not rebuild absent intermediate
            // containers: created intermediates (e.g. a nested <data>/<page2_po_item>) get matched by
            // resolveDataNode when navigating SIBLING fields, diverting them into this empty branch
            // and hiding their real data nodes (observed: a spacer "…body.space" made every sibling
            // table cell unreadable). Attaching the bare leaf to the deepest present ancestor keeps it
            // findable without shadowing.
            int leafIdx = -1;
            for (int i = segments.length - 1; i >= 0; i--) {
                if (!segments[i].name.startsWith("#")) { leafIdx = i; break; }
            }
            if (leafIdx < 0) return;
            org.w3c.dom.Element parent = dataRoot;
            for (int i = 0; i < leafIdx; i++) {
                SomSegment seg = segments[i];
                if (seg.name.startsWith("#")) continue;
                // navigate by the SOM name, then by the segment's template-bound data path
                // (renamed row groups: "body" stored as "IM_ITEMS/DATA") — same order
                // resolveDataNode uses.
                org.w3c.dom.Element child = findChildElement(parent, seg.name, seg.index);
                if (child == null) {
                    child = resolveBoundChild(parent, segments, i, seg.index);
                }
                if (child != null) { parent = child; continue; } // enter present container
                // A container the template REPEATS (occur min/max/initial > 1) that the caller
                // addresses with an explicit instance index ("Zaznam[1]") must exist as a real
                // sibling data group — otherwise every row's leaf would attach to the same
                // ancestor and the writes collapse onto one node. Create the missing instances
                // (0..index) adjacent to any existing ones — real row containers, along the
                // BOUND data path so the binding engine consumes them. Unindexed absent
                // containers are still skipped (sparse-data semantics: the leaf attaches to
                // the deepest PRESENT ancestor, see above).
                if (seg.explicit && (seg.index > 0 || templateAllowsRepeat(segments, i))) {
                    String[] comps = templateBindDataPath(segments, i);
                    if (comps == null) comps = new String[]{seg.name};
                    parent = ensureBoundChild(datasets, parent, comps, seg.index);
                }
            }
            SomSegment leafSeg = segments[leafIdx];
            String[] leafComps = templateBindDataPath(segments, leafIdx);
            if (leafComps == null) leafComps = new String[]{leafSeg.name};
            if (leafSeg.explicit && leafSeg.index > 0) {
                // repeated LEAF field ("pole[2]"): pad sibling value nodes up to the index so
                // the instance lands at its own position, not appended as a lower index.
                current = ensureBoundChild(datasets, parent, leafComps, leafSeg.index);
            } else {
                // fresh leaf (never reuse an existing same-name node here — a leaf that resolved
                // onto a dataGroup container was deliberately rejected above; see the guard)
                for (int i = 0; i < leafComps.length - 1; i++) {
                    org.w3c.dom.Element next = findChildElement(parent, leafComps[i], 0);
                    parent = next != null ? next : ensureInstance(datasets, parent, leafComps[i], 0);
                }
                current = datasets.createElement(leafComps[leafComps.length - 1]);
                parent.appendChild(current);
            }
        }

        // Remove existing child nodes and set text
        while (current.getFirstChild() != null) {
            current.removeChild(current.getFirstChild());
        }
        current.appendChild(datasets.createTextNode(value != null ? value : ""));

        // Write back to COS. Only the datasets DOM was mutated — mark just that packet dirty so
        // write-back leaves template/config/etc. byte-identical to the source (re-serializing an
        // unchanged packet can produce XFA that Acrobat rejects; see XfaPacketWriter).
        org.aspose.pdf.engine.xfa.packet.XfaPacket dsPacket = packetSet.get("datasets");
        if (dsPacket != null) {
            dsPacket.markDirty();
        }
        XfaPacketWriter.writeBack(xfaEntry, packetSet);
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

    // ── Convert (XFA → editable AcroForm) ──

    /**
     * Converts this XFA form into ordinary, <b>editable</b> AcroForm fields on the given
     * document so that generic PDF viewers (which cannot render XFA) display the form and
     * its data. Runs the data binding (merge of template + datasets), maps every Form DOM
     * field to the matching AcroForm field type at its statically-resolvable geometry, and
     * removes the {@code /XFA} entry (pure AcroForm). This is the Aspose <em>conversion</em>
     * operation — equivalent to {@code Form.setType(FormType.Standard)}.
     *
     * <p>This is <b>not</b> a flatten: the produced fields remain editable form fields; it
     * does not burn appearances into the page content. For a true flatten (fields burned into
     * the page and removed as fields) use {@code Form.flatten()}; to keep the fields but make
     * them non-editable, set the field read-only flag.</p>
     *
     * <p>It produces structure + values, not XFA render fidelity: dynamic layout and static
     * rendering are Stage C. Fields with flowed (non-positional) geometry are still created —
     * carrying their value — at a flagged placeholder position.</p>
     *
     * @param doc the document to add AcroForm fields to
     * @return the conversion result (counts, by-type, carried values, unmapped nodes)
     * @throws Exception if binding or document access fails
     */
    public org.aspose.pdf.engine.xfa.flatten.XfaFlattener.Result convertToAcroForm(
            org.aspose.pdf.Document doc) throws Exception {
        return convertToAcroForm(doc, org.aspose.pdf.engine.xfa.flatten.XfaFlattener.XfaPolicy.DROP);
    }

    /**
     * Converts this XFA form into editable AcroForm fields, with explicit {@code /XFA} policy.
     * A <em>conversion</em> (editable fields), not a flatten — see
     * {@link #convertToAcroForm(org.aspose.pdf.Document)}.
     *
     * @param doc    the document to add AcroForm fields to
     * @param policy {@code DROP} (pure AcroForm) or {@code KEEP} (hybrid)
     * @return the conversion result
     * @throws Exception if binding or document access fails
     */
    public org.aspose.pdf.engine.xfa.flatten.XfaFlattener.Result convertToAcroForm(
            org.aspose.pdf.Document doc,
            org.aspose.pdf.engine.xfa.flatten.XfaFlattener.XfaPolicy policy) throws Exception {
        org.aspose.pdf.engine.xfa.model.XfaModel model = org.aspose.pdf.engine.xfa.model.XfaModel.of(packetSet);
        org.aspose.pdf.engine.xfa.model.template.Template tpl = model.template();
        if (tpl == null) {
            // Template-less XFA (data/signature-only packet, no layout): nothing to convert.
            // Return an empty result instead of throwing so batch conversion is a clean no-op and
            // the document keeps its existing pages/fields.
            LOG.fine("XFA form has no template packet; convertToAcroForm is a no-op");
            return new org.aspose.pdf.engine.xfa.flatten.XfaFlattener.Result();
        }
        org.aspose.pdf.engine.xfa.model.datasets.Data data = model.data();
        // Build the SAME laid-out DOM the render track uses (scriptDrivenOccur + load-time scripts +
        // form-packet values + barcode) so the converted AcroForm has the SAME page geometry as Adobe —
        // empty occur min=0 detail subforms collapse (no blank-space page inflation) and script-toggled
        // variants resolve. Bound values a collapsed/hidden subform would drop are recovered by the
        // converter's placeUnplacedBoundFields fallback. -Dxfa.convertRenderDom=false reverts to the
        // legacy plain merge (every occur expanded, no scripts).
        org.aspose.pdf.engine.xfa.binding.FormDom dom =
                "false".equalsIgnoreCase(System.getProperty("xfa.convertRenderDom", "true"))
                        ? new org.aspose.pdf.engine.xfa.binding.BindingEngine().merge(tpl, data)
                        : buildPaintDom(tpl, data);
        // Drive the Stage-C layout/render track: paint static content + place fields at their real
        // laid-out positions + replace the XFA "Please wait…" placeholder pages (vs the Stage-A
        // grid-placeholder flatten, which dropped static content and mis-placed flowed fields).
        return org.aspose.pdf.engine.xfa.flatten.XfaAcroFormConverter.convert(doc, dom, tpl, policy, acroFormDict);
    }

    /**
     * Paints the XFA form's POSITIONED content (Stage C, C2) onto page 1 of {@code doc}:
     * box model (fill/border/corners), field and caption text, honouring {@code presence}
     * (hidden/invisible not painted). Flowed content is deferred to C3 (not painted at
     * placeholder coordinates). Complements {@link #convertToAcroForm(org.aspose.pdf.Document)}
     * (interactive widgets) with a static painted visual layer.
     *
     * @param doc the document to paint onto
     * @return the paint result
     * @throws Exception if binding or document access fails
     */
    public org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.Result paintPositionedContent(
            org.aspose.pdf.Document doc) throws Exception {
        org.aspose.pdf.engine.xfa.model.XfaModel model = org.aspose.pdf.engine.xfa.model.XfaModel.of(packetSet);
        org.aspose.pdf.engine.xfa.model.template.Template tpl = model.template();
        if (tpl == null) {
            LOG.fine("XFA form has no template packet; paintPositionedContent is a no-op");
            return new org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.Result();
        }
        org.aspose.pdf.engine.xfa.model.datasets.Data data = model.data();
        org.aspose.pdf.engine.xfa.binding.FormDom dom =
                new org.aspose.pdf.engine.xfa.binding.BindingEngine().merge(tpl, data);
        return org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.paint(doc, dom, tpl);
    }

    /**
     * Paginates and paints the XFA form across multiple pages (Stage C, L3): a flowed form is
     * split into pages and emitted as an N-page PDF; a positioned form authored as page-sized
     * subforms emits one page per subform; a genuine single-page positioned form is painted as
     * one page (delegating to the C2 painter). Each page is painted by reusing the validated C2
     * paint primitives.
     *
     * @param doc the document to paginate onto (pages are added as needed)
     * @return the paginate-and-paint result (page count, mode, paint counters)
     * @throws Exception if binding or document access fails
     */
    public org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator.Result paintPaginatedContent(
            org.aspose.pdf.Document doc) throws Exception {
        org.aspose.pdf.engine.xfa.model.XfaModel model = org.aspose.pdf.engine.xfa.model.XfaModel.of(packetSet);
        org.aspose.pdf.engine.xfa.model.template.Template tpl = model.template();
        if (tpl == null) {
            LOG.fine("XFA form has no template packet; paintPaginatedContent is a no-op");
            return new org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator.Result();
        }
        org.aspose.pdf.engine.xfa.model.datasets.Data data = model.data();
        org.aspose.pdf.engine.xfa.binding.FormDom dom = buildPaintDom(tpl, data);
        return org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator.paint(doc, dom, tpl);
    }

    /**
     * Builds the merged Form DOM the visual tracks (render and AcroForm conversion) lay out, so both
     * produce the SAME page geometry as Adobe: spec-correct {@code <occur initial>=min} (script-toggled
     * variant subforms start absent; empty {@code occur min=0} detail subforms collapse instead of
     * reserving blank space), the load-time scripts run (calculate/initialize/presence), saved
     * {@code <form>}-packet values filled into otherwise-empty fields, and barcode fields seeded.
     * Each step is independently kill-switchable via its system property.
     *
     * @param tpl  the XFA template
     * @param data the datasets data ({@code null} for empty merge)
     * @return the merged, scripted, value-enriched Form DOM
     */
    private org.aspose.pdf.engine.xfa.binding.FormDom buildPaintDom(
            org.aspose.pdf.engine.xfa.model.template.Template tpl,
            org.aspose.pdf.engine.xfa.model.datasets.Data data) {
        // spec-correct <occur initial>=min so script-toggled variant subforms start absent and load-time
        // scripts addInstance() the selected one (and empty occur min=0 detail rows collapse).
        org.aspose.pdf.engine.xfa.binding.FormDom dom =
                new org.aspose.pdf.engine.xfa.binding.BindingEngine().scriptDrivenOccur(true).merge(tpl, data);
        // Run load-time scripts (initialize/calculate/ready) so script-driven presence + dynamic instances
        // are reflected in the layout, the way Adobe renders it. Default on; -Dxfa.runScripts=false off.
        if (!"false".equalsIgnoreCase(System.getProperty("xfa.runScripts", "true"))) {
            try {
                org.aspose.pdf.engine.xfa.script.XfaScripting.execute(dom, tpl);
            } catch (RuntimeException scriptFailure) {
                java.util.logging.Logger.getLogger(XfaForm.class.getName())
                        .fine("XFA load-script execution skipped: " + scriptFailure);
            }
        }
        // Saved form-packet values: Adobe persists computed values (never written to <datasets>) in a
        // separate <form> packet (e.g. 11902 percentTime). Fill them into fields our merge left empty.
        if (!"false".equalsIgnoreCase(System.getProperty("xfa.formPacket", "true"))) {
            try {
                applyFormPacketValues(dom);
            } catch (RuntimeException ex) {
                java.util.logging.Logger.getLogger(XfaForm.class.getName())
                        .fine("XFA form-packet value fallback skipped: " + ex);
            }
        }
        // 2D-barcode (QR): seed an empty barcode field with the matching data subtree's XML so the
        // renderer can generate a real, scannable code.
        if (!"false".equalsIgnoreCase(System.getProperty("xfa.barcodeFallback", "true"))
                && data != null && data.getElement() != null
                && dom.getRoot() != null && dom.getRoot().getElement() != null) {
            try {
                injectBarcodeFallback(dom.getRoot().getElement(), data.getElement());
            } catch (RuntimeException ex) {
                java.util.logging.Logger.getLogger(XfaForm.class.getName())
                        .fine("XFA barcode fallback skipped: " + ex);
            }
        }
        return dom;
    }

    /**
     * Fills empty merged-form fields from the saved XFA {@code <form>} packet (XFA 3.0 form packet).
     *
     * <p>Adobe persists the fully-merged form — including values it COMPUTED but never wrote back to
     * {@code <datasets>} — in the form packet. We re-merge from datasets and run scripts, so a field
     * that is absent from the data and has no {@code calculate} (e.g. 11902's {@code percentTime}, whose
     * value Adobe reverse-derived to 50/85/75) stays blank. This copies each saved form-packet value
     * into a field our merge left empty; it never overrides a value the data or a script produced.</p>
     */
    private void applyFormPacketValues(org.aspose.pdf.engine.xfa.binding.FormDom dom) {
        if (dom == null || dom.getRoot() == null || dom.getRoot().getElement() == null) {
            return;
        }
        org.w3c.dom.Document formPkt = getFormPacketDocument();
        if (formPkt == null || formPkt.getDocumentElement() == null) {
            return;
        }
        org.w3c.dom.Element mergedRoot = dom.getRoot().getElement();
        // The form packet wraps the root subform in <form>; descend to the matching root subform.
        org.w3c.dom.Element packetRoot = matchingChild(formPkt.getDocumentElement(), mergedRoot);
        if (packetRoot == null) {
            packetRoot = firstChildElement(formPkt.getDocumentElement(), "subform");
        }
        if (packetRoot == null) {
            return;
        }
        // Map each merged field element to its FormField so a fill writes through the typed <value>
        // content (e.g. <decimal>) AND ff.getValue(), which the painter reads in preference to the DOM.
        java.util.Map<org.w3c.dom.Element, org.aspose.pdf.engine.xfa.binding.FormField> byElement =
                new java.util.IdentityHashMap<>();
        for (org.aspose.pdf.engine.xfa.binding.FormField ff : dom.getFields()) {
            if (ff.getFormNode() != null && ff.getFormNode().getElement() != null) {
                byElement.put(ff.getFormNode().getElement(), ff);
            }
        }
        fillFromPacket(mergedRoot, packetRoot, byElement);
    }

    /** Returns the {@code form} packet DOM (keyed name first, else any packet whose root is {@code <form>}). */
    private org.w3c.dom.Document getFormPacketDocument() {
        org.w3c.dom.Document f = getForm();
        if (f != null) {
            return f;
        }
        for (org.aspose.pdf.engine.xfa.packet.XfaPacket p : packetSet.all()) {
            org.w3c.dom.Document d = p.getDocument();
            if (d != null && d.getDocumentElement() != null
                    && "form".equals(localName(d.getDocumentElement()))) {
                return d;
            }
        }
        return null;
    }

    /** First child element of {@code parent} matching {@code ref} by local name and {@code name} attribute. */
    private static org.w3c.dom.Element matchingChild(org.w3c.dom.Element parent, org.w3c.dom.Element ref) {
        String ln = localName(ref);
        String nm = ref.getAttribute("name");
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && ln.equals(localName(n))
                    && nm.equals(((org.w3c.dom.Element) n).getAttribute("name"))) {
                return (org.w3c.dom.Element) n;
            }
        }
        return null;
    }

    /**
     * Walks the merged form tree and the saved form-packet tree in lockstep, matching children by
     * (local name, {@code name} attribute, occurrence index among same-keyed siblings). Where a merged
     * field is empty and the packet holds a saved value, fills it through the {@link
     * org.aspose.pdf.engine.xfa.binding.FormField} so the typed {@code <value>} content and
     * {@code getValue()} both reflect it.
     */
    private static void fillFromPacket(org.w3c.dom.Element merged, org.w3c.dom.Element packet,
            java.util.Map<org.w3c.dom.Element, org.aspose.pdf.engine.xfa.binding.FormField> byElement) {
        if ("field".equals(localName(merged))) {
            org.aspose.pdf.engine.xfa.binding.FormField ff = byElement.get(merged);
            String cur = ff != null ? ff.getValue() : textContentOfValue(merged);
            if (cur != null && !cur.trim().isEmpty()) {
                return; // the data or a script already produced a value — never override it
            }
            String saved = textContentOfValue(packet);
            if (saved != null && !saved.trim().isEmpty()) {
                if (ff != null) {
                    ff.setValue(saved.trim());
                } else {
                    setBarcodeValueText(merged, saved.trim());
                }
                // Adopt the saved field's display <items> and conditional <border>/<fill> so the value
                // renders as its LABEL (not the raw save code) and any script-applied background paints —
                // our re-merge produces neither for a bind="none" + bindItems dropdown (14758's PO Status:
                // saved value M01 → "MEASURE DATE", over a green fill the form's status script set).
                adoptPacketItems(merged, packet);
                adoptPacketFill(merged, packet);
            }
            return; // a field has no nested fields
        }
        java.util.Map<String, java.util.List<org.w3c.dom.Element>> pkt = new java.util.HashMap<>();
        for (org.w3c.dom.Node n = packet.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) n;
                pkt.computeIfAbsent(keyOf(e), k -> new java.util.ArrayList<>()).add(e);
            }
        }
        java.util.Map<String, Integer> seen = new java.util.HashMap<>();
        for (org.w3c.dom.Node n = merged.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            org.w3c.dom.Element c = (org.w3c.dom.Element) n;
            String key = keyOf(c);
            int idx = seen.merge(key, 1, Integer::sum) - 1;
            java.util.List<org.w3c.dom.Element> pl = pkt.get(key);
            if (pl != null && idx < pl.size()) {
                fillFromPacket(c, pl.get(idx), byElement);
            }
        }
    }

    /** Match key for a form-tree element: local name + {@code name} attribute. */
    private static String keyOf(org.w3c.dom.Element e) {
        return localName(e) + "\u0000" + e.getAttribute("name");
    }

    /** Text content of a host element's {@code <value>} child (any typed content), or null. */
    private static String textContentOfValue(org.w3c.dom.Element host) {
        org.w3c.dom.Element value = firstChildElement(host, "value");
        return value == null ? null : value.getTextContent();
    }

    /**
     * Copies the saved field's {@code <items>} lists (display labels + the {@code save="1"} codes) from
     * the form packet into a merged field that has none, so a bound dropdown value renders as its label
     * via the painter's save→display lookup. A {@code bindItems} (data-driven) dropdown produces no
     * static {@code <items>} on our merge, so without this the raw save code (e.g. "M01") would show.
     * No-op if the merged field already carries items.
     */
    private static void adoptPacketItems(org.w3c.dom.Element merged, org.w3c.dom.Element packet) {
        for (org.w3c.dom.Node n = merged.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && "items".equals(localName(n))) {
                return; // already has items
            }
        }
        org.w3c.dom.Document doc = merged.getOwnerDocument();
        for (org.w3c.dom.Node n = packet.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && "items".equals(localName(n))) {
                merged.appendChild(doc.importNode(n, true));
            }
        }
    }

    /**
     * Copies the saved field's conditional background — a {@code <border>} carrying a {@code <fill>}, or a
     * direct {@code <fill>} — from the form packet onto a merged field that has none, so a colour a script
     * applied at runtime (14758's green PO Status) is rendered. No-op when the merged field already
     * declares its own border or fill (we never override the template's own chrome).
     */
    private static void adoptPacketFill(org.w3c.dom.Element merged, org.w3c.dom.Element packet) {
        if (firstChildElement(merged, "border") != null || firstChildElement(merged, "fill") != null) {
            return; // merged field carries its own chrome — leave it
        }
        org.w3c.dom.Element pBorder = firstChildElement(packet, "border");
        org.w3c.dom.Element pFill = firstChildElement(packet, "fill");
        org.w3c.dom.Element source = null;
        if (pBorder != null && firstChildElement(pBorder, "fill") != null) {
            source = pBorder;
        } else if (pFill != null) {
            source = pFill;
        }
        if (source != null) {
            merged.appendChild(merged.getOwnerDocument().importNode(source, true));
        }
    }

    /**
     * Walks the merged form DOM under {@code formEl}, seeding each {@code <ui><barcode>} field that has
     * no value with the XML of the data subtree its {@code name} implies (debtor / application /
     * creditor, else the whole record), so the renderer can generate a QR from real form data. Operates
     * on the DOM tree (not the {@link org.aspose.pdf.engine.xfa.binding.FormField} list) so it also
     * reaches barcode fields in script-added instances, which carry no registered FormField. A field
     * that already holds a value (a successful calculate) is left untouched.
     */
    private static void injectBarcodeFallback(org.w3c.dom.Element formEl, org.w3c.dom.Element dataRoot) {
        if ("field".equals(localName(formEl))) {
            org.w3c.dom.Element ui = firstChildElement(formEl, "ui");
            if (ui != null && firstChildElement(ui, "barcode") != null && barcodeValueText(formEl).isEmpty()) {
                String name = formEl.getAttribute("name");
                name = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
                org.w3c.dom.Element subtree = null;
                if (name.contains("dluznik")) {
                    subtree = firstDescendant(dataRoot, "dluznik");
                } else if (name.contains("prihlaska") || name.contains("prihllaska")) {
                    subtree = firstDescendant(dataRoot, "prihlaska_pohledavky");
                } else if (name.contains("veritel")) {
                    subtree = firstDescendant(dataRoot, "veritel");
                }
                String xml = serializeXml(subtree != null ? subtree : dataRoot);
                if (xml != null && !xml.isEmpty()) {
                    setBarcodeValueText(formEl, xml);
                }
            }
            return; // a field has no nested fields
        }
        for (org.w3c.dom.Node n = formEl.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                injectBarcodeFallback((org.w3c.dom.Element) n, dataRoot);
            }
        }
    }

    /** The current {@code <value><text>} content of a field, or "" if absent/empty. */
    private static String barcodeValueText(org.w3c.dom.Element field) {
        org.w3c.dom.Element value = firstChildElement(field, "value");
        org.w3c.dom.Element text = value == null ? null : firstChildElement(value, "text");
        String s = text == null ? null : text.getTextContent();
        return s == null ? "" : s.trim();
    }

    /** Writes {@code xml} into the field's {@code <value><text>}, creating the nodes as needed. */
    private static void setBarcodeValueText(org.w3c.dom.Element field, String xml) {
        String ns = field.getNamespaceURI();
        org.w3c.dom.Element value = firstChildElement(field, "value");
        if (value == null) {
            value = field.getOwnerDocument().createElementNS(ns, "value");
            field.appendChild(value);
        }
        org.w3c.dom.Element text = firstChildElement(value, "text");
        if (text == null) {
            text = field.getOwnerDocument().createElementNS(ns, "text");
            value.appendChild(text);
        }
        text.setTextContent(xml);
    }

    /** The local name of {@code n}, falling back to the node name for non-namespaced DOM. */
    private static String localName(org.w3c.dom.Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    /** First child {@link org.w3c.dom.Element} of {@code parent} with the given local name, or null. */
    private static org.w3c.dom.Element firstChildElement(org.w3c.dom.Element parent, String localName) {
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && localName.equals(localName(n))) {
                return (org.w3c.dom.Element) n;
            }
        }
        return null;
    }

    /** The first descendant element with the given local name (depth-first), or null. */
    private static org.w3c.dom.Element firstDescendant(org.w3c.dom.Element root, String localName) {
        for (org.w3c.dom.Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            org.w3c.dom.Element e = (org.w3c.dom.Element) n;
            if (localName.equals(localName(e))) {
                return e;
            }
            org.w3c.dom.Element deep = firstDescendant(e, localName);
            if (deep != null) {
                return deep;
            }
        }
        return null;
    }

    /** Serialises a DOM element to compact XML (no XML declaration, whitespace-normalised), or null. */
    private static String serializeXml(org.w3c.dom.Element el) {
        try {
            javax.xml.transform.Transformer t =
                    javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            t.transform(new javax.xml.transform.dom.DOMSource(el),
                    new javax.xml.transform.stream.StreamResult(sw));
            // Collapse the indentation whitespace between tags so the QR payload stays compact.
            return sw.toString().replaceAll(">\\s+<", "><").trim();
        } catch (Exception ex) {
            return null;
        }
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
        /** true when the caller wrote an explicit {@code [n]} instance index. */
        final boolean explicit;

        SomSegment(String name, int index) {
            this(name, index, false);
        }

        SomSegment(String name, int index, boolean explicit) {
            this.name = name;
            this.index = index;
            this.explicit = explicit;
        }

        static SomSegment parse(String segment) {
            int bracketPos = segment.indexOf('[');
            if (bracketPos < 0) {
                return new SomSegment(segment, 0, false);
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
            return new SomSegment(name, index, true);
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
