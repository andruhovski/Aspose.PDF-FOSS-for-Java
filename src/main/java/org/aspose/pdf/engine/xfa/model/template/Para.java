package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>para</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Para extends XfaNode {

    /** Wraps a backing <code>para</code> element. */
    public Para(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>hAlign</code> attribute. */
    public enum HAlignValue {
        CENTER("center"),
        JUSTIFY("justify"),
        JUSTIFYALL("justifyAll"),
        LEFT("left"),
        RADIX("radix"),
        RIGHT("right");
        private final String v;
        HAlignValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static HAlignValue fromValue(String s) {
            for (HAlignValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>hAlign</code> attribute, or null. */
    public HAlignValue getHAlign() {
        String v = getAttribute("hAlign");
        return v == null ? null : HAlignValue.fromValue(v);
    }
    /** Sets the <code>hAlign</code> attribute. */
    public void setHAlign(HAlignValue value) {
        setAttribute("hAlign", value == null ? null : value.value());
    }
    /** @return the raw <code>hAlign</code> string, or null. */
    public String getHAlignRaw() { return getAttribute("hAlign"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>lineHeight</code> attribute, or null. */
    public XfaMeasurement getLineHeight() { return getMeasurement("lineHeight"); }
    /** Sets the <code>lineHeight</code> attribute. */
    public void setLineHeight(XfaMeasurement value) { setAttribute("lineHeight", value == null ? null : value.format()); }

    /** @return the typed <code>marginLeft</code> attribute, or null. */
    public XfaMeasurement getMarginLeft() { return getMeasurement("marginLeft"); }
    /** Sets the <code>marginLeft</code> attribute. */
    public void setMarginLeft(XfaMeasurement value) { setAttribute("marginLeft", value == null ? null : value.format()); }

    /** @return the typed <code>marginRight</code> attribute, or null. */
    public XfaMeasurement getMarginRight() { return getMeasurement("marginRight"); }
    /** Sets the <code>marginRight</code> attribute. */
    public void setMarginRight(XfaMeasurement value) { setAttribute("marginRight", value == null ? null : value.format()); }

    /** @return the typed <code>orphans</code> attribute, or null. */
    public java.lang.Integer getOrphans() { return getInteger("orphans"); }
    /** Sets the <code>orphans</code> attribute. */
    public void setOrphans(java.lang.Integer value) { setAttribute("orphans", value == null ? null : value.toString()); }

    /** @return the typed <code>preserve</code> attribute, or null. */
    public String getPreserve() { return getString("preserve"); }
    /** Sets the <code>preserve</code> attribute. */
    public void setPreserve(String value) { setAttribute("preserve", value); }

    /** @return the typed <code>radixOffset</code> attribute, or null. */
    public XfaMeasurement getRadixOffset() { return getMeasurement("radixOffset"); }
    /** Sets the <code>radixOffset</code> attribute. */
    public void setRadixOffset(XfaMeasurement value) { setAttribute("radixOffset", value == null ? null : value.format()); }

    /** @return the typed <code>spaceAbove</code> attribute, or null. */
    public XfaMeasurement getSpaceAbove() { return getMeasurement("spaceAbove"); }
    /** Sets the <code>spaceAbove</code> attribute. */
    public void setSpaceAbove(XfaMeasurement value) { setAttribute("spaceAbove", value == null ? null : value.format()); }

    /** @return the typed <code>spaceBelow</code> attribute, or null. */
    public XfaMeasurement getSpaceBelow() { return getMeasurement("spaceBelow"); }
    /** Sets the <code>spaceBelow</code> attribute. */
    public void setSpaceBelow(XfaMeasurement value) { setAttribute("spaceBelow", value == null ? null : value.format()); }

    /** @return the typed <code>tabDefault</code> attribute, or null. */
    public String getTabDefault() { return getString("tabDefault"); }
    /** Sets the <code>tabDefault</code> attribute. */
    public void setTabDefault(String value) { setAttribute("tabDefault", value); }

    /** @return the typed <code>tabStops</code> attribute, or null. */
    public String getTabStops() { return getString("tabStops"); }
    /** Sets the <code>tabStops</code> attribute. */
    public void setTabStops(String value) { setAttribute("tabStops", value); }

    /** @return the typed <code>textIndent</code> attribute, or null. */
    public XfaMeasurement getTextIndent() { return getMeasurement("textIndent"); }
    /** Sets the <code>textIndent</code> attribute. */
    public void setTextIndent(XfaMeasurement value) { setAttribute("textIndent", value == null ? null : value.format()); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** Allowed values of the <code>vAlign</code> attribute. */
    public enum VAlignValue {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");
        private final String v;
        VAlignValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static VAlignValue fromValue(String s) {
            for (VAlignValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>vAlign</code> attribute, or null. */
    public VAlignValue getVAlign() {
        String v = getAttribute("vAlign");
        return v == null ? null : VAlignValue.fromValue(v);
    }
    /** Sets the <code>vAlign</code> attribute. */
    public void setVAlign(VAlignValue value) {
        setAttribute("vAlign", value == null ? null : value.value());
    }
    /** @return the raw <code>vAlign</code> string, or null. */
    public String getVAlignRaw() { return getAttribute("vAlign"); }

    /** @return the typed <code>widows</code> attribute, or null. */
    public java.lang.Integer getWidows() { return getInteger("widows"); }
    /** Sets the <code>widows</code> attribute. */
    public void setWidows(java.lang.Integer value) { setAttribute("widows", value == null ? null : value.toString()); }

    /** @return the <code>hyphenation</code> child (typed), or null. */
    public Hyphenation getHyphenation() { return (Hyphenation) getChild("hyphenation"); }
    /** Ensures and returns the <code>hyphenation</code> child. */
    public Hyphenation ensureHyphenation() { return (Hyphenation) ensureChild("hyphenation"); }
}
