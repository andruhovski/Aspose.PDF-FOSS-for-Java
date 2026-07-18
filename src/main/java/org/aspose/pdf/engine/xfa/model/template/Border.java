package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `border`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Border extends XfaNode {

    /// Wraps a backing `border` element.
    public Border(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `break` attribute.
    public enum BreakValue {
        CLOSE("close"),
        OPEN("open");
        private final String v;
        BreakValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static BreakValue fromValue(String s) {
            for (BreakValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `break` attribute, or null.
    public BreakValue getBreak() {
        String v = getAttribute("break");
        return v == null ? null : BreakValue.fromValue(v);
    }
    /// Sets the `break` attribute.
    public void setBreak(BreakValue value) {
        setAttribute("break", value == null ? null : value.value());
    }
    /// @return the raw `break` string, or null.
    public String getBreakRaw() { return getAttribute("break"); }

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

    /// @return the `fill` child (typed), or null.
    public Fill getFill() { return (Fill) getChild("fill"); }
    /// Ensures and returns the `fill` child.
    public Fill ensureFill() { return (Fill) ensureChild("fill"); }

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /// @return the `corner` child (typed), or null.
    public Corner getCorner() { return (Corner) getChild("corner"); }
    /// Ensures and returns the `corner` child.
    public Corner ensureCorner() { return (Corner) ensureChild("corner"); }

    /// @return the `edge` child (typed), or null.
    public Edge getEdge() { return (Edge) getChild("edge"); }
    /// Ensures and returns the `edge` child.
    public Edge ensureEdge() { return (Edge) ensureChild("edge"); }
}
