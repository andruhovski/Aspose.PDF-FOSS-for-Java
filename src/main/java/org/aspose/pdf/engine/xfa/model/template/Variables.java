package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `variables`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Variables extends XfaNode {

    /// Wraps a backing `variables` element.
    public Variables(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `boolean` children (typed).
    public java.util.List<Boolean> getBooleanList() {
        java.util.List<Boolean> r = new java.util.ArrayList<Boolean>();
        for (XfaNode n : getChildren("boolean")) { r.add((Boolean) n); }
        return r;
    }
    /// Appends a new `boolean` child.
    public Boolean addBoolean() { return (Boolean) addChild("boolean"); }

    /// @return the `date` children (typed).
    public java.util.List<Date> getDateList() {
        java.util.List<Date> r = new java.util.ArrayList<Date>();
        for (XfaNode n : getChildren("date")) { r.add((Date) n); }
        return r;
    }
    /// Appends a new `date` child.
    public Date addDate() { return (Date) addChild("date"); }

    /// @return the `dateTime` children (typed).
    public java.util.List<DateTime> getDateTimeList() {
        java.util.List<DateTime> r = new java.util.ArrayList<DateTime>();
        for (XfaNode n : getChildren("dateTime")) { r.add((DateTime) n); }
        return r;
    }
    /// Appends a new `dateTime` child.
    public DateTime addDateTime() { return (DateTime) addChild("dateTime"); }

    /// @return the `decimal` children (typed).
    public java.util.List<Decimal> getDecimalList() {
        java.util.List<Decimal> r = new java.util.ArrayList<Decimal>();
        for (XfaNode n : getChildren("decimal")) { r.add((Decimal) n); }
        return r;
    }
    /// Appends a new `decimal` child.
    public Decimal addDecimal() { return (Decimal) addChild("decimal"); }

    /// @return the `exData` children (typed).
    public java.util.List<ExData> getExDataList() {
        java.util.List<ExData> r = new java.util.ArrayList<ExData>();
        for (XfaNode n : getChildren("exData")) { r.add((ExData) n); }
        return r;
    }
    /// Appends a new `exData` child.
    public ExData addExData() { return (ExData) addChild("exData"); }

    /// @return the `float` children (typed).
    public java.util.List<Float> getFloatList() {
        java.util.List<Float> r = new java.util.ArrayList<Float>();
        for (XfaNode n : getChildren("float")) { r.add((Float) n); }
        return r;
    }
    /// Appends a new `float` child.
    public Float addFloat() { return (Float) addChild("float"); }

    /// @return the `image` children (typed).
    public java.util.List<Image> getImageList() {
        java.util.List<Image> r = new java.util.ArrayList<Image>();
        for (XfaNode n : getChildren("image")) { r.add((Image) n); }
        return r;
    }
    /// Appends a new `image` child.
    public Image addImage() { return (Image) addChild("image"); }

    /// @return the `integer` children (typed).
    public java.util.List<Integer> getIntegerList() {
        java.util.List<Integer> r = new java.util.ArrayList<Integer>();
        for (XfaNode n : getChildren("integer")) { r.add((Integer) n); }
        return r;
    }
    /// Appends a new `integer` child.
    public Integer addInteger() { return (Integer) addChild("integer"); }

    /// @return the `manifest` children (typed).
    public java.util.List<Manifest> getManifestList() {
        java.util.List<Manifest> r = new java.util.ArrayList<Manifest>();
        for (XfaNode n : getChildren("manifest")) { r.add((Manifest) n); }
        return r;
    }
    /// Appends a new `manifest` child.
    public Manifest addManifest() { return (Manifest) addChild("manifest"); }

    /// @return the `script` children (typed).
    public java.util.List<Script> getScriptList() {
        java.util.List<Script> r = new java.util.ArrayList<Script>();
        for (XfaNode n : getChildren("script")) { r.add((Script) n); }
        return r;
    }
    /// Appends a new `script` child.
    public Script addScript() { return (Script) addChild("script"); }

    /// @return the `text` children (typed).
    public java.util.List<Text> getTextList() {
        java.util.List<Text> r = new java.util.ArrayList<Text>();
        for (XfaNode n : getChildren("text")) { r.add((Text) n); }
        return r;
    }
    /// Appends a new `text` child.
    public Text addText() { return (Text) addChild("text"); }

    /// @return the `time` children (typed).
    public java.util.List<Time> getTimeList() {
        java.util.List<Time> r = new java.util.ArrayList<Time>();
        for (XfaNode n : getChildren("time")) { r.add((Time) n); }
        return r;
    }
    /// Appends a new `time` child.
    public Time addTime() { return (Time) addChild("time"); }
}
