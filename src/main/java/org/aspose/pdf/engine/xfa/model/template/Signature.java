package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `signature`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Signature extends XfaNode {

    /// Wraps a backing `signature` element.
    public Signature(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `type` attribute.
    public enum TypeValue {
        PDF1_3("PDF1.3"),
        PDF1_6("PDF1.6");
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

    /// @return the `border` child (typed), or null.
    public Border getBorder() { return (Border) getChild("border"); }
    /// Ensures and returns the `border` child.
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `filter` child (typed), or null.
    public Filter getFilter() { return (Filter) getChild("filter"); }
    /// Ensures and returns the `filter` child.
    public Filter ensureFilter() { return (Filter) ensureChild("filter"); }

    /// @return the `manifest` child (typed), or null.
    public Manifest getManifest() { return (Manifest) getChild("manifest"); }
    /// Ensures and returns the `manifest` child.
    public Manifest ensureManifest() { return (Manifest) ensureChild("manifest"); }

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
