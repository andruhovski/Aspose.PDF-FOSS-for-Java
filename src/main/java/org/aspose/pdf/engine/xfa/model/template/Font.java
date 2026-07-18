package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `font`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Font extends XfaNode {

    /// Wraps a backing `font` element.
    public Font(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `baselineShift` attribute, or null.
    public XfaMeasurement getBaselineShift() { return getMeasurement("baselineShift"); }
    /// Sets the `baselineShift` attribute.
    public void setBaselineShift(XfaMeasurement value) { setAttribute("baselineShift", value == null ? null : value.format()); }

    /// @return the typed `fontHorizontalScale` attribute, or null.
    public String getFontHorizontalScale() { return getString("fontHorizontalScale"); }
    /// Sets the `fontHorizontalScale` attribute.
    public void setFontHorizontalScale(String value) { setAttribute("fontHorizontalScale", value); }

    /// @return the typed `fontVerticalScale` attribute, or null.
    public String getFontVerticalScale() { return getString("fontVerticalScale"); }
    /// Sets the `fontVerticalScale` attribute.
    public void setFontVerticalScale(String value) { setAttribute("fontVerticalScale", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `kerningMode` attribute.
    public enum KerningModeValue {
        NONE("none"),
        PAIR("pair");
        private final String v;
        KerningModeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static KerningModeValue fromValue(String s) {
            for (KerningModeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `kerningMode` attribute, or null.
    public KerningModeValue getKerningMode() {
        String v = getAttribute("kerningMode");
        return v == null ? null : KerningModeValue.fromValue(v);
    }
    /// Sets the `kerningMode` attribute.
    public void setKerningMode(KerningModeValue value) {
        setAttribute("kerningMode", value == null ? null : value.value());
    }
    /// @return the raw `kerningMode` string, or null.
    public String getKerningModeRaw() { return getAttribute("kerningMode"); }

    /// @return the typed `letterSpacing` attribute, or null.
    public String getLetterSpacing() { return getString("letterSpacing"); }
    /// Sets the `letterSpacing` attribute.
    public void setLetterSpacing(String value) { setAttribute("letterSpacing", value); }

    /// Allowed values of the `lineThrough` attribute.
    public enum LineThroughValue {
        V_0("0"),
        V_1("1"),
        V_2("2");
        private final String v;
        LineThroughValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static LineThroughValue fromValue(String s) {
            for (LineThroughValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `lineThrough` attribute, or null.
    public LineThroughValue getLineThrough() {
        String v = getAttribute("lineThrough");
        return v == null ? null : LineThroughValue.fromValue(v);
    }
    /// Sets the `lineThrough` attribute.
    public void setLineThrough(LineThroughValue value) {
        setAttribute("lineThrough", value == null ? null : value.value());
    }
    /// @return the raw `lineThrough` string, or null.
    public String getLineThroughRaw() { return getAttribute("lineThrough"); }

    /// Allowed values of the `lineThroughPeriod` attribute.
    public enum LineThroughPeriodValue {
        ALL("all"),
        WORD("word");
        private final String v;
        LineThroughPeriodValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static LineThroughPeriodValue fromValue(String s) {
            for (LineThroughPeriodValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `lineThroughPeriod` attribute, or null.
    public LineThroughPeriodValue getLineThroughPeriod() {
        String v = getAttribute("lineThroughPeriod");
        return v == null ? null : LineThroughPeriodValue.fromValue(v);
    }
    /// Sets the `lineThroughPeriod` attribute.
    public void setLineThroughPeriod(LineThroughPeriodValue value) {
        setAttribute("lineThroughPeriod", value == null ? null : value.value());
    }
    /// @return the raw `lineThroughPeriod` string, or null.
    public String getLineThroughPeriodRaw() { return getAttribute("lineThroughPeriod"); }

    /// Allowed values of the `overline` attribute.
    public enum OverlineValue {
        V_0("0"),
        V_1("1"),
        V_2("2");
        private final String v;
        OverlineValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OverlineValue fromValue(String s) {
            for (OverlineValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `overline` attribute, or null.
    public OverlineValue getOverline() {
        String v = getAttribute("overline");
        return v == null ? null : OverlineValue.fromValue(v);
    }
    /// Sets the `overline` attribute.
    public void setOverline(OverlineValue value) {
        setAttribute("overline", value == null ? null : value.value());
    }
    /// @return the raw `overline` string, or null.
    public String getOverlineRaw() { return getAttribute("overline"); }

    /// Allowed values of the `overlinePeriod` attribute.
    public enum OverlinePeriodValue {
        ALL("all"),
        WORD("word");
        private final String v;
        OverlinePeriodValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OverlinePeriodValue fromValue(String s) {
            for (OverlinePeriodValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `overlinePeriod` attribute, or null.
    public OverlinePeriodValue getOverlinePeriod() {
        String v = getAttribute("overlinePeriod");
        return v == null ? null : OverlinePeriodValue.fromValue(v);
    }
    /// Sets the `overlinePeriod` attribute.
    public void setOverlinePeriod(OverlinePeriodValue value) {
        setAttribute("overlinePeriod", value == null ? null : value.value());
    }
    /// @return the raw `overlinePeriod` string, or null.
    public String getOverlinePeriodRaw() { return getAttribute("overlinePeriod"); }

    /// Allowed values of the `posture` attribute.
    public enum PostureValue {
        ITALIC("italic"),
        NORMAL("normal");
        private final String v;
        PostureValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PostureValue fromValue(String s) {
            for (PostureValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `posture` attribute, or null.
    public PostureValue getPosture() {
        String v = getAttribute("posture");
        return v == null ? null : PostureValue.fromValue(v);
    }
    /// Sets the `posture` attribute.
    public void setPosture(PostureValue value) {
        setAttribute("posture", value == null ? null : value.value());
    }
    /// @return the raw `posture` string, or null.
    public String getPostureRaw() { return getAttribute("posture"); }

    /// @return the typed `size` attribute, or null.
    public XfaMeasurement getSize() { return getMeasurement("size"); }
    /// Sets the `size` attribute.
    public void setSize(XfaMeasurement value) { setAttribute("size", value == null ? null : value.format()); }

    /// @return the typed `typeface` attribute, or null.
    public String getTypeface() { return getString("typeface"); }
    /// Sets the `typeface` attribute.
    public void setTypeface(String value) { setAttribute("typeface", value); }

    /// Allowed values of the `underline` attribute.
    public enum UnderlineValue {
        V_0("0"),
        V_1("1"),
        V_2("2");
        private final String v;
        UnderlineValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static UnderlineValue fromValue(String s) {
            for (UnderlineValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `underline` attribute, or null.
    public UnderlineValue getUnderline() {
        String v = getAttribute("underline");
        return v == null ? null : UnderlineValue.fromValue(v);
    }
    /// Sets the `underline` attribute.
    public void setUnderline(UnderlineValue value) {
        setAttribute("underline", value == null ? null : value.value());
    }
    /// @return the raw `underline` string, or null.
    public String getUnderlineRaw() { return getAttribute("underline"); }

    /// Allowed values of the `underlinePeriod` attribute.
    public enum UnderlinePeriodValue {
        ALL("all"),
        WORD("word");
        private final String v;
        UnderlinePeriodValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static UnderlinePeriodValue fromValue(String s) {
            for (UnderlinePeriodValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `underlinePeriod` attribute, or null.
    public UnderlinePeriodValue getUnderlinePeriod() {
        String v = getAttribute("underlinePeriod");
        return v == null ? null : UnderlinePeriodValue.fromValue(v);
    }
    /// Sets the `underlinePeriod` attribute.
    public void setUnderlinePeriod(UnderlinePeriodValue value) {
        setAttribute("underlinePeriod", value == null ? null : value.value());
    }
    /// @return the raw `underlinePeriod` string, or null.
    public String getUnderlinePeriodRaw() { return getAttribute("underlinePeriod"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// Allowed values of the `weight` attribute.
    public enum WeightValue {
        BOLD("bold"),
        NORMAL("normal");
        private final String v;
        WeightValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static WeightValue fromValue(String s) {
            for (WeightValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `weight` attribute, or null.
    public WeightValue getWeight() {
        String v = getAttribute("weight");
        return v == null ? null : WeightValue.fromValue(v);
    }
    /// Sets the `weight` attribute.
    public void setWeight(WeightValue value) {
        setAttribute("weight", value == null ? null : value.value());
    }
    /// @return the raw `weight` string, or null.
    public String getWeightRaw() { return getAttribute("weight"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `fill` child (typed), or null.
    public Fill getFill() { return (Fill) getChild("fill"); }
    /// Ensures and returns the `fill` child.
    public Fill ensureFill() { return (Fill) ensureChild("fill"); }
}
