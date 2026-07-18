package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `certificates`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Certificates extends XfaNode {

    /// Wraps a backing `certificates` element.
    public Certificates(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// Allowed values of the `credentialServerPolicy` attribute.
    public enum CredentialServerPolicyValue {
        OPTIONAL("optional"),
        REQUIRED("required");
        private final String v;
        CredentialServerPolicyValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static CredentialServerPolicyValue fromValue(String s) {
            for (CredentialServerPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `credentialServerPolicy` attribute, or null.
    public CredentialServerPolicyValue getCredentialServerPolicy() {
        String v = getAttribute("credentialServerPolicy");
        return v == null ? null : CredentialServerPolicyValue.fromValue(v);
    }
    /// Sets the `credentialServerPolicy` attribute.
    public void setCredentialServerPolicy(CredentialServerPolicyValue value) {
        setAttribute("credentialServerPolicy", value == null ? null : value.value());
    }
    /// @return the raw `credentialServerPolicy` string, or null.
    public String getCredentialServerPolicyRaw() { return getAttribute("credentialServerPolicy"); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `url` attribute, or null.
    public String getUrl() { return getString("url"); }
    /// Sets the `url` attribute.
    public void setUrl(String value) { setAttribute("url", value); }

    /// @return the typed `urlPolicy` attribute, or null.
    public String getUrlPolicy() { return getString("urlPolicy"); }
    /// Sets the `urlPolicy` attribute.
    public void setUrlPolicy(String value) { setAttribute("urlPolicy", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `issuers` child (typed), or null.
    public Issuers getIssuers() { return (Issuers) getChild("issuers"); }
    /// Ensures and returns the `issuers` child.
    public Issuers ensureIssuers() { return (Issuers) ensureChild("issuers"); }

    /// @return the `keyUsage` child (typed), or null.
    public KeyUsage getKeyUsage() { return (KeyUsage) getChild("keyUsage"); }
    /// Ensures and returns the `keyUsage` child.
    public KeyUsage ensureKeyUsage() { return (KeyUsage) ensureChild("keyUsage"); }

    /// @return the `oids` child (typed), or null.
    public Oids getOids() { return (Oids) getChild("oids"); }
    /// Ensures and returns the `oids` child.
    public Oids ensureOids() { return (Oids) ensureChild("oids"); }

    /// @return the `signing` child (typed), or null.
    public Signing getSigning() { return (Signing) getChild("signing"); }
    /// Ensures and returns the `signing` child.
    public Signing ensureSigning() { return (Signing) ensureChild("signing"); }

    /// @return the `subjectDNs` child (typed), or null.
    public SubjectDNs getSubjectDNs() { return (SubjectDNs) getChild("subjectDNs"); }
    /// Ensures and returns the `subjectDNs` child.
    public SubjectDNs ensureSubjectDNs() { return (SubjectDNs) ensureChild("subjectDNs"); }
}
