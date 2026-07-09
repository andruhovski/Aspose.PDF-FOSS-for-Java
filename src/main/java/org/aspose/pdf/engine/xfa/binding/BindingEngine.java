package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.binding.som.SomExpr;
import org.aspose.pdf.engine.xfa.binding.som.SomResolver;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The XFA data-binding (merge) engine: combines the typed template with the
 * datasets data to produce the {@link FormDom} (XFA 3.0 binding chapter).
 *
 * <p>Supports empty merge (form-only, occur expanded to {@code initial}) and the
 * four {@code bind match} modes — {@code once}, {@code dataRef}, {@code global},
 * {@code none} — plus occur expansion. The Form DOM mirrors the template element
 * types (in the template namespace) so it remains typed and navigable.</p>
 *
 * <p>SOM is structural only here; a {@code dataRef} whose predicate contains
 * script is flagged on the {@link FormDom} (not evaluated) — Stage B.</p>
 */
public final class BindingEngine {

    private static final String NS = XfaNode.TEMPLATE_NS;
    private static final Set<String> CONTAINERS = new HashSet<>(java.util.Arrays.asList(
            "subform", "subformSet", "exclGroup", "area"));

    private final SomResolver som = new SomResolver();

    /**
     * Names of all template containers (subform/subformSet/exclGroup/area) for the
     * current merge. The last-resort descent must not cross into a data group whose
     * name matches one of these — that group is "claimed" by (i.e. is the data region
     * of) another template subform, and its values belong to that subform's fields, not
     * to an outer field reaching in. Set per {@link #merge}; read-only during the merge.
     */
    private Set<String> claimedNames = java.util.Collections.emptySet();

    /**
     * When {@code true}, the merge is currently building {@code <pageSet>} master-page furniture:
     * fields it registers go to the FormDom's master channel ({@link FormDom#getMasterFields()})
     * rather than the interactive flow-field list, so flatten / AcroForm conversion (which mirror
     * the flow fields) are unaffected while the render track can still paint the furniture.
     */
    private boolean masterMode = false;

    /**
     * Master-page binding/rendering kill-switch ({@code -Dxfa.masterPages=false} disables it,
     * restoring the pre-feature behaviour where {@code <pageSet>} is copied verbatim and unbound).
     * Default on.
     */
    static boolean masterPagesEnabled() {
        return !"false".equalsIgnoreCase(System.getProperty("xfa.masterPages", "true"));
    }

    /**
     * When {@code false}, automatic binding falls back to the pre-A4-FIX behaviour:
     * direct child-by-name match only, with no scope (ancestor/sibling) search and no
     * scope inheritance through unbound containers. The default ({@code true}) is the
     * spec-correct scope-matching behaviour; the legacy mode exists only so a
     * before/after comparison can quantify how many fields scope matching recovers.
     */
    private final boolean scopeMatching;

    /**
     * When {@code true}, an {@code <occur>} with no explicit {@code initial} defaults {@code initial}
     * to {@code min} (the XFA spec rule) — so a script-toggled variant subform ({@code min="0"}) starts
     * absent and a load-time {@code initialize}/{@code change} script {@code addInstance()}s the
     * selected one. This is the RENDER path (which runs those scripts). The default ({@code false})
     * keeps {@code initial}=1 for the flatten / AcroForm-conversion path (which does NOT run scripts):
     * there the variant subforms must stay expanded so their bound data values are preserved (the
     * 207-value flatten baseline). Pairs with {@link FormDom#getZeroOccurSlots()}.
     */
    private boolean scriptDrivenOccur = false;

    /** Creates an engine with spec-correct scope matching enabled (the normal case). */
    public BindingEngine() {
        this(true);
    }

    /**
     * Enables the spec-correct {@code <occur initial>}=min default (render path; pairs with running the
     * load-time scripts that re-add the selected variant). Off by default to preserve the flatten path.
     *
     * @param on {@code true} on the render path
     * @return this engine (for chaining)
     */
    public BindingEngine scriptDrivenOccur(boolean on) {
        this.scriptDrivenOccur = on;
        return this;
    }

    /**
     * Creates an engine, optionally forcing the legacy direct-only binding mode.
     *
     * @param scopeMatching {@code true} for spec-correct direct+ancestor+sibling
     *                      matching; {@code false} for legacy direct-only (measurement)
     */
    public BindingEngine(boolean scopeMatching) {
        this.scopeMatching = scopeMatching;
    }

