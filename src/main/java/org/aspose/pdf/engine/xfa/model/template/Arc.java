package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `arc`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Arc extends XfaNode {

    /// Wraps a backing `arc` element.
    public Arc(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `circular` attribute.
    public enum CircularValue {
        V_0("0"),
        V_1("1");
        private final String v;
        CircularValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static CircularValue fromValue(String s) {
            for (CircularValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `circular` attribute, or null.
    public CircularValue getCircular() {
        String v = getAttribute("circular");
        return v == null ? null : CircularValue.fromValue(v);
    }
    /// Sets the `circular` attribute.
    public void setCircular(CircularValue value) {
        setAttribute("circular", value == null ? null : value.value());
    }
    /// @return the raw `circular` string, or null.
    public String getCircularRaw() { return getAttribute("circular"); }

    /// Allowed values of the `hand` attribute.
    public enum HandValue {
        EVEN("even"),
        LEFT("left"),
        RIGHT("right");
        private final String v;
        HandValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static HandValue fromValue(String s) {
            for (HandValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `hand` attribute, or null.
    public HandValue getHand() {
        String v = getAttribute("hand");
        return v == null ? null : HandValue.fromValue(v);
    }
    /// Sets the `hand` attribute.
    public void setHand(HandValue value) {
        setAttribute("hand", value == null ? null : value.value());
    }
    /// @return the raw `hand` string, or null.
    public String getHandRaw() { return getAttribute("hand"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `startAngle` attribute, or null.
    public String getStartAngle() { return getString("startAngle"); }
    /// Sets the `startAngle` attribute.
    public void setStartAngle(String value) { setAttribute("startAngle", value); }

    /// @return the typed `sweepAngle` attribute, or null.
    public String getSweepAngle() { return getString("sweepAngle"); }
    /// Sets the `sweepAngle` attribute.
    public void setSweepAngle(String value) { setAttribute("sweepAngle", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `edge` child (typed), or null.
    public Edge getEdge() { return (Edge) getChild("edge"); }
    /// Ensures and returns the `edge` child.
    public Edge ensureEdge() { return (Edge) ensureChild("edge"); }

    /// @return the `fill` child (typed), or null.
    public Fill getFill() { return (Fill) getChild("fill"); }
    /// Ensures and returns the `fill` child.
    public Fill ensureFill() { return (Fill) ensureChild("fill"); }
}
