package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>bookend</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Bookend extends XfaNode {

    /** Wraps a backing <code>bookend</code> element. */
    public Bookend(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>leader</code> attribute, or null. */
    public String getLeader() { return getString("leader"); }
    /** Sets the <code>leader</code> attribute. */
    public void setLeader(String value) { setAttribute("leader", value); }

    /** @return the typed <code>trailer</code> attribute, or null. */
    public String getTrailer() { return getString("trailer"); }
    /** Sets the <code>trailer</code> attribute. */
    public void setTrailer(String value) { setAttribute("trailer", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
