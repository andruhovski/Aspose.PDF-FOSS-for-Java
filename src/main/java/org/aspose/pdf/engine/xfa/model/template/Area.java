package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>area</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Area extends XfaNode {

    /** Wraps a backing <code>area</code> element. */
    public Area(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>colSpan</code> attribute, or null. */
    public java.lang.Integer getColSpan() { return getInteger("colSpan"); }
    /** Sets the <code>colSpan</code> attribute. */
    public void setColSpan(java.lang.Integer value) { setAttribute("colSpan", value == null ? null : value.toString()); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

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

    /** @return the typed <code>x</code> attribute, or null. */
    public XfaMeasurement getX() { return getMeasurement("x"); }
    /** Sets the <code>x</code> attribute. */
    public void setX(XfaMeasurement value) { setAttribute("x", value == null ? null : value.format()); }

    /** @return the typed <code>y</code> attribute, or null. */
    public XfaMeasurement getY() { return getMeasurement("y"); }
    /** Sets the <code>y</code> attribute. */
    public void setY(XfaMeasurement value) { setAttribute("y", value == null ? null : value.format()); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>area</code> children (typed). */
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /** Appends a new <code>area</code> child. */
    public Area addArea() { return (Area) addChild("area"); }

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

    /** @return the <code>exObject</code> children (typed). */
    public java.util.List<ExObject> getExObjectList() {
        java.util.List<ExObject> r = new java.util.ArrayList<ExObject>();
        for (XfaNode n : getChildren("exObject")) { r.add((ExObject) n); }
        return r;
    }
    /** Appends a new <code>exObject</code> child. */
    public ExObject addExObject() { return (ExObject) addChild("exObject"); }

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

    /** @return the <code>subformSet</code> children (typed). */
    public java.util.List<SubformSet> getSubformSetList() {
        java.util.List<SubformSet> r = new java.util.ArrayList<SubformSet>();
        for (XfaNode n : getChildren("subformSet")) { r.add((SubformSet) n); }
        return r;
    }
    /** Appends a new <code>subformSet</code> child. */
    public SubformSet addSubformSet() { return (SubformSet) addChild("subformSet"); }
}
