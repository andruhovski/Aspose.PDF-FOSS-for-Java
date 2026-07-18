package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `message`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Message extends XfaNode {

    /// Wraps a backing `message` element.
    public Message(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `text` children (typed).
    public java.util.List<Text> getTextList() {
        java.util.List<Text> r = new java.util.ArrayList<Text>();
        for (XfaNode n : getChildren("text")) { r.add((Text) n); }
        return r;
    }
    /// Appends a new `text` child.
    public Text addText() { return (Text) addChild("text"); }
}
