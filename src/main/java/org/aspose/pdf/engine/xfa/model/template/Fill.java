package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>fill</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Fill extends XfaNode {

    /** Wraps a backing <code>fill</code> element. */
    public Fill(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

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

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>color</code> child (typed), or null. */
    public Color getColor() { return (Color) getChild("color"); }
    /** Ensures and returns the <code>color</code> child. */
    public Color ensureColor() { return (Color) ensureChild("color"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>linear</code> child (typed), or null. */
    public Linear getLinear() { return (Linear) getChild("linear"); }
    /** Ensures and returns the <code>linear</code> child. */
    public Linear ensureLinear() { return (Linear) ensureChild("linear"); }

    /** @return the <code>pattern</code> child (typed), or null. */
    public Pattern getPattern() { return (Pattern) getChild("pattern"); }
    /** Ensures and returns the <code>pattern</code> child. */
    public Pattern ensurePattern() { return (Pattern) ensureChild("pattern"); }

    /** @return the <code>radial</code> child (typed), or null. */
    public Radial getRadial() { return (Radial) getChild("radial"); }
    /** Ensures and returns the <code>radial</code> child. */
    public Radial ensureRadial() { return (Radial) ensureChild("radial"); }

    /** @return the <code>solid</code> child (typed), or null. */
    public Solid getSolid() { return (Solid) getChild("solid"); }
    /** Ensures and returns the <code>solid</code> child. */
    public Solid ensureSolid() { return (Solid) ensureChild("solid"); }

    /** @return the <code>stipple</code> child (typed), or null. */
    public Stipple getStipple() { return (Stipple) getChild("stipple"); }
    /** Ensures and returns the <code>stipple</code> child. */
    public Stipple ensureStipple() { return (Stipple) ensureChild("stipple"); }
}
