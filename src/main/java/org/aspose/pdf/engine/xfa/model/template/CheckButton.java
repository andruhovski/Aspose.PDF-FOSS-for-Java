package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `checkButton`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class CheckButton extends XfaNode {

    /// Wraps a backing `checkButton` element.
    public CheckButton(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `mark` attribute.
    public enum MarkValue {
        CHECK("check"),
        CIRCLE("circle"),
        CROSS("cross"),
        DEFAULT("default"),
        DIAMOND("diamond"),
        SQUARE("square"),
        STAR("star");
        private final String v;
        MarkValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static MarkValue fromValue(String s) {
            for (MarkValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `mark` attribute, or null.
    public MarkValue getMark() {
        String v = getAttribute("mark");
        return v == null ? null : MarkValue.fromValue(v);
    }
    /// Sets the `mark` attribute.
    public void setMark(MarkValue value) {
        setAttribute("mark", value == null ? null : value.value());
    }
    /// @return the raw `mark` string, or null.
    public String getMarkRaw() { return getAttribute("mark"); }

    /// Allowed values of the `shape` attribute.
    public enum ShapeValue {
        ROUND("round"),
        SQUARE("square");
        private final String v;
        ShapeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ShapeValue fromValue(String s) {
            for (ShapeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `shape` attribute, or null.
    public ShapeValue getShape() {
        String v = getAttribute("shape");
        return v == null ? null : ShapeValue.fromValue(v);
    }
    /// Sets the `shape` attribute.
    public void setShape(ShapeValue value) {
        setAttribute("shape", value == null ? null : value.value());
    }
    /// @return the raw `shape` string, or null.
    public String getShapeRaw() { return getAttribute("shape"); }

    /// @return the typed `size` attribute, or null.
    public XfaMeasurement getSize() { return getMeasurement("size"); }
    /// Sets the `size` attribute.
    public void setSize(XfaMeasurement value) { setAttribute("size", value == null ? null : value.format()); }

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
