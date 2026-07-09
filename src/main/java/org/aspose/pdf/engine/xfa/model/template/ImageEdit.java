package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>imageEdit</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class ImageEdit extends XfaNode {

    /** Wraps a backing <code>imageEdit</code> element. */
    public ImageEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>data</code> attribute. */
    public enum DataValue {
        EMBED("embed"),
        LINK("link");
        private final String v;
        DataValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static DataValue fromValue(String s) {
            for (DataValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>data</code> attribute, or null. */
    public DataValue getData() {
        String v = getAttribute("data");
        return v == null ? null : DataValue.fromValue(v);
    }
    /** Sets the <code>data</code> attribute. */
    public void setData(DataValue value) {
        setAttribute("data", value == null ? null : value.value());
    }
    /** @return the raw <code>data</code> string, or null. */
    public String getDataRaw() { return getAttribute("data"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>border</code> child (typed), or null. */
    public Border getBorder() { return (Border) getChild("border"); }
    /** Ensures and returns the <code>border</code> child. */
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }
}
