package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `choiceList`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class ChoiceList extends XfaNode {

    /// Wraps a backing `choiceList` element.
    public ChoiceList(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `commitOn` attribute.
    public enum CommitOnValue {
        EXIT("exit"),
        SELECT("select");
        private final String v;
        CommitOnValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static CommitOnValue fromValue(String s) {
            for (CommitOnValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `commitOn` attribute, or null.
    public CommitOnValue getCommitOn() {
        String v = getAttribute("commitOn");
        return v == null ? null : CommitOnValue.fromValue(v);
    }
    /// Sets the `commitOn` attribute.
    public void setCommitOn(CommitOnValue value) {
        setAttribute("commitOn", value == null ? null : value.value());
    }
    /// @return the raw `commitOn` string, or null.
    public String getCommitOnRaw() { return getAttribute("commitOn"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `open` attribute.
    public enum OpenValue {
        ALWAYS("always"),
        MULTISELECT("multiSelect"),
        ONENTRY("onEntry"),
        USERCONTROL("userControl");
        private final String v;
        OpenValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OpenValue fromValue(String s) {
            for (OpenValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `open` attribute, or null.
    public OpenValue getOpen() {
        String v = getAttribute("open");
        return v == null ? null : OpenValue.fromValue(v);
    }
    /// Sets the `open` attribute.
    public void setOpen(OpenValue value) {
        setAttribute("open", value == null ? null : value.value());
    }
    /// @return the raw `open` string, or null.
    public String getOpenRaw() { return getAttribute("open"); }

    /// Allowed values of the `textEntry` attribute.
    public enum TextEntryValue {
        V_0("0"),
        V_1("1");
        private final String v;
        TextEntryValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TextEntryValue fromValue(String s) {
            for (TextEntryValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `textEntry` attribute, or null.
    public TextEntryValue getTextEntry() {
        String v = getAttribute("textEntry");
        return v == null ? null : TextEntryValue.fromValue(v);
    }
    /// Sets the `textEntry` attribute.
    public void setTextEntry(TextEntryValue value) {
        setAttribute("textEntry", value == null ? null : value.value());
    }
    /// @return the raw `textEntry` string, or null.
    public String getTextEntryRaw() { return getAttribute("textEntry"); }

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

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
