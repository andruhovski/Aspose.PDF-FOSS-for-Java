package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>certificates</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Certificates extends XfaNode {

    /** Wraps a backing <code>certificates</code> element. */
    public Certificates(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>credentialServerPolicy</code> attribute. */
    public enum CredentialServerPolicyValue {
        OPTIONAL("optional"),
        REQUIRED("required");
        private final String v;
        CredentialServerPolicyValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static CredentialServerPolicyValue fromValue(String s) {
            for (CredentialServerPolicyValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>credentialServerPolicy</code> attribute, or null. */
    public CredentialServerPolicyValue getCredentialServerPolicy() {
        String v = getAttribute("credentialServerPolicy");
        return v == null ? null : CredentialServerPolicyValue.fromValue(v);
    }
    /** Sets the <code>credentialServerPolicy</code> attribute. */
    public void setCredentialServerPolicy(CredentialServerPolicyValue value) {
        setAttribute("credentialServerPolicy", value == null ? null : value.value());
    }
    /** @return the raw <code>credentialServerPolicy</code> string, or null. */
    public String getCredentialServerPolicyRaw() { return getAttribute("credentialServerPolicy"); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>url</code> attribute, or null. */
    public String getUrl() { return getString("url"); }
    /** Sets the <code>url</code> attribute. */
    public void setUrl(String value) { setAttribute("url", value); }

    /** @return the typed <code>urlPolicy</code> attribute, or null. */
    public String getUrlPolicy() { return getString("urlPolicy"); }
    /** Sets the <code>urlPolicy</code> attribute. */
    public void setUrlPolicy(String value) { setAttribute("urlPolicy", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>issuers</code> child (typed), or null. */
    public Issuers getIssuers() { return (Issuers) getChild("issuers"); }
    /** Ensures and returns the <code>issuers</code> child. */
    public Issuers ensureIssuers() { return (Issuers) ensureChild("issuers"); }

    /** @return the <code>keyUsage</code> child (typed), or null. */
    public KeyUsage getKeyUsage() { return (KeyUsage) getChild("keyUsage"); }
    /** Ensures and returns the <code>keyUsage</code> child. */
    public KeyUsage ensureKeyUsage() { return (KeyUsage) ensureChild("keyUsage"); }

    /** @return the <code>oids</code> child (typed), or null. */
    public Oids getOids() { return (Oids) getChild("oids"); }
    /** Ensures and returns the <code>oids</code> child. */
    public Oids ensureOids() { return (Oids) ensureChild("oids"); }

    /** @return the <code>signing</code> child (typed), or null. */
    public Signing getSigning() { return (Signing) getChild("signing"); }
    /** Ensures and returns the <code>signing</code> child. */
    public Signing ensureSigning() { return (Signing) ensureChild("signing"); }

    /** @return the <code>subjectDNs</code> child (typed), or null. */
    public SubjectDNs getSubjectDNs() { return (SubjectDNs) getChild("subjectDNs"); }
    /** Ensures and returns the <code>subjectDNs</code> child. */
    public SubjectDNs ensureSubjectDNs() { return (SubjectDNs) ensureChild("subjectDNs"); }
}