    /** One field's bind directive. */
    private static final class Bind {
        FormField.BindingKind kind = FormField.BindingKind.ONCE;
        String ref;
    }

    /** One container's occur directive. */
    private static final class Occur {
        int initial = 1;
        int min = 1;
        int max = 1; // -1 == unbounded
    }

    /**
     * Empty merge: the form structure with no data (occur expanded to
     * {@code initial}), the prerequisite for blank-form flattening.
     *
     * @param template the typed template
     * @return the merged Form DOM
     */
    public FormDom mergeEmpty(Template template) {
        return merge(template, null);
    }

    /**
     * Data merge: the form structure populated from the data tree.
     *
     * @param template the typed template
     * @param dataRoot the user-data root ({@code <xfa:data>}), or {@code null} for empty merge
     * @return the merged Form DOM
     */
    public FormDom merge(Template template, XfaNode dataRoot) {
        Document formDoc = newDocument();
        XfaNode rootSubform = firstChild(template, "subform");
        if (rootSubform == null) {
            rootSubform = template;
        }
        claimedNames = new HashSet<>();
        collectContainerNames(template.getElement(), claimedNames);
        XfaNode rootData = null;
        if (dataRoot != null) {
            rootData = firstDataChild(dataRoot, rootSubform.getName());
            if (rootData == null) {
                // The record may sit below one or more wrapper data groups (e.g.
                // datasets nested an extra <data>/<dataset> level above the form root).
                // Descend to the named record so $record and relative refs resolve.
                rootData = findRecordDescendant(dataRoot, rootSubform.getName(), 4);
            }
            if (rootData == null) {
                rootData = dataRoot;
            }
        }
        FormDom dom = new FormDom(null, formDoc, dataRoot == null);
        String rootName = rootSubform.getName() == null ? "form1" : rootSubform.getName();
        // Tracks data nodes already consumed by an automatic binding (keyed by the
        // stable underlying DOM Element, since XfaNode wrappers are created per call).
        // A consumed data value binds to at most one field (spec p.182); this also
        // gives index inferral for repeated same-named siblings.
        Set<Element> consumed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        // The current record is the data node bound to the top-level subform; it is the
        // resolution base for $record references throughout the merge.
        XfaNode record = rootData;
        Element rootEl = buildContainer(rootSubform, rootData, record, dataRoot, formDoc, dom, rootName, consumed);
        formDoc.appendChild(rootEl);
        XfaNode rootNode = XfaNodeFactory.wrap(rootEl, null);
        FormDom result = new FormDom(rootNode, formDoc, dataRoot == null);
        result.getFields().addAll(dom.getFields());
        result.getMasterFields().addAll(dom.getMasterFields());
        result.getMasterPageAreas().addAll(dom.getMasterPageAreas());
        result.getDeferredScriptPredicates().addAll(dom.getDeferredScriptPredicates());
        result.getZeroOccurSlots().addAll(dom.getZeroOccurSlots());
        return result;
    }

    /* --------------------------- container --------------------------- */

    /**
     * Builds one container instance. {@code scope} is the data node that this
     * container's children search within for automatic binding: the data group this
     * container bound to, or &mdash; when the container is unbound &mdash; the
     * nearest bound ancestor's data node inherited from the parent. Inheriting the
     * scope through unbound subforms is what makes ancestor/scope matching work: a
     * field inside an unmatched subform still sees the bound ancestor's data.
     */
    private Element buildContainer(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                                   Document formDoc, FormDom dom, String path, Set<Element> consumed) {
        Element f = cloneShallow(tpl, formDoc);
        for (XfaNode child : tpl.getChildren()) {
            String ln = child.getElementName();
            if ("occur".equals(ln) || "bind".equals(ln)) {
                continue;
            }
            if ("field".equals(ln)) {
                expandField(child, scope, record, dataRoot, formDoc, dom, f, path, consumed);
            } else if ("exclGroup".equals(ln)) {
                expandExclGroup(child, scope, record, dataRoot, formDoc, dom, f, path, consumed);
            } else if (CONTAINERS.contains(ln)) {
                expandContainer(child, scope, record, dataRoot, formDoc, dom, f, path, consumed);
            } else if ("pageSet".equals(ln) && !masterMode && masterPagesEnabled()) {
                // Master pages (page furniture: headers/footers/address blocks). Bind their fields
                // into the FormDom master channel so the render track can paint them on each page,
                // while flatten / AcroForm conversion (flow fields only) stay unaffected.
                f.appendChild(buildPageSet(child, scope, record, dataRoot, formDoc, dom, path));
            } else {
                // property/leaf (value, ui, caption, border, ...) — copy verbatim
                f.appendChild(formDoc.importNode(child.getElement(), true));
            }
        }
        return f;
    }

