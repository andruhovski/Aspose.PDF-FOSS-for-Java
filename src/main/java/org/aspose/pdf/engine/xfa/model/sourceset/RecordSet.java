package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `recordSet`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class RecordSet extends XfaNode {

    /// Wraps a backing `recordSet` element.
    public RecordSet(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `bofAction` attribute.
    public enum BofActionValue {
        MOVEFIRST("moveFirst"),
        STAYBOF("stayBOF");
        private final String v;
        BofActionValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static BofActionValue fromValue(String s) {
            for (BofActionValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `bofAction` attribute, or null.
    public BofActionValue getBofAction() {
        String v = getAttribute("bofAction");
        return v == null ? null : BofActionValue.fromValue(v);
    }
    /// Sets the `bofAction` attribute.
    public void setBofAction(BofActionValue value) {
        setAttribute("bofAction", value == null ? null : value.value());
    }
    /// @return the raw `bofAction` string, or null.
    public String getBofActionRaw() { return getAttribute("bofAction"); }

    /// Allowed values of the `cursorLocation` attribute.
    public enum CursorLocationValue {
        CLIENT("client"),
        SERVER("server");
        private final String v;
        CursorLocationValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static CursorLocationValue fromValue(String s) {
            for (CursorLocationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `cursorLocation` attribute, or null.
    public CursorLocationValue getCursorLocation() {
        String v = getAttribute("cursorLocation");
        return v == null ? null : CursorLocationValue.fromValue(v);
    }
    /// Sets the `cursorLocation` attribute.
    public void setCursorLocation(CursorLocationValue value) {
        setAttribute("cursorLocation", value == null ? null : value.value());
    }
    /// @return the raw `cursorLocation` string, or null.
    public String getCursorLocationRaw() { return getAttribute("cursorLocation"); }

    /// Allowed values of the `cursorType` attribute.
    public enum CursorTypeValue {
        DYNAMIC("dynamic"),
        FORWARDONLY("forwardOnly"),
        KEYSET("keyset"),
        STATIC("static"),
        UNSPECIFIED("unspecified");
        private final String v;
        CursorTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static CursorTypeValue fromValue(String s) {
            for (CursorTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `cursorType` attribute, or null.
    public CursorTypeValue getCursorType() {
        String v = getAttribute("cursorType");
        return v == null ? null : CursorTypeValue.fromValue(v);
    }
    /// Sets the `cursorType` attribute.
    public void setCursorType(CursorTypeValue value) {
        setAttribute("cursorType", value == null ? null : value.value());
    }
    /// @return the raw `cursorType` string, or null.
    public String getCursorTypeRaw() { return getAttribute("cursorType"); }

    /// Allowed values of the `eofAction` attribute.
    public enum EofActionValue {
        ADDNEW("addNew"),
        MOVELAST("moveLast"),
        STAYEOF("stayEOF");
        private final String v;
        EofActionValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static EofActionValue fromValue(String s) {
            for (EofActionValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `eofAction` attribute, or null.
    public EofActionValue getEofAction() {
        String v = getAttribute("eofAction");
        return v == null ? null : EofActionValue.fromValue(v);
    }
    /// Sets the `eofAction` attribute.
    public void setEofAction(EofActionValue value) {
        setAttribute("eofAction", value == null ? null : value.value());
    }
    /// @return the raw `eofAction` string, or null.
    public String getEofActionRaw() { return getAttribute("eofAction"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `lockType` attribute.
    public enum LockTypeValue {
        BATCHOPTIMISTIC("batchOptimistic"),
        OPTIMISTIC("optimistic"),
        PESSIMISTIC("pessimistic"),
        READONLY("readOnly"),
        UNSPECIFIED("unspecified");
        private final String v;
        LockTypeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static LockTypeValue fromValue(String s) {
            for (LockTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `lockType` attribute, or null.
    public LockTypeValue getLockType() {
        String v = getAttribute("lockType");
        return v == null ? null : LockTypeValue.fromValue(v);
    }
    /// Sets the `lockType` attribute.
    public void setLockType(LockTypeValue value) {
        setAttribute("lockType", value == null ? null : value.value());
    }
    /// @return the raw `lockType` string, or null.
    public String getLockTypeRaw() { return getAttribute("lockType"); }

    /// @return the typed `max` attribute, or null.
    public java.lang.Integer getMax() { return getInteger("max"); }
    /// Sets the `max` attribute.
    public void setMax(java.lang.Integer value) { setAttribute("max", value == null ? null : value.toString()); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `extras` children (typed).
    public java.util.List<Extras> getExtrasList() {
        java.util.List<Extras> r = new java.util.ArrayList<Extras>();
        for (XfaNode n : getChildren("extras")) { r.add((Extras) n); }
        return r;
    }
    /// Appends a new `extras` child.
    public Extras addExtras() { return (Extras) addChild("extras"); }
}
