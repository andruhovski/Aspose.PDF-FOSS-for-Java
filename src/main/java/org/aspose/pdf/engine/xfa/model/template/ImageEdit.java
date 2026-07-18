package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `imageEdit`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class ImageEdit extends XfaNode {

    /// Wraps a backing `imageEdit` element.
    public ImageEdit(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `data` attribute.
    public enum DataValue {
        EMBED("embed"),
        LINK("link");
        private final String v;
        DataValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static DataValue fromValue(String s) {
            for (DataValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `data` attribute, or null.
    public DataValue getData() {
        String v = getAttribute("data");
        return v == null ? null : DataValue.fromValue(v);
    }
    /// Sets the `data` attribute.
    public void setData(DataValue value) {
        setAttribute("data", value == null ? null : value.value());
    }
    /// @return the raw `data` string, or null.
    public String getDataRaw() { return getAttribute("data"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

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
