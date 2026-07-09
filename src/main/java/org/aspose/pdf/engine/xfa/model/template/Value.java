package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>value</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Value extends XfaNode {

    /** Wraps a backing <code>value</code> element. */
    public Value(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>override</code> attribute. */
    public enum OverrideValue {
        V_0("0"),
        V_1("1");
        private final String v;
        OverrideValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static OverrideValue fromValue(String s) {
            for (OverrideValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>override</code> attribute, or null. */
    public OverrideValue getOverride() {
        String v = getAttribute("override");
        return v == null ? null : OverrideValue.fromValue(v);
    }
    /** Sets the <code>override</code> attribute. */
    public void setOverride(OverrideValue value) {
        setAttribute("override", value == null ? null : value.value());
    }
    /** @return the raw <code>override</code> string, or null. */
    public String getOverrideRaw() { return getAttribute("override"); }

    /** @return the typed <code>relevant</code> attribute, or null. */
    public String getRelevant() { return getString("relevant"); }
    /** Sets the <code>relevant</code> attribute. */
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>arc</code> child (typed), or null. */
    public Arc getArc() { return (Arc) getChild("arc"); }
    /** Ensures and returns the <code>arc</code> child. */
    public Arc ensureArc() { return (Arc) ensureChild("arc"); }

    /** @return the <code>boolean</code> child (typed), or null. */
    public Boolean getBoolean2() { return (Boolean) getChild("boolean"); }
    /** Ensures and returns the <code>boolean</code> child. */
    public Boolean ensureBoolean() { return (Boolean) ensureChild("boolean"); }

    /** @return the <code>date</code> child (typed), or null. */
    public Date getDate() { return (Date) getChild("date"); }
    /** Ensures and returns the <code>date</code> child. */
    public Date ensureDate() { return (Date) ensureChild("date"); }

    /** @return the <code>dateTime</code> child (typed), or null. */
    public DateTime getDateTime() { return (DateTime) getChild("dateTime"); }
    /** Ensures and returns the <code>dateTime</code> child. */
    public DateTime ensureDateTime() { return (DateTime) ensureChild("dateTime"); }

    /** @return the <code>decimal</code> child (typed), or null. */
    public Decimal getDecimal() { return (Decimal) getChild("decimal"); }
    /** Ensures and returns the <code>decimal</code> child. */
    public Decimal ensureDecimal() { return (Decimal) ensureChild("decimal"); }

    /** @return the <code>exData</code> child (typed), or null. */
    public ExData getExData() { return (ExData) getChild("exData"); }
    /** Ensures and returns the <code>exData</code> child. */
    public ExData ensureExData() { return (ExData) ensureChild("exData"); }

    /** @return the <code>float</code> child (typed), or null. */
    public Float getFloat() { return (Float) getChild("float"); }
    /** Ensures and returns the <code>float</code> child. */
    public Float ensureFloat() { return (Float) ensureChild("float"); }

    /** @return the <code>image</code> child (typed), or null. */
    public Image getImage() { return (Image) getChild("image"); }
    /** Ensures and returns the <code>image</code> child. */
    public Image ensureImage() { return (Image) ensureChild("image"); }

    /** @return the <code>integer</code> child (typed), or null. */
    public Integer getInteger2() { return (Integer) getChild("integer"); }
    /** Ensures and returns the <code>integer</code> child. */
    public Integer ensureInteger() { return (Integer) ensureChild("integer"); }

    /** @return the <code>line</code> child (typed), or null. */
    public Line getLine() { return (Line) getChild("line"); }
    /** Ensures and returns the <code>line</code> child. */
    public Line ensureLine() { return (Line) ensureChild("line"); }

    /** @return the <code>rectangle</code> child (typed), or null. */
    public Rectangle getRectangle() { return (Rectangle) getChild("rectangle"); }
    /** Ensures and returns the <code>rectangle</code> child. */
    public Rectangle ensureRectangle() { return (Rectangle) ensureChild("rectangle"); }

    /** @return the <code>text</code> child (typed), or null. */
    public Text getText() { return (Text) getChild("text"); }
    /** Ensures and returns the <code>text</code> child. */
    public Text ensureText() { return (Text) ensureChild("text"); }

    /** @return the <code>time</code> child (typed), or null. */
    public Time getTime() { return (Time) getChild("time"); }
    /** Ensures and returns the <code>time</code> child. */
    public Time ensureTime() { return (Time) ensureChild("time"); }
}
