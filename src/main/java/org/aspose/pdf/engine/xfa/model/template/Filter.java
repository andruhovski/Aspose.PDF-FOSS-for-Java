package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>filter</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Filter extends XfaNode {

    /** Wraps a backing <code>filter</code> element. */
    public Filter(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>addRevocationInfo</code> attribute, or null. */
    public String getAddRevocationInfo() { return getString("addRevocationInfo"); }
    /** Sets the <code>addRevocationInfo</code> attribute. */
    public void setAddRevocationInfo(String value) { setAttribute("addRevocationInfo", value); }

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

    /** @return the typed <code>version</code> attribute, or null. */
    public String getVersion() { return getString("version"); }
    /** Sets the <code>version</code> attribute. */
    public void setVersion(String value) { setAttribute("version", value); }

    /** @return the <code>appearanceFilter</code> child (typed), or null. */
    public AppearanceFilter getAppearanceFilter() { return (AppearanceFilter) getChild("appearanceFilter"); }
    /** Ensures and returns the <code>appearanceFilter</code> child. */
    public AppearanceFilter ensureAppearanceFilter() { return (AppearanceFilter) ensureChild("appearanceFilter"); }

    /** @return the <code>certificates</code> child (typed), or null. */
    public Certificates getCertificates() { return (Certificates) getChild("certificates"); }
    /** Ensures and returns the <code>certificates</code> child. */
    public Certificates ensureCertificates() { return (Certificates) ensureChild("certificates"); }

    /** @return the <code>digestMethods</code> child (typed), or null. */
    public DigestMethods getDigestMethods() { return (DigestMethods) getChild("digestMethods"); }
    /** Ensures and returns the <code>digestMethods</code> child. */
    public DigestMethods ensureDigestMethods() { return (DigestMethods) ensureChild("digestMethods"); }

    /** @return the <code>encodings</code> child (typed), or null. */
    public Encodings getEncodings() { return (Encodings) getChild("encodings"); }
    /** Ensures and returns the <code>encodings</code> child. */
    public Encodings ensureEncodings() { return (Encodings) ensureChild("encodings"); }

    /** @return the <code>handler</code> child (typed), or null. */
    public Handler getHandler() { return (Handler) getChild("handler"); }
    /** Ensures and returns the <code>handler</code> child. */
    public Handler ensureHandler() { return (Handler) ensureChild("handler"); }

    /** @return the <code>lockDocument</code> child (typed), or null. */
    public LockDocument getLockDocument() { return (LockDocument) getChild("lockDocument"); }
    /** Ensures and returns the <code>lockDocument</code> child. */
    public LockDocument ensureLockDocument() { return (LockDocument) ensureChild("lockDocument"); }

    /** @return the <code>mdp</code> child (typed), or null. */
    public Mdp getMdp() { return (Mdp) getChild("mdp"); }
    /** Ensures and returns the <code>mdp</code> child. */
    public Mdp ensureMdp() { return (Mdp) ensureChild("mdp"); }

    /** @return the <code>reasons</code> child (typed), or null. */
    public Reasons getReasons() { return (Reasons) getChild("reasons"); }
    /** Ensures and returns the <code>reasons</code> child. */
    public Reasons ensureReasons() { return (Reasons) ensureChild("reasons"); }

    /** @return the <code>timeStamp</code> child (typed), or null. */
    public TimeStamp getTimeStamp() { return (TimeStamp) getChild("timeStamp"); }
    /** Ensures and returns the <code>timeStamp</code> child. */
    public TimeStamp ensureTimeStamp() { return (TimeStamp) ensureChild("timeStamp"); }
}
