package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `breakAfter`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class BreakAfter extends XfaNode {

    /// Wraps a backing `breakAfter` element.
    public BreakAfter(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `leader` attribute, or null.
    public String getLeader() { return getString("leader"); }
    /// Sets the `leader` attribute.
    public void setLeader(String value) { setAttribute("leader", value); }

    /// Allowed values of the `startNew` attribute.
    public enum StartNewValue {
        V_0("0"),
        V_1("1");
        private final String v;
        StartNewValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static StartNewValue fromValue(String s) {
            for (StartNewValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `startNew` attribute, or null.
    public StartNewValue getStartNew() {
        String v = getAttribute("startNew");
        return v == null ? null : StartNewValue.fromValue(v);
    }
    /// Sets the `startNew` attribute.
    public void setStartNew(StartNewValue value) {
        setAttribute("startNew", value == null ? null : value.value());
    }
    /// @return the raw `startNew` string, or null.
    public String getStartNewRaw() { return getAttribute("startNew"); }

    /// @return the typed `target` attribute, or null.
    public String getTarget() { return getString("target"); }
    /// Sets the `target` attribute.
    public void setTarget(String value) { setAttribute("target", value); }

    /// Allowed values of the `targetType` attribute.
    public enum TargetTypeValue {
        AUTO("auto"),
        CONTENTAREA("contentArea"),
        PAGEAREA("pageArea"),
        PAGEEVEN("pageEven"),
        PAGEODD("pageOdd");
        private final String v;
        TargetTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TargetTypeValue fromValue(String s) {
            for (TargetTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `targetType` attribute, or null.
    public TargetTypeValue getTargetType() {
        String v = getAttribute("targetType");
        return v == null ? null : TargetTypeValue.fromValue(v);
    }
    /// Sets the `targetType` attribute.
    public void setTargetType(TargetTypeValue value) {
        setAttribute("targetType", value == null ? null : value.value());
    }
    /// @return the raw `targetType` string, or null.
    public String getTargetTypeRaw() { return getAttribute("targetType"); }

    /// @return the typed `trailer` attribute, or null.
    public String getTrailer() { return getString("trailer"); }
    /// Sets the `trailer` attribute.
    public void setTrailer(String value) { setAttribute("trailer", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `script` child (typed), or null.
    public Script getScript() { return (Script) getChild("script"); }
    /// Ensures and returns the `script` child.
    public Script ensureScript() { return (Script) ensureChild("script"); }
}
