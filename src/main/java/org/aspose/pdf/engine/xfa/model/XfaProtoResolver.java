package org.aspose.pdf.engine.xfa.model;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;
import java.util.logging.Logger;

/// Resolves XFA prototype references at the model level: `use` (intra-document
/// prototype by id) and `usehref` (prototype by href/id), plus `proto`
/// packet sources (XFA 3.0 sec on `use`/`usehref`).
///
/// A referencing element _inherits_ the prototype's attributes and
/// children; locally specified properties win. Resolution is grammar-level (not
/// data binding). Prototype chains are followed; cycles are detected and reported
/// (never infinite-looped); unresolved references degrade with a WARNING.
///
/// Resolution materialises inheritance into the DOM (missing attributes copied,
/// missing-by-name children appended). It is an explicit operation — a tree that
/// is never resolved round-trips unchanged.
public final class XfaProtoResolver {

    private static final Logger LOG = Logger.getLogger(XfaProtoResolver.class.getName());

    /// Outcome of a resolution pass.
    public static final class Report {
        private int resolved;
        private final List<String> unresolved = new ArrayList<>();
        private final List<String> cycles = new ArrayList<>();

        /// @return number of references successfully resolved.
        public int getResolvedCount() { return resolved; }
        /// @return descriptions of references that could not be resolved.
        public List<String> getUnresolved() { return unresolved; }
        /// @return descriptions of detected reference cycles.
        public List<String> getCycles() { return cycles; }

        @Override
        public String toString() {
            return "resolved=" + resolved + ", unresolved=" + unresolved + ", cycles=" + cycles;
        }
    }

    private final XfaNode root;
    private final Map<String, Element> idIndex = new HashMap<>();
    private final Map<String, XfaNode> protoSources = new HashMap<>();
    private final Report report = new Report();

    /// Creates a resolver over a template tree.
    ///
    /// @param root the typed template root (its subtree is indexed by `id`)
    public XfaProtoResolver(XfaNode root) {
        this.root = root;
        if (root != null) {
            indexIds(root.getElement());
        }
    }

    /// Registers an external prototype source (e.g. a `proto` packet),
    /// addressable by an href key used in `usehref`.
    ///
    /// @param hrefKey the href (the part before `#` in a usehref)
    /// @param source  the prototype source root node
    public void addProtoSource(String hrefKey, XfaNode source) {
        if (hrefKey != null && source != null) {
            protoSources.put(hrefKey, source);
            indexIds(source.getElement());
        }
    }

    /// Resolves all `use`/`usehref` references in the tree.
    ///
    /// @return a report of resolved / unresolved / cyclic references
    public Report resolve() {
        List<Element> refs = new ArrayList<>();
        collectRefs(root.getElement(), refs);
        for (Element e : refs) {
            Set<Element> onPath = Collections.newSetFromMap(new IdentityHashMap<>());
            resolveElement(e, onPath);
        }
        return report;
    }

    private void resolveElement(Element e, Set<Element> onPath) {
        String use = attr(e, "use");
        String usehref = attr(e, "usehref");
        if (use == null && usehref == null) {
            return;
        }
        if (!onPath.add(e)) {
            String desc = describe(e);
            report.cycles.add(desc);
            LOG.warning(() -> "XFA prototype cycle detected at " + desc);
            return;
        }
        Element proto = (usehref != null) ? lookupHref(usehref) : lookupUse(use);
        if (proto == null) {
            String ref = usehref != null ? usehref : use;
            report.unresolved.add(describe(e) + " -> " + ref);
            LOG.warning(() -> "Unresolved XFA prototype reference '" + ref + "' on " + describe(e));
            onPath.remove(e);
            return;
        }
        if (proto == e) {
            report.cycles.add(describe(e) + " (self)");
            onPath.remove(e);
            return;
        }
        // Resolve the prototype's own references first (chained prototypes).
        resolveElement(proto, onPath);
        merge(e, proto);
        report.resolved++;
        onPath.remove(e);
    }

    /// Copies prototype attributes the node lacks, and appends prototype children absent by name.
    private void merge(Element node, Element proto) {
        NamedNodeMap atts = proto.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node a = atts.item(i);
            String name = a.getNodeName();
            if (name.equals("use") || name.equals("usehref") || name.equals("id")
                    || name.startsWith("xmlns")) {
                continue;
            }
            if (!node.hasAttribute(name)) {
                node.setAttribute(name, a.getNodeValue());
            }
        }
        Set<String> present = childLocalNames(node);
        Node child = proto.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String ln = localName(child);
                if (!present.contains(ln)) {
                    Node imported = node.getOwnerDocument().importNode(child, true);
                    node.appendChild(imported);
                    present.add(ln);
                }
            }
            child = child.getNextSibling();
        }
    }

    /* --------------------------- lookups ---------------------------- */

    private Element lookupUse(String use) {
        if (use == null) {
            return null;
        }
        String id = use.startsWith("#") ? use.substring(1) : use;
        return idIndex.get(id);
    }

    private Element lookupHref(String usehref) {
        int hash = usehref.indexOf('#');
        String doc = hash >= 0 ? usehref.substring(0, hash) : usehref;
        String id = hash >= 0 ? usehref.substring(hash + 1) : "";
        if (doc.isEmpty()) {
            return idIndex.get(id); // same document
        }
        XfaNode source = protoSources.get(doc);
        if (source == null) {
            return null; // external document not loaded -> unresolved (WARNING)
        }
        return id.isEmpty() ? source.getElement() : findId(source.getElement(), id);
    }

    /* ----------------------------- index ---------------------------- */

    private void indexIds(Element e) {
        if (e == null) {
            return;
        }
        if (e.hasAttribute("id")) {
            idIndex.putIfAbsent(e.getAttribute("id"), e);
        }
        Node c = e.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                indexIds((Element) c);
            }
            c = c.getNextSibling();
        }
    }

    private Element findId(Element root, String id) {
        if (id.equals(root.getAttribute("id"))) {
            return root;
        }
        Node c = root.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                Element r = findId((Element) c, id);
                if (r != null) {
                    return r;
                }
            }
            c = c.getNextSibling();
        }
        return null;
    }

    private void collectRefs(Element e, List<Element> out) {
        if (e.hasAttribute("use") || e.hasAttribute("usehref")) {
            out.add(e);
        }
        Node c = e.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                collectRefs((Element) c, out);
            }
            c = c.getNextSibling();
        }
    }

    /* ----------------------------- utils ---------------------------- */

    private static String attr(Element e, String name) {
        return e.hasAttribute(name) ? e.getAttribute(name) : null;
    }

    private static Set<String> childLocalNames(Element e) {
        Set<String> out = new java.util.HashSet<>();
        Node c = e.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                out.add(localName(c));
            }
            c = c.getNextSibling();
        }
        return out;
    }

    private static String localName(Node n) {
        String ln = n.getLocalName();
        return ln != null ? ln : n.getNodeName();
    }

    private static String describe(Element e) {
        String ln = localName(e);
        String name = e.hasAttribute("name") ? e.getAttribute("name") : "";
        return "<" + ln + (name.isEmpty() ? "" : " name=\"" + name + "\"") + ">";
    }
}
