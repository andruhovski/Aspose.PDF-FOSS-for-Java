package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `keep`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Keep extends XfaNode {

    /// Wraps a backing `keep` element.
    public Keep(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `intact` attribute.
    public enum IntactValue {
        CONTENTAREA("contentArea"),
        NONE("none"),
        PAGEAREA("pageArea");
        private final String v;
        IntactValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static IntactValue fromValue(String s) {
            for (IntactValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `intact` attribute, or null.
    public IntactValue getIntact() {
        String v = getAttribute("intact");
        return v == null ? null : IntactValue.fromValue(v);
    }
    /// Sets the `intact` attribute.
    public void setIntact(IntactValue value) {
        setAttribute("intact", value == null ? null : value.value());
    }
    /// @return the raw `intact` string, or null.
    public String getIntactRaw() { return getAttribute("intact"); }

    /// Allowed values of the `next` attribute.
    public enum NextValue {
        CONTENTAREA("contentArea"),
        NONE("none"),
        PAGEAREA("pageArea");
        private final String v;
        NextValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static NextValue fromValue(String s) {
            for (NextValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `next` attribute, or null.
    public NextValue getNext() {
        String v = getAttribute("next");
        return v == null ? null : NextValue.fromValue(v);
    }
    /// Sets the `next` attribute.
    public void setNext(NextValue value) {
        setAttribute("next", value == null ? null : value.value());
    }
    /// @return the raw `next` string, or null.
    public String getNextRaw() { return getAttribute("next"); }

    /// Allowed values of the `previous` attribute.
    public enum PreviousValue {
        CONTENTAREA("contentArea"),
        NONE("none"),
        PAGEAREA("pageArea");
        private final String v;
        PreviousValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static PreviousValue fromValue(String s) {
            for (PreviousValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `previous` attribute, or null.
    public PreviousValue getPrevious() {
        String v = getAttribute("previous");
        return v == null ? null : PreviousValue.fromValue(v);
    }
    /// Sets the `previous` attribute.
    public void setPrevious(PreviousValue value) {
        setAttribute("previous", value == null ? null : value.value());
    }
    /// @return the raw `previous` string, or null.
    public String getPreviousRaw() { return getAttribute("previous"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
