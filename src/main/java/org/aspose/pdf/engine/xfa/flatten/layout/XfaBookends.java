package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/// Resolves the XFA **boundary content** a flowed form inserts at page boundaries (Stage C,
/// sprint L4): leaders / trailers at explicit breaks (§ break) and overflow leaders / trailers
/// (§ bookends), laying each referenced subform into a [XfaLayoutNode] prototype of known
/// height so [XfaPaginator] can insert it per page.
///
/// Two spec mechanisms are covered:
///
///   - **Overflow leader / trailer (bookends)** — a flowed container declares
///     `<overflow leader trailer>` (or the legacy `<break overflowLeader overflowTrailer>`);
///     the leader subform is inserted at the TOP of every page the container's content spans and
///     the trailer at the BOTTOM, within the leader/trailer subform's `<occur max>` limit.
///     This is the dominant corpus case (a table's column-header row repeated on each page).
///   - **Explicit-break leader / trailer** — a `<break before leader trailer>` (or the
///     `<breakBefore leader trailer>`) on a unit: at that forced break, the trailer is the
///     last object of the page being left and the leader the first object of the new page.
///
/// References are resolved within the template: `"#id"` matches an element's `id`
/// attribute; a bare name matches a `subform`/`area``name`. Each resolved
/// fragment is laid out by [XfaFlowLayout#layoutFragment] so its height feeds the split.
public final class XfaBookends {

    /// Unbounded repetition (no `<occur max>` cap).
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    private XfaBookends() {
    }

    /// The boundary content governing a paginating flowed form.
    public static final class Spec {
        /// Overflow leader prototype (origin-based), inserted at the top of each page; `null` if none.
        public final XfaLayoutNode overflowLeader;
        /// Overflow trailer prototype, inserted at the bottom of each page; `null` if none.
        public final XfaLayoutNode overflowTrailer;
        /// Max pages the overflow leader repeats on (its subform `<occur max>`; [#UNBOUNDED] default).
        public final int overflowLeaderMax;
        /// Max pages the overflow trailer repeats on.
        public final int overflowTrailerMax;
        /// The `name` of the container whose `<overflow>` declared this boundary content;
        ///  `null`/empty if none. The leader/trailer repeat only on pages whose content descends
        ///  from a form-DOM container of this name (so a single section table's header does not leak onto
        ///  every page of a multi-section form). Matched by name because the owner is a template element
        ///  while the laid-out units carry the merged form-DOM elements.
        public final String ownerName;
        /// Whether the leader/trailer subform lives INSIDE the owner (its header is part of the flowed
        ///  body, rendered once on the owner's first page). Then the boundary content repeats only on
        ///  CONTINUATION pages (page 2+ of the owner). When `false` the leader is a separate subform
        ///  not in the body, so it is placed on every page the owner spans (the standard table-header).
        public final boolean leaderInBody;

        Spec(XfaLayoutNode overflowLeader, XfaLayoutNode overflowTrailer,
             int overflowLeaderMax, int overflowTrailerMax, String ownerName, boolean leaderInBody) {
            this.overflowLeader = overflowLeader;
            this.overflowTrailer = overflowTrailer;
            this.overflowLeaderMax = overflowLeaderMax;
            this.overflowTrailerMax = overflowTrailerMax;
            this.ownerName = ownerName;
            this.leaderInBody = leaderInBody;
        }

        /// @return the overflow leader height in points (0 if none).
        public double leaderHeight() {
            return overflowLeader == null ? 0 : overflowLeader.getHeight();
        }

        /// @return the overflow trailer height in points (0 if none).
        public double trailerHeight() {
            return overflowTrailer == null ? 0 : overflowTrailer.getHeight();
        }

        /// @return `true` if any overflow boundary content is present.
        public boolean hasOverflow() {
            return overflowLeader != null || overflowTrailer != null;
        }
    }

    /// An empty spec (no boundary content).
    public static final Spec NONE = new Spec(null, null, UNBOUNDED, UNBOUNDED, null, false);

    /// Resolves the overflow leader/trailer (bookends) governing `tpl`'s flowed content.
    /// Scans the template for the first `<overflow>` (or legacy `<break>` with an
    /// `overflowLeader`/`overflowTrailer`) carrying a leader or trailer reference, then
    /// lays each referenced subform into a prototype node sized to the content region width.
    ///
    /// @param dom         the merged Form DOM (for bound values inside the boundary subforms), or `null`
    /// @param tpl         the template (declaration + reference scope), or `null`
    /// @param regionWidth the content-region width in points (the boundary content's available width)
    /// @return the resolved spec, or [#NONE] when no overflow boundary content is declared
    public static Spec overflow(FormDom dom, Template tpl, double regionWidth) {
        if (tpl == null) {
            return NONE;
        }
        Element decl = findOverflowDecl(tpl.getElement());
        if (decl == null) {
            return NONE;
        }
        String leaderRef;
        String trailerRef;
        if ("overflow".equals(local(decl))) {
            leaderRef = attr(decl, "leader");
            trailerRef = attr(decl, "trailer");
        } else { // legacy <break>
            leaderRef = attr(decl, "overflowLeader");
            trailerRef = attr(decl, "overflowTrailer");
        }
        Element leaderEl = resolveRef(tpl.getElement(), leaderRef);
        Element trailerEl = resolveRef(tpl.getElement(), trailerRef);
        XfaLayoutNode leader = XfaFlowLayout.layoutFragment(leaderEl, regionWidth, dom);
        XfaLayoutNode trailer = XfaFlowLayout.layoutFragment(trailerEl, regionWidth, dom);
        if (leader == null && trailer == null) {
            return NONE;
        }
        // The owning container = the element carrying the <overflow>/<break> declaration (its content is
        // what spans pages and so warrants the repeated header/footer). For a legacy <break> the owner is
        // the field/subform it sits on; for <overflow> it is the parent container.
        Element owner = decl.getParentNode() instanceof Element ? (Element) decl.getParentNode() : null;
        String ownerName = owner == null ? null : owner.getAttribute("name");
        // Is the leader/trailer subform part of the owner's flowed body (a header row wrapped inside the
        // table), or a separate subform referenced only for repetition? Determines per-page placement.
        boolean leaderInBody = owner != null
                && (isDescendant(leaderEl, owner) || isDescendant(trailerEl, owner));
        return new Spec(leader, trailer, occurMax(leaderEl), occurMax(trailerEl), ownerName, leaderInBody);
    }

