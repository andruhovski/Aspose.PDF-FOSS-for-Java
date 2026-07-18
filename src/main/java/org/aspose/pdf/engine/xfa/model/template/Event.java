package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `event`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Event extends XfaNode {

    /// Wraps a backing `event` element.
    public Event(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `activity` attribute.
    public enum ActivityValue {
        CHANGE("change"),
        CLICK("click"),
        DOCCLOSE("docClose"),
        DOCREADY("docReady"),
        ENTER("enter"),
        EXIT("exit"),
        FULL("full"),
        INDEXCHANGE("indexChange"),
        INITIALIZE("initialize"),
        MOUSEDOWN("mouseDown"),
        MOUSEENTER("mouseEnter"),
        MOUSEEXIT("mouseExit"),
        MOUSEUP("mouseUp"),
        POSTEXECUTE("postExecute"),
        POSTOPEN("postOpen"),
        POSTPRINT("postPrint"),
        POSTSAVE("postSave"),
        POSTSIGN("postSign"),
        POSTSUBMIT("postSubmit"),
        PREEXECUTE("preExecute"),
        PREOPEN("preOpen"),
        PREPRINT("prePrint"),
        PRESAVE("preSave"),
        PRESIGN("preSign"),
        PRESUBMIT("preSubmit"),
        READY("ready"),
        VALIDATIONSTATE("validationState");
        private final String v;
        ActivityValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ActivityValue fromValue(String s) {
            for (ActivityValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `activity` attribute, or null.
    public ActivityValue getActivity() {
        String v = getAttribute("activity");
        return v == null ? null : ActivityValue.fromValue(v);
    }
    /// Sets the `activity` attribute.
    public void setActivity(ActivityValue value) {
        setAttribute("activity", value == null ? null : value.value());
    }
    /// @return the raw `activity` string, or null.
    public String getActivityRaw() { return getAttribute("activity"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `listen` attribute.
    public enum ListenValue {
        REFANDDESCENDENTS("refAndDescendents"),
        REFONLY("refOnly");
        private final String v;
        ListenValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ListenValue fromValue(String s) {
            for (ListenValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `listen` attribute, or null.
    public ListenValue getListen() {
        String v = getAttribute("listen");
        return v == null ? null : ListenValue.fromValue(v);
    }
    /// Sets the `listen` attribute.
    public void setListen(ListenValue value) {
        setAttribute("listen", value == null ? null : value.value());
    }
    /// @return the raw `listen` string, or null.
    public String getListenRaw() { return getAttribute("listen"); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

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

    /// @return the `execute` child (typed), or null.
    public Execute getExecute() { return (Execute) getChild("execute"); }
    /// Ensures and returns the `execute` child.
    public Execute ensureExecute() { return (Execute) ensureChild("execute"); }

    /// @return the `script` child (typed), or null.
    public Script getScript() { return (Script) getChild("script"); }
    /// Ensures and returns the `script` child.
    public Script ensureScript() { return (Script) ensureChild("script"); }

    /// @return the `signData` child (typed), or null.
    public SignData getSignData() { return (SignData) getChild("signData"); }
    /// Ensures and returns the `signData` child.
    public SignData ensureSignData() { return (SignData) ensureChild("signData"); }

    /// @return the `submit` child (typed), or null.
    public Submit getSubmit() { return (Submit) getChild("submit"); }
    /// Ensures and returns the `submit` child.
    public Submit ensureSubmit() { return (Submit) ensureChild("submit"); }
}