    /* --------------------------- pageSet (master pages) --------------------------- */

    /**
     * Builds a bound {@code <pageSet>}: each {@code <pageArea>}'s furniture (subforms/draws/fields)
     * is data-bound into the {@link FormDom} <b>master channel</b>; structural children
     * ({@code medium}, {@code contentArea}, {@code occur}) are copied verbatim. The bound pageArea
     * elements are recorded on the FormDom in declaration order so the paginator can paint the
     * furniture of the pageArea assigned to each physical page.
     */
    private Element buildPageSet(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                                 Document formDoc, FormDom dom, String path) {
        Element ps = cloneShallow(tpl, formDoc);
        boolean prevMaster = masterMode;
        masterMode = true;
        try {
            for (XfaNode child : tpl.getChildren()) {
                if ("pageArea".equals(child.getElementName())) {
                    Element pa = buildPageArea(child, scope, record, dataRoot, formDoc, dom, path);
                    ps.appendChild(pa);
                    dom.addMasterPageArea(pa);
                } else {
                    ps.appendChild(formDoc.importNode(child.getElement(), true));
                }
            }
        } finally {
            masterMode = prevMaster;
        }
        return ps;
    }

    /**
     * Builds one bound {@code <pageArea>}: its furniture containers/fields are bound against the
     * same data record as the flow, but with an <b>independent</b> consumed set — master furniture
     * is a separate rendering of the data (the same value may appear both as page furniture and in
     * the flow), so it neither consumes from nor is consumed by the flow binding.
     */
    private Element buildPageArea(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                                  Document formDoc, FormDom dom, String path) {
        Element pa = cloneShallow(tpl, formDoc);
        Set<Element> masterConsumed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        String paPath = path + "." + nameOf(tpl);
        for (XfaNode child : tpl.getChildren()) {
            String ln = child.getElementName();
            if ("field".equals(ln)) {
                expandField(child, scope, record, dataRoot, formDoc, dom, pa, paPath, masterConsumed);
            } else if ("exclGroup".equals(ln)) {
                expandExclGroup(child, scope, record, dataRoot, formDoc, dom, pa, paPath, masterConsumed);
            } else if (CONTAINERS.contains(ln)) {
                expandContainer(child, scope, record, dataRoot, formDoc, dom, pa, paPath, masterConsumed);
            } else {
                // medium / contentArea / occur / draw — structural or static, copy verbatim
                pa.appendChild(formDoc.importNode(child.getElement(), true));
            }
        }
        return pa;
    }

    /** Registers a built field on the active channel (master furniture vs. interactive flow). */
    private void register(FormDom dom, FormField ff) {
        if (masterMode) {
            dom.addMasterField(ff);
        } else {
            dom.addField(ff);
        }
    }

