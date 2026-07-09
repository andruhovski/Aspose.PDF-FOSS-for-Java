package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>sourceSet</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class SourceSet extends XfaNode {

    /** Wraps a backing <code>sourceSet</code> element. */
    public SourceSet(Element element, XfaNode parent) {
        super(element, parent);
    }

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

    /** @return the <code>source</code> children (typed). */
    public java.util.List<Source> getSourceList() {
        java.util.List<Source> r = new java.util.ArrayList<Source>();
        for (XfaNode n : getChildren("source")) { r.add((Source) n); }
        return r;
    }
    /** Appends a new <code>source</code> child. */
    public Source addSource() { return (Source) addChild("source"); }
}
