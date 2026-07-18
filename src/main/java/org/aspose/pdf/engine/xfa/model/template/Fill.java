package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `fill`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Fill extends XfaNode {

    /// Wraps a backing `fill` element.
    public Fill(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /// @return the `linear` child (typed), or null.
    public Linear getLinear() { return (Linear) getChild("linear"); }
    /// Ensures and returns the `linear` child.
    public Linear ensureLinear() { return (Linear) ensureChild("linear"); }

    /// @return the `pattern` child (typed), or null.
    public Pattern getPattern() { return (Pattern) getChild("pattern"); }
    /// Ensures and returns the `pattern` child.
    public Pattern ensurePattern() { return (Pattern) ensureChild("pattern"); }

    /// @return the `radial` child (typed), or null.
    public Radial getRadial() { return (Radial) getChild("radial"); }
    /// Ensures and returns the `radial` child.
    public Radial ensureRadial() { return (Radial) ensureChild("radial"); }

    /// @return the `solid` child (typed), or null.
    public Solid getSolid() { return (Solid) getChild("solid"); }
    /// Ensures and returns the `solid` child.
    public Solid ensureSolid() { return (Solid) ensureChild("solid"); }

    /// @return the `stipple` child (typed), or null.
    public Stipple getStipple() { return (Stipple) getChild("stipple"); }
    /// Ensures and returns the `stipple` child.
    public Stipple ensureStipple() { return (Stipple) ensureChild("stipple"); }
}
