package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `break`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Break extends XfaNode {

    /// Wraps a backing `break` element.
    public Break(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `after` attribute.
    public enum AfterValue {
        AUTO("auto"),
        CONTENTAREA("contentArea"),
        PAGEAREA("pageArea"),
        PAGEEVEN("pageEven"),
        PAGEODD("pageOdd");
        private final String v;
        AfterValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static AfterValue fromValue(String s) {
            for (AfterValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `after` attribute, or null.
    public AfterValue getAfter() {
        String v = getAttribute("after");
        return v == null ? null : AfterValue.fromValue(v);
    }
    /// Sets the `after` attribute.
    public void setAfter(AfterValue value) {
        setAttribute("after", value == null ? null : value.value());
    }
    /// @return the raw `after` string, or null.
    public String getAfterRaw() { return getAttribute("after"); }

    /// @return the typed `afterTarget` attribute, or null.
    public String getAfterTarget() { return getString("afterTarget"); }
    /// Sets the `afterTarget` attribute.
    public void setAfterTarget(String value) { setAttribute("afterTarget", value); }

    /// Allowed values of the `before` attribute.
    public enum BeforeValue {
        AUTO("auto"),
        CONTENTAREA("contentArea"),
        PAGEAREA("pageArea"),
        PAGEEVEN("pageEven"),
        PAGEODD("pageOdd");
        private final String v;
        BeforeValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static BeforeValue fromValue(String s) {
            for (BeforeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `before` attribute, or null.
    public BeforeValue getBefore() {
        String v = getAttribute("before");
        return v == null ? null : BeforeValue.fromValue(v);
    }
    /// Sets the `before` attribute.
    public void setBefore(BeforeValue value) {
        setAttribute("before", value == null ? null : value.value());
    }
    /// @return the raw `before` string, or null.
    public String getBeforeRaw() { return getAttribute("before"); }

    /// @return the typed `beforeTarget` attribute, or null.
    public String getBeforeTarget() { return getString("beforeTarget"); }
    /// Sets the `beforeTarget` attribute.
    public void setBeforeTarget(String value) { setAttribute("beforeTarget", value); }

    /// @return the typed `bookendLeader` attribute, or null.
    public String getBookendLeader() { return getString("bookendLeader"); }
    /// Sets the `bookendLeader` attribute.
    public void setBookendLeader(String value) { setAttribute("bookendLeader", value); }

    /// @return the typed `bookendTrailer` attribute, or null.
    public String getBookendTrailer() { return getString("bookendTrailer"); }
    /// Sets the `bookendTrailer` attribute.
    public void setBookendTrailer(String value) { setAttribute("bookendTrailer", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `overflowLeader` attribute, or null.
    public String getOverflowLeader() { return getString("overflowLeader"); }
    /// Sets the `overflowLeader` attribute.
    public void setOverflowLeader(String value) { setAttribute("overflowLeader", value); }

    /// @return the typed `overflowTarget` attribute, or null.
    public String getOverflowTarget() { return getString("overflowTarget"); }
    /// Sets the `overflowTarget` attribute.
    public void setOverflowTarget(String value) { setAttribute("overflowTarget", value); }

    /// @return the typed `overflowTrailer` attribute, or null.
    public String getOverflowTrailer() { return getString("overflowTrailer"); }
    /// Sets the `overflowTrailer` attribute.
    public void setOverflowTrailer(String value) { setAttribute("overflowTrailer", value); }

    /// Allowed values of the `startNew` attribute.
    public enum StartNewValue {
        V_0("0"),
        V_1("1");
        private final String v;
        StartNewValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static StartNewValue fromValue(String s) {
            for (StartNewValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `startNew` attribute, or null.
    public StartNewValue getStartNew() {
        String v = getAttribute("startNew");
        return v == null ? null : StartNewValue.fromValue(v);
    }
    /// Sets the `startNew` attribute.
    public void setStartNew(StartNewValue value) {
        setAttribute("startNew", value == null ? null : value.value());
    }
    /// @return the raw `startNew` string, or null.
    public String getStartNewRaw() { return getAttribute("startNew"); }

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
