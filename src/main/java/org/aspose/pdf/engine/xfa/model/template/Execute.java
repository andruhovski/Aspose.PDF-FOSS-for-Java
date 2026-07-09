package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>execute</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Execute extends XfaNode {

    /** Wraps a backing <code>execute</code> element. */
    public Execute(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>connection</code> attribute, or null. */
    public String getConnection() { return getString("connection"); }
    /** Sets the <code>connection</code> attribute. */
    public void setConnection(String value) { setAttribute("connection", value); }

    /** Allowed values of the <code>executeType</code> attribute. */
    public enum ExecuteTypeValue {
        IMPORT("import"),
        REMERGE("remerge");
        private final String v;
        ExecuteTypeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ExecuteTypeValue fromValue(String s) {
            for (ExecuteTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>executeType</code> attribute, or null. */
    public ExecuteTypeValue getExecuteType() {
        String v = getAttribute("executeType");
        return v == null ? null : ExecuteTypeValue.fromValue(v);
    }
    /** Sets the <code>executeType</code> attribute. */
    public void setExecuteType(ExecuteTypeValue value) {
        setAttribute("executeType", value == null ? null : value.value());
    }
    /** @return the raw <code>executeType</code> string, or null. */
    public String getExecuteTypeRaw() { return getAttribute("executeType"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>runAt</code> attribute. */
    public enum RunAtValue {
        BOTH("both"),
        CLIENT("client"),
        SERVER("server");
        private final String v;
        RunAtValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static RunAtValue fromValue(String s) {
            for (RunAtValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>runAt</code> attribute, or null. */
    public RunAtValue getRunAt() {
        String v = getAttribute("runAt");
        return v == null ? null : RunAtValue.fromValue(v);
    }
    /** Sets the <code>runAt</code> attribute. */
    public void setRunAt(RunAtValue value) {
        setAttribute("runAt", value == null ? null : value.value());
    }
    /** @return the raw <code>runAt</code> string, or null. */
    public String getRunAtRaw() { return getAttribute("runAt"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
