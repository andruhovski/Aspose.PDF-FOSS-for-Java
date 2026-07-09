package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * The Form DOM: the template structure merged with data (repeating containers
 * expanded, fields bound), ready for layout (Stage C) and flattening (A5).
 *
 * <p>Exposes the merged root, the enumerated fields (name &rarr; value / items /
 * UI / binding kind), and any SOM predicates that contained script and were
 * therefore deferred (not evaluated) per the A4 scope.</p>
 */
public final class FormDom {

    private final XfaNode root;
    private final Document document;
    private final boolean emptyMerge;
    private final List<FormField> fields = new ArrayList<>();
    private final List<FormField> masterFields = new ArrayList<>();
    private final List<Element> masterPageAreas = new ArrayList<>();
    private final List<String> deferredScriptPredicates = new ArrayList<>();
    private final List<Element[]> zeroOccurSlots = new ArrayList<>();

    FormDom(XfaNode root, Document document, boolean emptyMerge) {
        this.root = root;
        this.document = document;
        this.emptyMerge = emptyMerge;
    }

    /** @return the merged Form DOM root node. */
    public XfaNode getRoot() {
        return root;
    }

    /** @return the backing form document. */
    public Document getDocument() {
        return document;
    }

    /** @return {@code true} if this was an empty (form-only, no data) merge. */
    public boolean isEmptyMerge() {
        return emptyMerge;
    }

    /** @return the enumerated form fields, in document order. */
    public List<FormField> getFields() {
        return fields;
    }

    /**
     * Finds the first field with the given dotted SOM path.
     *
     * @param somPath the path
     * @return the field, or {@code null}
     */
    public FormField fieldByPath(String somPath) {
        for (FormField f : fields) {
            if (f.getSomPath().equals(somPath)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Finds the first field with the given name.
     *
     * @param name the field name
     * @return the field, or {@code null}
     */
    public FormField fieldByName(String name) {
        for (FormField f : fields) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /** @return SOM predicate sites that contained script (FormCalc/JS) and were deferred to Stage B. */
    public List<String> getDeferredScriptPredicates() {
        return deferredScriptPredicates;
    }

    /**
     * The fields bound on the {@code <pageSet>} master pages (page furniture: headers, footers,
     * address blocks). These are a <b>separate channel</b> from {@link #getFields()} — they are
     * page-master content rendered on every physical page that uses their pageArea, and they are
     * deliberately excluded from the flatten / AcroForm-conversion field enumeration (which mirrors
     * the interactive flow fields only). The render track (XFA paginator) paints them as furniture.
     *
     * @return the master-page fields, in document order
     */
    public List<FormField> getMasterFields() {
        return masterFields;
    }

    /**
     * The bound {@code <pageArea>} elements of the first {@code <pageSet>}, in declaration order.
     * Each carries its master furniture (subforms/draws/fields) with data values resolved, so the
     * paginator can paint the furniture of the pageArea assigned to a given physical page.
     *
     * @return the bound pageArea elements (may be empty)
     */
    public List<Element> getMasterPageAreas() {
        return masterPageAreas;
    }

    void addField(FormField f) {
        fields.add(f);
    }

    void addMasterField(FormField f) {
        masterFields.add(f);
    }

    void addMasterPageArea(Element pageArea) {
        masterPageAreas.add(pageArea);
    }

    /**
     * Records a variable-occurrence container that bound to ZERO instances (an {@code <occur min="0">}
     * subform whose data was absent / that is shown only when a script {@code addInstance()}s it). The
     * paired {@code [parentElement, templateElement]} lets the script host create an instanceManager
     * for it anchored at the right Form-DOM parent even though no instance element exists to walk to.
     *
     * @param parentEl the Form-DOM parent element the instances would be appended under
     * @param template the template subform element (clone source for a fresh instance)
     */
    void addZeroOccurSlot(Element parentEl, Element template) {
        zeroOccurSlots.add(new Element[]{parentEl, template});
    }

    /**
     * @return the zero-instance variable-occurrence slots as {@code [parentElement, templateElement]}
     *         pairs (see {@link #addZeroOccurSlot}); empty when every occur container bound ≥1 instance
     */
    public List<Element[]> getZeroOccurSlots() {
        return zeroOccurSlots;
    }

    /**
     * Registers a new field for a runtime-added node (Stage B instanceManager {@code addInstance}):
     * builds a {@link FormField} for {@code formNode} and appends it so the render track sees the new
     * instance's field and value. The binding kind is {@code UNBOUND} (created at runtime, not by a
     * data merge).
     *
     * @return the registered field
     */
    public FormField registerField(String name, String somPath, String value,
                                   List<String> items, String uiType, XfaNode formNode) {
        FormField f = new FormField(name, somPath, value, items, uiType,
                FormField.BindingKind.UNBOUND, formNode);
        fields.add(f);
        return f;
    }

    /**
     * Removes a field (Stage B instanceManager {@code removeInstance}).
     *
     * @param f the field to remove
     * @return {@code true} if it was present
     */
    public boolean removeField(FormField f) {
        return fields.remove(f);
    }

    void addDeferredScriptPredicate(String site) {
        deferredScriptPredicates.add(site);
    }
}
