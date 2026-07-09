package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>arc</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Arc extends XfaNode {

    /** Wraps a backing <code>arc</code> element. */
    public Arc(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>circular</code> attribute. */
    public enum CircularValue {
        V_0("0"),
        V_1("1");
        private final String v;
        CircularValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static CircularValue fromValue(String s) {
            for (CircularValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>circular</code> attribute, or null. */
    public CircularValue getCircular() {
        String v = getAttribute("circular");
        return v == null ? null : CircularValue.fromValue(v);
    }
    /** Sets the <code>circular</code> attribute. */
    public void setCircular(CircularValue value) {
        setAttribute("circular", value == null ? null : value.value());
    }
    /** @return the raw <code>circular</code> string, or null. */
    public String getCircularRaw() { return getAttribute("circular"); }

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

    /** @return the typed <code>startAngle</code> attribute, or null. */
    public String getStartAngle() { return getString("startAngle"); }
    /** Sets the <code>startAngle</code> attribute. */
    public void setStartAngle(String value) { setAttribute("startAngle", value); }

    /** @return the typed <code>sweepAngle</code> attribute, or null. */
    public String getSweepAngle() { return getString("sweepAngle"); }
    /** Sets the <code>sweepAngle</code> attribute. */
    public void setSweepAngle(String value) { setAttribute("sweepAngle", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>edge</code> child (typed), or null. */
    public Edge getEdge() { return (Edge) getChild("edge"); }
    /** Ensures and returns the <code>edge</code> child. */
    public Edge ensureEdge() { return (Edge) ensureChild("edge"); }

    /** @return the <code>fill</code> child (typed), or null. */
    public Fill getFill() { return (Fill) getChild("fill"); }
    /** Ensures and returns the <code>fill</code> child. */
    public Fill ensureFill() { return (Fill) ensureChild("fill"); }
}
