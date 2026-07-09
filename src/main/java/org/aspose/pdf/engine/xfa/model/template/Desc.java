package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>desc</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Desc extends XfaNode {

    /** Wraps a backing <code>desc</code> element. */
    public Desc(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

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
