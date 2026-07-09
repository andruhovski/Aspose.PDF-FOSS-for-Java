package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>field</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Field extends XfaNode {

    /** Wraps a backing <code>field</code> element. */
    public Field(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>access</code> attribute. */
    public enum AccessValue {
        NONINTERACTIVE("nonInteractive"),
        OPEN("open"),
        PROTECTED("protected"),
        READONLY("readOnly");
        private final String v;
        AccessValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static AccessValue fromValue(String s) {
            for (AccessValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>access</code> attribute, or null. */
    public AccessValue getAccess() {
        String v = getAttribute("access");
        return v == null ? null : AccessValue.fromValue(v);
    }
    /** Sets the <code>access</code> attribute. */
    public void setAccess(AccessValue value) {
        setAttribute("access", value == null ? null : value.value());
    }
    /** @return the raw <code>access</code> string, or null. */
    public String getAccessRaw() { return getAttribute("access"); }

    /** @return the typed <code>accessKey</code> attribute, or null. */
    public String getAccessKey() { return getString("accessKey"); }
    /** Sets the <code>accessKey</code> attribute. */
    public void setAccessKey(String value) { setAttribute("accessKey", value); }

    /** Allowed values of the <code>anchorType</code> attribute. */
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
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static AnchorTypeValue fromValue(String s) {
            for (AnchorTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>anchorType</code> attribute, or null. */
    public AnchorTypeValue getAnchorType() {
        String v = getAttribute("anchorType");
        return v == null ? null : AnchorTypeValue.fromValue(v);
    }
    /** Sets the <code>anchorType</code> attribute. */
    public void setAnchorType(AnchorTypeValue value) {
        setAttribute("anchorType", value == null ? null : value.value());
    }
    /** @return the raw <code>anchorType</code> string, or null. */
    public String getAnchorTypeRaw() { return getAttribute("anchorType"); }

    /** @return the typed <code>colSpan</code> attribute, or null. */
    public java.lang.Integer getColSpan() { return getInteger("colSpan"); }
    /** Sets the <code>colSpan</code> attribute. */
    public void setColSpan(java.lang.Integer value) { setAttribute("colSpan", value == null ? null : value.toString()); }

    /** @return the typed <code>h</code> attribute, or null. */
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /** Sets the <code>h</code> attribute. */
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>locale</code> attribute, or null. */
    public String getLocale() { return getString("locale"); }
    /** Sets the <code>locale</code> attribute. */
    public void setLocale(String value) { setAttribute("locale", value); }

    /** @return the typed <code>maxH</code> attribute, or null. */
    public XfaMeasurement getMaxH() { return getMeasurement("maxH"); }
    /** Sets the <code>maxH</code> attribute. */
    public void setMaxH(XfaMeasurement value) { setAttribute("maxH", value == null ? null : value.format()); }

    /** @return the typed <code>maxW</code> attribute, or null. */
    public XfaMeasurement getMaxW() { return getMeasurement("maxW"); }
    /** Sets the <code>maxW</code> attribute. */
    public void setMaxW(XfaMeasurement value) { setAttribute("maxW", value == null ? null : value.format()); }

    /** @return the typed <code>minH</code> attribute, or null. */
    public XfaMeasurement getMinH() { return getMeasurement("minH"); }
    /** Sets the <code>minH</code> attribute. */
    public void setMinH(XfaMeasurement value) { setAttribute("minH", value == null ? null : value.format()); }

    /** @return the typed <code>minW</code> attribute, or null. */
    public XfaMeasurement getMinW() { return getMeasurement("minW"); }
    /** Sets the <code>minW</code> attribute. */
    public void setMinW(XfaMeasurement value) { setAttribute("minW", value == null ? null : value.format()); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** Allowed values of the <code>presence</code> attribute. */
    public enum PresenceValue {
        HIDDEN("hidden"),
        INACTIVE("inactive"),
        INVISIBLE("invisible"),
        VISIBLE("visible");
        private final String v;
        PresenceValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PresenceValue fromValue(String s) {
            for (PresenceValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>presence</code> attribute, or null. */
    public PresenceValue getPresence() {
        String v = getAttribute("presence");
        return v == null ? null : PresenceValue.fromValue(v);
    }
    /** Sets the <code>presence</code> attribute. */
    public void setPresence(PresenceValue value) {
        setAttribute("presence", value == null ? null : value.value());
    }
    /** @return the raw <code>presence</code> string, or null. */
    public String getPresenceRaw() { return getAttribute("presence"); }

    /** @return the typed <code>relevant</code> attribute, or null. */
    public String getRelevant() { return getString("relevant"); }
    /** Sets the <code>relevant</code> attribute. */
    public void setRelevant(String value) { setAttribute("relevant", value); }

    /** @return the typed <code>rotate</code> attribute, or null. */
    public String getRotate() { return getString("rotate"); }
    /** Sets the <code>rotate</code> attribute. */
    public void setRotate(String value) { setAttribute("rotate", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>w</code> attribute, or null. */
    public XfaMeasurement getW() { return getMeasurement("w"); }
    /** Sets the <code>w</code> attribute. */
    public void setW(XfaMeasurement value) { setAttribute("w", value == null ? null : value.format()); }

    /** @return the typed <code>x</code> attribute, or null. */
    public XfaMeasurement getX() { return getMeasurement("x"); }
    /** Sets the <code>x</code> attribute. */
    public void setX(XfaMeasurement value) { setAttribute("x", value == null ? null : value.format()); }

    /** @return the typed <code>y</code> attribute, or null. */
    public XfaMeasurement getY() { return getMeasurement("y"); }
    /** Sets the <code>y</code> attribute. */
    public void setY(XfaMeasurement value) { setAttribute("y", value == null ? null : value.format()); }

    /** @return the <code>assist</code> child (typed), or null. */
    public Assist getAssist() { return (Assist) getChild("assist"); }
    /** Ensures and returns the <code>assist</code> child. */
    public Assist ensureAssist() { return (Assist) ensureChild("assist"); }

    /** @return the <code>bind</code> child (typed), or null. */
    public Bind getBind() { return (Bind) getChild("bind"); }
    /** Ensures and returns the <code>bind</code> child. */
    public Bind ensureBind() { return (Bind) ensureChild("bind"); }

    /** @return the <code>border</code> child (typed), or null. */
    public Border getBorder() { return (Border) getChild("border"); }
    /** Ensures and returns the <code>border</code> child. */
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /** @return the <code>calculate</code> child (typed), or null. */
    public Calculate getCalculate() { return (Calculate) getChild("calculate"); }
    /** Ensures and returns the <code>calculate</code> child. */
    public Calculate ensureCalculate() { return (Calculate) ensureChild("calculate"); }

    /** @return the <code>caption</code> child (typed), or null. */
    public Caption getCaption() { return (Caption) getChild("caption"); }
    /** Ensures and returns the <code>caption</code> child. */
    public Caption ensureCaption() { return (Caption) ensureChild("caption"); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>font</code> child (typed), or null. */
    public Font getFont() { return (Font) getChild("font"); }
    /** Ensures and returns the <code>font</code> child. */
    public Font ensureFont() { return (Font) ensureChild("font"); }

    /** @return the <code>format</code> child (typed), or null. */
    public Format getFormat() { return (Format) getChild("format"); }
    /** Ensures and returns the <code>format</code> child. */
    public Format ensureFormat() { return (Format) ensureChild("format"); }

    /** @return the <code>keep</code> child (typed), or null. */
    public Keep getKeep() { return (Keep) getChild("keep"); }
    /** Ensures and returns the <code>keep</code> child. */
    public Keep ensureKeep() { return (Keep) ensureChild("keep"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /** @return the <code>para</code> child (typed), or null. */
    public Para getPara() { return (Para) getChild("para"); }
    /** Ensures and returns the <code>para</code> child. */
    public Para ensurePara() { return (Para) ensureChild("para"); }

    /** @return the <code>traversal</code> child (typed), or null. */
    public Traversal getTraversal() { return (Traversal) getChild("traversal"); }
    /** Ensures and returns the <code>traversal</code> child. */
    public Traversal ensureTraversal() { return (Traversal) ensureChild("traversal"); }

    /** @return the <code>ui</code> child (typed), or null. */
    public Ui getUi() { return (Ui) getChild("ui"); }
    /** Ensures and returns the <code>ui</code> child. */
    public Ui ensureUi() { return (Ui) ensureChild("ui"); }

    /** @return the <code>validate</code> child (typed), or null. */
    public Validate getValidate() { return (Validate) getChild("validate"); }
    /** Ensures and returns the <code>validate</code> child. */
    public Validate ensureValidate() { return (Validate) ensureChild("validate"); }

    /** @return the <code>value</code> child (typed), or null. */
    public Value getValue() { return (Value) getChild("value"); }
    /** Ensures and returns the <code>value</code> child. */
    public Value ensureValue() { return (Value) ensureChild("value"); }

    /** @return the <code>bindItems</code> children (typed). */
    public java.util.List<BindItems> getBindItemsList() {
        java.util.List<BindItems> r = new java.util.ArrayList<BindItems>();
        for (XfaNode n : getChildren("bindItems")) { r.add((BindItems) n); }
        return r;
    }
    /** Appends a new <code>bindItems</code> child. */
    public BindItems addBindItems() { return (BindItems) addChild("bindItems"); }

    /** @return the <code>calcProperty</code> children (typed). */
    public java.util.List<CalcProperty> getCalcPropertyList() {
        java.util.List<CalcProperty> r = new java.util.ArrayList<CalcProperty>();
        for (XfaNode n : getChildren("calcProperty")) { r.add((CalcProperty) n); }
        return r;
    }
    /** Appends a new <code>calcProperty</code> child. */
    public CalcProperty addCalcProperty() { return (CalcProperty) addChild("calcProperty"); }

    /** @return the <code>connect</code> children (typed). */
    public java.util.List<Connect> getConnectList() {
        java.util.List<Connect> r = new java.util.ArrayList<Connect>();
        for (XfaNode n : getChildren("connect")) { r.add((Connect) n); }
        return r;
    }
    /** Appends a new <code>connect</code> child. */
    public Connect addConnect() { return (Connect) addChild("connect"); }

    /** @return the <code>event</code> children (typed). */
    public java.util.List<Event> getEventList() {
        java.util.List<Event> r = new java.util.ArrayList<Event>();
        for (XfaNode n : getChildren("event")) { r.add((Event) n); }
        return r;
    }
    /** Appends a new <code>event</code> child. */
    public Event addEvent() { return (Event) addChild("event"); }

    /** @return the <code>items</code> children (typed). */
    public java.util.List<Items> getItemsList() {
        java.util.List<Items> r = new java.util.ArrayList<Items>();
        for (XfaNode n : getChildren("items")) { r.add((Items) n); }
        return r;
    }
    /** Appends a new <code>items</code> child. */
    public Items addItems() { return (Items) addChild("items"); }

    /** @return the <code>setProperty</code> children (typed). */
    public java.util.List<SetProperty> getSetPropertyList() {
        java.util.List<SetProperty> r = new java.util.ArrayList<SetProperty>();
        for (XfaNode n : getChildren("setProperty")) { r.add((SetProperty) n); }
        return r;
    }
    /** Appends a new <code>setProperty</code> child. */
    public SetProperty addSetProperty() { return (SetProperty) addChild("setProperty"); }
}
