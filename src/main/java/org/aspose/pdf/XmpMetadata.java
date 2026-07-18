package org.aspose.pdf;

import org.aspose.pdf.engine.xmp.XmpNamespaceRegistry;
import org.aspose.pdf.engine.xmp.XmpParser;
import org.aspose.pdf.engine.xmp.XmpProperty;
import org.aspose.pdf.engine.xmp.XmpWriter;

import java.util.*;
import java.util.logging.Logger;

/// Provides access to XMP metadata of a PDF document (ISO 32000-1 §14.3.2, ISO 16684-1).
///
/// Implements a Map-like interface with string keys in "prefix:localName" format.
/// Iterable over key-value pairs. Lazy-parses the XMP XML on first access.
///
/// Standard key prefixes: `dc:`, `xmp:`, `pdf:`, `xmpMM:`,
/// `xmpRights:`, `pdfaid:`. See [DefaultMetadataProperties] for constants.
///
public class XmpMetadata implements Iterable<Map.Entry<String, XmpValue>> {

    private static final Logger LOG = Logger.getLogger(XmpMetadata.class.getName());

    private final XmpNamespaceRegistry registry;
    private Map<String, XmpProperty> properties;
    private byte[] rawBytes;
    private boolean dirty;

    // Well-known dc: property types
    private static final Set<String> LANG_ALT_PROPS = new HashSet<>(Arrays.asList(
            "dc:title", "dc:description", "dc:rights"));
    private static final Set<String> SEQ_PROPS = new HashSet<>(Arrays.asList(
            "dc:creator", "dc:date"));
    private static final Set<String> BAG_PROPS = new HashSet<>(Arrays.asList(
            "dc:subject", "dc:language", "dc:publisher", "dc:relation",
            "dc:type", "dc:contributor"));

    /// Creates empty XMP metadata.
    public XmpMetadata() {
        this.registry = new XmpNamespaceRegistry();
        this.properties = new LinkedHashMap<>();
        this.dirty = false;
    }

    /// Creates XMP metadata from raw XML bytes.
    ///
    /// @param xmpBytes the UTF-8 XMP XML bytes
    public XmpMetadata(byte[] xmpBytes) {
        this.registry = new XmpNamespaceRegistry();
        this.rawBytes = xmpBytes;
        this.properties = null; // lazy parse
        this.dirty = false;
    }

    /// Returns the value for the given key, or null if not present.
    ///
    /// @param key the property key ("prefix:localName")
    /// @return the value, or null
    public XmpValue get(String key) {
        ensureParsed();
        XmpProperty prop = properties.get(key);
        if (prop == null) return null;
        return propertyToValue(prop);
    }

    /// Returns true if the given key is present.
    ///
    /// @param key the property key
    /// @return true if present
    public boolean contains(String key) {
        ensureParsed();
        return properties.containsKey(key);
    }

    /// Returns all property keys.
    ///
    /// @return collection of keys
    public java.util.Collection<String> getKeys() {
        ensureParsed();
        return Collections.unmodifiableSet(properties.keySet());
    }

    /// Sets a property value. Creates or replaces the property.
    ///
    /// @param key   the property key
    /// @param value the value
    public void set(String key, XmpValue value) {
        ensureParsed();
        XmpProperty prop = createProperty(key, value);
        properties.put(key, prop);
        dirty = true;
    }

    /// Sets a property value from a string.
    ///
    /// @param key   the property key
    /// @param value the string value
    public void set(String key, String value) {
        set(key, new XmpValue(value));
    }

    /// Adds a property value. Same as set() — creates or replaces.
    ///
    /// @param key   the property key
    /// @param value the value
    public void add(String key, XmpValue value) {
        set(key, value);
    }

    /// Adds a property value from a string.
    ///
    /// @param key   the property key
    /// @param value the string value
    public void add(String key, String value) {
        set(key, value);
    }

    /// Removes a property.
    ///
    /// @param key the property key
    public void remove(String key) {
        ensureParsed();
        if (properties.remove(key) != null) {
            dirty = true;
        }
    }

    /// Registers a custom namespace prefix-URI mapping.
    ///
    /// @param prefix       the prefix
    /// @param namespaceUri the namespace URI
    public void registerNamespaceUri(String prefix, String namespaceUri) {
        registry.register(prefix, namespaceUri);
    }

    /// Registers a custom namespace with description.
    ///
    /// @param prefix            the prefix
    /// @param namespaceUri      the namespace URI
    /// @param schemaDescription ignored (for API compatibility)
    public void registerNamespaceUri(String prefix, String namespaceUri, String schemaDescription) {
        registry.register(prefix, namespaceUri);
    }

