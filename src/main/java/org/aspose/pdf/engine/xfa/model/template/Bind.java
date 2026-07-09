package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>bind</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Bind extends XfaNode {

    /** Wraps a backing <code>bind</code> element. */
    public Bind(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>match</code> attribute. */
    public enum MatchValue {
        DATAREF("dataRef"),
        GLOBAL("global"),
        NONE("none"),
        ONCE("once");
        private final String v;
        MatchValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static MatchValue fromValue(String s) {
            for (MatchValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>match</code> attribute, or null. */
    public MatchValue getMatch() {
        String v = getAttribute("match");
        return v == null ? null : MatchValue.fromValue(v);
    }
    /** Sets the <code>match</code> attribute. */
    public void setMatch(MatchValue value) {
        setAttribute("match", value == null ? null : value.value());
    }
    /** @return the raw <code>match</code> string, or null. */
    public String getMatchRaw() { return getAttribute("match"); }

    /** @return the typed <code>ref</code> attribute, or null. */
    public String getRef() { return getString("ref"); }
    /** Sets the <code>ref</code> attribute. */
    public void setRef(String value) { setAttribute("ref", value); }

    /** @return the <code>picture</code> child (typed), or null. */
    public Picture getPicture() { return (Picture) getChild("picture"); }
    /** Ensures and returns the <code>picture</code> child. */
    public Picture ensurePicture() { return (Picture) ensureChild("picture"); }
}
