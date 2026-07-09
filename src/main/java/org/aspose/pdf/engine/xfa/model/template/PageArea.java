package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>pageArea</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class PageArea extends XfaNode {

    /** Wraps a backing <code>pageArea</code> element. */
    public PageArea(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>blankOrNotBlank</code> attribute. */
    public enum BlankOrNotBlankValue {
        ANY("any"),
        BLANK("blank"),
        NOTBLANK("notBlank");
        private final String v;
        BlankOrNotBlankValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static BlankOrNotBlankValue fromValue(String s) {
            for (BlankOrNotBlankValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>blankOrNotBlank</code> attribute, or null. */
    public BlankOrNotBlankValue getBlankOrNotBlank() {
        String v = getAttribute("blankOrNotBlank");
        return v == null ? null : BlankOrNotBlankValue.fromValue(v);
    }
    /** Sets the <code>blankOrNotBlank</code> attribute. */
    public void setBlankOrNotBlank(BlankOrNotBlankValue value) {
        setAttribute("blankOrNotBlank", value == null ? null : value.value());
    }
    /** @return the raw <code>blankOrNotBlank</code> string, or null. */
    public String getBlankOrNotBlankRaw() { return getAttribute("blankOrNotBlank"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>initialNumber</code> attribute, or null. */
    public java.lang.Integer getInitialNumber() { return getInteger("initialNumber"); }
    /** Sets the <code>initialNumber</code> attribute. */
    public void setInitialNumber(java.lang.Integer value) { setAttribute("initialNumber", value == null ? null : value.toString()); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>numbered</code> attribute, or null. */
    public java.lang.Integer getNumbered() { return getInteger("numbered"); }
    /** Sets the <code>numbered</code> attribute. */
    public void setNumbered(java.lang.Integer value) { setAttribute("numbered", value == null ? null : value.toString()); }

    /** Allowed values of the <code>oddOrEven</code> attribute. */
    public enum OddOrEvenValue {
        ANY("any"),
        EVEN("even"),
        ODD("odd");
        private final String v;
        OddOrEvenValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static OddOrEvenValue fromValue(String s) {
            for (OddOrEvenValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>oddOrEven</code> attribute, or null. */
    public OddOrEvenValue getOddOrEven() {
        String v = getAttribute("oddOrEven");
        return v == null ? null : OddOrEvenValue.fromValue(v);
    }
    /** Sets the <code>oddOrEven</code> attribute. */
    public void setOddOrEven(OddOrEvenValue value) {
        setAttribute("oddOrEven", value == null ? null : value.value());
    }
    /** @return the raw <code>oddOrEven</code> string, or null. */
    public String getOddOrEvenRaw() { return getAttribute("oddOrEven"); }

    /** Allowed values of the <code>pagePosition</code> attribute. */
    public enum PagePositionValue {
        ANY("any"),
        FIRST("first"),
        LAST("last"),
        ONLY("only"),
        REST("rest");
        private final String v;
        PagePositionValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PagePositionValue fromValue(String s) {
            for (PagePositionValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>pagePosition</code> attribute, or null. */
    public PagePositionValue getPagePosition() {
        String v = getAttribute("pagePosition");
        return v == null ? null : PagePositionValue.fromValue(v);
    }
    /** Sets the <code>pagePosition</code> attribute. */
    public void setPagePosition(PagePositionValue value) {
        setAttribute("pagePosition", value == null ? null : value.value());
    }
    /** @return the raw <code>pagePosition</code> string, or null. */
    public String getPagePositionRaw() { return getAttribute("pagePosition"); }

    /** @return the typed <code>relevant</code> attribute, or null. */
    public String getRelevant() { return getString("relevant"); }
    /** Sets the <code>relevant</code> attribute. */
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>medium</code> child (typed), or null. */
    public Medium getMedium() { return (Medium) getChild("medium"); }
    /** Ensures and returns the <code>medium</code> child. */
    public Medium ensureMedium() { return (Medium) ensureChild("medium"); }

    /** @return the <code>occur</code> child (typed), or null. */
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /** Ensures and returns the <code>occur</code> child. */
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /** @return the <code>area</code> children (typed). */
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /** Appends a new <code>area</code> child. */
    public Area addArea() { return (Area) addChild("area"); }

    /** @return the <code>contentArea</code> children (typed). */
    public java.util.List<ContentArea> getContentAreaList() {
        java.util.List<ContentArea> r = new java.util.ArrayList<ContentArea>();
        for (XfaNode n : getChildren("contentArea")) { r.add((ContentArea) n); }
        return r;
    }
    /** Appends a new <code>contentArea</code> child. */
    public ContentArea addContentArea() { return (ContentArea) addChild("contentArea"); }

    /** @return the <code>draw</code> children (typed). */
    public java.util.List<Draw> getDrawList() {
        java.util.List<Draw> r = new java.util.ArrayList<Draw>();
        for (XfaNode n : getChildren("draw")) { r.add((Draw) n); }
        return r;
    }
    /** Appends a new <code>draw</code> child. */
    public Draw addDraw() { return (Draw) addChild("draw"); }

    /** @return the <code>exclGroup</code> children (typed). */
    public java.util.List<ExclGroup> getExclGroupList() {
        java.util.List<ExclGroup> r = new java.util.ArrayList<ExclGroup>();
        for (XfaNode n : getChildren("exclGroup")) { r.add((ExclGroup) n); }
        return r;
    }
    /** Appends a new <code>exclGroup</code> child. */
    public ExclGroup addExclGroup() { return (ExclGroup) addChild("exclGroup"); }

    /** @return the <code>field</code> children (typed). */
    public java.util.List<Field> getFieldList() {
        java.util.List<Field> r = new java.util.ArrayList<Field>();
        for (XfaNode n : getChildren("field")) { r.add((Field) n); }
        return r;
    }
    /** Appends a new <code>field</code> child. */
    public Field addField() { return (Field) addChild("field"); }

    /** @return the <code>subform</code> children (typed). */
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /** Appends a new <code>subform</code> child. */
    public Subform addSubform() { return (Subform) addChild("subform"); }
}
