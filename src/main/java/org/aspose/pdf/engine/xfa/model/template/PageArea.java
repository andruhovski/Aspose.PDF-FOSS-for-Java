package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `pageArea`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class PageArea extends XfaNode {

    /// Wraps a backing `pageArea` element.
    public PageArea(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `blankOrNotBlank` attribute.
    public enum BlankOrNotBlankValue {
        ANY("any"),
        BLANK("blank"),
        NOTBLANK("notBlank");
        private final String v;
        BlankOrNotBlankValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static BlankOrNotBlankValue fromValue(String s) {
            for (BlankOrNotBlankValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `blankOrNotBlank` attribute, or null.
    public BlankOrNotBlankValue getBlankOrNotBlank() {
        String v = getAttribute("blankOrNotBlank");
        return v == null ? null : BlankOrNotBlankValue.fromValue(v);
    }
    /// Sets the `blankOrNotBlank` attribute.
    public void setBlankOrNotBlank(BlankOrNotBlankValue value) {
        setAttribute("blankOrNotBlank", value == null ? null : value.value());
    }
    /// @return the raw `blankOrNotBlank` string, or null.
    public String getBlankOrNotBlankRaw() { return getAttribute("blankOrNotBlank"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `initialNumber` attribute, or null.
    public java.lang.Integer getInitialNumber() { return getInteger("initialNumber"); }
    /// Sets the `initialNumber` attribute.
    public void setInitialNumber(java.lang.Integer value) { setAttribute("initialNumber", value == null ? null : value.toString()); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `numbered` attribute, or null.
    public java.lang.Integer getNumbered() { return getInteger("numbered"); }
    /// Sets the `numbered` attribute.
    public void setNumbered(java.lang.Integer value) { setAttribute("numbered", value == null ? null : value.toString()); }

    /// Allowed values of the `oddOrEven` attribute.
    public enum OddOrEvenValue {
        ANY("any"),
        EVEN("even"),
        ODD("odd");
        private final String v;
        OddOrEvenValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OddOrEvenValue fromValue(String s) {
            for (OddOrEvenValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `oddOrEven` attribute, or null.
    public OddOrEvenValue getOddOrEven() {
        String v = getAttribute("oddOrEven");
        return v == null ? null : OddOrEvenValue.fromValue(v);
    }
    /// Sets the `oddOrEven` attribute.
    public void setOddOrEven(OddOrEvenValue value) {
        setAttribute("oddOrEven", value == null ? null : value.value());
    }
    /// @return the raw `oddOrEven` string, or null.
    public String getOddOrEvenRaw() { return getAttribute("oddOrEven"); }

    /// Allowed values of the `pagePosition` attribute.
    public enum PagePositionValue {
        ANY("any"),
        FIRST("first"),
        LAST("last"),
        ONLY("only"),
        REST("rest");
        private final String v;
        PagePositionValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PagePositionValue fromValue(String s) {
            for (PagePositionValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `pagePosition` attribute, or null.
    public PagePositionValue getPagePosition() {
        String v = getAttribute("pagePosition");
        return v == null ? null : PagePositionValue.fromValue(v);
    }
    /// Sets the `pagePosition` attribute.
    public void setPagePosition(PagePositionValue value) {
        setAttribute("pagePosition", value == null ? null : value.value());
    }
    /// @return the raw `pagePosition` string, or null.
    public String getPagePositionRaw() { return getAttribute("pagePosition"); }

    /// @return the typed `relevant` attribute, or null.
    public String getRelevant() { return getString("relevant"); }
    /// Sets the `relevant` attribute.
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `desc` child (typed), or null.
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /// Ensures and returns the `desc` child.
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `medium` child (typed), or null.
    public Medium getMedium() { return (Medium) getChild("medium"); }
    /// Ensures and returns the `medium` child.
    public Medium ensureMedium() { return (Medium) ensureChild("medium"); }

    /// @return the `occur` child (typed), or null.
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /// Ensures and returns the `occur` child.
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /// @return the `area` children (typed).
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /// Appends a new `area` child.
    public Area addArea() { return (Area) addChild("area"); }

    /// @return the `contentArea` children (typed).
    public java.util.List<ContentArea> getContentAreaList() {
        java.util.List<ContentArea> r = new java.util.ArrayList<ContentArea>();
        for (XfaNode n : getChildren("contentArea")) { r.add((ContentArea) n); }
        return r;
    }
    /// Appends a new `contentArea` child.
    public ContentArea addContentArea() { return (ContentArea) addChild("contentArea"); }

    /// @return the `draw` children (typed).
    public java.util.List<Draw> getDrawList() {
        java.util.List<Draw> r = new java.util.ArrayList<Draw>();
        for (XfaNode n : getChildren("draw")) { r.add((Draw) n); }
        return r;
    }
    /// Appends a new `draw` child.
    public Draw addDraw() { return (Draw) addChild("draw"); }

    /// @return the `exclGroup` children (typed).
    public java.util.List<ExclGroup> getExclGroupList() {
        java.util.List<ExclGroup> r = new java.util.ArrayList<ExclGroup>();
        for (XfaNode n : getChildren("exclGroup")) { r.add((ExclGroup) n); }
        return r;
    }
    /// Appends a new `exclGroup` child.
    public ExclGroup addExclGroup() { return (ExclGroup) addChild("exclGroup"); }

    /// @return the `field` children (typed).
    public java.util.List<Field> getFieldList() {
        java.util.List<Field> r = new java.util.ArrayList<Field>();
        for (XfaNode n : getChildren("field")) { r.add((Field) n); }
        return r;
    }
    /// Appends a new `field` child.
    public Field addField() { return (Field) addChild("field"); }

    /// @return the `subform` children (typed).
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /// Appends a new `subform` child.
    public Subform addSubform() { return (Subform) addChild("subform"); }
}
