package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `hyphenation`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Hyphenation extends XfaNode {

    /// Wraps a backing `hyphenation` element.
    public Hyphenation(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `excludeAllCaps` attribute.
    public enum ExcludeAllCapsValue {
        V_0("0"),
        V_1("1");
        private final String v;
        ExcludeAllCapsValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ExcludeAllCapsValue fromValue(String s) {
            for (ExcludeAllCapsValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `excludeAllCaps` attribute, or null.
    public ExcludeAllCapsValue getExcludeAllCaps() {
        String v = getAttribute("excludeAllCaps");
        return v == null ? null : ExcludeAllCapsValue.fromValue(v);
    }
    /// Sets the `excludeAllCaps` attribute.
    public void setExcludeAllCaps(ExcludeAllCapsValue value) {
        setAttribute("excludeAllCaps", value == null ? null : value.value());
    }
    /// @return the raw `excludeAllCaps` string, or null.
    public String getExcludeAllCapsRaw() { return getAttribute("excludeAllCaps"); }

    /// Allowed values of the `excludeInitialCap` attribute.
    public enum ExcludeInitialCapValue {
        V_0("0"),
        V_1("1");
        private final String v;
        ExcludeInitialCapValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ExcludeInitialCapValue fromValue(String s) {
            for (ExcludeInitialCapValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `excludeInitialCap` attribute, or null.
    public ExcludeInitialCapValue getExcludeInitialCap() {
        String v = getAttribute("excludeInitialCap");
        return v == null ? null : ExcludeInitialCapValue.fromValue(v);
    }
    /// Sets the `excludeInitialCap` attribute.
    public void setExcludeInitialCap(ExcludeInitialCapValue value) {
        setAttribute("excludeInitialCap", value == null ? null : value.value());
    }
    /// @return the raw `excludeInitialCap` string, or null.
    public String getExcludeInitialCapRaw() { return getAttribute("excludeInitialCap"); }

    /// Allowed values of the `hyphenate` attribute.
    public enum HyphenateValue {
        V_0("0"),
        V_1("1");
        private final String v;
        HyphenateValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static HyphenateValue fromValue(String s) {
            for (HyphenateValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `hyphenate` attribute, or null.
    public HyphenateValue getHyphenate() {
        String v = getAttribute("hyphenate");
        return v == null ? null : HyphenateValue.fromValue(v);
    }
    /// Sets the `hyphenate` attribute.
    public void setHyphenate(HyphenateValue value) {
        setAttribute("hyphenate", value == null ? null : value.value());
    }
    /// @return the raw `hyphenate` string, or null.
    public String getHyphenateRaw() { return getAttribute("hyphenate"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `pushCharacterCount` attribute, or null.
    public java.lang.Integer getPushCharacterCount() { return getInteger("pushCharacterCount"); }
    /// Sets the `pushCharacterCount` attribute.
    public void setPushCharacterCount(java.lang.Integer value) { setAttribute("pushCharacterCount", value == null ? null : value.toString()); }

    /// @return the typed `remainCharacterCount` attribute, or null.
    public java.lang.Integer getRemainCharacterCount() { return getInteger("remainCharacterCount"); }
    /// Sets the `remainCharacterCount` attribute.
    public void setRemainCharacterCount(java.lang.Integer value) { setAttribute("remainCharacterCount", value == null ? null : value.toString()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the typed `wordCharacterCount` attribute, or null.
    public java.lang.Integer getWordCharacterCount() { return getInteger("wordCharacterCount"); }
    /// Sets the `wordCharacterCount` attribute.
    public void setWordCharacterCount(java.lang.Integer value) { setAttribute("wordCharacterCount", value == null ? null : value.toString()); }
}
