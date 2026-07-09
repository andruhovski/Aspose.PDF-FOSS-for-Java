package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>subformSet</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class SubformSet extends XfaNode {

    /** Wraps a backing <code>subformSet</code> element. */
    public SubformSet(Element element, XfaNode parent) {
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

    /** Allowed values of the <code>relation</code> attribute. */
    public enum RelationValue {
        CHOICE("choice"),
        ORDERED("ordered"),
        UNORDERED("unordered");
        private final String v;
        RelationValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static RelationValue fromValue(String s) {
            for (RelationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>relation</code> attribute, or null. */
    public RelationValue getRelation() {
        String v = getAttribute("relation");
        return v == null ? null : RelationValue.fromValue(v);
    }
    /** Sets the <code>relation</code> attribute. */
    public void setRelation(RelationValue value) {
        setAttribute("relation", value == null ? null : value.value());
    }
    /** @return the raw <code>relation</code> string, or null. */
    public String getRelationRaw() { return getAttribute("relation"); }

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

    /** @return the <code>bookend</code> child (typed), or null. */
    public Bookend getBookend() { return (Bookend) getChild("bookend"); }
    /** Ensures and returns the <code>bookend</code> child. */
    public Bookend ensureBookend() { return (Bookend) ensureChild("bookend"); }

    /** @return the <code>break</code> child (typed), or null. */
    public Break getBreak() { return (Break) getChild("break"); }
    /** Ensures and returns the <code>break</code> child. */
    public Break ensureBreak() { return (Break) ensureChild("break"); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>occur</code> child (typed), or null. */
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /** Ensures and returns the <code>occur</code> child. */
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /** @return the <code>overflow</code> child (typed), or null. */
    public Overflow getOverflow() { return (Overflow) getChild("overflow"); }
    /** Ensures and returns the <code>overflow</code> child. */
    public Overflow ensureOverflow() { return (Overflow) ensureChild("overflow"); }

    /** @return the <code>breakAfter</code> children (typed). */
    public java.util.List<BreakAfter> getBreakAfterList() {
        java.util.List<BreakAfter> r = new java.util.ArrayList<BreakAfter>();
        for (XfaNode n : getChildren("breakAfter")) { r.add((BreakAfter) n); }
        return r;
    }
    /** Appends a new <code>breakAfter</code> child. */
    public BreakAfter addBreakAfter() { return (BreakAfter) addChild("breakAfter"); }

    /** @return the <code>breakBefore</code> children (typed). */
    public java.util.List<BreakBefore> getBreakBeforeList() {
        java.util.List<BreakBefore> r = new java.util.ArrayList<BreakBefore>();
        for (XfaNode n : getChildren("breakBefore")) { r.add((BreakBefore) n); }
        return r;
    }
    /** Appends a new <code>breakBefore</code> child. */
    public BreakBefore addBreakBefore() { return (BreakBefore) addChild("breakBefore"); }

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
