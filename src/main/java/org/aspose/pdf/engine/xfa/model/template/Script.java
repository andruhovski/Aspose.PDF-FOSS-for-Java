package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `script`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Script extends XfaNode {

    /// Wraps a backing `script` element.
    public Script(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `binding` attribute, or null.
    public String getBinding() { return getString("binding"); }
    /// Sets the `binding` attribute.
    public void setBinding(String value) { setAttribute("binding", value); }

    /// @return the typed `contentType` attribute, or null.
    public String getContentType() { return getString("contentType"); }
    /// Sets the `contentType` attribute.
    public void setContentType(String value) { setAttribute("contentType", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

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

    /// @return this element's text content.
    public String getValue() { return getTextContent(); }
    /// Sets this element's text content.
    public void setValue(String value) { setTextContent(value); }
}
