package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `execute`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Execute extends XfaNode {

    /// Wraps a backing `execute` element.
    public Execute(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `connection` attribute, or null.
    public String getConnection() { return getString("connection"); }
    /// Sets the `connection` attribute.
    public void setConnection(String value) { setAttribute("connection", value); }

    /// Allowed values of the `executeType` attribute.
    public enum ExecuteTypeValue {
        IMPORT("import"),
        REMERGE("remerge");
        private final String v;
        ExecuteTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ExecuteTypeValue fromValue(String s) {
            for (ExecuteTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `executeType` attribute, or null.
    public ExecuteTypeValue getExecuteType() {
        String v = getAttribute("executeType");
        return v == null ? null : ExecuteTypeValue.fromValue(v);
    }
    /// Sets the `executeType` attribute.
    public void setExecuteType(ExecuteTypeValue value) {
        setAttribute("executeType", value == null ? null : value.value());
    }
    /// @return the raw `executeType` string, or null.
    public String getExecuteTypeRaw() { return getAttribute("executeType"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `runAt` attribute.
    public enum RunAtValue {
        BOTH("both"),
        CLIENT("client"),
        SERVER("server");
        private final String v;
        RunAtValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static RunAtValue fromValue(String s) {
            for (RunAtValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `runAt` attribute, or null.
    public RunAtValue getRunAt() {
        String v = getAttribute("runAt");
        return v == null ? null : RunAtValue.fromValue(v);
    }
    /// Sets the `runAt` attribute.
    public void setRunAt(RunAtValue value) {
        setAttribute("runAt", value == null ? null : value.value());
    }
    /// @return the raw `runAt` string, or null.
    public String getRunAtRaw() { return getAttribute("runAt"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
