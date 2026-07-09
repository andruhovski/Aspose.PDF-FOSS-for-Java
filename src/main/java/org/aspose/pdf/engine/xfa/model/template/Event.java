package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>event</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Event extends XfaNode {

    /** Wraps a backing <code>event</code> element. */
    public Event(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>activity</code> attribute. */
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
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ActivityValue fromValue(String s) {
            for (ActivityValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>activity</code> attribute, or null. */
    public ActivityValue getActivity() {
        String v = getAttribute("activity");
        return v == null ? null : ActivityValue.fromValue(v);
    }
    /** Sets the <code>activity</code> attribute. */
    public void setActivity(ActivityValue value) {
        setAttribute("activity", value == null ? null : value.value());
    }
    /** @return the raw <code>activity</code> string, or null. */
    public String getActivityRaw() { return getAttribute("activity"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>listen</code> attribute. */
    public enum ListenValue {
        REFANDDESCENDENTS("refAndDescendents"),
        REFONLY("refOnly");
        private final String v;
        ListenValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ListenValue fromValue(String s) {
            for (ListenValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>listen</code> attribute, or null. */
    public ListenValue getListen() {
        String v = getAttribute("listen");
        return v == null ? null : ListenValue.fromValue(v);
    }
    /** Sets the <code>listen</code> attribute. */
    public void setListen(ListenValue value) {
        setAttribute("listen", value == null ? null : value.value());
    }
    /** @return the raw <code>listen</code> string, or null. */
    public String getListenRaw() { return getAttribute("listen"); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>ref</code> attribute, or null. */
    public String getRef() { return getString("ref"); }
    /** Sets the <code>ref</code> attribute. */
    public void setRef(String value) { setAttribute("ref", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>execute</code> child (typed), or null. */
    public Execute getExecute() { return (Execute) getChild("execute"); }
    /** Ensures and returns the <code>execute</code> child. */
    public Execute ensureExecute() { return (Execute) ensureChild("execute"); }

    /** @return the <code>script</code> child (typed), or null. */
    public Script getScript() { return (Script) getChild("script"); }
    /** Ensures and returns the <code>script</code> child. */
    public Script ensureScript() { return (Script) ensureChild("script"); }

    /** @return the <code>signData</code> child (typed), or null. */
    public SignData getSignData() { return (SignData) getChild("signData"); }
    /** Ensures and returns the <code>signData</code> child. */
    public SignData ensureSignData() { return (SignData) ensureChild("signData"); }

    /** @return the <code>submit</code> child (typed), or null. */
    public Submit getSubmit() { return (Submit) getChild("submit"); }
    /** Ensures and returns the <code>submit</code> child. */
    public Submit ensureSubmit() { return (Submit) ensureChild("submit"); }
}
