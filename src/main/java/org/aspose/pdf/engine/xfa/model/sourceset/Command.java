package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>command</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Command extends XfaNode {

    /** Wraps a backing <code>command</code> element. */
    public Command(Element element, XfaNode parent) {
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

    /** @return the typed <code>timeout</code> attribute, or null. */
    public java.lang.Integer getTimeout() { return getInteger("timeout"); }
    /** Sets the <code>timeout</code> attribute. */
    public void setTimeout(java.lang.Integer value) { setAttribute("timeout", value == null ? null : value.toString()); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>delete</code> child (typed), or null. */
    public Delete getDelete() { return (Delete) getChild("delete"); }
    /** Ensures and returns the <code>delete</code> child. */
    public Delete ensureDelete() { return (Delete) ensureChild("delete"); }

    /** @return the <code>insert</code> child (typed), or null. */
    public Insert getInsert() { return (Insert) getChild("insert"); }
    /** Ensures and returns the <code>insert</code> child. */
    public Insert ensureInsert() { return (Insert) ensureChild("insert"); }

    /** @return the <code>query</code> child (typed), or null. */
    public Query getQuery() { return (Query) getChild("query"); }
    /** Ensures and returns the <code>query</code> child. */
    public Query ensureQuery() { return (Query) ensureChild("query"); }

    /** @return the <code>update</code> child (typed), or null. */
    public Update getUpdate() { return (Update) getChild("update"); }
    /** Ensures and returns the <code>update</code> child. */
    public Update ensureUpdate() { return (Update) ensureChild("update"); }
}
