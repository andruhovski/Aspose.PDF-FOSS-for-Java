package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `value`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Value extends XfaNode {

    /// Wraps a backing `value` element.
    public Value(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `override` attribute.
    public enum OverrideValue {
        V_0("0"),
        V_1("1");
        private final String v;
        OverrideValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OverrideValue fromValue(String s) {
            for (OverrideValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `override` attribute, or null.
    public OverrideValue getOverride() {
        String v = getAttribute("override");
        return v == null ? null : OverrideValue.fromValue(v);
    }
    /// Sets the `override` attribute.
    public void setOverride(OverrideValue value) {
        setAttribute("override", value == null ? null : value.value());
    }
    /// @return the raw `override` string, or null.
    public String getOverrideRaw() { return getAttribute("override"); }

    /// @return the typed `relevant` attribute, or null.
    public String getRelevant() { return getString("relevant"); }
    /// Sets the `relevant` attribute.
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `arc` child (typed), or null.
    public Arc getArc() { return (Arc) getChild("arc"); }
    /// Ensures and returns the `arc` child.
    public Arc ensureArc() { return (Arc) ensureChild("arc"); }

    /// @return the `boolean` child (typed), or null.
    public Boolean getBoolean2() { return (Boolean) getChild("boolean"); }
    /// Ensures and returns the `boolean` child.
    public Boolean ensureBoolean() { return (Boolean) ensureChild("boolean"); }

    /// @return the `date` child (typed), or null.
    public Date getDate() { return (Date) getChild("date"); }
    /// Ensures and returns the `date` child.
    public Date ensureDate() { return (Date) ensureChild("date"); }

    /// @return the `dateTime` child (typed), or null.
    public DateTime getDateTime() { return (DateTime) getChild("dateTime"); }
    /// Ensures and returns the `dateTime` child.
    public DateTime ensureDateTime() { return (DateTime) ensureChild("dateTime"); }

    /// @return the `decimal` child (typed), or null.
    public Decimal getDecimal() { return (Decimal) getChild("decimal"); }
    /// Ensures and returns the `decimal` child.
    public Decimal ensureDecimal() { return (Decimal) ensureChild("decimal"); }

    /// @return the `exData` child (typed), or null.
    public ExData getExData() { return (ExData) getChild("exData"); }
    /// Ensures and returns the `exData` child.
    public ExData ensureExData() { return (ExData) ensureChild("exData"); }

    /// @return the `float` child (typed), or null.
    public Float getFloat() { return (Float) getChild("float"); }
    /// Ensures and returns the `float` child.
    public Float ensureFloat() { return (Float) ensureChild("float"); }

    /// @return the `image` child (typed), or null.
    public Image getImage() { return (Image) getChild("image"); }
    /// Ensures and returns the `image` child.
    public Image ensureImage() { return (Image) ensureChild("image"); }

    /// @return the `integer` child (typed), or null.
    public Integer getInteger2() { return (Integer) getChild("integer"); }
    /// Ensures and returns the `integer` child.
    public Integer ensureInteger() { return (Integer) ensureChild("integer"); }

    /// @return the `line` child (typed), or null.
    public Line getLine() { return (Line) getChild("line"); }
    /// Ensures and returns the `line` child.
    public Line ensureLine() { return (Line) ensureChild("line"); }

    /// @return the `rectangle` child (typed), or null.
    public Rectangle getRectangle() { return (Rectangle) getChild("rectangle"); }
    /// Ensures and returns the `rectangle` child.
    public Rectangle ensureRectangle() { return (Rectangle) ensureChild("rectangle"); }

    /// @return the `text` child (typed), or null.
    public Text getText() { return (Text) getChild("text"); }
    /// Ensures and returns the `text` child.
    public Text ensureText() { return (Text) ensureChild("text"); }

    /// @return the `time` child (typed), or null.
    public Time getTime() { return (Time) getChild("time"); }
    /// Ensures and returns the `time` child.
    public Time ensureTime() { return (Time) ensureChild("time"); }
}
