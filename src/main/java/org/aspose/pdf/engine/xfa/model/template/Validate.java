package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `validate`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Validate extends XfaNode {

    /// Wraps a backing `validate` element.
    public Validate(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `formatTest` attribute.
    public enum FormatTestValue {
        DISABLED("disabled"),
        ERROR("error"),
        WARNING("warning");
        private final String v;
        FormatTestValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static FormatTestValue fromValue(String s) {
            for (FormatTestValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `formatTest` attribute, or null.
    public FormatTestValue getFormatTest() {
        String v = getAttribute("formatTest");
        return v == null ? null : FormatTestValue.fromValue(v);
    }
    /// Sets the `formatTest` attribute.
    public void setFormatTest(FormatTestValue value) {
        setAttribute("formatTest", value == null ? null : value.value());
    }
    /// @return the raw `formatTest` string, or null.
    public String getFormatTestRaw() { return getAttribute("formatTest"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `nullTest` attribute.
    public enum NullTestValue {
        DISABLED("disabled"),
        ERROR("error"),
        WARNING("warning");
        private final String v;
        NullTestValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static NullTestValue fromValue(String s) {
            for (NullTestValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `nullTest` attribute, or null.
    public NullTestValue getNullTest() {
        String v = getAttribute("nullTest");
        return v == null ? null : NullTestValue.fromValue(v);
    }
    /// Sets the `nullTest` attribute.
    public void setNullTest(NullTestValue value) {
        setAttribute("nullTest", value == null ? null : value.value());
    }
    /// @return the raw `nullTest` string, or null.
    public String getNullTestRaw() { return getAttribute("nullTest"); }

    /// Allowed values of the `scriptTest` attribute.
    public enum ScriptTestValue {
        DISABLED("disabled"),
        ERROR("error"),
        WARNING("warning");
        private final String v;
        ScriptTestValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ScriptTestValue fromValue(String s) {
            for (ScriptTestValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `scriptTest` attribute, or null.
    public ScriptTestValue getScriptTest() {
        String v = getAttribute("scriptTest");
        return v == null ? null : ScriptTestValue.fromValue(v);
    }
    /// Sets the `scriptTest` attribute.
    public void setScriptTest(ScriptTestValue value) {
        setAttribute("scriptTest", value == null ? null : value.value());
    }
    /// @return the raw `scriptTest` string, or null.
    public String getScriptTestRaw() { return getAttribute("scriptTest"); }

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

    /// @return the `picture` child (typed), or null.
    public Picture getPicture() { return (Picture) getChild("picture"); }
    /// Ensures and returns the `picture` child.
    public Picture ensurePicture() { return (Picture) ensureChild("picture"); }

    /// @return the `script` child (typed), or null.
    public Script getScript() { return (Script) getChild("script"); }
    /// Ensures and returns the `script` child.
    public Script ensureScript() { return (Script) ensureChild("script"); }
}
