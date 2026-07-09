package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>extras</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Extras extends XfaNode {

    /** Wraps a backing <code>extras</code> element. */
    public Extras(Element element, XfaNode parent) {
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

    /** @return the <code>extras</code> children (typed). */
    public java.util.List<Extras> getExtrasList() {
        java.util.List<Extras> r = new java.util.ArrayList<Extras>();
        for (XfaNode n : getChildren("extras")) { r.add((Extras) n); }
        return r;
    }
    /** Appends a new <code>extras</code> child. */
    public Extras addExtras() { return (Extras) addChild("extras"); }

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
}
