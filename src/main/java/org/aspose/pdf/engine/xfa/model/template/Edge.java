package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>edge</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Edge extends XfaNode {

    /** Wraps a backing <code>edge</code> element. */
    public Edge(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>cap</code> attribute. */
    public enum CapValue {
        BUTT("butt"),
        ROUND("round"),
        SQUARE("square");
        private final String v;
        CapValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static CapValue fromValue(String s) {
            for (CapValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>cap</code> attribute, or null. */
    public CapValue getCap() {
        String v = getAttribute("cap");
        return v == null ? null : CapValue.fromValue(v);
    }
    /** Sets the <code>cap</code> attribute. */
    public void setCap(CapValue value) {
        setAttribute("cap", value == null ? null : value.value());
    }
    /** @return the raw <code>cap</code> string, or null. */
    public String getCapRaw() { return getAttribute("cap"); }

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

    /** Allowed values of the <code>stroke</code> attribute. */
    public enum StrokeValue {
        DASHDOT("dashDot"),
        DASHDOTDOT("dashDotDot"),
        DASHED("dashed"),
        DOTTED("dotted"),
        EMBOSSED("embossed"),
        ETCHED("etched"),
        LOWERED("lowered"),
        RAISED("raised"),
        SOLID("solid");
        private final String v;
        StrokeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static StrokeValue fromValue(String s) {
            for (StrokeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>stroke</code> attribute, or null. */
    public StrokeValue getStroke() {
        String v = getAttribute("stroke");
        return v == null ? null : StrokeValue.fromValue(v);
    }
    /** Sets the <code>stroke</code> attribute. */
    public void setStroke(StrokeValue value) {
        setAttribute("stroke", value == null ? null : value.value());
    }
    /** @return the raw <code>stroke</code> string, or null. */
    public String getStrokeRaw() { return getAttribute("stroke"); }

    /** @return the typed <code>thickness</code> attribute, or null. */
    public XfaMeasurement getThickness() { return getMeasurement("thickness"); }
    /** Sets the <code>thickness</code> attribute. */
    public void setThickness(XfaMeasurement value) { setAttribute("thickness", value == null ? null : value.format()); }

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
}
