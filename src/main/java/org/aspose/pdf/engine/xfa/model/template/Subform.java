package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `subform`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Subform extends XfaNode {

    /// Wraps a backing `subform` element.
    public Subform(Element element, XfaNode parent) {
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

    /// Allowed values of the `allowMacro` attribute.
    public enum AllowMacroValue {
        V_0("0"),
        V_1("1");
        private final String v;
        AllowMacroValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AllowMacroValue fromValue(String s) {
            for (AllowMacroValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `allowMacro` attribute, or null.
    public AllowMacroValue getAllowMacro() {
        String v = getAttribute("allowMacro");
        return v == null ? null : AllowMacroValue.fromValue(v);
    }
    /// Sets the `allowMacro` attribute.
    public void setAllowMacro(AllowMacroValue value) {
        setAttribute("allowMacro", value == null ? null : value.value());
    }
    /// @return the raw `allowMacro` string, or null.
    public String getAllowMacroRaw() { return getAttribute("allowMacro"); }

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

    /// @return the typed `columnWidths` attribute, or null.
    public String getColumnWidths() { return getString("columnWidths"); }
    /// Sets the `columnWidths` attribute.
    public void setColumnWidths(String value) { setAttribute("columnWidths", value); }

    /// @return the typed `h` attribute, or null.
    public XfaMeasurement getH() { return getMeasurement("h"); }
    /// Sets the `h` attribute.
    public void setH(XfaMeasurement value) { setAttribute("h", value == null ? null : value.format()); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `layout` attribute.
    public enum LayoutValue {
        LR_TB("lr-tb"),
        POSITION("position"),
        RL_TB("rl-tb"),
        ROW("row"),
        TABLE("table"),
        TB("tb");
        private final String v;
        LayoutValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static LayoutValue fromValue(String s) {
            for (LayoutValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `layout` attribute, or null.
    public LayoutValue getLayout() {
        String v = getAttribute("layout");
        return v == null ? null : LayoutValue.fromValue(v);
    }
    /// Sets the `layout` attribute.
    public void setLayout(LayoutValue value) {
        setAttribute("layout", value == null ? null : value.value());
    }
    /// @return the raw `layout` string, or null.
    public String getLayoutRaw() { return getAttribute("layout"); }

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

    /// Allowed values of the `restoreState` attribute.
    public enum RestoreStateValue {
        AUTO("auto"),
        MANUAL("manual");
        private final String v;
        RestoreStateValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static RestoreStateValue fromValue(String s) {
            for (RestoreStateValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `restoreState` attribute, or null.
    public RestoreStateValue getRestoreState() {
        String v = getAttribute("restoreState");
        return v == null ? null : RestoreStateValue.fromValue(v);
    }
    /// Sets the `restoreState` attribute.
    public void setRestoreState(RestoreStateValue value) {
        setAttribute("restoreState", value == null ? null : value.value());
    }
    /// @return the raw `restoreState` string, or null.
    public String getRestoreStateRaw() { return getAttribute("restoreState"); }

    /// Allowed values of the `scope` attribute.
    public enum ScopeValue {
        NAME("name"),
        NONE("none");
        private final String v;
        ScopeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static ScopeValue fromValue(String s) {
            for (ScopeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `scope` attribute, or null.
    public ScopeValue getScope() {
        String v = getAttribute("scope");
        return v == null ? null : ScopeValue.fromValue(v);
    }
    /// Sets the `scope` attribute.
    public void setScope(ScopeValue value) {
        setAttribute("scope", value == null ? null : value.value());
    }
    /// @return the raw `scope` string, or null.
    public String getScopeRaw() { return getAttribute("scope"); }

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

    /// @return the `bookend` child (typed), or null.
    public Bookend getBookend() { return (Bookend) getChild("bookend"); }
    /// Ensures and returns the `bookend` child.
    public Bookend ensureBookend() { return (Bookend) ensureChild("bookend"); }

    /// @return the `border` child (typed), or null.
    public Border getBorder() { return (Border) getChild("border"); }
    /// Ensures and returns the `border` child.
    public Border ensureBorder() { return (Border) ensureChild("border"); }

    /// @return the `break` child (typed), or null.
    public Break getBreak() { return (Break) getChild("break"); }
    /// Ensures and returns the `break` child.
    public Break ensureBreak() { return (Break) ensureChild("break"); }

    /// @return the `calculate` child (typed), or null.
    public Calculate getCalculate() { return (Calculate) getChild("calculate"); }
    /// Ensures and returns the `calculate` child.
    public Calculate ensureCalculate() { return (Calculate) ensureChild("calculate"); }

    /// @return the `desc` child (typed), or null.
    public Desc getDesc() { return (Desc) getChild("desc"); }
    /// Ensures and returns the `desc` child.
    public Desc ensureDesc() { return (Desc) ensureChild("desc"); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `keep` child (typed), or null.
    public Keep getKeep() { return (Keep) getChild("keep"); }
    /// Ensures and returns the `keep` child.
    public Keep ensureKeep() { return (Keep) ensureChild("keep"); }

    /// @return the `margin` child (typed), or null.
    public Margin getMargin() { return (Margin) getChild("margin"); }
    /// Ensures and returns the `margin` child.
    public Margin ensureMargin() { return (Margin) ensureChild("margin"); }

    /// @return the `occur` child (typed), or null.
    public Occur getOccur() { return (Occur) getChild("occur"); }
    /// Ensures and returns the `occur` child.
    public Occur ensureOccur() { return (Occur) ensureChild("occur"); }

    /// @return the `overflow` child (typed), or null.
    public Overflow getOverflow() { return (Overflow) getChild("overflow"); }
    /// Ensures and returns the `overflow` child.
    public Overflow ensureOverflow() { return (Overflow) ensureChild("overflow"); }

    /// @return the `pageSet` child (typed), or null.
    public PageSet getPageSet() { return (PageSet) getChild("pageSet"); }
    /// Ensures and returns the `pageSet` child.
    public PageSet ensurePageSet() { return (PageSet) ensureChild("pageSet"); }

    /// @return the `para` child (typed), or null.
    public Para getPara() { return (Para) getChild("para"); }
    /// Ensures and returns the `para` child.
    public Para ensurePara() { return (Para) ensureChild("para"); }

    /// @return the `traversal` child (typed), or null.
    public Traversal getTraversal() { return (Traversal) getChild("traversal"); }
    /// Ensures and returns the `traversal` child.
    public Traversal ensureTraversal() { return (Traversal) ensureChild("traversal"); }

    /// @return the `validate` child (typed), or null.
    public Validate getValidate() { return (Validate) getChild("validate"); }
    /// Ensures and returns the `validate` child.
    public Validate ensureValidate() { return (Validate) ensureChild("validate"); }

    /// @return the `variables` child (typed), or null.
    public Variables getVariables() { return (Variables) getChild("variables"); }
    /// Ensures and returns the `variables` child.
    public Variables ensureVariables() { return (Variables) ensureChild("variables"); }

    /// @return the `area` children (typed).
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /// Appends a new `area` child.
    public Area addArea() { return (Area) addChild("area"); }

    /// @return the `breakAfter` children (typed).
    public java.util.List<BreakAfter> getBreakAfterList() {
        java.util.List<BreakAfter> r = new java.util.ArrayList<BreakAfter>();
        for (XfaNode n : getChildren("breakAfter")) { r.add((BreakAfter) n); }
        return r;
    }
    /// Appends a new `breakAfter` child.
    public BreakAfter addBreakAfter() { return (BreakAfter) addChild("breakAfter"); }

    /// @return the `breakBefore` children (typed).
    public java.util.List<BreakBefore> getBreakBeforeList() {
        java.util.List<BreakBefore> r = new java.util.ArrayList<BreakBefore>();
        for (XfaNode n : getChildren("breakBefore")) { r.add((BreakBefore) n); }
        return r;
    }
    /// Appends a new `breakBefore` child.
    public BreakBefore addBreakBefore() { return (BreakBefore) addChild("breakBefore"); }

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

    /// @return the `draw` children (typed).
    public java.util.List<Draw> getDrawList() {
        java.util.List<Draw> r = new java.util.ArrayList<Draw>();
        for (XfaNode n : getChildren("draw")) { r.add((Draw) n); }
        return r;
    }
    /// Appends a new `draw` child.
    public Draw addDraw() { return (Draw) addChild("draw"); }

    /// @return the `event` children (typed).
    public java.util.List<Event> getEventList() {
        java.util.List<Event> r = new java.util.ArrayList<Event>();
        for (XfaNode n : getChildren("event")) { r.add((Event) n); }
        return r;
    }
    /// Appends a new `event` child.
    public Event addEvent() { return (Event) addChild("event"); }

    /// @return the `exclGroup` children (typed).
    public java.util.List<ExclGroup> getExclGroupList() {
        java.util.List<ExclGroup> r = new java.util.ArrayList<ExclGroup>();
        for (XfaNode n : getChildren("exclGroup")) { r.add((ExclGroup) n); }
        return r;
    }
    /// Appends a new `exclGroup` child.
    public ExclGroup addExclGroup() { return (ExclGroup) addChild("exclGroup"); }

    /// @return the `exObject` children (typed).
    public java.util.List<ExObject> getExObjectList() {
        java.util.List<ExObject> r = new java.util.ArrayList<ExObject>();
        for (XfaNode n : getChildren("exObject")) { r.add((ExObject) n); }
        return r;
    }
    /// Appends a new `exObject` child.
    public ExObject addExObject() { return (ExObject) addChild("exObject"); }

    /// @return the `field` children (typed).
    public java.util.List<Field> getFieldList() {
        java.util.List<Field> r = new java.util.ArrayList<Field>();
        for (XfaNode n : getChildren("field")) { r.add((Field) n); }
        return r;
    }
    /// Appends a new `field` child.
    public Field addField() { return (Field) addChild("field"); }

    /// @return the `proto` children (typed).
    public java.util.List<Proto> getProtoList() {
        java.util.List<Proto> r = new java.util.ArrayList<Proto>();
        for (XfaNode n : getChildren("proto")) { r.add((Proto) n); }
        return r;
    }
    /// Appends a new `proto` child.
    public Proto addProto() { return (Proto) addChild("proto"); }

    /// @return the `setProperty` children (typed).
    public java.util.List<SetProperty> getSetPropertyList() {
        java.util.List<SetProperty> r = new java.util.ArrayList<SetProperty>();
        for (XfaNode n : getChildren("setProperty")) { r.add((SetProperty) n); }
        return r;
    }
    /// Appends a new `setProperty` child.
    public SetProperty addSetProperty() { return (SetProperty) addChild("setProperty"); }

    /// @return the `subform` children (typed).
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /// Appends a new `subform` child.
    public Subform addSubform() { return (Subform) addChild("subform"); }

    /// @return the `subformSet` children (typed).
    public java.util.List<SubformSet> getSubformSetList() {
        java.util.List<SubformSet> r = new java.util.ArrayList<SubformSet>();
        for (XfaNode n : getChildren("subformSet")) { r.add((SubformSet) n); }
        return r;
    }
    /// Appends a new `subformSet` child.
    public SubformSet addSubformSet() { return (SubformSet) addChild("subformSet"); }
}