    /// Returns the namespace URI for a prefix.
    ///
    /// @param prefix the prefix
    /// @return the URI, or null
    public String getNamespaceUriByPrefix(String prefix) {
        return registry.getUri(prefix);
    }

    /// Returns the prefix for a namespace URI.
    ///
    /// @param namespaceUri the URI
    /// @return the prefix, or null
    public String getPrefixByNamespaceUri(String namespaceUri) {
        return registry.getPrefix(namespaceUri);
    }

    /// Returns the full XMP XML as UTF-8 bytes.
    ///
    /// @return the XMP XML bytes
    public byte[] getBytes() {
        // If we still hold the raw input bytes and nothing has been mutated
        // through the typed API, prefer the original byte payload. This keeps
        // user-supplied XMP packets (including non-rdf shapes like a bare
        // "<test/>") round-tripping verbatim through setXmpMetadata/save —
        // XmpWriter only emits properties it understands and would otherwise
        // discard everything outside <rdf:RDF> (PDFNEWNET-39955).
        if (!dirty && rawBytes != null && rawBytes.length > 0) {
            return rawBytes.clone();
        }
        ensureParsed();
        return XmpWriter.serialize(properties, registry);
    }

    /// Returns an iterator over all key-value pairs.
    @Override
    public Iterator<Map.Entry<String, XmpValue>> iterator() {
        ensureParsed();
        List<Map.Entry<String, XmpValue>> entries = new ArrayList<>();
        for (Map.Entry<String, XmpProperty> e : properties.entrySet()) {
            entries.add(new AbstractMap.SimpleEntry<>(e.getKey(), propertyToValue(e.getValue())));
        }
        return entries.iterator();
    }

    /// Returns true if properties have been modified since parsing.
    ///
    /// @return true if dirty
    public boolean isDirty() {
        return dirty;
    }

    /// Returns the internal namespace registry.
    ///
    /// @return the registry
    public XmpNamespaceRegistry getRegistry() {
        return registry;
    }

    /// Returns the internal properties map (for serialization by Document).
    ///
    /// @return the properties map
    public Map<String, XmpProperty> getProperties() {
        ensureParsed();
        return properties;
    }

    // ── Private helpers ──

    private void ensureParsed() {
        if (properties == null) {
            if (rawBytes != null && rawBytes.length > 0) {
                properties = XmpParser.parse(rawBytes, registry);
            } else {
                properties = new LinkedHashMap<>();
            }
        }
    }

    private XmpProperty createProperty(String key, XmpValue value) {
        XmpProperty.ValueType type = inferType(key);
        String strValue = value.toString();

        XmpProperty prop = new XmpProperty(key, strValue, type);

        if (type == XmpProperty.ValueType.LANG_ALT) {
            prop.addLangAltEntry("x-default", strValue);
        } else if (type == XmpProperty.ValueType.SEQ || type == XmpProperty.ValueType.BAG) {
            if (value.isArray() && value.toArray() != null) {
                List<String> items = new ArrayList<>();
                for (Object item : value.toArray()) {
                    items.add(String.valueOf(item));
                }
                prop.setArrayItems(items);
            } else {
                prop.addArrayItem(strValue);
            }
        }

        if (value.isNamedValues()) {
            prop.setType(XmpProperty.ValueType.STRUCT);
            List<XmpProperty> fields = new ArrayList<>();
            for (Map.Entry<String, XmpValue> entry : value.toNamedValues()) {
                fields.add(new XmpProperty(entry.getKey(), entry.getValue().toString(),
                        XmpProperty.ValueType.SIMPLE));
            }
            prop.setStructFields(fields);
        }

        return prop;
    }

    private XmpProperty.ValueType inferType(String key) {
        if (LANG_ALT_PROPS.contains(key)) return XmpProperty.ValueType.LANG_ALT;
        if (SEQ_PROPS.contains(key)) return XmpProperty.ValueType.SEQ;
        if (BAG_PROPS.contains(key)) return XmpProperty.ValueType.BAG;
        return XmpProperty.ValueType.SIMPLE;
    }

    private XmpValue propertyToValue(XmpProperty prop) {
        switch (prop.getType()) {
            case SEQ:
            case BAG: {
                List<String> items = prop.getArrayItems();
                if (!items.isEmpty()) {
                    return new XmpValue(items.toArray());
                }
                return new XmpValue(prop.getValue() != null ? prop.getValue() : "");
            }
            case STRUCT: {
                List<Map.Entry<String, XmpValue>> entries = new ArrayList<>();
                for (XmpProperty field : prop.getStructFields()) {
                    entries.add(new AbstractMap.SimpleEntry<>(field.getKey(),
                            propertyToValue(field)));
                }
                return new XmpValue(entries);
            }
            default:
                return new XmpValue(prop.getValue() != null ? prop.getValue() : "");
        }
    }
}
