package org.aspose.pdf.engine.xfa.model.localeset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>datePattern</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class DatePattern extends XfaNode {

    /** Wraps a backing <code>datePattern</code> element. */
    public DatePattern(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>name</code> attribute. */
    public enum NameValue {
        FULL("full"),
        LONG("long"),
        MED("med"),
        SHORT("short");
        private final String v;
        NameValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static NameValue fromValue(String s) {
            for (NameValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>name</code> attribute, or null. */
    public NameValue getName2() {
        String v = getAttribute("name");
        return v == null ? null : NameValue.fromValue(v);
    }
    /** Sets the <code>name</code> attribute. */
    public void setName(NameValue value) {
        setAttribute("name", value == null ? null : value.value());
    }
    /** @return the raw <code>name</code> string, or null. */
    public String getNameRaw() { return getAttribute("name"); }

    /** @return this element's text content. */
    public String getValue() { return getTextContent(); }
    /** Sets this element's text content. */
    public void setValue(String value) { setTextContent(value); }
}
