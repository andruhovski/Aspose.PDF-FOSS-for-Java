package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `radial`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Radial extends XfaNode {

    /// Wraps a backing `radial` element.
    public Radial(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `type` attribute.
    public enum TypeValue {
        TOCENTER("toCenter"),
        TOEDGE("toEdge");
        private final String v;
        TypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TypeValue fromValue(String s) {
            for (TypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `type` attribute, or null.
    public TypeValue getType() {
        String v = getAttribute("type");
        return v == null ? null : TypeValue.fromValue(v);
    }
    /// Sets the `type` attribute.
    public void setType(TypeValue value) {
        setAttribute("type", value == null ? null : value.value());
    }
    /// @return the raw `type` string, or null.
    public String getTypeRaw() { return getAttribute("type"); }

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
