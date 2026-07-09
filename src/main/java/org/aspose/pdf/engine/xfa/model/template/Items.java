package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>items</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Items extends XfaNode {

    /** Wraps a backing <code>items</code> element. */
    public Items(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** Allowed values of the <code>presence</code> attribute. */
    public enum PresenceValue {
        HIDDEN("hidden"),
        INACTIVE("inactive"),
        INVISIBLE("invisible"),
        VISIBLE("visible");
        private final String v;
        PresenceValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PresenceValue fromValue(String s) {
            for (PresenceValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>presence</code> attribute, or null. */
    public PresenceValue getPresence() {
        String v = getAttribute("presence");
        return v == null ? null : PresenceValue.fromValue(v);
    }
    /** Sets the <code>presence</code> attribute. */
    public void setPresence(PresenceValue value) {
        setAttribute("presence", value == null ? null : value.value());
    }
    /** @return the raw <code>presence</code> string, or null. */
    public String getPresenceRaw() { return getAttribute("presence"); }

    /** @return the typed <code>ref</code> attribute, or null. */
    public String getRef() { return getString("ref"); }
    /** Sets the <code>ref</code> attribute. */
    public void setRef(String value) { setAttribute("ref", value); }

    /** Allowed values of the <code>save</code> attribute. */
    public enum SaveValue {
        V_0("0"),
        V_1("1");
        private final String v;
        SaveValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static SaveValue fromValue(String s) {
            for (SaveValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>save</code> attribute, or null. */
    public SaveValue getSave() {
        String v = getAttribute("save");
        return v == null ? null : SaveValue.fromValue(v);
    }
    /** Sets the <code>save</code> attribute. */
    public void setSave(SaveValue value) {
        setAttribute("save", value == null ? null : value.value());
    }
    /** @return the raw <code>save</code> string, or null. */
    public String getSaveRaw() { return getAttribute("save"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>boolean</code> children (typed). */
    public java.util.List<Boolean> getBooleanList() {
        java.util.List<Boolean> r = new java.util.ArrayList<Boolean>();
        for (XfaNode n : getChildren("boolean")) { r.add((Boolean) n); }
        return r;
    }
    /** Appends a new <code>boolean</code> child. */
    public Boolean addBoolean() { return (Boolean) addChild("boolean"); }

    /** @return the <code>date</code> children (typed). */
    public java.util.List<Date> getDateList() {
        java.util.List<Date> r = new java.util.ArrayList<Date>();
        for (XfaNode n : getChildren("date")) { r.add((Date) n); }
        return r;
    }
    /** Appends a new <code>date</code> child. */
    public Date addDate() { return (Date) addChild("date"); }

    /** @return the <code>dateTime</code> children (typed). */
    public java.util.List<DateTime> getDateTimeList() {
        java.util.List<DateTime> r = new java.util.ArrayList<DateTime>();
        for (XfaNode n : getChildren("dateTime")) { r.add((DateTime) n); }
        return r;
    }
    /** Appends a new <code>dateTime</code> child. */
    public DateTime addDateTime() { return (DateTime) addChild("dateTime"); }

    /** @return the <code>decimal</code> children (typed). */
    public java.util.List<Decimal> getDecimalList() {
        java.util.List<Decimal> r = new java.util.ArrayList<Decimal>();
        for (XfaNode n : getChildren("decimal")) { r.add((Decimal) n); }
        return r;
    }
    /** Appends a new <code>decimal</code> child. */
    public Decimal addDecimal() { return (Decimal) addChild("decimal"); }

    /** @return the <code>exData</code> children (typed). */
    public java.util.List<ExData> getExDataList() {
        java.util.List<ExData> r = new java.util.ArrayList<ExData>();
        for (XfaNode n : getChildren("exData")) { r.add((ExData) n); }
        return r;
    }
    /** Appends a new <code>exData</code> child. */
    public ExData addExData() { return (ExData) addChild("exData"); }

    /** @return the <code>float</code> children (typed). */
    public java.util.List<Float> getFloatList() {
        java.util.List<Float> r = new java.util.ArrayList<Float>();
        for (XfaNode n : getChildren("float")) { r.add((Float) n); }
        return r;
    }
    /** Appends a new <code>float</code> child. */
    public Float addFloat() { return (Float) addChild("float"); }

    /** @return the <code>image</code> children (typed). */
    public java.util.List<Image> getImageList() {
        java.util.List<Image> r = new java.util.ArrayList<Image>();
        for (XfaNode n : getChildren("image")) { r.add((Image) n); }
        return r;
    }
    /** Appends a new <code>image</code> child. */
    public Image addImage() { return (Image) addChild("image"); }

    /** @return the <code>integer</code> children (typed). */
    public java.util.List<Integer> getIntegerList() {
        java.util.List<Integer> r = new java.util.ArrayList<Integer>();
        for (XfaNode n : getChildren("integer")) { r.add((Integer) n); }
        return r;
    }
    /** Appends a new <code>integer</code> child. */
    public Integer addInteger() { return (Integer) addChild("integer"); }

    /** @return the <code>text</code> children (typed). */
    public java.util.List<Text> getTextList() {
        java.util.List<Text> r = new java.util.ArrayList<Text>();
        for (XfaNode n : getChildren("text")) { r.add((Text) n); }
        return r;
    }
    /** Appends a new <code>text</code> child. */
    public Text addText() { return (Text) addChild("text"); }

    /** @return the <code>time</code> children (typed). */
    public java.util.List<Time> getTimeList() {
        java.util.List<Time> r = new java.util.ArrayList<Time>();
        for (XfaNode n : getChildren("time")) { r.add((Time) n); }
        return r;
    }
    /** Appends a new <code>time</code> child. */
    public Time addTime() { return (Time) addChild("time"); }
}
