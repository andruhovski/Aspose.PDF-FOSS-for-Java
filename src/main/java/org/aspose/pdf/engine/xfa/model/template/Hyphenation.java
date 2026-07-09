package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>hyphenation</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Hyphenation extends XfaNode {

    /** Wraps a backing <code>hyphenation</code> element. */
    public Hyphenation(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>excludeAllCaps</code> attribute. */
    public enum ExcludeAllCapsValue {
        V_0("0"),
        V_1("1");
        private final String v;
        ExcludeAllCapsValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ExcludeAllCapsValue fromValue(String s) {
            for (ExcludeAllCapsValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>excludeAllCaps</code> attribute, or null. */
    public ExcludeAllCapsValue getExcludeAllCaps() {
        String v = getAttribute("excludeAllCaps");
        return v == null ? null : ExcludeAllCapsValue.fromValue(v);
    }
    /** Sets the <code>excludeAllCaps</code> attribute. */
    public void setExcludeAllCaps(ExcludeAllCapsValue value) {
        setAttribute("excludeAllCaps", value == null ? null : value.value());
    }
    /** @return the raw <code>excludeAllCaps</code> string, or null. */
    public String getExcludeAllCapsRaw() { return getAttribute("excludeAllCaps"); }

    /** Allowed values of the <code>excludeInitialCap</code> attribute. */
    public enum ExcludeInitialCapValue {
        V_0("0"),
        V_1("1");
        private final String v;
        ExcludeInitialCapValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ExcludeInitialCapValue fromValue(String s) {
            for (ExcludeInitialCapValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>excludeInitialCap</code> attribute, or null. */
    public ExcludeInitialCapValue getExcludeInitialCap() {
        String v = getAttribute("excludeInitialCap");
        return v == null ? null : ExcludeInitialCapValue.fromValue(v);
    }
    /** Sets the <code>excludeInitialCap</code> attribute. */
    public void setExcludeInitialCap(ExcludeInitialCapValue value) {
        setAttribute("excludeInitialCap", value == null ? null : value.value());
    }
    /** @return the raw <code>excludeInitialCap</code> string, or null. */
    public String getExcludeInitialCapRaw() { return getAttribute("excludeInitialCap"); }

    /** Allowed values of the <code>hyphenate</code> attribute. */
    public enum HyphenateValue {
        V_0("0"),
        V_1("1");
        private final String v;
        HyphenateValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static HyphenateValue fromValue(String s) {
            for (HyphenateValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>hyphenate</code> attribute, or null. */
    public HyphenateValue getHyphenate() {
        String v = getAttribute("hyphenate");
        return v == null ? null : HyphenateValue.fromValue(v);
    }
    /** Sets the <code>hyphenate</code> attribute. */
    public void setHyphenate(HyphenateValue value) {
        setAttribute("hyphenate", value == null ? null : value.value());
    }
    /** @return the raw <code>hyphenate</code> string, or null. */
    public String getHyphenateRaw() { return getAttribute("hyphenate"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>pushCharacterCount</code> attribute, or null. */
    public java.lang.Integer getPushCharacterCount() { return getInteger("pushCharacterCount"); }
    /** Sets the <code>pushCharacterCount</code> attribute. */
    public void setPushCharacterCount(java.lang.Integer value) { setAttribute("pushCharacterCount", value == null ? null : value.toString()); }

    /** @return the typed <code>remainCharacterCount</code> attribute, or null. */
    public java.lang.Integer getRemainCharacterCount() { return getInteger("remainCharacterCount"); }
    /** Sets the <code>remainCharacterCount</code> attribute. */
    public void setRemainCharacterCount(java.lang.Integer value) { setAttribute("remainCharacterCount", value == null ? null : value.toString()); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>wordCharacterCount</code> attribute, or null. */
    public java.lang.Integer getWordCharacterCount() { return getInteger("wordCharacterCount"); }
    /** Sets the <code>wordCharacterCount</code> attribute. */
    public void setWordCharacterCount(java.lang.Integer value) { setAttribute("wordCharacterCount", value == null ? null : value.toString()); }
}