    private void expandContainer(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                                 Document formDoc, FormDom dom, Element parent, String path, Set<Element> consumed) {
        Occur occ = readOccur(tpl);
        Bind bind = readBind(tpl, dom, path);
        List<XfaNode> bound = resolveContainerData(tpl, bind, scope, record, dataRoot, consumed);
        boolean dataDriven = scope != null && bind.kind != FormField.BindingKind.NONE;
        int count = computeCount(occ, bound.size(), dataDriven);
        // A variable-occurrence container that bound to ZERO instances still needs an instanceManager
        // anchored at this parent, so a load-time script can addInstance() the selected variant (e.g.
        // 11367's Fyzická/Právnická "Státní příslušnost"/"Právní řád založení" boxes, expanded only by
        // the exclGroup's initialize script). Record the (parent, template) slot for the script host.
        if (count == 0 && !masterMode && (occ.max == -1 || occ.max > occ.min) && tpl.getElement() != null) {
            dom.addZeroOccurSlot(parent, tpl.getElement());
        }
        for (int i = 0; i < count; i++) {
            XfaNode dn = i < bound.size() ? bound.get(i) : null;
            if (dn != null) {
                consumed.add(dn.getElement());
            }
            // bound -> descend into the matched data group; otherwise (unbound automatic
            // OR match=none layout subform) inherit the scope so descendant automatic
            // matches and relative/$ dataRef references still resolve against the nearest
            // bound data context. Legacy mode does not inherit (reproduces pre-fix loss).
            XfaNode childScope = dn != null ? dn : (scopeMatching ? scope : null);
            String childPath = path + "." + nameOf(tpl) + (count > 1 ? "[" + i + "]" : "");
            parent.appendChild(buildContainer(tpl, childScope, record, dataRoot, formDoc, dom, childPath, consumed));
        }
    }

    private void expandField(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                             Document formDoc, FormDom dom, Element parent, String path, Set<Element> consumed) {
        Occur occ = readOccur(tpl);
        Bind bind = readBind(tpl, dom, path);
        List<XfaNode> bound = resolveFieldData(tpl, bind, scope, record, dataRoot, consumed);
        boolean dataDriven = scope != null && bind.kind != FormField.BindingKind.NONE;
        int count = computeCount(occ, bound.size(), dataDriven);
        for (int i = 0; i < count; i++) {
            XfaNode dn = i < bound.size() ? bound.get(i) : null;
            // Only automatic (once/scope) binding consumes: a data value then binds at
            // most one field. dataRef is explicit and global may bind many fields, so
            // neither consumes (preserving the Receipt example: a field directly bound
            // to Total_Price plus a dataRef field referencing the same node).
            if (dn != null && bind.kind == FormField.BindingKind.ONCE) {
                consumed.add(dn.getElement());
            }
            String fieldPath = path + "." + nameOf(tpl) + (count > 1 ? "[" + i + "]" : "");
            parent.appendChild(buildField(tpl, dn, bind, formDoc, dom, fieldPath));
        }
    }

    /* --------------------------- exclGroup --------------------------- */

    /**
     * Expands an {@code exclGroup} (radio / exclusion group). Unlike a subform, an
     * exclGroup binds to a single data <em>value</em> — the selection — not a data
     * group; its child {@code field}s are the radio options. The Form DOM surfaces ONE
     * value for the exclGroup (the bound selection), with the option on-values as its
     * items, and does NOT enumerate the option fields as independent scalar values
     * (no double counting). A5 maps the surfaced value to the chosen radio option.
     */
    private void expandExclGroup(XfaNode tpl, XfaNode scope, XfaNode record, XfaNode dataRoot,
                                 Document formDoc, FormDom dom, Element parent, String path, Set<Element> consumed) {
        Occur occ = readOccur(tpl);
        Bind bind = readBind(tpl, dom, path);
        // selection is a data VALUE resolved by the exclGroup's name (same path the
        // binding already reached — A4-FIX/FIX2/SAFETY): reuse the field-value resolver.
        List<XfaNode> bound = resolveFieldData(tpl, bind, scope, record, dataRoot, consumed);
        boolean dataDriven = scope != null && bind.kind != FormField.BindingKind.NONE;
        int count = computeCount(occ, bound.size(), dataDriven);
        for (int i = 0; i < count; i++) {
            XfaNode dn = i < bound.size() ? bound.get(i) : null;
            if (dn != null && bind.kind == FormField.BindingKind.ONCE) {
                consumed.add(dn.getElement());
            }
            String exclPath = path + "." + nameOf(tpl) + (count > 1 ? "[" + i + "]" : "");
            parent.appendChild(buildExclGroup(tpl, dn, bind, formDoc, dom, exclPath));
        }
    }

