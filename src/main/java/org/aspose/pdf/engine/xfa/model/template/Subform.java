package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>subform</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Subform extends XfaNode {

    /** Wraps a backing <code>subform</code> element. */
    public Subform(Element element, XfaNode parent) {
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

    /** Allowed values of the <code>allowMacro</code> attribute. */
    public enum AllowMacroValue {
        V_0("0"),
        V_1("1");
        private final String v;
        AllowMacroValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static AllowMacroValue fromValue(String s) {
            for (AllowMacroValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>allowMacro</code> attribute, or null. */
    public AllowMacroValue getAllowMacro() {
        String v = getAttribute("allowMacro");
        return v == null ? null : AllowMacroValue.fromValue(v);
    }
    /** Sets the <code>allowMacro</code> attribute. */
    public void setAllowMacro(AllowMacroValue value) {
        setAttribute("allowMacro", value == null ? null : value.value());
    }
    /** @return the raw <code>allowMacro</code> string, or null. */
    public String getAllowMacroRaw() { return getAttribute("allowMacro"); }

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

    /** @return the typed <code>columnWidths</code> attribute, or null. */
    public String getColumnWidths() { return getString("columnWidths"); }
    /** Sets the <code>columnWidths</code> attribute. */
    public void setColumnWidths(String value) { setAttribute("columnWidths", value); }

    /** @return the typed <code>h</code> attribute, or null. */
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /** Sets the <code>h</code> attribute. */
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** Allowed values of the <code>layout</code> attribute. */
    public enum LayoutValue {
        LR_TB("lr-tb"),
        POSITION("position"),
        RL_TB("rl-tb"),
        ROW("row"),
        TABLE("table"),
        TB("tb");
        private final String v;
        LayoutValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static LayoutValue fromValue(String s) {
            for (LayoutValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>layout</code> attribute, or null. */
    public LayoutValue getLayout() {
        String v = getAttribute("layout");
        return v == null ? null : LayoutValue.fromValue(v);
    }
    /** Sets the <code>layout</code> attribute. */
    public void setLayout(LayoutValue value) {
        setAttribute("layout", value == null ? null : value.value());
    }
    /** @return the raw <code>layout</code> string, or null. */
    public String getLayoutRaw() { return getAttribute("layout"); }

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

    /** Allowed values of the <code>restoreState</code> attribute. */
    public enum RestoreStateValue {
        AUTO("auto"),
        MANUAL("manual");
        private final String v;
        RestoreStateValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static RestoreStateValue fromValue(String s) {
            for (RestoreStateValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>restoreState</code> attribute, or null. */
    public RestoreStateValue getRestoreState() {
        String v = getAttribute("restoreState");
        return v == null ? null : RestoreStateValue.fromValue(v);
    }
    /** Sets the <code>restoreState</code> attribute. */
    public void setRestoreState(RestoreStateValue value) {
        setAttribute("restoreState", value == null ? null : value.value());
    }
    /** @return the raw <code>restoreState</code> string, or null. */
    public String getRestoreStateRaw() { return getAttribute("restoreState"); }

    /** Allowed values of the <code>scope</code> attribute. */
    public enum ScopeValue {
        NAME("name"),
        NONE("none");
        private final String v;
        ScopeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ScopeValue fromValue(String s) {
            for (ScopeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>scope</code> attribute, or null. */
    public ScopeValue getScope() {
        String v = getAttribute("scope");
        return v == null ? null : ScopeValue.fromValue(v);
    }
    /** Sets the <code>scope</code> attribute. */
    public void setScope(ScopeValue value) {
        setAttribute("scope", value == null ? null : value.value());
    }
    /** @return the raw <code>scope</code> string, or null. */
    public String getScopeRaw() { return getAttribute("scope"); }

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

    /** @return the <code>bookend</code> child (typed), or null. */
    public Bookend getBookend() { return (Bookend) getChild("bookend"); }
    /** Ensures and returns the <code>bookend</code> child. */
    public Bookend ensureBookend() { return (Bookend) ensureChild("bookend"); }

    /** @return the <code>border</code> child (typed), or null. */
    public Border getBorder() { return (Border) getChild("border"); }
    /** Ensures and returns the <code>border</code> child. */
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /** @return the <code>break</code> child (typed), or null. */
    public Break getBreak() { return (Break) getChild("break"); }
    /** Ensures and returns the <code>break</code> child. */
    public Break ensureBreak() { return (Break) ensureChild("break"); }

    /** @return the <code>calculate</code> child (typed), or null. */
    public Calculate getCalculate() { return (Calculate) getChild("calculate"); }
    /** Ensures and returns the <code>calculate</code> child. */
    public Calculate ensureCalculate() { return (Calculate) ensureChild("calculate"); }

    /** @return the <code>desc</code> child (typed), or null. */
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /** Ensures and returns the <code>desc</code> child. */
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>keep</code> child (typed), or null. */
    public Keep getKeep() { return (Keep) getChild("keep"); }
    /** Ensures and returns the <code>keep</code> child. */
    public Keep ensureKeep() { return (Keep) ensureChild("keep"); }

    /** @return the <code>margin</code> child (typed), or null. */
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /** Ensures and returns the <code>margin</code> child. */
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /** @return the <code>occur</code> child (typed), or null. */
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /** Ensures and returns the <code>occur</code> child. */
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /** @return the <code>overflow</code> child (typed), or null. */
    public Overflow getOverflow() { return (Overflow) getChild("overflow"); }
    /** Ensures and returns the <code>overflow</code> child. */
    public Overflow ensureOverflow() { return (Overflow) ensureChild("overflow"); }

    /** @return the <code>pageSet</code> child (typed), or null. */
    public PageSet getPageSet() { return (PageSet) getChild("pageSet"); }
    /** Ensures and returns the <code>pageSet</code> child. */
    public PageSet ensurePageSet() { return (PageSet) ensureChild("pageSet"); }

    /** @return the <code>para</code> child (typed), or null. */
    public Para getPara() { return (Para) getChild("para"); }
    /** Ensures and returns the <code>para</code> child. */
    public Para ensurePara() { return (Para) ensureChild("para"); }

    /** @return the <code>traversal</code> child (typed), or null. */
    public Traversal getTraversal() { return (Traversal) getChild("traversal"); }
    /** Ensures and returns the <code>traversal</code> child. */
    public Traversal ensureTraversal() { return (Traversal) ensureChild("traversal"); }

    /** @return the <code>validate</code> child (typed), or null. */
    public Validate getValidate() { return (Validate) getChild("validate"); }
    /** Ensures and returns the <code>validate</code> child. */
    public Validate ensureValidate() { return (Validate) ensureChild("validate"); }

    /** @return the <code>variables</code> child (typed), or null. */
    public Variables getVariables() { return (Variables) getChild("variables"); }
    /** Ensures and returns the <code>variables</code> child. */
    public Variables ensureVariables() { return (Variables) ensureChild("variables"); }

    /** @return the <code>area</code> children (typed). */
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /** Appends a new <code>area</code> child. */
    public Area addArea() { return (Area) addChild("area"); }

    /** @return the <code>breakAfter</code> children (typed). */
    public java.util.List<BreakAfter> getBreakAfterList() {
        java.util.List<BreakAfter> r = new java.util.ArrayList<BreakAfter>();
        for (XfaNode n : getChildren("breakAfter")) { r.add((BreakAfter) n); }
        return r;
    }
    /** Appends a new <code>breakAfter</code> child. */
    public BreakAfter addBreakAfter() { return (BreakAfter) addChild("breakAfter"); }

    /** @return the <code>breakBefore</code> children (typed). */
    public java.util.List<BreakBefore> getBreakBeforeList() {
        java.util.List<BreakBefore> r = new java.util.ArrayList<BreakBefore>();
        for (XfaNode n : getChildren("breakBefore")) { r.add((BreakBefore) n); }
        return r;
    }
    /** Appends a new <code>breakBefore</code> child. */
    public BreakBefore addBreakBefore() { return (BreakBefore) addChild("breakBefore"); }

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

    /** @return the <code>draw</code> children (typed). */
    public java.util.List<Draw> getDrawList() {
        java.util.List<Draw> r = new java.util.ArrayList<Draw>();
        for (XfaNode n : getChildren("draw")) { r.add((Draw) n); }
        return r;
    }
    /** Appends a new <code>draw</code> child. */
    public Draw addDraw() { return (Draw) addChild("draw"); }

    /** @return the <code>event</code> children (typed). */
    public java.util.List<Event> getEventList() {
        java.util.List<Event> r = new java.util.ArrayList<Event>();
        for (XfaNode n : getChildren("event")) { r.add((Event) n); }
        return r;
    }
    /** Appends a new <code>event</code> child. */
    public Event addEvent() { return (Event) addChild("event"); }

    /** @return the <code>exclGroup</code> children (typed). */
    public java.util.List<ExclGroup> getExclGroupList() {
        java.util.List<ExclGroup> r = new java.util.ArrayList<ExclGroup>();
        for (XfaNode n : getChildren("exclGroup")) { r.add((ExclGroup) n); }
        return r;
    }
    /** Appends a new <code>exclGroup</code> child. */
    public ExclGroup addExclGroup() { return (ExclGroup) addChild("exclGroup"); }

    /** @return the <code>exObject</code> children (typed). */
    public java.util.List<ExObject> getExObjectList() {
        java.util.List<ExObject> r = new java.util.ArrayList<ExObject>();
        for (XfaNode n : getChildren("exObject")) { r.add((ExObject) n); }
        return r;
    }
    /** Appends a new <code>exObject</code> child. */
    public ExObject addExObject() { return (ExObject) addChild("exObject"); }

    /** @return the <code>field</code> children (typed). */
    public java.util.List<Field> getFieldList() {
        java.util.List<Field> r = new java.util.ArrayList<Field>();
        for (XfaNode n : getChildren("field")) { r.add((Field) n); }
        return r;
    }
    /** Appends a new <code>field</code> child. */
    public Field addField() { return (Field) addChild("field"); }

    /** @return the <code>proto</code> children (typed). */
    public java.util.List<Proto> getProtoList() {
        java.util.List<Proto> r = new java.util.ArrayList<Proto>();
        for (XfaNode n : getChildren("proto")) { r.add((Proto) n); }
        return r;
    }
    /** Appends a new <code>proto</code> child. */
    public Proto addProto() { return (Proto) addChild("proto"); }

    /** @return the <code>setProperty</code> children (typed). */
    public java.util.List<SetProperty> getSetPropertyList() {
        java.util.List<SetProperty> r = new java.util.ArrayList<SetProperty>();
        for (XfaNode n : getChildren("setProperty")) { r.add((SetProperty) n); }
        return r;
    }
    /** Appends a new <code>setProperty</code> child. */
    public SetProperty addSetProperty() { return (SetProperty) addChild("setProperty"); }

    /** @return the <code>subform</code> children (typed). */
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /** Appends a new <code>subform</code> child. */
    public Subform addSubform() { return (Subform) addChild("subform"); }

    /** @return the <code>subformSet</code> children (typed). */
    public java.util.List<SubformSet> getSubformSetList() {
        java.util.List<SubformSet> r = new java.util.ArrayList<SubformSet>();
        for (XfaNode n : getChildren("subformSet")) { r.add((SubformSet) n); }
        return r;
    }
    /** Appends a new <code>subformSet</code> child. */
    public SubformSet addSubformSet() { return (SubformSet) addChild("subformSet"); }
}
