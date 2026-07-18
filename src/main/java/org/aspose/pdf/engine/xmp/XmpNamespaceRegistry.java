package org.aspose.pdf.engine.xmp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/// Registry of XMP namespace prefix-to-URI mappings (ISO 16684-1).
///
/// Pre-populated with standard namespaces (Dublin Core, XMP, PDF, etc.).
/// Custom namespaces can be registered at runtime.
///
public final class XmpNamespaceRegistry {

    private final Map<String, String> prefixToUri = new LinkedHashMap<>();
    private final Map<String, String> uriToPrefix = new LinkedHashMap<>();

    /// Creates a registry pre-populated with standard XMP namespaces.
    public XmpNamespaceRegistry() {
        register("dc", "http://purl.org/dc/elements/1.1/");
        register("xmp", "http://ns.adobe.com/xap/1.0/");
        register("xmpMM", "http://ns.adobe.com/xap/1.0/mm/");
        register("xmpRights", "http://ns.adobe.com/xap/1.0/rights/");
        register("pdf", "http://ns.adobe.com/pdf/1.3/");
        register("pdfaid", "http://www.aiim.org/pdfa/ns/id/");
        register("pdfaExtension", "http://www.aiim.org/pdfa/ns/extension/");
        register("pdfaSchema", "http://www.aiim.org/pdfa/ns/schema#");
        register("pdfaProperty", "http://www.aiim.org/pdfa/ns/property#");
        register("pdfaType", "http://www.aiim.org/pdfa/ns/type#");
        register("photoshop", "http://ns.adobe.com/photoshop/1.0/");
        register("tiff", "http://ns.adobe.com/tiff/1.0/");
        register("exif", "http://ns.adobe.com/exif/1.0/");
        register("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        register("x", "adobe:ns:meta/");
        register("xml", "http://www.w3.org/XML/1998/namespace");
        register("stEvt", "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#");
        register("stRef", "http://ns.adobe.com/xap/1.0/sType/ResourceRef#");
    }

    /// Registers a namespace prefix-URI mapping.
    ///
    /// @param prefix       the namespace prefix (e.g. "dc")
    /// @param namespaceUri the namespace URI
    public void register(String prefix, String namespaceUri) {
        if (prefix == null || namespaceUri == null) return;
        prefixToUri.put(prefix, namespaceUri);
        uriToPrefix.put(namespaceUri, prefix);
    }

    /// Returns the URI for the given prefix, or null if unknown.
    ///
    /// @param prefix the namespace prefix
    /// @return the namespace URI, or null
    public String getUri(String prefix) {
        return prefixToUri.get(prefix);
    }

    /// Returns the prefix for the given URI, or null if unknown.
    ///
    /// @param uri the namespace URI
    /// @return the prefix, or null
    public String getPrefix(String uri) {
        return uriToPrefix.get(uri);
    }

    /// Returns true if the prefix is registered.
    ///
    /// @param prefix the prefix to check
    /// @return true if known
    public boolean hasPrefix(String prefix) {
        return prefixToUri.containsKey(prefix);
    }

    /// Returns an unmodifiable view of all prefix-to-URI mappings.
    ///
    /// @return the prefix-to-URI map
    public Map<String, String> getAllMappings() {
        return Collections.unmodifiableMap(prefixToUri);
    }

    /// Returns the namespace mappings that are actually used by the given properties.
    ///
    /// @param properties the property map keyed by "prefix:localName"
    /// @return map of prefix → URI for used namespaces
    public Map<String, String> getUsedNamespaces(Map<String, XmpProperty> properties) {
        Map<String, String> used = new LinkedHashMap<>();
        for (String key : properties.keySet()) {
            int colon = key.indexOf(':');
            if (colon > 0) {
                String prefix = key.substring(0, colon);
                String uri = prefixToUri.get(prefix);
                if (uri != null) {
                    used.put(prefix, uri);
                }
            }
        }
        return used;
    }
}
