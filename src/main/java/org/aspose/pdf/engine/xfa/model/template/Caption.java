package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>caption</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Caption extends XfaNode {

    /** Wraps a backing <code>caption</code> element. */
    public Caption(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>placement</code> attribute. */
    public enum PlacementValue {
        BOTTOM("bottom"),
        INLINE("inline"),
        LEFT("left"),
        RIGHT("right"),
        TOP("top");
        private final String v;
        PlacementValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PlacementValue fromValue(String s) {
            for (PlacementValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>placement</code> attribute, or null. */
    public PlacementValue getPlacement() {
        String v = getAttribute("placement");
        return v == null ? null : PlacementValue.fromValue(v);
    }
    /** Sets the <code>placement</code> attribute. */
    public void setPlacement(PlacementValue value) {
        setAttribute("placement", value == null ? null : value.value());
    }
    /** @return the raw <code>placement</code> string, or null. */
    public String getPlacementRaw() { return getAttribute("placement"); }

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

    /** @return the typed <code>reserve</code> attribute, or null. */
    public XfaMeasurement getReserve() { return getMeasurement("reserve"); }
    /** Sets the <code>reserve</code> attribute. */
    public void setReserve(XfaMeasurement value) { setAttribute("reserve", value == null ? null : value.format()); }

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

    /** @return the <code>font</code> child (typed), or null. */
    public Font getFont() { return (Font) getChild("font"); }
    /** Ensures and returns the <code>font</code> child. */
    public Font ensureFont() { return (Font) ensureChild("font"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /** @return the <code>para</code> child (typed), or null. */
    public Para getPara() { return (Para) getChild("para"); }
    /** Ensures and returns the <code>para</code> child. */
    public Para ensurePara() { return (Para) ensureChild("para"); }

    /** @return the <code>value</code> child (typed), or null. */
    public Value getValue() { return (Value) getChild("value"); }
    /** Ensures and returns the <code>value</code> child. */
    public Value ensureValue() { return (Value) ensureChild("value"); }
}
