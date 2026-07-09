package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>template</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Template extends XfaNode {

    /** Wraps a backing <code>template</code> element. */
    public Template(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>baseProfile</code> attribute. */
    public enum BaseProfileValue {
        FULL("full"),
        INTERACTIVEFORMS("interactiveForms");
        private final String v;
        BaseProfileValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static BaseProfileValue fromValue(String s) {
            for (BaseProfileValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>baseProfile</code> attribute, or null. */
    public BaseProfileValue getBaseProfile() {
        String v = getAttribute("baseProfile");
        return v == null ? null : BaseProfileValue.fromValue(v);
    }
    /** Sets the <code>baseProfile</code> attribute. */
    public void setBaseProfile(BaseProfileValue value) {
        setAttribute("baseProfile", value == null ? null : value.value());
    }
    /** @return the raw <code>baseProfile</code> string, or null. */
    public String getBaseProfileRaw() { return getAttribute("baseProfile"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>subform</code> children (typed). */
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /** Appends a new <code>subform</code> child. */
    public Subform addSubform() { return (Subform) addChild("subform"); }
}
