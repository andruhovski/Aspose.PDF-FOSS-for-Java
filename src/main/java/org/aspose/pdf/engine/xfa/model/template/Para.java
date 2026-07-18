package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `para`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Para extends XfaNode {

    /// Wraps a backing `para` element.
    public Para(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `hAlign` attribute.
    public enum HAlignValue {
        CENTER("center"),
        JUSTIFY("justify"),
        JUSTIFYALL("justifyAll"),
        LEFT("left"),
        RADIX("radix"),
        RIGHT("right");
        private final String v;
        HAlignValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static HAlignValue fromValue(String s) {
            for (HAlignValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `hAlign` attribute, or null.
    public HAlignValue getHAlign() {
        String v = getAttribute("hAlign");
        return v == null ? null : HAlignValue.fromValue(v);
    }
    /// Sets the `hAlign` attribute.
    public void setHAlign(HAlignValue value) {
        setAttribute("hAlign", value == null ? null : value.value());
    }
    /// @return the raw `hAlign` string, or null.
    public String getHAlignRaw() { return getAttribute("hAlign"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `lineHeight` attribute, or null.
    public XfaMeasurement getLineHeight() { return getMeasurement("lineHeight"); }
    /// Sets the `lineHeight` attribute.
    public void setLineHeight(XfaMeasurement value) { setAttribute("lineHeight", value == null ? null : value.format()); }

    /// @return the typed `marginLeft` attribute, or null.
    public XfaMeasurement getMarginLeft() { return getMeasurement("marginLeft"); }
    /// Sets the `marginLeft` attribute.
    public void setMarginLeft(XfaMeasurement value) { setAttribute("marginLeft", value == null ? null : value.format()); }

    /// @return the typed `marginRight` attribute, or null.
    public XfaMeasurement getMarginRight() { return getMeasurement("marginRight"); }
    /// Sets the `marginRight` attribute.
    public void setMarginRight(XfaMeasurement value) { setAttribute("marginRight", value == null ? null : value.format()); }

    /// @return the typed `orphans` attribute, or null.
    public java.lang.Integer getOrphans() { return getInteger("orphans"); }
    /// Sets the `orphans` attribute.
    public void setOrphans(java.lang.Integer value) { setAttribute("orphans", value == null ? null : value.toString()); }

    /// @return the typed `preserve` attribute, or null.
    public String getPreserve() { return getString("preserve"); }
    /// Sets the `preserve` attribute.
    public void setPreserve(String value) { setAttribute("preserve", value); }

    /// @return the typed `radixOffset` attribute, or null.
    public XfaMeasurement getRadixOffset() { return getMeasurement("radixOffset"); }
    /// Sets the `radixOffset` attribute.
    public void setRadixOffset(XfaMeasurement value) { setAttribute("radixOffset", value == null ? null : value.format()); }

    /// @return the typed `spaceAbove` attribute, or null.
    public XfaMeasurement getSpaceAbove() { return getMeasurement("spaceAbove"); }
    /// Sets the `spaceAbove` attribute.
    public void setSpaceAbove(XfaMeasurement value) { setAttribute("spaceAbove", value == null ? null : value.format()); }

    /// @return the typed `spaceBelow` attribute, or null.
    public XfaMeasurement getSpaceBelow() { return getMeasurement("spaceBelow"); }
    /// Sets the `spaceBelow` attribute.
    public void setSpaceBelow(XfaMeasurement value) { setAttribute("spaceBelow", value == null ? null : value.format()); }

    /// @return the typed `tabDefault` attribute, or null.
    public String getTabDefault() { return getString("tabDefault"); }
    /// Sets the `tabDefault` attribute.
    public void setTabDefault(String value) { setAttribute("tabDefault", value); }

    /// @return the typed `tabStops` attribute, or null.
    public String getTabStops() { return getString("tabStops"); }
    /// Sets the `tabStops` attribute.
    public void setTabStops(String value) { setAttribute("tabStops", value); }

    /// @return the typed `textIndent` attribute, or null.
    public XfaMeasurement getTextIndent() { return getMeasurement("textIndent"); }
    /// Sets the `textIndent` attribute.
    public void setTextIndent(XfaMeasurement value) { setAttribute("textIndent", value == null ? null : value.format()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// Allowed values of the `vAlign` attribute.
    public enum VAlignValue {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");
        private final String v;
        VAlignValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static VAlignValue fromValue(String s) {
            for (VAlignValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `vAlign` attribute, or null.
    public VAlignValue getVAlign() {
        String v = getAttribute("vAlign");
        return v == null ? null : VAlignValue.fromValue(v);
    }
    /// Sets the `vAlign` attribute.
    public void setVAlign(VAlignValue value) {
        setAttribute("vAlign", value == null ? null : value.value());
    }
    /// @return the raw `vAlign` string, or null.
    public String getVAlignRaw() { return getAttribute("vAlign"); }

    /// @return the typed `widows` attribute, or null.
    public java.lang.Integer getWidows() { return getInteger("widows"); }
    /// Sets the `widows` attribute.
    public void setWidows(java.lang.Integer value) { setAttribute("widows", value == null ? null : value.toString()); }

    /// @return the `hyphenation` child (typed), or null.
    public Hyphenation getHyphenation() { return (Hyphenation) getChild("hyphenation"); }
    /// Ensures and returns the `hyphenation` child.
    public Hyphenation ensureHyphenation() { return (Hyphenation) ensureChild("hyphenation"); }
}
