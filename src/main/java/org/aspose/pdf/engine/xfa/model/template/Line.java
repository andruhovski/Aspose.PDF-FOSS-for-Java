package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `line`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Line extends XfaNode {

    /// Wraps a backing `line` element.
    public Line(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /// Allowed values of the `slope` attribute.
    public enum SlopeValue {
        SYM_2F("/"),
        SYM_5C("\\");
        private final String v;
        SlopeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static SlopeValue fromValue(String s) {
            for (SlopeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `slope` attribute, or null.
    public SlopeValue getSlope() {
        String v = getAttribute("slope");
        return v == null ? null : SlopeValue.fromValue(v);
    }
    /// Sets the `slope` attribute.
    public void setSlope(SlopeValue value) {
        setAttribute("slope", value == null ? null : value.value());
    }
    /// @return the raw `slope` string, or null.
    public String getSlopeRaw() { return getAttribute("slope"); }

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
}
