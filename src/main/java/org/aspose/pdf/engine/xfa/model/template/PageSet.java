package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `pageSet`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class PageSet extends XfaNode {

    /// Wraps a backing `pageSet` element.
    public PageSet(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// Allowed values of the `relation` attribute.
    public enum RelationValue {
        DUPLEXPAGINATED("duplexPaginated"),
        ORDEREDOCCURRENCE("orderedOccurrence"),
        SIMPLEXPAGINATED("simplexPaginated");
        private final String v;
        RelationValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static RelationValue fromValue(String s) {
            for (RelationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `relation` attribute, or null.
    public RelationValue getRelation() {
        String v = getAttribute("relation");
        return v == null ? null : RelationValue.fromValue(v);
    }
    /// Sets the `relation` attribute.
    public void setRelation(RelationValue value) {
        setAttribute("relation", value == null ? null : value.value());
    }
    /// @return the raw `relation` string, or null.
    public String getRelationRaw() { return getAttribute("relation"); }

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

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `occur` child (typed), or null.
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /// Ensures and returns the `occur` child.
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /// @return the `pageArea` children (typed).
    public java.util.List<PageArea> getPageAreaList() {
        java.util.List<PageArea> r = new java.util.ArrayList<PageArea>();
        for (XfaNode n : getChildren("pageArea")) { r.add((PageArea) n); }
        return r;
    }
    /// Appends a new `pageArea` child.
    public PageArea addPageArea() { return (PageArea) addChild("pageArea"); }

    /// @return the `pageSet` children (typed).
    public java.util.List<PageSet> getPageSetList() {
        java.util.List<PageSet> r = new java.util.ArrayList<PageSet>();
        for (XfaNode n : getChildren("pageSet")) { r.add((PageSet) n); }
        return r;
    }
    /// Appends a new `pageSet` child.
    public PageSet addPageSet() { return (PageSet) addChild("pageSet"); }
}
