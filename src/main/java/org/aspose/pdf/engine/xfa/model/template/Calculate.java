package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `calculate`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Calculate extends XfaNode {

    /// Wraps a backing `calculate` element.
    public Calculate(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `override` attribute.
    public enum OverrideValue {
        DISABLED("disabled"),
        ERROR("error"),
        IGNORE("ignore"),
        WARNING("warning");
        private final String v;
        OverrideValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OverrideValue fromValue(String s) {
            for (OverrideValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `override` attribute, or null.
    public OverrideValue getOverride() {
        String v = getAttribute("override");
        return v == null ? null : OverrideValue.fromValue(v);
    }
    /// Sets the `override` attribute.
    public void setOverride(OverrideValue value) {
        setAttribute("override", value == null ? null : value.value());
    }
    /// @return the raw `override` string, or null.
    public String getOverrideRaw() { return getAttribute("override"); }

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

    /// @return the `message` child (typed), or null.
    public Message getMessage() { return (Message) getChild("message"); }
    /// Ensures and returns the `message` child.
    public Message ensureMessage() { return (Message) ensureChild("message"); }

    /// @return the `script` child (typed), or null.
    public Script getScript() { return (Script) getChild("script"); }
    /// Ensures and returns the `script` child.
    public Script ensureScript() { return (Script) ensureChild("script"); }
}
