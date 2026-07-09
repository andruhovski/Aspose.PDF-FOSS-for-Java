package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>script</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Script extends XfaNode {

    /** Wraps a backing <code>script</code> element. */
    public Script(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>binding</code> attribute, or null. */
    public String getBinding() { return getString("binding"); }
    /** Sets the <code>binding</code> attribute. */
    public void setBinding(String value) { setAttribute("binding", value); }

    /** @return the typed <code>contentType</code> attribute, or null. */
    public String getContentType() { return getString("contentType"); }
    /** Sets the <code>contentType</code> attribute. */
    public void setContentType(String value) { setAttribute("contentType", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

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

    /** @return this element's text content. */
    public String getValue() { return getTextContent(); }
    /** Sets this element's text content. */
    public void setValue(String value) { setTextContent(value); }
}
