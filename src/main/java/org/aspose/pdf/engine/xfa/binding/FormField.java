package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.model.XfaNode;

import java.util.ArrayList;
import java.util.List;

/// A field in the merged Form DOM: its SOM path, bound value, choice items,
/// UI hint and the binding kind that produced it.
public final class FormField {

    /// How a node obtained (or did not obtain) its data binding.
    public enum BindingKind {
        /// `bind match="once"` (or default automatic binding) — bound to first match.
        ONCE,
        /// `bind match="dataRef"` — bound via a SOM `ref`.
        DATAREF,
        /// `bind match="global"` — bound to a globally-named data node.
        GLOBAL,
        /// `bind match="none"` — template-only, never data-bound.
        NONE,
        /// Bindable but no matching data was found (empty merge or absent data).
        UNBOUND
    }

    private final String name;
    private final String somPath;
    private String value;
    private final List<String> items;
    private final String uiType;
    private final BindingKind kind;
    private final XfaNode formNode;

    FormField(String name, String somPath, String value, List<String> items,
              String uiType, BindingKind kind, XfaNode formNode) {
        this.name = name;
        this.somPath = somPath;
        this.value = value;
        this.items = items == null ? new ArrayList<>() : items;
        this.uiType = uiType;
        this.kind = kind;
        this.formNode = formNode;
    }

    /// @return the field name.
    public String getName() {
        return name;
    }

    /// @return the field's dotted SOM path in the form.
    public String getSomPath() {
        return somPath;
    }

    /// @return the bound (or default) value, or `null`.
    public String getValue() {
        return value;
    }

    /// Sets this field's value, writing through to the Form DOM node's `<value><text>` so a
    /// Stage-B script (calculate / initialize) result flows into the render track (which reads the
    /// value from this field and, on save, from the node). Idempotent for an unchanged value.
    ///
    /// @param newValue the computed value (a `null` clears it)
    public void setValue(String newValue) {
        this.value = newValue;
        if (formNode != null && formNode.getElement() != null) {
            writeNodeValue(formNode.getElement(), newValue);
        }
    }

    /// Writes `v` into the field element's `<value><text>` child (creating it as needed).
    private static void writeNodeValue(org.w3c.dom.Element field, String v) {
        String ns = field.getNamespaceURI();
        org.w3c.dom.Element value = firstChildNs(field, "value");
        if (value == null) {
            value = field.getOwnerDocument().createElementNS(ns, "value");
            field.appendChild(value);
        }
        // Reuse the existing typed content child (text/integer/decimal/…) if present, else add <text>.
        org.w3c.dom.Node content = null;
        for (org.w3c.dom.Node n = value.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                content = n;
                break;
            }
        }
        if (content == null) {
            content = value.getOwnerDocument().createElementNS(ns, "text");
            value.appendChild(content);
        }
        content.setTextContent(v == null ? "" : v);
    }

    private static org.w3c.dom.Element firstChildNs(org.w3c.dom.Element parent, String local) {
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                String ln = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
                if (local.equals(ln)) {
                    return (org.w3c.dom.Element) n;
                }
            }
        }
        return null;
    }

    /// @return the choice items (empty if none).
    public List<String> getItems() {
        return items;
    }

    /// @return the UI widget element name (e.g. `textEdit`), or `null`.
    public String getUiType() {
        return uiType;
    }

    /// @return how this field was bound.
    public BindingKind getKind() {
        return kind;
    }

    /// For an `exclGroup` (radio group), the index into [#getItems()] of the
    /// option whose on-value equals the bound selection [#getValue()] — i.e.
    /// the chosen radio option. Returns `-1` when there is no selection or no item
    /// matches (and for non-exclGroup fields).
    ///
    /// @return the selected option index, or `-1`
    public int getSelectedItemIndex() {
        if (value == null) {
            return -1;
        }
        return items.indexOf(value);
    }

    /// @return the field's node in the Form DOM.
    public XfaNode getFormNode() {
        return formNode;
    }

    @Override
    public String toString() {
        return somPath + "=" + value + " [" + kind + "]";
    }
}
