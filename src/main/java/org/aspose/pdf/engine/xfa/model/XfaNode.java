package org.aspose.pdf.engine.xfa.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/// Base of the typed XFA template model. Each `XfaNode` is a typed view
/// over a backing [org.w3c.dom.Element]; typed getters/setters read and
/// write attributes and child elements directly on that Element.
///
/// Because the DOM is the backing store, round-trip is inherent: free child
/// ordering, unknown attributes and unknown child elements are all preserved
/// (they live in the DOM and are never dropped). Typed subclasses (generated
/// from the XFA template model) add element-specific accessors.
public class XfaNode {

    /// The XFA template target namespace.
    public static final String TEMPLATE_NS = "http://www.xfa.org/schema/xfa-template/3.0/";

    /// The backing DOM element.
    protected final Element element;
    /// Parent node, or `null` for the root of a loaded tree.
    protected XfaNode parent;

    /// Creates a node over a backing element.
    ///
    /// @param element backing DOM element (must not be `null`)
    /// @param parent  parent node, or `null`
    public XfaNode(Element element, XfaNode parent) {
        if (element == null) {
            throw new IllegalArgumentException("backing element must not be null");
        }
        this.element = element;
        this.parent = parent;
    }

    /// @return the backing DOM element.
    public Element getElement() {
        return element;
    }

    /// @return the parent node, or `null`.
    public XfaNode getParent() {
        return parent;
    }

    /// @return the element's local name.
    public String getElementName() {
        String ln = element.getLocalName();
        return ln != null ? ln : element.getNodeName();
    }

    /// @return the element's namespace URI (`null` if none).
    public String getNamespace() {
        return element.getNamespaceURI();
    }

    /* --------------------------- attributes -------------------------- */

    /// Raw attribute value.
    ///
    /// @param name attribute (no-namespace) local name
    /// @return the value, or `null` if the attribute is absent
    public String getAttribute(String name) {
        return element.hasAttribute(name) ? element.getAttribute(name) : null;
    }

    /// Sets or removes an attribute. A `null` value removes it.
    ///
    /// @param name  attribute name
    /// @param value new value, or `null` to remove
    public void setAttribute(String name, String value) {
        if (value == null) {
            element.removeAttribute(name);
        } else {
            element.setAttribute(name, value);
        }
    }

    /// @return the attribute as a string (alias of [#getAttribute]).
    public String getString(String name) {
        return getAttribute(name);
    }

    /// The attribute parsed as an XFA integer.
    ///
    /// @param name attribute name
    /// @return boxed integer, or `null` if absent/unparseable
    public Integer getInteger(String name) {
        String v = getAttribute(name);
        if (v == null) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /// The attribute parsed as an XFA boolean (`"1"`/`"true"` =>
    /// true, `"0"`/`"false"` => false).
    ///
    /// @param name attribute name
    /// @return boxed boolean, or `null` if absent/unrecognised
    public Boolean getBoolean(String name) {
        String v = getAttribute(name);
        if (v == null) {
            return null;
        }
        v = v.trim();
        if (v.equals("1") || v.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (v.equals("0") || v.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }

    /// The attribute parsed as an [XfaMeasurement].
    ///
    /// @param name attribute name
    /// @return the measurement, or `null` if absent/invalid
    public XfaMeasurement getMeasurement(String name) {
        return XfaMeasurement.parse(getAttribute(name));
    }

    /// Sets a measurement attribute (removes it when `m` is null).
    public void setMeasurement(String name, XfaMeasurement m) {
        setAttribute(name, m == null ? null : m.format());
    }

    /* ------------------------- XFA common attrs ---------------------- */

    /// @return the `id` attribute, or `null`.
    public String getId() {
        return getAttribute("id");
    }

    /// @return the `name` attribute, or `null`.
    public String getName() {
        return getAttribute("name");
    }

    /// @return the `use` reference (intra-document prototype id), or `null`.
    public String getUse() {
        return getAttribute("use");
    }

    /// @return the `usehref` reference (prototype href), or `null`.
    public String getUseHref() {
        return getAttribute("usehref");
    }

    /* ---------------------------- children --------------------------- */

    /// @return all typed child element nodes, in document order.
    public List<XfaNode> getChildren() {
        List<XfaNode> out = new ArrayList<>();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                out.add(XfaNodeFactory.wrap((Element) n, this));
            }
        }
        return out;
    }

    /// The first child element with the given local name, typed.
    ///
    /// @param localName child element local name
    /// @return the typed child, or `null` if none
    public XfaNode getChild(String localName) {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && localMatches(n, localName)) {
                return XfaNodeFactory.wrap((Element) n, this);
            }
        }
        return null;
    }

    /// All child elements with the given local name, typed, in document order.
    ///
    /// @param localName child element local name
    /// @return the typed children (possibly empty)
    public List<XfaNode> getChildren(String localName) {
        List<XfaNode> out = new ArrayList<>();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && localMatches(n, localName)) {
                out.add(XfaNodeFactory.wrap((Element) n, this));
            }
        }
        return out;
    }

    /// Ensures a single child of the given name exists (creates it if missing)
    /// and returns it. Used by typed single-child setters.
    ///
    /// @param localName child local name
    /// @return the existing or newly created typed child
    public XfaNode ensureChild(String localName) {
        XfaNode existing = getChild(localName);
        if (existing != null) {
            return existing;
        }
        Document doc = element.getOwnerDocument();
        Element created = doc.createElementNS(element.getNamespaceURI(), localName);
        element.appendChild(created);
        return XfaNodeFactory.wrap(created, this);
    }

    /// Appends a new child element of the given name and returns it (for
    /// repeating children).
    ///
    /// @param localName child local name
    /// @return the new typed child
    public XfaNode addChild(String localName) {
        Document doc = element.getOwnerDocument();
        Element created = doc.createElementNS(element.getNamespaceURI(), localName);
        element.appendChild(created);
        return XfaNodeFactory.wrap(created, this);
    }

    /// @return the text content of this element (concatenated descendant text).
    public String getTextContent() {
        return element.getTextContent();
    }

    /// Sets this element's text content (replaces children with a single text node).
    ///
    /// @param text the text
    public void setTextContent(String text) {
        element.setTextContent(text);
    }

    private static boolean localMatches(Node n, String localName) {
        String ln = n.getLocalName();
        if (ln != null) {
            return ln.equals(localName);
        }
        return localName.equals(n.getNodeName());
    }

    @Override
    public String toString() {
        return "<" + getElementName()
                + (getName() != null ? " name=\"" + getName() + "\"" : "") + ">";
    }
}
