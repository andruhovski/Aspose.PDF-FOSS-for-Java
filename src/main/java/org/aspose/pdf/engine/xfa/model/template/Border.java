package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>border</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Border extends XfaNode {

    /** Wraps a backing <code>border</code> element. */
    public Border(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>break</code> attribute. */
    public enum BreakValue {
        CLOSE("close"),
        OPEN("open");
        private final String v;
        BreakValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static BreakValue fromValue(String s) {
            for (BreakValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>break</code> attribute, or null. */
    public BreakValue getBreak() {
        String v = getAttribute("break");
        return v == null ? null : BreakValue.fromValue(v);
    }
    /** Sets the <code>break</code> attribute. */
    public void setBreak(BreakValue value) {
        setAttribute("break", value == null ? null : value.value());
    }
    /** @return the raw <code>break</code> string, or null. */
    public String getBreakRaw() { return getAttribute("break"); }

    /** Allowed values of the <code>hand</code> attribute. */
    public enum HandValue {
        EVEN("even"),
        LEFT("left"),
        RIGHT("right");
        private final String v;
        HandValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static HandValue fromValue(String s) {
            for (HandValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>hand</code> attribute, or null. */
    public HandValue getHand() {
        String v = getAttribute("hand");
        return v == null ? null : HandValue.fromValue(v);
    }
    /** Sets the <code>hand</code> attribute. */
    public void setHand(HandValue value) {
        setAttribute("hand", value == null ? null : value.value());
    }
    /** @return the raw <code>hand</code> string, or null. */
    public String getHandRaw() { return getAttribute("hand"); }

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

    /** @return the typed <code>relevant</code> attribute, or null. */
    public String getRelevant() { return getString("relevant"); }
    /** Sets the <code>relevant</code> attribute. */
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>fill</code> child (typed), or null. */
    public Fill getFill() { return (Fill) getChild("fill"); }
    /** Ensures and returns the <code>fill</code> child. */
    public Fill ensureFill() { return (Fill) ensureChild("fill"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /** @return the <code>corner</code> child (typed), or null. */
    public Corner getCorner() { return (Corner) getChild("corner"); }
    /** Ensures and returns the <code>corner</code> child. */
    public Corner ensureCorner() { return (Corner) ensureChild("corner"); }

    /** @return the <code>edge</code> child (typed), or null. */
    public Edge getEdge() { return (Edge) getChild("edge"); }
    /** Ensures and returns the <code>edge</code> child. */
    public Edge ensureEdge() { return (Edge) ensureChild("edge"); }
}
