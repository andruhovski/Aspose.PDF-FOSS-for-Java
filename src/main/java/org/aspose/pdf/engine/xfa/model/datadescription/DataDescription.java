package org.aspose.pdf.engine.xfa.model.datadescription;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>dataDescription</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class DataDescription extends XfaNode {

    /** Wraps a backing <code>dataDescription</code> element. */
    public DataDescription(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>dd:name</code> attribute, or null. */
    public String getDd_name() { return getString("dd:name"); }
    /** Sets the <code>dd:name</code> attribute. */
    public void setDd_name(String value) { setAttribute("dd:name", value); }

    /** @return the <code>dd:group</code> child (typed), or null. */
    public Dd_group getDd_group() { return (Dd_group) getChild("dd:group"); }
    /** Ensures and returns the <code>dd:group</code> child. */
    public Dd_group ensureDd_group() { return (Dd_group) ensureChild("dd:group"); }
}
