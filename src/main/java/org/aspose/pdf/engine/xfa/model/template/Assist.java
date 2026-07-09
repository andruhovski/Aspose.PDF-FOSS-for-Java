package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>assist</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Assist extends XfaNode {

    /** Wraps a backing <code>assist</code> element. */
    public Assist(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>role</code> attribute, or null. */
    public String getRole() { return getString("role"); }
    /** Sets the <code>role</code> attribute. */
    public void setRole(String value) { setAttribute("role", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>speak</code> child (typed), or null. */
    public Speak getSpeak() { return (Speak) getChild("speak"); }
    /** Ensures and returns the <code>speak</code> child. */
    public Speak ensureSpeak() { return (Speak) ensureChild("speak"); }

    /** @return the <code>toolTip</code> child (typed), or null. */
    public ToolTip getToolTip() { return (ToolTip) getChild("toolTip"); }
    /** Ensures and returns the <code>toolTip</code> child. */
    public ToolTip ensureToolTip() { return (ToolTip) ensureChild("toolTip"); }
}
