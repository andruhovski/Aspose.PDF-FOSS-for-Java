package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `command`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Command extends XfaNode {

    /// Wraps a backing `command` element.
    public Command(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `name` attribute, or null.
    public String getName() { return getString("name"); }
    /// Sets the `name` attribute.
    public void setName(String value) { setAttribute("name", value); }

    /// @return the typed `timeout` attribute, or null.
    public java.lang.Integer getTimeout() { return getInteger("timeout"); }
    /// Sets the `timeout` attribute.
    public void setTimeout(java.lang.Integer value) { setAttribute("timeout", value == null ? null : value.toString()); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `delete` child (typed), or null.
    public Delete getDelete() { return (Delete) getChild("delete"); }
    /// Ensures and returns the `delete` child.
    public Delete ensureDelete() { return (Delete) ensureChild("delete"); }

    /// @return the `insert` child (typed), or null.
    public Insert getInsert() { return (Insert) getChild("insert"); }
    /// Ensures and returns the `insert` child.
    public Insert ensureInsert() { return (Insert) ensureChild("insert"); }

    /// @return the `query` child (typed), or null.
    public Query getQuery() { return (Query) getChild("query"); }
    /// Ensures and returns the `query` child.
    public Query ensureQuery() { return (Query) ensureChild("query"); }

    /// @return the `update` child (typed), or null.
    public Update getUpdate() { return (Update) getChild("update"); }
    /// Ensures and returns the `update` child.
    public Update ensureUpdate() { return (Update) ensureChild("update"); }
}