    private Element buildExclGroup(XfaNode tpl, XfaNode dataNode, Bind bind,
                                   Document formDoc, FormDom dom, String path) {
        Element f = cloneShallow(tpl, formDoc);

        String selection;
        FormField.BindingKind kind;
        if (bind.kind == FormField.BindingKind.NONE) {
            kind = FormField.BindingKind.NONE;
            selection = templateDefaultValue(tpl);
        } else if (dataNode != null) {
            selection = dataNode.getTextContent();
            kind = bind.kind;
        } else {
            kind = FormField.BindingKind.UNBOUND;
            selection = templateDefaultValue(tpl); // template default selection, if any
        }

        // copy the group's children verbatim (options kept intact for A5) and collect
        // the option on-values; option fields are NOT enumerated as separate FormFields.
        List<String> options = new ArrayList<>();
        for (XfaNode child : tpl.getChildren()) {
            String ln = child.getElementName();
            if ("bind".equals(ln) || "occur".equals(ln)) {
                continue;
            }
            f.appendChild(formDoc.importNode(child.getElement(), true));
            if ("field".equals(ln)) {
                options.add(optionOnValue(child));
            }
        }

        FormField ff = new FormField(nameOf(tpl), path, selection, options,
                "exclGroup", kind, XfaNodeFactory.wrap(f, null));
        register(dom, ff);
        return f;
    }

    /** The on-value an exclGroup option contributes when selected (its value, else its items, else name). */
    private static String optionOnValue(XfaNode optionField) {
        String v = templateDefaultValue(optionField);
        if (v != null && !v.isEmpty()) {
            return v;
        }
        List<String> items = extractItems(optionField);
        if (!items.isEmpty()) {
            return items.get(0);
        }
        return nameOf(optionField);
    }

    /* ----------------------------- field ----------------------------- */

    private Element buildField(XfaNode tpl, XfaNode dataNode, Bind bind,
                               Document formDoc, FormDom dom, String path) {
        Element f = cloneShallow(tpl, formDoc);

        String boundValue = null;
        FormField.BindingKind kind;
        if (bind.kind == FormField.BindingKind.NONE) {
            kind = FormField.BindingKind.NONE;
        } else if (dataNode != null) {
            boundValue = dataNode.getTextContent();
            kind = bind.kind;
        } else {
            kind = FormField.BindingKind.UNBOUND;
        }

        boolean valueWritten = false;
        for (XfaNode child : tpl.getChildren()) {
            String ln = child.getElementName();
            if ("bind".equals(ln) || "occur".equals(ln)) {
                continue;
            }
            Node imported = formDoc.importNode(child.getElement(), true);
            if ("value".equals(ln) && boundValue != null) {
                setValueText((Element) imported, boundValue);
                valueWritten = true;
            }
            f.appendChild(imported);
        }
        if (!valueWritten && boundValue != null) {
            Element value = formDoc.createElementNS(NS, "value");
            Element text = formDoc.createElementNS(NS, "text");
            text.setTextContent(boundValue);
            value.appendChild(text);
            f.appendChild(value);
        }

        String value = boundValue != null ? boundValue : templateDefaultValue(tpl);
        FormField ff = new FormField(nameOf(tpl), path, value, extractItems(tpl),
                uiType(tpl), kind, XfaNodeFactory.wrap(f, null));
        register(dom, ff);
        return f;
    }

    /* ------------------------- binding resolution -------------------- */

    /**
     * Resolves the data <em>values</em> an automatic field binds to, in
     * binding-precedence order (direct, then ancestor, then sibling scope match),
     * skipping already-consumed nodes. {@code dataRef} resolves the explicit SOM
     * reference; {@code global} searches the whole data tree; {@code none} binds
     * nothing.
     */
    private List<XfaNode> resolveFieldData(XfaNode tpl, Bind bind, XfaNode scope,
                                           XfaNode record, XfaNode dataRoot, Set<Element> consumed) {
        List<XfaNode> out = new ArrayList<>();
        String name = nameOf(tpl);
        switch (bind.kind) {
            case NONE:
                return out;
            case GLOBAL:
                // global binds a globally-named node anywhere; it may serve many
                // fields, so it is not subject to (and does not add to) consumed.
                if (dataRoot != null) {
                    collectDescendantsByName(dataRoot, name, out);
                }
                return out;
            case DATAREF:
                if (bind.ref != null) {
                    out.addAll(som.resolveNodes(bind.ref, dataRefContext(scope, record, dataRoot)));
                }
                return out;
            default: // ONCE / automatic — direct + scope (ancestor/sibling) match
                if (scope == null) {
                    return out;
                }
                for (XfaNode cand : candidates(name, scope)) {
                    if (isDataValue(cand) && !consumed.contains(cand.getElement())) {
                        out.add(cand);
                    }
                }
                if (out.isEmpty() && scopeMatching) {
                    // Last resort: the data may enclose the value in extra groups the
                    // template does not mirror (data-independence — spec sibling match
                    // generalised by depth). Descend, shallowest-first, into the scope
                    // then the record subtree for an unconsumed value of this name.
                    // The descent may pass through the field's OWN ancestor containers'
                    // data, but must not cross into a peer/unrelated subform's data group
                    // (that would bind a wrong value — A4-SAFETY).
                    Set<String> fieldAncestors = ancestorContainerNames(tpl);
                    descendForValue(name, scope, out, consumed, fieldAncestors);
                    if (out.isEmpty() && record != null && record.getElement() != scope.getElement()) {
                        descendForValue(name, record, out, consumed, fieldAncestors);
                    }
                }
                return out;
        }
    }

