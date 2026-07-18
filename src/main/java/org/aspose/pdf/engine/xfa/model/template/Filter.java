package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `filter`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Filter extends XfaNode {

    /// Wraps a backing `filter` element.
    public Filter(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `addRevocationInfo` attribute, or null.
    public String getAddRevocationInfo() { return getString("addRevocationInfo"); }
    /// Sets the `addRevocationInfo` attribute.
    public void setAddRevocationInfo(String value) { setAttribute("addRevocationInfo", value); }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

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

    /// @return the typed `version` attribute, or null.
    public String getVersion() { return getString("version"); }
    /// Sets the `version` attribute.
    public void setVersion(String value) { setAttribute("version", value); }

    /// @return the `appearanceFilter` child (typed), or null.
    public AppearanceFilter getAppearanceFilter() { return (AppearanceFilter) getChild("appearanceFilter"); }
    /// Ensures and returns the `appearanceFilter` child.
    public AppearanceFilter ensureAppearanceFilter() { return (AppearanceFilter) ensureChild("appearanceFilter"); }

    /// @return the `certificates` child (typed), or null.
    public Certificates getCertificates() { return (Certificates) getChild("certificates"); }
    /// Ensures and returns the `certificates` child.
    public Certificates ensureCertificates() { return (Certificates) ensureChild("certificates"); }

    /// @return the `digestMethods` child (typed), or null.
    public DigestMethods getDigestMethods() { return (DigestMethods) getChild("digestMethods"); }
    /// Ensures and returns the `digestMethods` child.
    public DigestMethods ensureDigestMethods() { return (DigestMethods) ensureChild("digestMethods"); }

    /// @return the `encodings` child (typed), or null.
    public Encodings getEncodings() { return (Encodings) getChild("encodings"); }
    /// Ensures and returns the `encodings` child.
    public Encodings ensureEncodings() { return (Encodings) ensureChild("encodings"); }

    /// @return the `handler` child (typed), or null.
    public Handler getHandler() { return (Handler) getChild("handler"); }
    /// Ensures and returns the `handler` child.
    public Handler ensureHandler() { return (Handler) ensureChild("handler"); }

    /// @return the `lockDocument` child (typed), or null.
    public LockDocument getLockDocument() { return (LockDocument) getChild("lockDocument"); }
    /// Ensures and returns the `lockDocument` child.
    public LockDocument ensureLockDocument() { return (LockDocument) ensureChild("lockDocument"); }

    /// @return the `mdp` child (typed), or null.
    public Mdp getMdp() { return (Mdp) getChild("mdp"); }
    /// Ensures and returns the `mdp` child.
    public Mdp ensureMdp() { return (Mdp) ensureChild("mdp"); }

    /// @return the `reasons` child (typed), or null.
    public Reasons getReasons() { return (Reasons) getChild("reasons"); }
    /// Ensures and returns the `reasons` child.
    public Reasons ensureReasons() { return (Reasons) ensureChild("reasons"); }

    /// @return the `timeStamp` child (typed), or null.
    public TimeStamp getTimeStamp() { return (TimeStamp) getChild("timeStamp"); }
    /// Ensures and returns the `timeStamp` child.
    public TimeStamp ensureTimeStamp() { return (TimeStamp) ensureChild("timeStamp"); }
}
