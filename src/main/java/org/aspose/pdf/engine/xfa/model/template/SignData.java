package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `signData`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class SignData extends XfaNode {

    /// Wraps a backing `signData` element.
    public SignData(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// Allowed values of the `operation` attribute.
    public enum OperationValue {
        CLEAR("clear"),
        SIGN("sign"),
        VERIFY("verify");
        private final String v;
        OperationValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OperationValue fromValue(String s) {
            for (OperationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `operation` attribute, or null.
    public OperationValue getOperation() {
        String v = getAttribute("operation");
        return v == null ? null : OperationValue.fromValue(v);
    }
    /// Sets the `operation` attribute.
    public void setOperation(OperationValue value) {
        setAttribute("operation", value == null ? null : value.value());
    }
    /// @return the raw `operation` string, or null.
    public String getOperationRaw() { return getAttribute("operation"); }

    /// @return the typed `ref` attribute, or null.
    public String getRef() { return getString("ref"); }
    /// Sets the `ref` attribute.
    public void setRef(String value) { setAttribute("ref", value); }

    /// @return the typed `target` attribute, or null.
    public String getTarget() { return getString("target"); }
    /// Sets the `target` attribute.
    public void setTarget(String value) { setAttribute("target", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `filter` child (typed), or null.
    public Filter getFilter() { return (Filter) getChild("filter"); }
    /// Ensures and returns the `filter` child.
    public Filter ensureFilter() { return (Filter) ensureChild("filter"); }

    /// @return the `manifest` child (typed), or null.
    public Manifest getManifest() { return (Manifest) getChild("manifest"); }
    /// Ensures and returns the `manifest` child.
    public Manifest ensureManifest() { return (Manifest) ensureChild("manifest"); }
}