    /**
     * Breadth-first descent for an unconsumed data <em>value</em> named {@code name}
     * below {@code from} (shallowest match first). The lowest-precedence automatic
     * match: used only when direct/ancestor/sibling all fail, for data that nests the
     * value under groups absent from the template. Bounded in depth; consumed-tracking
     * keeps a value bound to a single field.
     */
    private void descendForValue(String name, XfaNode from, List<XfaNode> out,
                                 Set<Element> consumed, Set<String> fieldAncestors) {
        List<XfaNode> frontier = from.getChildren();
        for (int depth = 0; depth < 8 && !frontier.isEmpty() && out.isEmpty(); depth++) {
            List<XfaNode> next = new ArrayList<>();
            for (XfaNode n : frontier) {
                boolean named = name.equals(n.getElementName()) || name.equals(n.getName());
                if (named && isDataValue(n) && !consumed.contains(n.getElement())) {
                    out.add(n);
                    return;
                }
                // Descend into a group only if it is NOT claimed by another template
                // subform, OR it is one of the field's own ancestor containers. A group
                // claimed by a PEER/unrelated subform holds that subform's data — crossing
                // into it would bind a WRONG value (A4-SAFETY); the field's own ancestor
                // groups (e.g. a table Cell/Row the data nests under) are legitimate.
                if (!isDataValue(n) && (!isClaimed(n) || isFieldAncestor(n, fieldAncestors))) {
                    next.addAll(n.getChildren());
                }
            }
            frontier = next;
        }
    }

    private boolean isClaimed(XfaNode group) {
        return claimedNames.contains(group.getElementName())
                || (group.getName() != null && claimedNames.contains(group.getName()));
    }

    private static boolean isFieldAncestor(XfaNode group, Set<String> fieldAncestors) {
        return fieldAncestors.contains(group.getElementName())
                || (group.getName() != null && fieldAncestors.contains(group.getName()));
    }

    /** Names of the template containers on the field's own ancestor path. */
    private static Set<String> ancestorContainerNames(XfaNode fieldTpl) {
        Set<String> names = new HashSet<>();
        for (XfaNode p = fieldTpl.getParent(); p != null; p = p.getParent()) {
            if (CONTAINERS.contains(p.getElementName())) {
                String nm = p.getName();
                if (nm != null && !nm.isEmpty()) {
                    names.add(nm);
                }
            }
        }
        return names;
    }