    /// Resolves the explicit-break leader for a unit whose `<break before>`/`<breakBefore>`
    /// carries a `leader` reference, laid out for placement at the top of the unit's page.
    ///
    /// @return the leader prototype, or `null` if the unit declares no break leader
    public static XfaLayoutNode breakLeader(Element unitSource, FormDom dom, Template tpl, double regionWidth) {
        return resolveBreakSide(unitSource, dom, tpl, regionWidth, true);
    }

    /// Resolves the explicit-break trailer for a unit whose `<break before>`/`<breakBefore>`
    /// carries a `trailer` reference, laid out for placement at the bottom of the page being left.
    ///
    /// @return the trailer prototype, or `null` if the unit declares no break trailer
    public static XfaLayoutNode breakTrailer(Element unitSource, FormDom dom, Template tpl, double regionWidth) {
        return resolveBreakSide(unitSource, dom, tpl, regionWidth, false);
    }

    private static XfaLayoutNode resolveBreakSide(Element unitSource, FormDom dom, Template tpl,
                                                  double regionWidth, boolean leader) {
        if (unitSource == null || tpl == null) {
            return null;
        }
        String ref = breakRef(unitSource, leader ? "leader" : "trailer");
        if (ref == null) {
            return null;
        }
        Element target = resolveRef(tpl.getElement(), ref);
        return XfaFlowLayout.layoutFragment(target, regionWidth, dom);
    }

    /// Reads a `leader`/`trailer` attribute off a unit's break-before declaration.
    private static String breakRef(Element el, String name) {
        String direct = attr(el, name); // tolerate the attribute directly on the container
        Element breakBefore = firstChild(el, "breakBefore");
        if (breakBefore != null && attr(breakBefore, name) != null) {
            return attr(breakBefore, name);
        }
        Element brk = firstChild(el, "break");
        if (brk != null && truthy(attr(brk, "before")) && attr(brk, name) != null) {
            return attr(brk, name);
        }
        return direct;
    }

    /* ------------------------------ resolution ------------------------------ */

    /// Whether template element `node` is `ancestor` or nested within it (same DOM).
    private static boolean isDescendant(Element node, Element ancestor) {
        for (Node n = node; n != null; n = n.getParentNode()) {
            if (n == ancestor) {
                return true;
            }
        }
        return false;
    }

    /// The first `<overflow>`/`<break>` in `root` that declares overflow boundary content.
    private static Element findOverflowDecl(Element root) {
        String ln = local(root);
        if ("overflow".equals(ln) && (attr(root, "leader") != null || attr(root, "trailer") != null)) {
            return root;
        }
        if ("break".equals(ln)
                && (attr(root, "overflowLeader") != null || attr(root, "overflowTrailer") != null)) {
            return root;
        }
        for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element found = findOverflowDecl((Element) n);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /// Resolves a leader/trailer reference within `scope`: `"#id"` matches an element's
    /// `id`; a bare name matches the nearest `subform`/`subformSet`/`area`
    /// whose `name` equals the reference (a SOM-style name lookup).
    static Element resolveRef(Element scope, String ref) {
        if (scope == null || ref == null || ref.isEmpty()) {
            return null;
        }
        if (ref.charAt(0) == '#') {
            return findById(scope, ref.substring(1));
        }
        return findByName(scope, ref);
    }

    private static Element findById(Element el, String id) {
        if (id.equals(attr(el, "id"))) {
            return el;
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element found = findById((Element) n, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Element findByName(Element el, String name) {
        String ln = local(el);
        if (("subform".equals(ln) || "subformSet".equals(ln) || "area".equals(ln))
                && name.equals(attr(el, "name"))) {
            return el;
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element found = findByName((Element) n, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /// A subform's `<occur max>` as a repetition cap (`-1` ⇒ [#UNBOUNDED]; default unbounded).
    private static int occurMax(Element subform) {
        if (subform == null) {
            return UNBOUNDED;
        }
        Element occur = firstChild(subform, "occur");
        if (occur == null) {
            return UNBOUNDED; // a header with no occur cap repeats on every page
        }
        String max = attr(occur, "max");
        if (max == null) {
            return UNBOUNDED;
        }
        try {
            int v = Integer.parseInt(max.trim());
            return v < 0 ? UNBOUNDED : v;
        } catch (NumberFormatException e) {
            return UNBOUNDED;
        }
    }

    /* -------------------------------- helpers -------------------------------- */

    private static boolean truthy(String v) {
        return v != null && !v.isEmpty() && !"auto".equals(v);
    }

    private static String attr(Element el, String name) {
        String v = el.getAttribute(name);
        return v == null || v.isEmpty() ? null : v;
    }

    private static String local(Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    private static Element firstChild(Element el, String localName) {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ln = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
                if (localName.equals(ln)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }
}
