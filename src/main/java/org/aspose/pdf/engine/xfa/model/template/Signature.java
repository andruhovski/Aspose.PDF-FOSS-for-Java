package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>signature</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Signature extends XfaNode {

    /** Wraps a backing <code>signature</code> element. */
    public Signature(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>type</code> attribute. */
    public enum TypeValue {
        PDF1_3("PDF1.3"),
        PDF1_6("PDF1.6");
        private final String v;
        TypeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static TypeValue fromValue(String s) {
            for (TypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>type</code> attribute, or null. */
    public TypeValue getType() {
        String v = getAttribute("type");
        return v == null ? null : TypeValue.fromValue(v);
    }
    /** Sets the <code>type</code> attribute. */
    public void setType(TypeValue value) {
        setAttribute("type", value == null ? null : value.value());
    }
    /** @return the raw <code>type</code> string, or null. */
    public String getTypeRaw() { return getAttribute("type"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>border</code> child (typed), or null. */
    public Border getBorder() { return (Border) getChild("border"); }
    /** Ensures and returns the <code>border</code> child. */
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>filter</code> child (typed), or null. */
    public Filter getFilter() { return (Filter) getChild("filter"); }
    /** Ensures and returns the <code>filter</code> child. */
    public Filter ensureFilter() { return (Filter) ensureChild("filter"); }

    /** @return the <code>manifest</code> child (typed), or null. */
    public Manifest getManifest() { return (Manifest) getChild("manifest"); }
    /** Ensures and returns the <code>manifest</code> child. */
    public Manifest ensureManifest() { return (Manifest) ensureChild("manifest"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