    private static void collectContainerNames(Element el, Set<String> out) {
        String ln = el.getLocalName();
        if (CONTAINERS.contains(ln)) {
            String nm = el.getAttribute("name");
            if (nm != null && !nm.isEmpty()) {
                out.add(nm);
            }
        }
        org.w3c.dom.Node c = el.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                collectContainerNames((Element) c, out);
            }
            c = c.getNextSibling();
        }
    }

    /**
     * Resolves the data <em>groups</em> a subform/exclGroup binds to, in
     * binding-precedence order, skipping consumed nodes. One match per occurrence
     * drives occur expansion; consuming each match in turn gives index inferral for
     * repeated same-named sibling subforms.
     */
    private List<XfaNode> resolveContainerData(XfaNode tpl, Bind bind, XfaNode scope,
                                               XfaNode record, XfaNode dataRoot, Set<Element> consumed) {
        List<XfaNode> out = new ArrayList<>();
        String name = nameOf(tpl);
        if (bind.kind == FormField.BindingKind.NONE) {
            return out;
        }
        if (bind.kind == FormField.BindingKind.DATAREF) {
            if (bind.ref != null) {
                for (XfaNode n : som.resolveNodes(bind.ref, dataRefContext(scope, record, dataRoot))) {
                    if (!consumed.contains(n.getElement())) {
                        out.add(n);
                    }
                }
            }
            return out;
        }
        // ONCE / automatic (global on a container is not meaningful — spec restricts
        // global to fields — so it falls through to the automatic group search).
        if (scope == null) {
            return out;
        }
        for (XfaNode cand : candidates(name, scope)) {
            if (isDataGroup(cand) && !consumed.contains(cand.getElement())) {
                out.add(cand);
            }
        }
        return out;
    }

    /**
     * Builds the SOM resolution context for a {@code dataRef} reference: the current
     * node is the data context bound to the enclosing container ({@code scope}), so
     * relative and {@code $} references resolve against it; {@code $record} resolves to
     * the merge's record and {@code $data} to the datasets root.
     */
    private static SomResolver.Context dataRefContext(XfaNode scope, XfaNode record, XfaNode dataRoot) {
        SomResolver.Context ctx = SomResolver.Context.of(scope != null ? scope : record);
        ctx.record = record;
        ctx.data = dataRoot;
        return ctx;
    }

    /**
     * Candidate data nodes named {@code name}, in binding-precedence order. Spec mode
     * uses the full {@link SomResolver#scopeMatch} (direct + ancestor + sibling);
     * legacy mode returns only direct children of {@code scope} (pre-A4-FIX).
     */
    private List<XfaNode> candidates(String name, XfaNode scope) {
        if (scopeMatching) {
            return som.scopeMatch(name, scope);
        }
        List<XfaNode> direct = new ArrayList<>();
        for (XfaNode c : scope.getChildren()) {
            if (name.equals(c.getElementName()) || name.equals(c.getName())) {
                direct.add(c);
            }
        }
        return direct;
    }

    /** A data value is a leaf (no element children); a field binds a data value. */
    private static boolean isDataValue(XfaNode n) {
        return n.getChildren().isEmpty();
    }

    /** A data group has element children; a subform/exclGroup binds a data group. */
    private static boolean isDataGroup(XfaNode n) {
        return !n.getChildren().isEmpty();
    }

    private static void collectDescendantsByName(XfaNode node, String name, List<XfaNode> out) {
        for (XfaNode c : node.getChildren()) {
            if (name.equals(c.getElementName()) || name.equals(c.getName())) {
                out.add(c);
            }
            collectDescendantsByName(c, name, out);
        }
    }

    private int computeCount(Occur occ, int matches, boolean dataDriven) {
        // Data merge: the instance count is the number of matching data records, clamped to
        // [min, max]. A data-driven container with NO matching data therefore yields occur.min
        // instances — for an explicit <occur min="0"> that is ZERO (the optional/variant subform is
        // absent, e.g. a form's unselected "Fyzická osoba" variant when the data only carries
        // "Právnická osoba"). Only the no-data (empty-merge) path uses occur.initial.
        int base = dataDriven ? matches : occ.initial;
        if (base < occ.min) {
            base = occ.min;
        }
        if (occ.max >= 0 && base > occ.max) {
            base = occ.max;
        }
        return Math.max(0, base);
    }

    /* ----------------------------- helpers --------------------------- */

    private Occur readOccur(XfaNode tpl) {
        Occur o = new Occur();
        XfaNode occ = tpl.getChild("occur");
        if (occ != null) {
            o.min = intAttr(occ, "min", 1);
            o.max = intAttr(occ, "max", 1);
            // XFA spec: <occur initial> defaults to the value of MIN (not 1). So <occur min="0"> with
            // no explicit initial yields ZERO initial instances — a script-toggled variant subform
            // (e.g. the Fyzická-osoba "Státní příslušnost" vs Právnická-osoba "Právní řád založení"
            // boxes on 11367) stays absent until its initialize/change script addInstance()s it. Only
            // on the render path (scriptDrivenOccur); the flatten path keeps initial=1 (no scripts).
            o.initial = intAttr(occ, "initial", scriptDrivenOccur ? o.min : 1);
        }
        return o;
    }

    private Bind readBind(XfaNode tpl, FormDom dom, String path) {
        Bind b = new Bind();
        XfaNode bind = tpl.getChild("bind");
        if (bind != null) {
            String match = bind.getAttribute("match");
            if (match != null) {
                switch (match) {
                    case "none": b.kind = FormField.BindingKind.NONE; break;
                    case "global": b.kind = FormField.BindingKind.GLOBAL; break;
                    case "dataRef": b.kind = FormField.BindingKind.DATAREF; break;
                    default: b.kind = FormField.BindingKind.ONCE; break;
                }
            }
            b.ref = bind.getAttribute("ref");
            if (b.kind == FormField.BindingKind.DATAREF && b.ref != null) {
                SomExpr e = som.parse(b.ref);
                if (e.hasScriptPredicate()) {
                    dom.addDeferredScriptPredicate(path + ": " + b.ref);
                }
            }
        }
        return b;
    }

    private static int intAttr(XfaNode n, String name, int dflt) {
        Integer v = n.getInteger(name);
        return v == null ? dflt : v;
    }

    private static String nameOf(XfaNode n) {
        String nm = n.getName();
        return nm != null ? nm : n.getElementName();
    }

    private Element cloneShallow(XfaNode tpl, Document formDoc) {
        Element src = tpl.getElement();
        Element f = formDoc.createElementNS(NS, tpl.getElementName());
        org.w3c.dom.NamedNodeMap atts = src.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node a = atts.item(i);
            String an = a.getNodeName();
            if (an.startsWith("xmlns")) {
                continue;
            }
            f.setAttribute(an, a.getNodeValue());
        }
        return f;
    }

    private static void setValueText(Element value, String text) {
        // find first element child (text/integer/...) and set its text; else create <text>
        Node c = value.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                c.setTextContent(text);
                return;
            }
            c = c.getNextSibling();
        }
        Element t = value.getOwnerDocument().createElementNS(NS, "text");
        t.setTextContent(text);
        value.appendChild(t);
    }

    private static String templateDefaultValue(XfaNode tpl) {
        XfaNode value = tpl.getChild("value");
        if (value == null) {
            return null;
        }
        for (XfaNode c : value.getChildren()) {
            return c.getTextContent();
        }
        return null;
    }

    private static String uiType(XfaNode tpl) {
        XfaNode ui = tpl.getChild("ui");
        if (ui == null) {
            return null;
        }
        for (XfaNode c : ui.getChildren()) {
            return c.getElementName();
        }
        return null;
    }

    private static List<String> extractItems(XfaNode tpl) {
        List<String> items = new ArrayList<>();
        XfaNode it = tpl.getChild("items");
        if (it != null) {
            for (XfaNode c : it.getChildren()) {
                items.add(c.getTextContent());
            }
        }
        return items;
    }

    private static XfaNode firstChild(XfaNode parent, String name) {
        return parent.getChild(name);
    }

    /**
     * Searches for the named record below {@code dataRoot}, descending through up to
     * {@code maxDepth} wrapper data groups (breadth-first, shallowest match wins). Used
     * when the form root subform's name is not a direct child of the datasets data node
     * because the data nests it under an extra group level. Returns {@code null} if not
     * found within the depth bound.
     */
    private static XfaNode findRecordDescendant(XfaNode dataRoot, String name, int maxDepth) {
        if (name == null || maxDepth <= 0) {
            return null;
        }
        List<XfaNode> frontier = new ArrayList<>(dataRoot.getChildren());
        for (int depth = 0; depth < maxDepth && !frontier.isEmpty(); depth++) {
            List<XfaNode> next = new ArrayList<>();
            for (XfaNode n : frontier) {
                if (name.equals(n.getElementName()) || name.equals(n.getName())) {
                    return n;
                }
                next.addAll(n.getChildren());
            }
            frontier = next;
        }
        return null;
    }

    private static XfaNode firstDataChild(XfaNode dataRoot, String name) {
        if (name == null) {
            return null;
        }
        for (XfaNode c : dataRoot.getChildren()) {
            if (name.equals(c.getElementName()) || name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    private static Document newDocument() {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            return f.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create form document", e);
        }
    }
}
