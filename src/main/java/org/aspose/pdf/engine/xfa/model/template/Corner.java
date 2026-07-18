package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `corner`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Corner extends XfaNode {

    /// Wraps a backing `corner` element.
    public Corner(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `inverted` attribute.
    public enum InvertedValue {
        V_0("0"),
        V_1("1");
        private final String v;
        InvertedValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static InvertedValue fromValue(String s) {
            for (InvertedValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `inverted` attribute, or null.
    public InvertedValue getInverted() {
        String v = getAttribute("inverted");
        return v == null ? null : InvertedValue.fromValue(v);
    }
    /// Sets the `inverted` attribute.
    public void setInverted(InvertedValue value) {
        setAttribute("inverted", value == null ? null : value.value());
    }
    /// @return the raw `inverted` string, or null.
    public String getInvertedRaw() { return getAttribute("inverted"); }

    /// Allowed values of the `join` attribute.
    public enum JoinValue {
        ROUND("round"),
        SQUARE("square");
        private final String v;
        JoinValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static JoinValue fromValue(String s) {
            for (JoinValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `join` attribute, or null.
    public JoinValue getJoin() {
        String v = getAttribute("join");
        return v == null ? null : JoinValue.fromValue(v);
    }
    /// Sets the `join` attribute.
    public void setJoin(JoinValue value) {
        setAttribute("join", value == null ? null : value.value());
    }
    /// @return the raw `join` string, or null.
    public String getJoinRaw() { return getAttribute("join"); }

    /// Allowed values of the `presence` attribute.
    public enum PresenceValue {
        HIDDEN("hidden"),
        INACTIVE("inactive"),
        INVISIBLE("invisible"),
        VISIBLE("visible");
        private final String v;
        PresenceValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PresenceValue fromValue(String s) {
            for (PresenceValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `presence` attribute, or null.
    public PresenceValue getPresence() {
        String v = getAttribute("presence");
        return v == null ? null : PresenceValue.fromValue(v);
    }
    /// Sets the `presence` attribute.
    public void setPresence(PresenceValue value) {
        setAttribute("presence", value == null ? null : value.value());
    }
    /// @return the raw `presence` string, or null.
    public String getPresenceRaw() { return getAttribute("presence"); }

    /// @return the typed `radius` attribute, or null.
    public XfaMeasurement getRadius() { return getMeasurement("radius"); }
    /// Sets the `radius` attribute.
    public void setRadius(XfaMeasurement value) { setAttribute("radius", value == null ? null : value.format()); }

    /// Allowed values of the `stroke` attribute.
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
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static StrokeValue fromValue(String s) {
            for (StrokeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `stroke` attribute, or null.
    public StrokeValue getStroke() {
        String v = getAttribute("stroke");
        return v == null ? null : StrokeValue.fromValue(v);
    }
    /// Sets the `stroke` attribute.
    public void setStroke(StrokeValue value) {
        setAttribute("stroke", value == null ? null : value.value());
    }
    /// @return the raw `stroke` string, or null.
    public String getStrokeRaw() { return getAttribute("stroke"); }

    /// @return the typed `thickness` attribute, or null.
    public XfaMeasurement getThickness() { return getMeasurement("thickness"); }
    /// Sets the `thickness` attribute.
    public void setThickness(XfaMeasurement value) { setAttribute("thickness", value == null ? null : value.format()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `color` child (typed), or null.
    public Color getColor() { return (Color) getChild("color"); }
    /// Ensures and returns the `color` child.
    public Color ensureColor() { return (Color) ensureChild("color"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
