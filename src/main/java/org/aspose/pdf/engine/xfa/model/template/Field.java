package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `field`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Field extends XfaNode {

    /// Wraps a backing `field` element.
    public Field(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `access` attribute.
    public enum AccessValue {
        NONINTERACTIVE("nonInteractive"),
        OPEN("open"),
        PROTECTED("protected"),
        READONLY("readOnly");
        private final String v;
        AccessValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AccessValue fromValue(String s) {
            for (AccessValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `access` attribute, or null.
    public AccessValue getAccess() {
        String v = getAttribute("access");
        return v == null ? null : AccessValue.fromValue(v);
    }
    /// Sets the `access` attribute.
    public void setAccess(AccessValue value) {
        setAttribute("access", value == null ? null : value.value());
    }
    /// @return the raw `access` string, or null.
    public String getAccessRaw() { return getAttribute("access"); }

    /// @return the typed `accessKey` attribute, or null.
    public String getAccessKey() { return getString("accessKey"); }
    /// Sets the `accessKey` attribute.
    public void setAccessKey(String value) { setAttribute("accessKey", value); }

    /// Allowed values of the `anchorType` attribute.
    public enum AnchorTypeValue {
        BOTTOMCENTER("bottomCenter"),
        BOTTOMLEFT("bottomLeft"),
        BOTTOMRIGHT("bottomRight"),
        MIDDLECENTER("middleCenter"),
        MIDDLELEFT("middleLeft"),
        MIDDLERIGHT("middleRight"),
        TOPCENTER("topCenter"),
        TOPLEFT("topLeft"),
        TOPRIGHT("topRight");
        private final String v;
        AnchorTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AnchorTypeValue fromValue(String s) {
            for (AnchorTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `anchorType` attribute, or null.
    public AnchorTypeValue getAnchorType() {
        String v = getAttribute("anchorType");
        return v == null ? null : AnchorTypeValue.fromValue(v);
    }
    /// Sets the `anchorType` attribute.
    public void setAnchorType(AnchorTypeValue value) {
        setAttribute("anchorType", value == null ? null : value.value());
    }
    /// @return the raw `anchorType` string, or null.
    public String getAnchorTypeRaw() { return getAttribute("anchorType"); }

    /// @return the typed `colSpan` attribute, or null.
    public java.lang.Integer getColSpan() { return getInteger("colSpan"); }
    /// Sets the `colSpan` attribute.
    public void setColSpan(java.lang.Integer value) { setAttribute("colSpan", value == null ? null : value.toString()); }

    /// @return the typed `h` attribute, or null.
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /// Sets the `h` attribute.
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `locale` attribute, or null.
    public String getLocale() { return getString("locale"); }
    /// Sets the `locale` attribute.
    public void setLocale(String value) { setAttribute("locale", value); }

    /// @return the typed `maxH` attribute, or null.
    public XfaMeasurement getMaxH() { return getMeasurement("maxH"); }
    /// Sets the `maxH` attribute.
    public void setMaxH(XfaMeasurement value) { setAttribute("maxH", value == null ? null : value.format()); }

    /// @return the typed `maxW` attribute, or null.
    public XfaMeasurement getMaxW() { return getMeasurement("maxW"); }
    /// Sets the `maxW` attribute.
    public void setMaxW(XfaMeasurement value) { setAttribute("maxW", value == null ? null : value.format()); }

    /// @return the typed `minH` attribute, or null.
    public XfaMeasurement getMinH() { return getMeasurement("minH"); }
    /// Sets the `minH` attribute.
    public void setMinH(XfaMeasurement value) { setAttribute("minH", value == null ? null : value.format()); }

    /// @return the typed `minW` attribute, or null.
    public XfaMeasurement getMinW() { return getMeasurement("minW"); }
    /// Sets the `minW` attribute.
    public void setMinW(XfaMeasurement value) { setAttribute("minW", value == null ? null : value.format()); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// Allowed values of the `presence` attribute.
    public enum PresenceValue {
        HIDDEN("hidden"),
        INACTIVE("inactive"),
        INVISIBLE("invisible"),
        VISIBLE("visible");
        private final String v;
        PresenceValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PresenceValue fromValue(String s) {
            for (PresenceValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `presence` attribute, or null.
    public PresenceValue getPresence() {
        String v = getAttribute("presence");
        return v == null ? null : PresenceValue.fromValue(v);
    }
    /// Sets the `presence` attribute.
    public void setPresence(PresenceValue value) {
        setAttribute("presence", value == null ? null : value.value());
    }
    /// @return the raw `presence` string, or null.
    public String getPresenceRaw() { return getAttribute("presence"); }

    /// @return the typed `relevant` attribute, or null.
    public String getRelevant() { return getString("relevant"); }
    /// Sets the `relevant` attribute.
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /// @return the typed `rotate` attribute, or null.
    public String getRotate() { return getString("rotate"); }
    /// Sets the `rotate` attribute.
    public void setRotate(String value) { setAttribute("rotate", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the typed `w` attribute, or null.
    public XfaMeasurement getW() { return getMeasurement("w"); }
    /// Sets the `w` attribute.
    public void setW(XfaMeasurement value) { setAttribute("w", value == null ? null : value.format()); }

    /// @return the typed `x` attribute, or null.
    public XfaMeasurement getX() { return getMeasurement("x"); }
    /// Sets the `x` attribute.
    public void setX(XfaMeasurement value) { setAttribute("x", value == null ? null : value.format()); }

    /// @return the typed `y` attribute, or null.
    public XfaMeasurement getY() { return getMeasurement("y"); }
    /// Sets the `y` attribute.
    public void setY(XfaMeasurement value) { setAttribute("y", value == null ? null : value.format()); }

    /// @return the `assist` child (typed), or null.
    public Assist getAssist() { return (Assist) getChild("assist"); }
    /// Ensures and returns the `assist` child.
    public Assist ensureAssist() { return (Assist) ensureChild("assist"); }

    /// @return the `bind` child (typed), or null.
    public Bind getBind() { return (Bind) getChild("bind"); }
    /// Ensures and returns the `bind` child.
    public Bind ensureBind() { return (Bind) ensureChild("bind"); }

    /// @return the `border` child (typed), or null.
    public Border getBorder() { return (Border) getChild("border"); }
    /// Ensures and returns the `border` child.
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /// @return the `calculate` child (typed), or null.
    public Calculate getCalculate() { return (Calculate) getChild("calculate"); }
    /// Ensures and returns the `calculate` child.
    public Calculate ensureCalculate() { return (Calculate) ensureChild("calculate"); }

    /// @return the `caption` child (typed), or null.
    public Caption getCaption() { return (Caption) getChild("caption"); }
    /// Ensures and returns the `caption` child.
    public Caption ensureCaption() { return (Caption) ensureChild("caption"); }

    /// @return the `desc` child (typed), or null.
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /// Ensures and returns the `desc` child.
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `font` child (typed), or null.
    public Font getFont() { return (Font) getChild("font"); }
    /// Ensures and returns the `font` child.
    public Font ensureFont() { return (Font) ensureChild("font"); }

    /// @return the `format` child (typed), or null.
    public Format getFormat() { return (Format) getChild("format"); }
    /// Ensures and returns the `format` child.
    public Format ensureFormat() { return (Format) ensureChild("format"); }

    /// @return the `keep` child (typed), or null.
    public Keep getKeep() { return (Keep) getChild("keep"); }
    /// Ensures and returns the `keep` child.
    public Keep ensureKeep() { return (Keep) ensureChild("keep"); }

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /// @return the `para` child (typed), or null.
    public Para getPara() { return (Para) getChild("para"); }
    /// Ensures and returns the `para` child.
    public Para ensurePara() { return (Para) ensureChild("para"); }

    /// @return the `traversal` child (typed), or null.
    public Traversal getTraversal() { return (Traversal) getChild("traversal"); }
    /// Ensures and returns the `traversal` child.
    public Traversal ensureTraversal() { return (Traversal) ensureChild("traversal"); }

    /// @return the `ui` child (typed), or null.
    public Ui getUi() { return (Ui) getChild("ui"); }
    /// Ensures and returns the `ui` child.
    public Ui ensureUi() { return (Ui) ensureChild("ui"); }

    /// @return the `validate` child (typed), or null.
    public Validate getValidate() { return (Validate) getChild("validate"); }
    /// Ensures and returns the `validate` child.
    public Validate ensureValidate() { return (Validate) ensureChild("validate"); }

    /// @return the `value` child (typed), or null.
    public Value getValue() { return (Value) getChild("value"); }
    /// Ensures and returns the `value` child.
    public Value ensureValue() { return (Value) ensureChild("value"); }

    /// @return the `bindItems` children (typed).
    public java.util.List<BindItems> getBindItemsList() {
        java.util.List<BindItems> r = new java.util.ArrayList<BindItems>();
        for (XfaNode n : getChildren("bindItems")) { r.add((BindItems) n); }
        return r;
    }
    /// Appends a new `bindItems` child.
    public BindItems addBindItems() { return (BindItems) addChild("bindItems"); }

    /// @return the `calcProperty` children (typed).
    public java.util.List<CalcProperty> getCalcPropertyList() {
        java.util.List<CalcProperty> r = new java.util.ArrayList<CalcProperty>();
        for (XfaNode n : getChildren("calcProperty")) { r.add((CalcProperty) n); }
        return r;
    }
    /// Appends a new `calcProperty` child.
    public CalcProperty addCalcProperty() { return (CalcProperty) addChild("calcProperty"); }

    /// @return the `connect` children (typed).
    public java.util.List<Connect> getConnectList() {
        java.util.List<Connect> r = new java.util.ArrayList<Connect>();
        for (XfaNode n : getChildren("connect")) { r.add((Connect) n); }
        return r;
    }
    /// Appends a new `connect` child.
    public Connect addConnect() { return (Connect) addChild("connect"); }

    /// @return the `event` children (typed).
    public java.util.List<Event> getEventList() {
        java.util.List<Event> r = new java.util.ArrayList<Event>();
        for (XfaNode n : getChildren("event")) { r.add((Event) n); }
        return r;
    }
    /// Appends a new `event` child.
    public Event addEvent() { return (Event) addChild("event"); }

    /// @return the `items` children (typed).
    public java.util.List<Items> getItemsList() {
        java.util.List<Items> r = new java.util.ArrayList<Items>();
        for (XfaNode n : getChildren("items")) { r.add((Items) n); }
        return r;
    }
    /// Appends a new `items` child.
    public Items addItems() { return (Items) addChild("items"); }

    /// @return the `setProperty` children (typed).
    public java.util.List<SetProperty> getSetPropertyList() {
        java.util.List<SetProperty> r = new java.util.ArrayList<SetProperty>();
        for (XfaNode n : getChildren("setProperty")) { r.add((SetProperty) n); }
        return r;
    }
    /// Appends a new `setProperty` child.
    public SetProperty addSetProperty() { return (SetProperty) addChild("setProperty"); }
}
