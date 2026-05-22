package org.aspose.pdf.engine.xmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Internal representation of an XMP property value.
 * <p>
 * Supports simple text, URI, Language Alternative (rdf:Alt),
 * ordered array (rdf:Seq), unordered array (rdf:Bag), and
 * structure (nested properties) value types.
 * </p>
 */
public class XmpProperty {

    /** The type of XMP property value. */
    public enum ValueType {
        SIMPLE,
        URI,
        LANG_ALT,
        SEQ,
        BAG,
        STRUCT
    }

    private final String key;
    private String value;
    private ValueType type;
    private List<String> arrayItems;
    private List<LangAltEntry> langAltEntries;
    private List<XmpProperty> structFields;

    /**
     * Creates a simple text property.
     *
     * @param key   the property key ("prefix:localName")
     * @param value the string value
     * @param type  the value type
     */
    public XmpProperty(String key, String value, ValueType type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    /** Returns the property key. */
    public String getKey() { return key; }

    /** Returns the simple string value (or x-default for lang-alt, first item for arrays). */
    public String getValue() {
        if (type == ValueType.LANG_ALT && langAltEntries != null && !langAltEntries.isEmpty()) {
            for (LangAltEntry e : langAltEntries) {
                if ("x-default".equals(e.lang)) return e.value;
            }
            return langAltEntries.get(0).value;
        }
        if ((type == ValueType.SEQ || type == ValueType.BAG)
                && arrayItems != null && !arrayItems.isEmpty()) {
            return arrayItems.get(0);
        }
        return value;
    }

    /** Sets the simple string value. */
    public void setValue(String value) { this.value = value; }

    /** Returns the value type. */
    public ValueType getType() { return type; }

    /** Sets the value type. */
    public void setType(ValueType type) { this.type = type; }

    /** Returns array items (for SEQ/BAG types). */
    public List<String> getArrayItems() {
        return arrayItems != null ? arrayItems : Collections.emptyList();
    }

    /** Sets array items. */
    public void setArrayItems(List<String> items) { this.arrayItems = items; }

    /** Returns language alternative entries (for LANG_ALT type). */
    public List<LangAltEntry> getLangAltEntries() {
        return langAltEntries != null ? langAltEntries : Collections.emptyList();
    }

    /** Sets language alternative entries. */
    public void setLangAltEntries(List<LangAltEntry> entries) { this.langAltEntries = entries; }

    /** Returns structure fields (for STRUCT type). */
    public List<XmpProperty> getStructFields() {
        return structFields != null ? structFields : Collections.emptyList();
    }

    /** Sets structure fields. */
    public void setStructFields(List<XmpProperty> fields) { this.structFields = fields; }

    /** Adds an array item. */
    public void addArrayItem(String item) {
        if (arrayItems == null) arrayItems = new ArrayList<>();
        arrayItems.add(item);
    }

    /** Adds a language alternative entry. */
    public void addLangAltEntry(String lang, String text) {
        if (langAltEntries == null) langAltEntries = new ArrayList<>();
        langAltEntries.add(new LangAltEntry(lang, text));
    }

    /** A language-tagged value entry in a Language Alternative. */
    public static class LangAltEntry {
        /** The language tag (e.g. "x-default", "en-US"). */
        public final String lang;
        /** The text value. */
        public final String value;

        public LangAltEntry(String lang, String value) {
            this.lang = lang;
            this.value = value;
        }
    }
}
