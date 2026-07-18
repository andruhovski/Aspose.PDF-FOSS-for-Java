package org.aspose.pdf.forms.xfa;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.*;
import java.util.logging.Logger;

/// Namespace context for XFA XML documents.
/// Implements [javax.xml.namespace.NamespaceContext] to support XPath queries
/// over XFA template, datasets, and other packets.
///
/// Auto-detects the template namespace version (2.6, 2.8, or 3.0) from the actual
/// XML and registers it under the "tpl" prefix.
///
public class XfaNamespaceContext implements NamespaceContext {

    private static final Logger LOGGER = Logger.getLogger(XfaNamespaceContext.class.getName());

    /// Default XDP namespace.
    public static final String XDP_NS = "http://ns.adobe.com/xdp/";

    /// Default template namespace (3.0). May be overridden by detection.
    public static final String TEMPLATE_NS_DEFAULT = "http://www.xfa.org/schema/xfa-template/3.0/";

    /// Data namespace.
    public static final String DATA_NS = "http://www.xfa.org/schema/xfa-data/1.0/";

    /// Config namespace (default).
    public static final String CONFIG_NS_DEFAULT = "http://www.xfa.org/schema/xci/3.0/";

    /// Locale set namespace.
    public static final String LOCALE_NS = "http://www.xfa.org/schema/xfa-locale-set/2.7/";

    /// Form namespace.
    public static final String FORM_NS = "http://www.xfa.org/schema/xfa-form/2.8/";

    private static final String TEMPLATE_NS_PREFIX = "http://www.xfa.org/schema/xfa-template/";
    private static final String DATA_NS_PREFIX = "http://www.xfa.org/schema/xfa-data/";

    private final Map<String, String> prefixToUri;
    private final Map<String, List<String>> uriToPrefix;

    /// Creates namespace context with auto-detection of template version.
    ///
    /// @param templateDoc the template XML document (may be null)
    /// @param datasetsDoc the datasets XML document (may be null)
    public XfaNamespaceContext(org.w3c.dom.Document templateDoc,
                               org.w3c.dom.Document datasetsDoc) {
        // Step 1: Initialize default mappings
        prefixToUri = new HashMap<>();
        prefixToUri.put("xdp", XDP_NS);
        prefixToUri.put("tpl", TEMPLATE_NS_DEFAULT);
        prefixToUri.put("xfa", DATA_NS);
        prefixToUri.put("cfg", CONFIG_NS_DEFAULT);
        prefixToUri.put("loc", LOCALE_NS);
        prefixToUri.put("form", FORM_NS);

        // Step 2: Auto-detect template namespace version
        if (templateDoc != null) {
            org.w3c.dom.Element root = templateDoc.getDocumentElement();
            if (root != null) {
                String ns = root.getNamespaceURI();
                if (ns != null && ns.startsWith(TEMPLATE_NS_PREFIX)) {
                    LOGGER.fine("Detected template namespace: " + ns);
                    prefixToUri.put("tpl", ns);
                }
            }
        }

        // Step 3: Auto-detect data namespace version
        if (datasetsDoc != null) {
            org.w3c.dom.Element root = datasetsDoc.getDocumentElement();
            if (root != null) {
                String ns = root.getNamespaceURI();
                if (ns != null && ns.startsWith(DATA_NS_PREFIX)) {
                    LOGGER.fine("Detected data namespace: " + ns);
                    prefixToUri.put("xfa", ns);
                }
            }
        }

        // Step 4: Build reverse map (uri → list of prefixes)
        uriToPrefix = new HashMap<>();
        for (Map.Entry<String, String> entry : prefixToUri.entrySet()) {
            uriToPrefix.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                       .add(entry.getKey());
        }
    }

    /// Returns the namespace URI bound to the given prefix.
    ///
    /// @param prefix the namespace prefix to look up
    /// @return the namespace URI, or [XMLConstants#NULL\_NS\_URI] if not bound
    /// @throws IllegalArgumentException if prefix is null
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix must not be null");
        }
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return XMLConstants.NULL_NS_URI;
        }
        if ("xml".equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        if ("xmlns".equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        String uri = prefixToUri.get(prefix);
        return uri != null ? uri : XMLConstants.NULL_NS_URI;
    }

    /// Returns a prefix bound to the given namespace URI, or null if none is bound.
    ///
    /// @param namespaceURI the namespace URI to look up
    /// @return a prefix bound to the URI, or null
    /// @throws IllegalArgumentException if namespaceURI is null
    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI must not be null");
        }
        List<String> prefixes = uriToPrefix.get(namespaceURI);
        return (prefixes != null && !prefixes.isEmpty()) ? prefixes.get(0) : null;
    }

    /// Returns an iterator over all prefixes bound to the given namespace URI.
    ///
    /// @param namespaceURI the namespace URI to look up
    /// @return an iterator over bound prefixes (may be empty)
    /// @throws IllegalArgumentException if namespaceURI is null
    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI must not be null");
        }
        List<String> prefixes = uriToPrefix.get(namespaceURI);
        if (prefixes == null || prefixes.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Collections.unmodifiableList(prefixes).iterator();
    }
}
