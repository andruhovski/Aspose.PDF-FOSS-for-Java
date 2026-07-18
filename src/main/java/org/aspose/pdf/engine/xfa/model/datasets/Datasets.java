package org.aspose.pdf.engine.xfa.model.datasets;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

import java.util.List;

/// The `<xfa:datasets>` packet wrapper. This element is not described by
/// the XFA data model (which defines only the inner `<data>` subtree), so it
/// is hand-modelled here as a typed container over otherwise-generic content.
///
/// The actual user-data tree under `<xfa:data>` mirrors the form's data
/// hierarchy and is therefore form-specific — it stays generic. This container
/// exposes the data root and named data nodes for A4 binding to consume, without
/// imposing any schema on the user data.
public final class Datasets extends XfaNode {

    /// XFA data namespace (version-independent family covered by the factory).
    public static final String DATA_NS = "http://www.xfa.org/schema/xfa-data/1.0/";

    /// Wraps a backing `datasets` element.
    ///
    /// @param element backing element
    /// @param parent  parent node, or `null`
    public Datasets(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// The `<xfa:data>` child (the user-data root), typed as [Data].
    ///
    /// @return the data node, or `null` if absent
    public Data getData() {
        XfaNode n = getChild("data");
        return n instanceof Data ? (Data) n : (n == null ? null : new Data(n.getElement(), this));
    }

    /// Ensures and returns the `<xfa:data>` child.
    ///
    /// @return the data node
    public Data ensureData() {
        Data d = getData();
        if (d != null) {
            return d;
        }
        org.w3c.dom.Document doc = getElement().getOwnerDocument();
        Element created = doc.createElementNS(DATA_NS, "data");
        getElement().appendChild(created);
        return new Data(created, this);
    }

    /// The `<xfa:Script>` dataset-level script child (inert; held as text), if present.
    ///
    /// @return the script node, or `null`
    public XfaNode getScript() {
        return getChild("Script");
    }

    /// All top-level data records/groups under `<xfa:data>` (named data nodes).
    ///
    /// @return the data root's child nodes, or empty if there is no data root
    public List<XfaNode> getDataChildren() {
        Data d = getData();
        return d == null ? java.util.Collections.emptyList() : d.getChildren();
    }
}
