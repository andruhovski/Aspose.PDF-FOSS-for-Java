package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `speak`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Speak extends XfaNode {

    /// Wraps a backing `speak` element.
    public Speak(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `disable` attribute.
    public enum DisableValue {
        V_0("0"),
        V_1("1");
        private final String v;
        DisableValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static DisableValue fromValue(String s) {
            for (DisableValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `disable` attribute, or null.
    public DisableValue getDisable() {
        String v = getAttribute("disable");
        return v == null ? null : DisableValue.fromValue(v);
    }
    /// Sets the `disable` attribute.
    public void setDisable(DisableValue value) {
        setAttribute("disable", value == null ? null : value.value());
    }
    /// @return the raw `disable` string, or null.
    public String getDisableRaw() { return getAttribute("disable"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `priority` attribute.
    public enum PriorityValue {
        CAPTION("caption"),
        CUSTOM("custom"),
        NAME("name"),
        TOOLTIP("toolTip");
        private final String v;
        PriorityValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PriorityValue fromValue(String s) {
            for (PriorityValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `priority` attribute, or null.
    public PriorityValue getPriority() {
        String v = getAttribute("priority");
        return v == null ? null : PriorityValue.fromValue(v);
    }
    /// Sets the `priority` attribute.
    public void setPriority(PriorityValue value) {
        setAttribute("priority", value == null ? null : value.value());
    }
    /// @return the raw `priority` string, or null.
    public String getPriorityRaw() { return getAttribute("priority"); }

    /// @return the typed `rid` attribute, or null.
    public String getRid() { return getString("rid"); }
    /// Sets the `rid` attribute.
    public void setRid(String value) { setAttribute("rid", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return this element's text content.
    public String getValue() { return getTextContent(); }
    /// Sets this element's text content.
    public void setValue(String value) { setTextContent(value); }
}
