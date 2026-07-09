package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>query</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Query extends XfaNode {

    /** Wraps a backing <code>query</code> element. */
    public Query(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>commandType</code> attribute. */
    public enum CommandTypeValue {
        STOREDPROC("storedProc"),
        TABLE("table"),
        TEXT("text"),
        UNKNOWN("unknown");
        private final String v;
        CommandTypeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static CommandTypeValue fromValue(String s) {
            for (CommandTypeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>commandType</code> attribute, or null. */
    public CommandTypeValue getCommandType() {
        String v = getAttribute("commandType");
        return v == null ? null : CommandTypeValue.fromValue(v);
    }
    /** Sets the <code>commandType</code> attribute. */
    public void setCommandType(CommandTypeValue value) {
        setAttribute("commandType", value == null ? null : value.value());
    }
    /** @return the raw <code>commandType</code> string, or null. */
    public String getCommandTypeRaw() { return getAttribute("commandType"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>name</code> attribute, or null. */
    public String getName() { return getString("name"); }
    /** Sets the <code>name</code> attribute. */
    public void setName(String value) { setAttribute("name", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>recordSet</code> child (typed), or null. */
    public RecordSet getRecordSet() { return (RecordSet) getChild("recordSet"); }
    /** Ensures and returns the <code>recordSet</code> child. */
    public RecordSet ensureRecordSet() { return (RecordSet) ensureChild("recordSet"); }

    /** @return the <code>select</code> child (typed), or null. */
    public Select getSelect() { return (Select) getChild("select"); }
    /** Ensures and returns the <code>select</code> child. */
    public Select ensureSelect() { return (Select) ensureChild("select"); }

    /** @return the <code>map</code> children (typed). */
    public java.util.List<Map> getMapList() {
        java.util.List<Map> r = new java.util.ArrayList<Map>();
        for (XfaNode n : getChildren("map")) { r.add((Map) n); }
        return r;
    }
    /** Appends a new <code>map</code> child. */
    public Map addMap() { return (Map) addChild("map"); }
}
