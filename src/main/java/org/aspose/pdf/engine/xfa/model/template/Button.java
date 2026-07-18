package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `button`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Button extends XfaNode {

    /// Wraps a backing `button` element.
    public Button(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `highlight` attribute.
    public enum HighlightValue {
        INVERTED("inverted"),
        NONE("none"),
        OUTLINE("outline"),
        PUSH("push");
        private final String v;
        HighlightValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static HighlightValue fromValue(String s) {
            for (HighlightValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `highlight` attribute, or null.
    public HighlightValue getHighlight() {
        String v = getAttribute("highlight");
        return v == null ? null : HighlightValue.fromValue(v);
    }
    /// Sets the `highlight` attribute.
    public void setHighlight(HighlightValue value) {
        setAttribute("highlight", value == null ? null : value.value());
    }
    /// @return the raw `highlight` string, or null.
    public String getHighlightRaw() { return getAttribute("highlight"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

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
}
