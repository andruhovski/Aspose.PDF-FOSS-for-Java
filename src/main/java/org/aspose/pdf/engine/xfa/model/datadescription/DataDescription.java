package org.aspose.pdf.engine.xfa.model.datadescription;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `dataDescription`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class DataDescription extends XfaNode {

    /// Wraps a backing `dataDescription` element.
    public DataDescription(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `dd:name` attribute, or null.
    public String getDd_name() { return getString("dd:name"); }
    /// Sets the `dd:name` attribute.
    public void setDd_name(String value) { setAttribute("dd:name", value); }

    /// @return the `dd:group` child (typed), or null.
    public Dd_group getDd_group() { return (Dd_group) getChild("dd:group"); }
    /// Ensures and returns the `dd:group` child.
    public Dd_group ensureDd_group() { return (Dd_group) ensureChild("dd:group"); }
}
